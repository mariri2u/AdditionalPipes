/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.item.crafting;

import java.util.Map;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;

public class RecipesRestore implements IRecipe {

	private ItemPipe getItemPipe(InventoryCrafting inventory) {
		ItemPipe pipe = null;
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null) {
				if (!(item.getItem() instanceof ItemPipe) || pipe != null) {
					return null;
				}
				pipe = (ItemPipe) item.getItem();
			}
		}
		return pipe;
	}

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		return getCraftingResult(inventory) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		ItemPipe pipe = getItemPipe(inventory);
		if (pipe == null) {
			return null;
		}
		String classSrc = BlockGenericPipe.pipes.get(pipe.itemID).getSimpleName();
		// PipeFluidsGold -> PipeItemsGold
		String classDst = classSrc.replaceAll("Fluids|Power|Structure", "Items");
		if (classSrc.equals(classDst)) {
			return null;
		}
		for (Map.Entry<Integer, Class<? extends Pipe>> e : BlockGenericPipe.pipes.entrySet()) {
			if (e.getValue().getSimpleName().equals(classDst)) {
				return new ItemStack(e.getKey(), 1, 0);
			}
		}
		return null;
	}

	@Override
	public int getRecipeSize() {
		return 1;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

}
