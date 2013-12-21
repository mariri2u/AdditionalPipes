/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.AdditionalPipes;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.PipeItemsWood;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsDistributor extends Pipe<PipeTransportItemsDistributor> implements IPipeTransportItemsHook {

	public PipeLogicDistributor logic = new PipeLogicDistributor(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (!Utils.checkPipesConnections(tile, container))
				return false;

			if (tile instanceof TileGenericPipe) {
				TileGenericPipe pipe = (TileGenericPipe) tile;

				return (pipe.pipe.transport instanceof PipeTransportItems) && !(pipe.pipe instanceof PipeItemsWood);
			} else if (tile instanceof IInventory)
				return true;

			return false;
		}
	};

	public PipeItemsDistributor(int itemID) {
		super(new PipeTransportItemsDistributor(), itemID);
	}

	@Override
	public void initialize() {
		logic.initialize();
		super.initialize();
	}

	@Override
	public void onBlockPlaced() {
		logic.onBlockPlaced();
		super.onBlockPlaced();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		logic.onNeighborBlockChange(blockId);
		super.onNeighborBlockChange(blockId);
	}

	@Override
	public boolean outputOpen(ForgeDirection to) {
		return logic.outputOpen(to);
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

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		switch(direction){
			case UNKNOWN:
			case DOWN: return PipeIconProvider.TYPE.PipeItemsDistributor_Down.ordinal();
			case UP: return PipeIconProvider.TYPE.PipeItemsDistributor_Up.ordinal();
			case NORTH: return PipeIconProvider.TYPE.PipeItemsDistributor_North.ordinal();
			case SOUTH: return PipeIconProvider.TYPE.PipeItemsDistributor_South.ordinal();
			case WEST: return PipeIconProvider.TYPE.PipeItemsDistributor_West.ordinal();
			case EAST: return PipeIconProvider.TYPE.PipeItemsDistributor_East.ordinal();
			default: throw new IllegalArgumentException("direction out of bounds");
		}
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
		if (AdditionalPipes.distributorSplitsStack) {
			AdditionalPipes.logger.severe("Distributor error!");
			new Throwable().printStackTrace();
			possibleOrientations.clear();
			return possibleOrientations;
		}

		logic.switchIfNeeded();

		LinkedList<ForgeDirection> result = Lists.newLinkedList();
		ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[container.getBlockMetadata()];
		if (possibleOrientations.contains(side)) {
			result.add(side);
		}

		if (++logic.curTick >= logic.distData[side.ordinal()]) {
			logic.switchPosition();
		}

		return result;
	}

	@Override
	public void entityEntered(TravelingItem item, ForgeDirection orientation) {
	}

	@Override
	public void readjustSpeed(TravelingItem item) {
	}

}
