/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.proxy;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import additionalpipes.chunk.EntityBlockEx;
import buildcraft.api.core.LaserKind;
import buildcraft.transport.ItemPipe;

import com.google.common.collect.Multiset;

import cpw.mods.fml.common.SidedProxy;

public class APProxy {

	@SidedProxy(clientSide = "additionalpipes.proxy.ClientProxy", serverSide = "additionalpipes.proxy.APProxy")
	public static APProxy proxy;

	public void init() {
	}

	public void registerRenderer(ItemPipe pipe) {
	}

	public void showLasers(Multiset<ChunkCoordIntPair> coordSet) {
	}

	public void hideLasers() {
	}

	public void toggleLasers() {
	}

	public EntityBlockEx newEntityBlockEx(World world, double i, double j, double k, double iSize, double jSize, double kSize, LaserKind laserKind) {
		return new EntityBlockEx(world, i, j, k, iSize, jSize, kSize);
	}

}
