/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.chunk;

import net.minecraft.world.World;
import additionalpipes.proxy.APProxy;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.core.EntityBlock;
import buildcraft.core.proxy.CoreProxy;

public class BoxEx {

	int x, z, y, size;
	boolean yStretch;

	private EntityBlock lasers[];

	public BoxEx() {
		reset();
	}

	public void reset() {

		x = Integer.MAX_VALUE;
		y = Integer.MAX_VALUE;
		z = Integer.MAX_VALUE;
		size = Integer.MAX_VALUE;
		yStretch = false;
	}

	public void initialize(int x, int y, int z, int size, boolean yStretch) {

		this.x = x;
		this.y = y;
		this.z = z;
		this.size = size;
		this.yStretch = yStretch;
	}

	public static EntityBlock createLaser(World world, Position p1, Position p2, LaserKind kind) {
		if (p1.equals(p2))
			return null;

		double iSize = p2.x - p1.x;
		double jSize = p2.y - p1.y;
		double kSize = p2.z - p1.z;

		double i = p1.x;
		double j = p1.y;
		double k = p1.z;

		if (iSize != 0) {
			i += 0.5;
			j += 0.45;
			k += 0.45;

			jSize = 0.10;
			kSize = 0.10;
		} else if (jSize != 0) {
			i += 0.45;
			j += 0.5;
			k += 0.45;

			iSize = 0.10;
			kSize = 0.10;
		} else if (kSize != 0) {
			i += 0.45;
			j += 0.45;
			k += 0.5;

			iSize = 0.10;
			jSize = 0.10;
		}

		EntityBlock block = APProxy.proxy.newEntityBlockEx(world, i, j, k, iSize, jSize, kSize, kind);

		world.spawnEntityInWorld(block);

		return block;
	}

	public static EntityBlock[] createLaserBox(World world, int x, int y, int z, int size, LaserKind kind, boolean yStretch) {
		EntityBlock lasers[] = new EntityBlock[yStretch ? 8 : 4];
		Position[] p = new Position[4];

		p[0] = new Position(x - 0.45, y, z - 0.45);
		p[1] = new Position(x + size - 0.55, y, z - 0.45);
		p[2] = new Position(x - 0.45, y, z + size - 0.55);
		p[3] = new Position(x + size - 0.55, y, z + size - 0.55);

		lasers[0] = createLaser(world, p[0], p[1], kind);
		lasers[1] = createLaser(world, p[2], p[3], kind);
		lasers[2] = createLaser(world, p[0], p[2], kind);
		lasers[3] = createLaser(world, p[1], p[3], kind);
		if (yStretch) {
			for (int i = 0; i < 4; i++) {
				Position p1 = new Position(p[i]);
				p1.y = y - 16;
				Position p2 = new Position(p[i]);
				p2.y = y + 16;
				lasers[i + 4] = createLaser(world, p1, p2, kind);
			}
		}

		return lasers;
	}

	public void createLasers(World world, LaserKind kind) {

		if (lasers == null) {
			lasers = BoxEx.createLaserBox(world, x, y, z, size, kind, yStretch);
		}
	}

	public void deleteLasers() {

		if (lasers != null) {
			for (EntityBlock b : lasers) {
				CoreProxy.proxy.removeEntity(b);
			}

			lasers = null;
		}
	}

}
