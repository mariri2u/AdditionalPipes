/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.chunk;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.ForceChunkEvent;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.UnforceChunkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkWatchEvent;
import additionalpipes.AdditionalPipes;
import additionalpipes.network.APPacketIds;
import additionalpipes.network.PacketChunkCoordList;
import additionalpipes.tileentity.TileTeleportTether;
import additionalpipes.utils.APDefaultProps;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ChunkManager implements ITickHandler, ForgeChunkManager.OrderedLoadingCallback {

	private static ChunkManager INSTANCE = new ChunkManager();
	private Map<EntityPlayerMP, Multiset<ChunkCoordIntPair>> players = new MapMaker().weakKeys().makeMap();

	private ChunkManager() {
		TickRegistry.registerTickHandler(this, Side.SERVER);
		ForgeChunkManager.setForcedChunkLoadingCallback(AdditionalPipes.instance, this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static ChunkManager instance() {
		return INSTANCE;
	}

	public void toggleLasers(EntityPlayerMP player, boolean showAllPersistentChunks) {
		if (!players.containsKey(player)) {
			Multiset<ChunkCoordIntPair> newSet = HashMultiset.create();
			if (showAllPersistentChunks) {
				newSet.add(null);
			}
			players.put(player, newSet);
			sendPersistentChunks(player);
		} else {
			hideLasers(player);
		}
	}

	public void hideLasers(EntityPlayerMP player) {
		players.remove(player);

		PacketChunkCoordList packet = new PacketChunkCoordList(APPacketIds.UPDATE_LASERS, ImmutableMultiset.<ChunkCoordIntPair>of());
		CoreProxy.proxy.sendToPlayer(player, packet);
	}

	private void updatePersistentChunks() {
		for (EntityPlayerMP player : players.keySet()) {
			sendPersistentChunks(player);
		}
	}

	public void sendPersistentChunks(EntityPlayerMP player) {
		Multiset<ChunkCoordIntPair> oldSet = players.get(player);
		boolean showAllPersistentChunks = oldSet.remove(null);
		Multiset<ChunkCoordIntPair> newSet = HashMultiset.create();

		WorldServer world = (WorldServer) player.worldObj;
		for (Map.Entry<ChunkCoordIntPair, Ticket> e : ForgeChunkManager.getPersistentChunksFor(world).entries()) {
			if (!showAllPersistentChunks && !APDefaultProps.ID.equals(e.getValue().getModId())) {
				continue;
			}

			if (world.getPlayerManager().isPlayerWatchingChunk(player, e.getKey().chunkXPos, e.getKey().chunkZPos)) {
				newSet.add(e.getKey());
			}
		}

		if (!oldSet.equals(newSet)) {
			PacketChunkCoordList packet = new PacketChunkCoordList(APPacketIds.UPDATE_LASERS, newSet);
			CoreProxy.proxy.sendToPlayer(player, packet);
			if (showAllPersistentChunks) {
				newSet.add(null);
			}
			players.put(player, newSet);
		}
	}

	private Set<EntityPlayerMP> toUpdate = Sets.newHashSet();

	public void scheduleUpdateChunks(EntityPlayerMP player) {
		toUpdate.add(player);
	}

	/* Event Handler */

	@ForgeSubscribe
	public void onChunkWatch(ChunkWatchEvent event) {
		// Watch, UnWatch
		if (players.containsKey(event.player)) {
			scheduleUpdateChunks(event.player);
		}
	}

	@ForgeSubscribe
	public void onForceChunk(ForceChunkEvent event) {
		scheduleUpdateChunks(null);
	}

	@ForgeSubscribe
	public void onUnforceChunk(UnforceChunkEvent event) {
		scheduleUpdateChunks(null);
	}

	/* ITickHandler */

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (toUpdate.remove(null)) {
			updatePersistentChunks();
		} else {
			for (EntityPlayerMP player : toUpdate) {
				if (players.containsKey(player)) {
					sendPersistentChunks(player);
				}
			}
		}
		if (!toUpdate.isEmpty()) {
			toUpdate.clear();
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel() {
		return "Additional Pipes - Chunk Manager";
	}

	/* ForgeChunkManager.OrderedLoadingCallback */

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			int tileX = data.getInteger("tileX"), tileY = data.getInteger("tileY"), tileZ = data.getInteger("tileZ");
			TileTeleportTether tether = (TileTeleportTether) world.getBlockTileEntity(tileX, tileY, tileZ);
			tether.forceChunkLoading(ticket);
		}
	}

	@Override
	public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world, int maxTicketCount) {
		if (AdditionalPipes.blockTeleportTether == null) {
			return Collections.emptyList();
		}

		List<Ticket> validTickets = Lists.newArrayListWithCapacity(tickets.size());
		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			int tileX = data.getInteger("tileX"), tileY = data.getInteger("tileY"), tileZ = data.getInteger("tileZ");
			if (world.getBlockId(tileX, tileY, tileZ) == AdditionalPipes.blockTeleportTether.blockID) {
				validTickets.add(ticket);
			}
		}
		return validTickets;
	}

}