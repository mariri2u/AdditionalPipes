/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportPower;

import com.google.common.primitives.Floats;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipePowerRedstone extends Pipe<PipeTransportPower> {

	private int powerLevel = 0;
	private SafeTimeTracker outputTracker = new SafeTimeTracker();

	public PipePowerRedstone(int itemID) {
		super(new PipeTransportPower(), itemID);
		transport.initFromPipe(getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return powerLevel > 0 ? PipeIconProvider.TYPE.PipePowerRedstone_Powered.ordinal() :
			PipeIconProvider.TYPE.PipePowerRedstone_Standard.ordinal();
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

		if (outputTracker.markTimeIfDelay(container.worldObj, 10)) {
			double power = Floats.max(transport.displayPower);
			int newPowerLevel = MathHelper.ceiling_double_int(power * 15 / transport.maxPower);
			if (powerLevel != newPowerLevel) {
				powerLevel = newPowerLevel;
				container.scheduleRenderUpdate();
				updateNeighbors(true);
			}
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
