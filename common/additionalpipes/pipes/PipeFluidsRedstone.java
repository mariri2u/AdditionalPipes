/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import additionalpipes.client.texture.PipeIconProvider;
import additionalpipes.utils.APUtils;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeFluidsRedstone extends Pipe<PipeTransportFluids> {

	private int powerLevel = 0;

	public PipeFluidsRedstone(int itemID) {
		super(new PipeTransportFluids(), itemID);

		transport.flowRate = 80;
		transport.travelDelay = 4;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return powerLevel > 0 ? PipeIconProvider.TYPE.PipeFluidsRedstone_Powered.ordinal()
				: PipeIconProvider.TYPE.PipeFluidsRedstone_Standard.ordinal();
	}

	private int getFluidAmount() {
		//FluidStack fluid = transport.internalTanks[ForgeDirection.UNKNOWN.ordinal()].getFluid();
		FluidStack fluid = transport.renderCache[ForgeDirection.UNKNOWN.ordinal()];
		return fluid != null ? fluid.amount : 0;
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

		int newPowerLevel = APUtils.divideAndCeil(getFluidAmount() * 15, PipeTransportFluids.LIQUID_IN_PIPE);
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
