/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import additionalpipes.block.BlockTeleportTether;
import buildcraft.core.ItemBlockBuildCraft;

import com.google.common.base.Strings;

public class ItemTeleportTether extends ItemBlockBuildCraft {

	public ItemTeleportTether(int itemID) {
		super(itemID);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {
		int[] dirs = BlockTeleportTether.getDirectionsForStack(stack);
		String line = Strings.repeat("X", dirs[3] + 1 + dirs[1]);
		for (int y = 0; y < dirs[0]; y++) {
			list.add(line);
		}
		list.add(line.substring(0, dirs[3]) + "O" + line.substring(0, dirs[1]));
		for (int y = 0; y < dirs[2]; y++) {
			list.add(line);
		}
		//list.add(Ints.join(", ", dirs));
		//list.add(String.format("%d x %d", dirs[1] + 1 + dirs[3], dirs[0] + 1 + dirs[2]));
	}

}
