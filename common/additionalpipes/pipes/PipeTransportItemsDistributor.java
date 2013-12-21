/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.AdditionalPipes;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class PipeTransportItemsDistributor extends PipeTransportItems {

	private final Map<TravelingItem, TravelingItem> toInject = Maps.newHashMap();
	private final Map<Integer, ForgeDirection> outputMap = Maps.newHashMap();

	private int[] getDistributionCount(TravelingItem item, EnumSet<ForgeDirection> blacklist) {
		PipeLogicDistributor logic = ((PipeItemsDistributor) container.pipe).logic;
		logic.switchIfNeeded();

		EnumSet<ForgeDirection> outputSides = EnumSet.noneOf(ForgeDirection.class);
		int totalDistData = 0;
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (!blacklist.contains(side) && canReceivePipeObjects(side, item)) {
				outputSides.add(side);
				totalDistData += logic.distData[side.ordinal()];
			}
		}
		if (totalDistData == 0) {
			return null;
		}
		totalDistData -= logic.curTick;

		int[] oris = new int[] { 0, 0, 0, 0, 0, 0 };
		int stackSize = item.getItemStack().stackSize;

		int distUnit = stackSize > totalDistData ? stackSize / totalDistData : 1;
		int distExtra = stackSize > totalDistData ? stackSize % totalDistData : 0;

		int initTick = logic.curTick;
		boolean switched = false;
		ForgeDirection output = ForgeDirection.VALID_DIRECTIONS[container.getBlockMetadata()];
		for (ForgeDirection initSide = output; output != ForgeDirection.UNKNOWN; output = logic.getNextSide(output, initSide)) {
			if (!outputSides.contains(output)) {
				continue;
			}
			int data = logic.distData[output.ordinal()];
			int available = data * distUnit - initTick;
			initTick = 0;
			if (distExtra > 0) {
				int extras = Math.min(data, distExtra);
				available += extras;
				distExtra -= extras;
				if (distExtra == 0) {
					logic.switchTo(output, false);
					logic.curTick = extras;
					switched = true;
				}
			}
			if (stackSize <= available) {
				oris[output.ordinal()] = stackSize;
				if (!switched) {
					logic.curTick += stackSize;
					if (logic.curTick >= data) {
						logic.switchTo(output, false);
						logic.switchPosition();
					}
				}
				break;
			}
			stackSize -= available;
			oris[output.ordinal()] = available;
			if (!switched) {
				logic.curTick = 0;
			}
		}
		if (output == ForgeDirection.UNKNOWN) {
			AdditionalPipes.logger.severe("Distributor error!");
			return null;
		}
		return oris;
	}

	@SuppressWarnings("unchecked")
	private TravelingItem createTravelingItem(TravelingItem item, int stackSize) {
		ItemStack newStack = item.getItemStack().copy();
		newStack.stackSize = stackSize;

		TravelingItem newItem = new TravelingItem(item.xCoord, item.yCoord, item.zCoord, newStack);
		newItem.color = item.color;
		newItem.setSpeed(item.getSpeed());
		if (item.hasExtraData()) {
			NBTTagCompound newData = newItem.getExtraData(), data = item.getExtraData();
			for (NBTBase tag : (Collection<NBTBase>) data.getTags()) {
				newData.setTag(tag.getName(), tag);
			}
		}
		newItem.setInsetionHandler(item.getInsertionHandler());

		return newItem;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!container.worldObj.isRemote) {
			for (Map.Entry<TravelingItem, TravelingItem> e : toInject.entrySet()) {
				TravelingItem newItem = e.getKey(), item = e.getValue();
				super.injectItem(newItem, item.input);
				newItem.blacklist.addAll(item.blacklist);
			}
			toInject.clear();
		}
	}

	private void distributeItems(TravelingItem item, int[] oris) {
		ForgeDirection firstDir = ForgeDirection.UNKNOWN;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (oris[o.ordinal()] > 0) {
				if (firstDir == ForgeDirection.UNKNOWN) {
					// Use the original TravelingItem
					firstDir = o;
					item.getItemStack().stackSize = oris[o.ordinal()];
				} else {
					TravelingItem newItem = createTravelingItem(item, oris[o.ordinal()]);
					outputMap.put(newItem.id, o);
					toInject.put(newItem, item);
				}
			}
		}

		outputMap.put(item.id, firstDir);
	}

	@Override
	public void injectItem(TravelingItem item, ForgeDirection input) {
		if (!AdditionalPipes.distributorSplitsStack || container.worldObj.isRemote) {
			super.injectItem(item, input);
			return;
		}

		item.reset();

		int[] oris = getDistributionCount(item, item.blacklist);// The blacklist is empty
		if (oris == null) {
			// It is impossible to determine destination
			super.injectItem(item, input);
			return;
		}

		item.blacklist.add(input);
		distributeItems(item, oris);
		super.injectItem(item, input);
	}

	@Override
	protected void reverseItem(TravelingItem item) {
		if (!AdditionalPipes.distributorSplitsStack || container.worldObj.isRemote) {
			super.reverseItem(item);
			return;
		}

		item.blacklist.add(item.input.getOpposite());

		int[] oris = getDistributionCount(item, item.blacklist);
		if (oris == null) {
			// It is impossible to determine destination
			super.reverseItem(item);
			return;
		}

		distributeItems(item, oris);
		super.reverseItem(item);
	}

	@Override
	public ForgeDirection resolveDestination(TravelingItem item) {
		if (!AdditionalPipes.distributorSplitsStack) {
			return super.resolveDestination(item);
		}

		return Objects.firstNonNull(outputMap.remove(item.id), ForgeDirection.UNKNOWN);
	}

}
