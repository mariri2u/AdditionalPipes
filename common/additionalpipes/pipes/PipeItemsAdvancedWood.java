/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.inventory.ISelectiveInventory;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.core.utils.Utils;
import buildcraft.transport.pipes.PipeItemsWood;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsAdvancedWood extends PipeItemsWood {

	public PipeLogicAdvancedWood logic = new PipeLogicAdvancedWood(this);

	public PipeItemsAdvancedWood(int itemID) {
		super(itemID);

		standardIconIndex = PipeIconProvider.TYPE.PipeItemsAdvancedWood_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllAdvancedWood_Solid.ordinal();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return super.blockActivated(entityplayer) || logic.blockActivated(entityplayer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	/**
	 * Return the itemstack that can be if something can be extracted from this
	 * inventory, null if none. On certain cases, the extractable slot depends
	 * on the position of the pipe.
	 */
	@Override
	public ItemStack[] checkExtract(IInventory inventory, boolean doRemove, ForgeDirection from) {

		// ISELECTIVEINVENTORY
		if (inventory instanceof ISelectiveInventory) {
			IInventory pipeFilters = logic.getFilters();
			ItemStack[] filter = new ItemStack[pipeFilters.getSizeInventory()];
			for (int i = 0; i < pipeFilters.getSizeInventory(); i++) {
				filter[i] = pipeFilters.getStackInSlot(i);
			}
			ItemStack[] stacks = ((ISelectiveInventory) inventory).extractItem(filter, logic.exclude, doRemove, from, (int) powerHandler.getEnergyStored());
			if (doRemove) {
				for (ItemStack stack : stacks) {
					if (stack != null) {
						powerHandler.useEnergy(stack.stackSize, stack.stackSize, true);
					}
				}
			}
			return stacks;

		// ISPECIALINVENTORY
		} else if (inventory instanceof ISpecialInventory) {
			ItemStack[] stacks = ((ISpecialInventory) inventory).extractItem(false, from, (int) powerHandler.getEnergyStored());
			if (stacks != null) {
				for (ItemStack stack : stacks) {
					if (stack != null && !canExtract(stack)) {
						return null;
					}
				}
				if (doRemove) {
					stacks = ((ISpecialInventory) inventory).extractItem(true, from, (int) powerHandler.getEnergyStored());
					for (ItemStack stack : stacks) {
						if (stack != null) {
							powerHandler.useEnergy(stack.stackSize, stack.stackSize, true);
						}
					}
				}
			}
			return stacks;

		} else {

			// This is a generic inventory
			IInventory inv = Utils.getInventory(inventory);
			return checkExtractGeneric2(InventoryWrapper.getWrappedInventory(inv), doRemove, from);
		}
	}

	public ItemStack[] checkExtractGeneric2(ISidedInventory inventory, boolean doRemove, ForgeDirection from) {
		if (inventory == null) {
			return null;
		}

		float energyUsable = powerHandler.getEnergyStored();
		List<ItemStack> result = Lists.newArrayList();
		for (int i : inventory.getAccessibleSlotsFromSide(from.ordinal())) {
			if (energyUsable < 1) {
				break;
			}

			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.stackSize > 0 && canExtract(stack)) {
				if (!inventory.canExtractItem(i, stack, from.ordinal())) {
					continue;
				}
				if (doRemove) {
					float energy = powerHandler.useEnergy(1, stack.stackSize, true);
					result.add(inventory.decrStackSize(i, (int) energy));
					energyUsable -= energy;
				} else {
					if (stack.stackSize > (int) energyUsable) {
						stack = stack.copy();
						stack.stackSize = (int) energyUsable;
					}
					result.add(stack);
					energyUsable -= stack.stackSize;
				}
			}
		}

		return result.toArray(new ItemStack[result.size()]);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		logic.readFromNBT(data);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		logic.writeToNBT(data);
	}

	public boolean canExtract(ItemStack stack) {
		return InvUtils.containsItem(logic.getFilters(), null, stack) == !logic.exclude;
	}

}
