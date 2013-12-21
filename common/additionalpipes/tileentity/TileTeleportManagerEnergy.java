/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.tileentity;

import ic2.api.Direction;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;

import java.util.List;

import logisticspipes.api.ILogisticsPowerProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import additionalpipes.AdditionalPipes;
import additionalpipes.api.ITeleportPipe;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.proxy.CoreProxy;

//disableEnergyUsage=false
public class TileTeleportManagerEnergy extends TileTeleportManager implements ILogisticsPowerProvider, IPowerReceptor, IEnergySink {

	public static final int BuildCraftMultiplier = 5;
	public static final int IC2Multiplier = 2;

	private PowerHandler powerHandler;
	private boolean isAdded = false;

	public boolean forcedLink = false;
	private int energyStored = 0;

	public TileTeleportManagerEnergy() {
		powerHandler = new PowerHandler(this, Type.STORAGE);
		powerHandler.configure(1, 256, Float.MAX_VALUE, 512);
	}

	public void addEnergy(float amount) {
		energyStored += amount;
		if (energyStored > AdditionalPipes.managerCapacity) {
			energyStored = AdditionalPipes.managerCapacity;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			return;
		}

		addEnergy(powerHandler.useEnergy(0, (AdditionalPipes.managerCapacity - energyStored) / BuildCraftMultiplier, true) * BuildCraftMultiplier);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			isAdded = true;
		}
	}

	@Override
	public void invalidate() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
		}
		super.onChunkUnload();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		forcedLink = nbttagcompound.getBoolean("forcedLink");
		energyStored = nbttagcompound.getInteger("powerLevel");
		powerHandler.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setBoolean("forcedLink", forcedLink);
		nbttagcompound.setInteger("powerLevel", energyStored);
		powerHandler.writeToNBT(nbttagcompound);
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction) {
		return true;
	}

	@Override
	public boolean isAddedToEnergyNet() {
		return isAdded;
	}

	@Override
	public int demandsEnergy() {
		return (AdditionalPipes.managerCapacity - energyStored) / IC2Multiplier;
	}

	@Override
	public int injectEnergy(Direction directionFrom, int amount) {
		int energy = energyStored;
		addEnergy(amount * IC2Multiplier);
		return amount - (energyStored - energy) / IC2Multiplier;
	}

	@Override
	public int getMaxSafeInput() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int getPowerLevel() {
		return energyStored;
	}

	@Override
	public boolean canForcedTeleport(ITeleportPipe pipe) {
		return forcedLink && matchesOwner(pipe);
	}

	@Override
	public boolean useEnergy(int amount, List<Object> providersToIgnore) {
		if (providersToIgnore != null && providersToIgnore.contains(this)) {
			return false;
		}
		if (canUseEnergy(amount, null)) {
			energyStored -= amount * AdditionalPipes.unitLP;
			return true;
		}
		return false;
	}

	@Override
	public boolean useEnergy(int amount) {
		return useEnergy(amount, null);
	}

	@Override
	public boolean canUseEnergy(int amount, List<Object> providersToIgnore) {
		if (providersToIgnore != null && providersToIgnore.contains(this)) {
			return false;
		}
		return energyStored >= amount * AdditionalPipes.unitLP;
	}

	@Override
	public boolean canUseEnergy(int amount) {
		return canUseEnergy(amount, null);
	}

}
