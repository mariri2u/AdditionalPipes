/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import additionalpipes.api.IRestrictedTile;
import buildcraft.core.utils.StringUtils;

public class RestrUtils {

	public static void accessDenied(EntityPlayer player, String owner) {
		player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00A7c" + String.format(StringUtils.localize("chat.edit.denied"), owner)));
	}

	public static boolean tryAccess(IRestrictedTile tile, EntityPlayer player) {
		if (!tile.canAccess(player)) {
			accessDenied(player, tile.getOwner());
			return false;
		}
		return true;
	}

	public static boolean tryEdit(IRestrictedTile tile, EntityPlayer player) {
		if (!tile.canEdit(player)) {
			accessDenied(player, tile.getOwner());
			return false;
		}
		return true;
	}

}
