/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.AdditionalPipes;
import additionalpipes.client.texture.PipeIconProvider;
import additionalpipes.rescueapi.RescueApi;
import additionalpipes.utils.APUtils;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.core.DefaultProps;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.TravelerSet;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeItemsTeleport extends Pipe<PipeTransportItems> implements IPipeTransportItemsHook, IItemTravelingHook, ITeleportLogicProvider {

	private PipeLogicTeleport logic = new PipeLogicTeleport(this);

	public PipeItemsTeleport(int itemID) {
		super(new PipeTransportItems(), itemID);
		transport.allowBouncing = true;
		transport.travelHook = this;
	}

	@Override
	public void initialize() {
		super.initialize();
		logic.initialize();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		logic.invalidate();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		logic.onChunkUnload();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeItemsTeleport.ordinal();
	}

	/*@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return logic.canPipeConnect(tile, side) && super.canPipeConnect(tile, side);
	}*/

	public boolean canReceiveItem(TravelingItem item) {
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (Utils.checkPipesConnections(container, container.getTile(o))) {
				return true;
			}
		}

		return false;
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, TravelingItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(TravelingItem item, ForgeDirection orientation) {
	}

	@Override
	public void readjustSpeed(TravelingItem item) {
	}

	@Override
	public void drop(PipeTransportItems pipe, TravelingItem item) {
	}

	@Override
	public void centerReached(PipeTransportItems pipe, TravelingItem item) {
		if (item.input == ForgeDirection.UNKNOWN) {
			// This item is teleported
			return;
		}
		if (CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			// This item will be teleported
			pipe.items.scheduleRemoval(item);
			return;
		}

		int energy = APUtils.divideAndCeil(item.getItemStack().stackSize, AdditionalPipes.unitItems);

		List<PipeItemsTeleport> connectedPipes = logic.getConnectedPipes(true);
		connectedPipes = Lists.newArrayList(connectedPipes);
		Collections.shuffle(connectedPipes, container.worldObj.rand);
		for (PipeItemsTeleport destPipe : connectedPipes) {
			if (destPipe.canReceiveItem(item) && logic.useEnergy(energy)) {
//				item.setPosition(destPipe.container.xCoord + 0.5, destPipe.container.yCoord + Utils.getPipeFloorOf(item.getItemStack()), destPipe.container.zCoord + 0.5);
				item.setPosition(destPipe.container.xCoord + 0.5, destPipe.container.yCoord + RescueApi.getPipeFloorOf(), destPipe.container.zCoord + 0.5);
				destPipe.transport.injectItem(item, ForgeDirection.UNKNOWN);
				pipe.items.scheduleRemoval(item);
				return;
			}
		}

		// There is no destination. Turn back
		item.output = item.input.getOpposite();
		item.input = ForgeDirection.UNKNOWN;
		int dimension = container.worldObj.provider.dimensionId;
		PacketDispatcher.sendPacketToAllAround(container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST, dimension, transport.createItemPacket(item));
	}

	@Override
	public boolean endReached(PipeTransportItems pipe, TravelingItem item, TileEntity tile) {
		return false;
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
	public PipeLogicTeleport getLogic() {
		return logic;
	}

}
