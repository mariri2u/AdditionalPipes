/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.api;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import buildcraft.api.transport.IPipeTile.PipeType;

public interface ITeleportPipe {

	public PipeType getType();

	public String getOwner();

	public int getFrequency();

	public boolean isPublic();

	public World getWorld();

	public ChunkCoordinates getPosition();

}
