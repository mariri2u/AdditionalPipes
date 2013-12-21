/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.LinkedList;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.gates.IAction;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsAdvancedInsertion extends Pipe<PipeTransportItems> implements IPipeTransportItemsHook {

	private PipeLogicAdvancedInsertion logic = new PipeLogicAdvancedInsertion();

	public PipeItemsAdvancedInsertion(int itemID) {
		super(new PipeTransportItems(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsAdvancedInsertion.ordinal();
	}

	@Override
	public void readjustSpeed(TravelingItem item) {
		if (item.getSpeed() > Utils.pipeNormalSpeed) {
			item.setSpeed(item.getSpeed() - Utils.pipeNormalSpeed / 2.0F);
		}

		if (item.getSpeed() < Utils.pipeNormalSpeed) {
			item.setSpeed(Utils.pipeNormalSpeed);
		}
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
		LinkedList<ForgeDirection> possibleInventories = Lists.newLinkedList();

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (o != pos.orientation.getOpposite()) {
				TileEntity tile = container.getTile(o);

				if (Utils.checkPipesConnections(tile, container) && tile instanceof IInventory) {
					if (logic.isDisabled() || InvUtils.isRoomForStack(item.getItemStack(), o.getOpposite(), (IInventory) tile)) {
						possibleInventories.add(o);
					}
				}
			}
		}

		if (logic.isDisabled()) {
			possibleOrientations.removeAll(possibleInventories);
			return possibleOrientations;
		}
		return !possibleInventories.isEmpty() ? possibleInventories : possibleOrientations;
	}

	@Override
	public void entityEntered(TravelingItem item, ForgeDirection orientation) {
	}

	@Override
	public LinkedList<IAction> getActions() {
		return logic.getActions(super.getActions());
	}

	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);
		logic.actionsActivated(actions);
	}

}
