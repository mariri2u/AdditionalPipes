/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.api;

import net.minecraft.entity.player.EntityPlayer;

public interface IRestrictedTile {

	public String getOwner();

	public void initOwner(String username);

	public AccessRule getAccessRule();

	public void setAccessRule(AccessRule rule);

	public boolean hasPermission(EntityPlayer player);

	public boolean canAccess(EntityPlayer player);

	public boolean canEdit(EntityPlayer player);

	public boolean tryAccess(EntityPlayer player);

	public boolean tryEdit(EntityPlayer player);

}
