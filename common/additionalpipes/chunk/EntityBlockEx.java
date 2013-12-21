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
import buildcraft.core.EntityBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityBlockEx extends EntityBlock {

	public EntityBlockEx(World world, double i, double j, double k, double iSize, double jSize, double kSize) {
		super(world, i, j, k, iSize, jSize, kSize);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender(float par1) {
		return 15 << 20 | 15 << 4;
	}

}
