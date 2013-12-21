/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.tileentity;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import additionalpipes.AdditionalPipes;
import additionalpipes.api.AccessRule;
import additionalpipes.api.IRestrictedTile;
import additionalpipes.block.BlockTeleportTether;
import additionalpipes.utils.APUtils;
import additionalpipes.utils.RestrUtils;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.TileNetworkData;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TileTeleportTether extends TileBuildCraft implements IRestrictedTile {

	private List<Ticket> chunkTickets = ImmutableList.of();
	private int loadedArea = 0;
	private int[] lastRequested = null;
	public EntityPlayer placedBy = null;

	public String owner = "";
	public AccessRule accessRule = AccessRule.SHARED;
	@TileNetworkData(staticSize = 4)
	public int[] directions = BlockTeleportTether.DEFAULT_AREA.clone();

	public TileTeleportTether() {}

	private boolean isInitialized() {
		return !(chunkTickets instanceof ImmutableList);
	}

	private int getAreaToLoad() {
		return (directions[0] + 1 + directions[2]) * (directions[1] + 1 + directions[3]);
	}

	private void warning(String message) {
		if (placedBy != null) {
			placedBy.sendChatToPlayer(ChatMessageComponent.func_111066_d(message));
		}
		EntityPlayer ownerPlayer = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(owner);
		if (ownerPlayer != null && placedBy != ownerPlayer) {
			ownerPlayer.sendChatToPlayer(ChatMessageComponent.func_111066_d(message));
		}
		AdditionalPipes.logger.warning(message);
	}

	private void unableToLoadChunk() {
		String message = String.format("[Additional Pipes] Teleport Tether at %d, %d, %d will not work because there are no more chunkloaders available",
				xCoord, yCoord, zCoord);
		warning(message);
	}

	private void tooLongTime() {
		String message = String.format("[Additional Pipes] Teleport Tether at %d, %d, %d will not work because chunkloader took too long",
				xCoord, yCoord, zCoord);
		warning(message);
	}

	public void requestTicket() {
		if (Arrays.equals(lastRequested, directions)) {
			return;
		}

		releaseTickets();
		lastRequested = directions.clone();

		int count = APUtils.divideAndCeil(getAreaToLoad(), BlockTeleportTether.maxChunkDepth);
		if (count >= BlockTeleportTether.maxTicketLength) {
			unableToLoadChunk();
			return;
		}

		long time = System.currentTimeMillis();
		chunkTickets = Lists.newArrayListWithCapacity(count);
		for (int i = 0; i < count; i++) {
			Ticket ticket = ForgeChunkManager.requestTicket(AdditionalPipes.instance, worldObj, Type.NORMAL);
			if (ticket == null) {
				unableToLoadChunk();
				return;
			}

			ticket.getModData().setInteger("tileX", xCoord);
			ticket.getModData().setInteger("tileY", yCoord);
			ticket.getModData().setInteger("tileZ", zCoord);
			forceChunkLoading(ticket);

			if (System.currentTimeMillis() - time >= BlockTeleportTether.LOADING_THRESHOLD) {
				tooLongTime();
				return;
			}
		}
	}

	private void releaseTickets() {
		for (Ticket ticket : chunkTickets) {
			ForgeChunkManager.releaseTicket(ticket);
		}
		loadedArea = 0;
	}

	public void forceChunkLoading(Ticket ticket) {
		if (!isInitialized()) {
			chunkTickets = Lists.newArrayList();
		}
		chunkTickets.add(ticket);

		int numOfChunks = ticket.getChunkList().size();
		int width = directions[1] + 1 + directions[3];
		int baseX = (xCoord >> 4) - directions[3], baseZ = (zCoord >> 4) - directions[0];
		int max = Math.min(getAreaToLoad(), loadedArea + BlockTeleportTether.maxChunkDepth);
		for (; loadedArea < max; loadedArea++) {
			ChunkCoordIntPair chunk = new ChunkCoordIntPair(baseX + loadedArea % width, baseZ + loadedArea / width);
			ForgeChunkManager.forceChunk(ticket, chunk);
			if (ticket.getChunkList().size() != ++numOfChunks) {
				unableToLoadChunk();
				return;
			}
		}
	}

	@Override
	public void validate() {
		super.validate();
		if (CoreProxy.proxy.isSimulating(worldObj) && isInitialized()) {
			// Moved by someone (Portal Gun, RP Frame, ...)
			lastRequested = null;
			requestTicket();
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		if (CoreProxy.proxy.isSimulating(worldObj) && !isInitialized()) {
			requestTicket();
		}
	}

	@Override
	public void invalidate() {
		releaseTickets();
		super.invalidate();
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		owner = data.getString("owner");
		accessRule = AccessRule.values()[data.getInteger("accessRule")];
		if (data.hasKey("directions")) {
			directions = data.getIntArray("directions");
		}
		lastRequested = directions.clone();
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setString("owner", owner);
		data.setInteger("accessRule", accessRule.ordinal());
		data.setIntArray("directions", directions);
	}

	@Override
	public String getOwner() {
		return owner;
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

}
