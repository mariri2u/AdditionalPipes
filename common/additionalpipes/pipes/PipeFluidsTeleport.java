/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import org.apache.commons.lang3.tuple.Pair;

import additionalpipes.AdditionalPipes;
import additionalpipes.client.texture.PipeIconProvider;
import additionalpipes.utils.APUtils;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportFluidsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeFluidsTeleport extends Pipe<PipeTransportFluids> implements IPipeTransportFluidsHook, ITeleportLogicProvider {

	private PipeLogicTeleport logic = new PipeLogicTeleport(this);

	public PipeFluidsTeleport(int itemID) {
		super(new PipeTransportFluids(), itemID);

		transport.flowRate = 80;
		transport.travelDelay = 4;
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
		return PipeIconProvider.TYPE.PipeFluidsTeleport.ordinal();
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			return 0;
		}

		FluidStack fluid = resource.copy();

		List<PipeFluidsTeleport> connectedPipes = logic.getConnectedPipes(true);
		connectedPipes = Lists.newArrayList(connectedPipes);
		Collections.shuffle(connectedPipes, container.worldObj.rand);
		OUTER: for (PipeFluidsTeleport destPipe : connectedPipes) {
			List<Pair<ForgeDirection, IFluidHandler>> fluidsList = destPipe.getPossibleFluidsMovements();
			Collections.shuffle(fluidsList, container.worldObj.rand);
			for (Pair<ForgeDirection, IFluidHandler> fluids : fluidsList) {
				ForgeDirection side = fluids.getLeft();
				IFluidHandler tank = fluids.getRight();

				int usedSingle = tank.fill(side, fluid, false);
				if (usedSingle > 0) {
					int energy = APUtils.divideAndCeil(usedSingle, AdditionalPipes.unitFluids);
					if (doFill ? logic.useEnergy(energy) : logic.canUseEnergy(energy)) {
						if (doFill) {
							tank.fill(side, fluid, doFill);
						}
						fluid.amount -= usedSingle;
						if (fluid.amount <= 0) {
							break OUTER;
						}
					}
				}
			}
		}

		return resource.amount - fluid.amount;
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

	private List<Pair<ForgeDirection, IFluidHandler>> getPossibleFluidsMovements() {
		List<Pair<ForgeDirection, IFluidHandler>> result = Lists.newArrayListWithCapacity(6);

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = container.getTile(o);

			if (Utils.checkPipesConnections(tile, container) && tile instanceof IFluidHandler) {
				result.add(Pair.of(o.getOpposite(), (IFluidHandler) tile));
			}
		}

		return result;
	}

	@Override
	public PipeLogicTeleport getLogic() {
		return logic;
	}

}
