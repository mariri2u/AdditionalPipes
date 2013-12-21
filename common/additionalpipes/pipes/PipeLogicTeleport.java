/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import additionalpipes.AdditionalPipes;
import additionalpipes.api.AccessRule;
import additionalpipes.api.IRestrictedTile;
import additionalpipes.api.ITeleportPipe;
import additionalpipes.api.TeleportManager;
import additionalpipes.api.TeleportManager.TeleportPipeEvent;
import additionalpipes.inventory.APGuiIds;
import additionalpipes.utils.APUtils;
import additionalpipes.utils.RestrUtils;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class PipeLogicTeleport implements ITeleportPipe, IRestrictedTile {

	public final Pipe<?> pipe;
	public int freq = 0;
	public boolean canReceive = false;
	public String owner = "";
	public AccessRule accessRule = AccessRule.SHARED;
	public boolean isPublic = false;

	public PipeLogicTeleport(Pipe<?> pipe) {
		this.pipe = pipe;
	}

	@Override
	public PipeType getType() {
		return pipe.transport.getPipeType();
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public int getFrequency() {
		return freq;
	}

	@Override
	public boolean isPublic() {
		return isPublic;
	}

	@Override
	public World getWorld() {
		return pipe.container.worldObj;
	}

	@Override
	public ChunkCoordinates getPosition() {
		return new ChunkCoordinates(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
	}

	@Override
	public void initOwner(String username) {
		if (Strings.isNullOrEmpty(owner)) {
			owner = username;
		}
	}

	@Override
	public AccessRule getAccessRule() {
		return accessRule;
	}

	@Override
	public void setAccessRule(AccessRule rule) {
		accessRule = rule;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return AdditionalPipes.disablePermissions || owner.equalsIgnoreCase(player.username);
	}

	@Override
	public boolean canAccess(EntityPlayer player) {
		return hasPermission(player) || accessRule != AccessRule.PRIVATE;
	}

	@Override
	public boolean canEdit(EntityPlayer player) {
		return hasPermission(player) || accessRule == AccessRule.SHARED;
	}

	@Override
	public boolean tryAccess(EntityPlayer player) {
		return RestrUtils.tryAccess(this, player);
	}

	@Override
	public boolean tryEdit(EntityPlayer player) {
		return RestrUtils.tryEdit(this, player);
	}

	public void doPreModify() {
		MinecraftForge.EVENT_BUS.post(new TeleportPipeEvent.PreModify(this));
	}

	public void doModify() {
		MinecraftForge.EVENT_BUS.post(new TeleportPipeEvent.Modify(this));
	}

	public void initialize() {
		if (CoreProxy.proxy.isSimulating(pipe.container.worldObj)) {
			TeleportManager.addTeleportPipe(this);
		}
	}

	public void invalidate() {
		if (CoreProxy.proxy.isSimulating(pipe.container.worldObj)) {
			TeleportManager.removeTeleportPipe(this);
		}
	}

	public void onChunkUnload() {
		if (CoreProxy.proxy.isSimulating(pipe.container.worldObj)) {
			TeleportManager.removeTeleportPipe(this);
		}
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		if (CoreProxy.proxy.isSimulating(pipe.container.worldObj)) {
			if (Strings.isNullOrEmpty(owner)) {
				owner = entityplayer.username;
			}

			if (tryAccess(entityplayer)) {
				entityplayer.openGui(AdditionalPipes.instance, APGuiIds.PIPE_TELEPORT, pipe.container.worldObj,
						pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
			}
		}

		return true;
	}

	/*public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return tile instanceof TileGenericPipe;
	}*/

	public List<PipeLogicTeleport> getConnectedPipeLogics(Boolean forcedReceive) {
		APUtils.checkIllegalClientAccess();

		Set<ITeleportPipe> pipes = TeleportManager.getPipes();
		List<PipeLogicTeleport> result = Lists.newArrayListWithExpectedSize(pipes.size() / 2);
		for (ITeleportPipe tp : pipes) {
			if (!(tp instanceof PipeLogicTeleport))
				continue;
			PipeLogicTeleport other = (PipeLogicTeleport) tp;
			if (this.getType() != other.getType())
				continue;
			if (!(this.isPublic ? other.isPublic : !other.isPublic && this.owner.equalsIgnoreCase(other.owner)))
				continue;
			if (this.freq != other.freq)
				continue;
			if (!this.canTeleport() || !other.canTeleport())
				continue;
			if (forcedReceive != null && other.canReceive != forcedReceive)
				continue;
			//if (!this.getPosition().equals(other.getPosition()) || this.getWorld() != other.getWorld())
			if (this != other)
				result.add(other);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public <P extends ITeleportLogicProvider> List<P> getConnectedPipes(Boolean forcedReceive) {
		return Lists.transform(getConnectedPipeLogics(forcedReceive), new Function<PipeLogicTeleport, P>() {
			@Override
			public P apply(PipeLogicTeleport input) {
				return (P) input.pipe;
			}
		});
	}

	public boolean useEnergy(int amount) {
		if (AdditionalPipes.disableEnergyUsage && AdditionalPipes.disableLinkingUsage) {
			return true;
		}

		return TeleportManager.useForcedEnergy(this, amount, AdditionalPipes.forcedEnergyFactor);
	}

	public boolean canUseEnergy(int amount) {
		if (AdditionalPipes.disableEnergyUsage && AdditionalPipes.disableLinkingUsage) {
			return true;
		}

		return TeleportManager.canUseForcedEnergy(this, amount, AdditionalPipes.forcedEnergyFactor);
	}

	public boolean canTeleport() {
		if (AdditionalPipes.disableEnergyUsage && AdditionalPipes.disableLinkingUsage) {
			return true;
		}

		return TeleportManager.canForcedTeleport(this);
	}

	public void readFromNBT(NBTTagCompound data) {
		freq = data.getInteger("freq");
		canReceive = data.getBoolean("canReceive");
		owner = data.getString("owner");
		accessRule = AccessRule.values()[data.getInteger("accessRule")];
		isPublic = data.getBoolean("isPublic");
	}

	public void writeToNBT(NBTTagCompound data) {
		data.setInteger("freq", freq);
		data.setBoolean("canReceive", canReceive);
		data.setString("owner", owner);
		data.setInteger("accessRule", accessRule.ordinal());
		data.setBoolean("isPublic", isPublic);
	}

}
