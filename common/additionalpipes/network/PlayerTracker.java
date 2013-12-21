/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import additionalpipes.AdditionalPipes;
import additionalpipes.chunk.ChunkManager;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.IPlayerTracker;

public class PlayerTracker implements IPlayerTracker {

	@Override
	public void onPlayerLogin(EntityPlayer player) {
		PacketConfiguration packet = new PacketConfiguration(APPacketIds.CONFIGURATION, AdditionalPipes.disablePermissions,
				AdditionalPipes.managerCapacity);
		CoreProxy.proxy.sendToPlayer(player, packet);
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		ChunkManager.instance().hideLasers((EntityPlayerMP) player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		ChunkManager.instance().hideLasers((EntityPlayerMP) player);
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
	}

}
