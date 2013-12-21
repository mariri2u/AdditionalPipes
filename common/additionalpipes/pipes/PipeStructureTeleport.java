/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import additionalpipes.api.TeleportManager.TeleportPipeEvent;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportStructure;
import buildcraft.transport.TileGenericPipe;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeStructureTeleport extends Pipe<PipeTransportStructure> implements ITeleportLogicProvider {

	private static final Field internalUpdateScheduled = ReflectionHelper.findField(Pipe.class, "internalUpdateScheduled");
	private static final Method receiveSignal = ReflectionHelper.findMethod(Pipe.class, null, new String[] { "receiveSignal" }, int.class, IPipe.WireColor.class);

	static {
		MinecraftForge.EVENT_BUS.register(new PipeStructureTeleport(0));
	}

	private PipeLogicTeleport logic = new PipeLogicTeleport(this);

	public PipeStructureTeleport(int itemID) {
		super(new PipeTransportStructure(), itemID);
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

	/*@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return logic.canPipeConnect(tile, side) && super.canPipeConnect(tile, side);
	}*/

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeStructureTeleport.ordinal();
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
	public boolean isWireConnectedTo(TileEntity tile, WireColor color) {
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}
		TileGenericPipe tilePipe = (TileGenericPipe) tile;
		return !(tilePipe.pipe instanceof PipeStructureTeleport) && super.isWireConnectedTo(tile, color);
	}

	public boolean isWireConnectedIndirectlyTo(TileEntity tile, WireColor color) {
		if (!(tile instanceof TileGenericPipe)) {
			return false;
		}
		TileGenericPipe tilePipe = (TileGenericPipe) tile;
		if (tilePipe.pipe instanceof PipeStructureTeleport) {
			return logic.getConnectedPipes(null).contains(tilePipe.pipe);
		}
		return super.isWireConnectedTo(tile, color);
	}

	public List<Pipe<?>> getAllConnectedPipes() {
		List<Pipe<?>> result = Lists.newArrayListWithExpectedSize(6);

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = container.getTile(o);

			if (tile instanceof TileGenericPipe) {
				result.add(((TileGenericPipe) tile).pipe);
			}
		}
		result.addAll(logic.<PipeStructureTeleport>getConnectedPipes(null));

		return result;
	}

	@Override
	public void updateSignalState() {
		for (IPipe.WireColor color : IPipe.WireColor.values()) {
			updateSignalStateForColor(color);
		}
	}

	private void updateSignalStateForColor(IPipe.WireColor color) {
		if (!wireSet[color.ordinal()]) {
			return;
		}

		// STEP 1: compute internal signal strength

		if (gate != null && gate.broadcastSignal[color.ordinal()]) {
			receiveSignalInvoke(255, color);
		} else {
			readNearbyPipesSignal(color);
		}

		// STEP 2: transmit signal in nearby blocks

		if (signalStrength[color.ordinal()] > 1) {
			for (Pipe<?> pipe : getAllConnectedPipes()) {
				if (BlockGenericPipe.isFullyDefined(pipe) && pipe.wireSet[color.ordinal()]) {
					if (isWireConnectedIndirectlyTo(pipe.container, color)) {
						receiveSignalInvoke(pipe, signalStrength[color.ordinal()] - 1, color);
					}
				}
			}
		}
	}

	private void readNearbyPipesSignal(WireColor color) {
		boolean foundBiggerSignal = false;

		List<Pipe<?>> connected = getAllConnectedPipes();
		for (Pipe<?> pipe : connected) {
			if (BlockGenericPipe.isFullyDefined(pipe)) {
				if (isWireConnectedIndirectlyTo(pipe.container, color)) {
					foundBiggerSignal |= receiveSignalInvoke(pipe.signalStrength[color.ordinal()] - 1, color);
				}
			}
		}

		if (!foundBiggerSignal && signalStrength[color.ordinal()] != 0) {
			signalStrength[color.ordinal()] = 0;
			//worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			container.scheduleRenderUpdate();

			for (Pipe<?> pipe : connected) {
				if (BlockGenericPipe.isFullyDefined(pipe)) {
					internalUpdateScheduledSet(pipe, true);
				}
			}
		}
	}

	private void internalUpdateScheduledSet(boolean value) {
		internalUpdateScheduledSet(this, value);
	}

	private static void internalUpdateScheduledSet(Pipe<?> pipe, boolean value) {
		try {
			internalUpdateScheduled.setBoolean(pipe, value);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean receiveSignalInvoke(int signal, IPipe.WireColor color) {
		return receiveSignalInvoke(this, signal, color);
	}

	private static boolean receiveSignalInvoke(Pipe<?> pipe, int signal, IPipe.WireColor color) {
		try {
			return (Boolean) receiveSignal.invoke(pipe, signal, color);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public PipeLogicTeleport getLogic() {
		return logic;
	}

	@ForgeSubscribe
	public void onPipePreModify(TeleportPipeEvent.PreModify event) {
		if (event.pipe instanceof PipeLogicTeleport && event.pipe.getType() == PipeType.STRUCTURE) {
			PipeStructureTeleport pipe = (PipeStructureTeleport) ((PipeLogicTeleport) event.pipe).pipe;
			pipe.internalUpdateScheduledSet(true);
			for (PipeStructureTeleport other : pipe.logic.<PipeStructureTeleport>getConnectedPipes(null)) {
				other.internalUpdateScheduledSet(true);
			}
		}
	}

}
