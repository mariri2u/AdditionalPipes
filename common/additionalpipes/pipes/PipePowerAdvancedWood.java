/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.pipes.PipePowerWood;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipePowerAdvancedWood extends PipePowerWood {

	public static final float RESISTANCE = 0.01F;

	private PowerHandler powerHandler;
	private boolean[] powerSources = new boolean[6];
	private boolean full;

	public PipePowerAdvancedWood(int itemID) {
		super(itemID);

		standardIconIndex = PipeIconProvider.TYPE.PipePowerAdvancedWood_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllAdvancedWood_Solid.ordinal();

		powerHandler = new PowerHandler(this, Type.PIPE);
		initPowerProvider();
	}

	private void initPowerProvider() {
		powerHandler.configure(1, 1000, 1, 1500);
		powerHandler.configurePowerPerdition(1, 100);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (container.worldObj.isRemote)
			return;

		if (powerHandler.getEnergyStored() <= 0)
			return;

		int sources = 0;
		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!container.isPipeConnected(o)) {
				powerSources[o.ordinal()] = false;
				continue;
			}
			if (powerHandler.isPowerSource(o)) {
				powerSources[o.ordinal()] = true;
			}
			if (powerSources[o.ordinal()]) {
				sources++;
			}
		}

		if (sources <= 0) {
			powerHandler.useEnergy(5, 5, true);
			return;
		}

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			if (!powerSources[o.ordinal()])
				continue;

			sendEnergy(transport, o);
		}
	}

	private float sendEnergy(PipeTransportPower trans, ForgeDirection o) {
		float energyUsable = powerHandler.useEnergy(1, 512, false);

		float energySend = trans.receiveEnergy(o, energyUsable * (1 - RESISTANCE)) / (1 - RESISTANCE);
		if (energySend > 0) {
			powerHandler.useEnergy(0, energySend, true);
		}
		return energySend;
	}

	@Override
	public boolean requestsPower() {
		if (full) {
			boolean request = powerHandler.getEnergyStored() < powerHandler.getMaxEnergyStored() / 2;
			if (request) {
				full = false;
			}
			return request;
		}
		full = powerHandler.getEnergyStored() >= powerHandler.getMaxEnergyStored() - 10;
		return !full;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		powerHandler.writeToNBT(data);
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		powerHandler.readFromNBT(data);
		initPowerProvider();
		data.removeTag("powerProvider");
		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
		}
		super.readFromNBT(data);
	}

}
