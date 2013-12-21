/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.client.texture.PipeIconProvider;
import additionalpipes.rescueapi.RescueApi;
import additionalpipes.utils.APUtils;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsRedstone extends Pipe<PipeTransportItems> implements IPipeTransportItemsHook {

	private int powerLevel = 0;

	public PipeItemsRedstone(int itemID) {
		super(new PipeTransportItems(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return powerLevel > 0 ? PipeIconProvider.TYPE.PipeItemsRedstone_Powered.ordinal()
				: PipeIconProvider.TYPE.PipeItemsRedstone_Standard.ordinal();
	}

	@Override
	public void readjustSpeed(TravelingItem item) {
		if (item.getSpeed() > RescueApi.getPipeNormalSpeed()) {
			item.setSpeed(item.getSpeed() - RescueApi.getPipeNormalSpeed() / 2.0F);
		}

		if (item.getSpeed() < RescueApi.getPipeNormalSpeed()) {
			item.setSpeed(RescueApi.getPipeNormalSpeed());
		}
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(TravelingItem item, ForgeDirection orientation) {
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		updatePowerLevel();
	}

	private void updatePowerLevel() {
		if (CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			return;
		}

		int stackPowerLevel = APUtils.divideAndCeil(transport.items.size() * 15, PipeTransportItems.MAX_PIPE_STACKS);

		int itemsPowerLevel = 0;
		if (stackPowerLevel < 15) {
			int numItems = 0;
			for (TravelingItem travellingItem : transport.items) {
				ItemStack stack = travellingItem.getItemStack();
				if (stack != null && stack.stackSize > 0)
					numItems += stack.stackSize;
			}
			itemsPowerLevel = APUtils.divideAndCeil(numItems * 15, PipeTransportItems.MAX_PIPE_ITEMS);
		}

		int newPowerLevel = Math.max(stackPowerLevel, itemsPowerLevel);
		if (powerLevel != newPowerLevel) {
			powerLevel = newPowerLevel;
			container.scheduleRenderUpdate();
			updateNeighbors(true);
		}
	}

	@Override
	public boolean canConnectRedstone() {
		return true;
	}

	@Override
	public int isIndirectlyPoweringTo(int l) {
		return Math.max(powerLevel, super.isIndirectlyPoweringTo(l));
	}

}
