/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import additionalpipes.AdditionalPipes;
import additionalpipes.chunk.ChunkManager;
import additionalpipes.inventory.components.AdditionalPipesContainer;
import additionalpipes.proxy.APProxy;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packetCustom, Player player) {
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packetCustom.data));
		try {
			int packetID = data.read();
			EntityPlayer playerEntity = (EntityPlayer) player;

			switch (packetID) {
				case APPacketIds.CONFIGURATION: {
					PacketConfiguration packet = new PacketConfiguration();
					packet.readData(data);
					AdditionalPipes.remoteDisablePermissions = packet.disablePermissions;
					AdditionalPipes.remoteManagerCapacity = packet.managerCapacity;
					break;
				}

				case APPacketIds.PUSH_PROPERTY: {
					PacketProperty packet = new PacketProperty();
					packet.readData(data);
					if (playerEntity.openContainer.windowId == packet.windowId && playerEntity.openContainer.isPlayerNotUsingContainer(playerEntity)
							&& playerEntity.openContainer instanceof AdditionalPipesContainer) {
						AdditionalPipesContainer container = (AdditionalPipesContainer) playerEntity.openContainer;
						container.changeProperty(packet.index, packet.prop, playerEntity);
					}
					break;
				}

				case APPacketIds.BROADCAST_PROPERTY: {
					PacketContainer packet = new PacketContainer();
					packet.readData(data);
					if (playerEntity.openContainer.windowId == packet.windowId && playerEntity.openContainer.isPlayerNotUsingContainer(playerEntity)
							&& playerEntity.openContainer instanceof AdditionalPipesContainer) {
						AdditionalPipesContainer container = (AdditionalPipesContainer) playerEntity.openContainer;
						container.setProperty(packet.index, data);
					}
					break;
				}

				case APPacketIds.TOGGLE_LASERS: {
					// Server
					PacketRequestChunks packet = new PacketRequestChunks();
					packet.readData(data);
					ChunkManager.instance().toggleLasers((EntityPlayerMP) player, AdditionalPipes.showAllPersistentChunks);
					break;
				}

				case APPacketIds.UPDATE_LASERS: {
					// Client
					PacketChunkCoordList packet = new PacketChunkCoordList();
					packet.readData(data);
					APProxy.proxy.showLasers(packet.coordSet);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
