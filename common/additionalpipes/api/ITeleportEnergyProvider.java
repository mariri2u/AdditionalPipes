/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.api;

public interface ITeleportEnergyProvider {

	public boolean canTeleport(ITeleportPipe pipe);

	public boolean canForcedTeleport(ITeleportPipe pipe);

	public boolean useEnergy(int amount);

	public boolean canUseEnergy(int amount);

}
