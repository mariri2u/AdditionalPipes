/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.proxy;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import additionalpipes.AdditionalPipes;
import additionalpipes.chunk.BoxEx;
import additionalpipes.chunk.EntityBlockEx;
import additionalpipes.client.gui.KeyHandler;
import additionalpipes.network.APPacketIds;
import additionalpipes.network.PacketRequestChunks;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.LaserKind;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.TransportProxyClient;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import cpw.mods.fml.client.registry.KeyBindingRegistry;

public class ClientProxy extends APProxy {

	@Override
	public void init() {
		super.init();
		//MinecraftForgeClient.preloadTexture(APDefaultProps.TEXTURE);
		KeyBindingRegistry.registerKeyBinding(new KeyHandler());
	}

	@Override
	public void registerRenderer(ItemPipe pipe) {
		MinecraftForgeClient.registerItemRenderer(pipe.itemID, TransportProxyClient.pipeItemRenderer);
	}

	private List<BoxEx> lasers = Lists.newLinkedList();
	private int playerY;

	@Override
	public void showLasers(Multiset<ChunkCoordIntPair> coordSet) {
		World world = Minecraft.getMinecraft().theWorld;
		if (world == null) {
			return;
		}
		hideLasers();

		for (Multiset.Entry<ChunkCoordIntPair> e : coordSet.entrySet()) {
			int chunkX = e.getElement().chunkXPos << 4;
			int chunkZ = e.getElement().chunkZPos << 4;

			BoxEx outsideLaser = new BoxEx();
			outsideLaser.initialize(chunkX, playerY, chunkZ, 16, true);
			outsideLaser.createLasers(world, e.getCount() == 1 ? LaserKind.Blue : LaserKind.Stripes);
			lasers.add(outsideLaser);

			BoxEx insideLaser = new BoxEx();
			insideLaser.initialize(chunkX + 7, playerY, chunkZ + 7, 2, false);
			insideLaser.createLasers(world, LaserKind.Red);
			lasers.add(insideLaser);
		}
	}

	@Override
	public void hideLasers() {
		for (BoxEx laser : lasers) {
			laser.deleteLasers();
		}
		lasers.clear();
	}

	@Override
	public void toggleLasers() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player == null) {
			return;
		}

		playerY = (int) player.boundingBox.minY;
		PacketRequestChunks packet = new PacketRequestChunks(APPacketIds.TOGGLE_LASERS, AdditionalPipes.showAllPersistentChunks);
		CoreProxy.proxy.sendToServer(packet.getPacket());
	}

	@Override
	public EntityBlockEx newEntityBlockEx(World world, double i, double j, double k, double iSize, double jSize, double kSize, LaserKind laserKind) {
		EntityBlockEx eb = super.newEntityBlockEx(world, i, j, k, iSize, jSize, kSize, laserKind);
		switch (laserKind) {
			case Blue:
				eb.texture = BuildCraftCore.blueLaserTexture;
				break;

			case Red:
				eb.texture = BuildCraftCore.redLaserTexture;
				break;

			case Stripes:
				eb.texture = BuildCraftCore.stripesLaserTexture;
				break;
		}
		return eb;
	}

}
