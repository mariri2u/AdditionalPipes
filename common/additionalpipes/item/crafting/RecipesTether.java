/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.item.crafting;

import java.util.Arrays;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import additionalpipes.AdditionalPipes;
import additionalpipes.block.BlockTeleportTether;
import additionalpipes.utils.APUtils;
import cpw.mods.fml.common.registry.GameRegistry;

public class RecipesTether implements IRecipe {

	public final int width, height;
	public final char[] pattern;

	private RecipesTether(int width, int height, char[] pattern) {
		this.width = width;
		this.height = height;
		this.pattern = pattern;
	}

	private int[] getDirections(InventoryCrafting inventory) {
		try {
			for (int x = 0; x <= 3 - width; x++) {
				for (int y = 0; y <= 3 - height; y++) {
					int[] result = getResult(inventory, x, y);
					if (result != null) {
						return result;
					}
				}
			}
		} catch (IllegalArgumentException e) {
		}
		return null;
	}

	private int[] getResult(InventoryCrafting inventory, int startX, int startY) {
		ItemStack center = null;
		ItemStack top = null, right = null, bottom = null, left = null;
		for (int invX = 0; invX < 3; invX++) {
			for (int invY = 0; invY < 3; invY++) {
				int x = invX - startX;
				int y = invY - startY;
				char kind = (x >= 0 && y >= 0 && x < width && y < height) ? pattern[x + y * width] : ' ';
				ItemStack stack = inventory.getStackInRowAndColumn(invX, invY);
				if ((kind != ' ') != isTether(stack)) {
					return null;
				}
				switch (kind) {
					case 'C':
						center = stack; break;
					case 'T':
						top = stack; break;
					case 'R':
						right = stack; break;
					case 'B':
						bottom = stack; break;
					case 'L':
						left = stack; break;
				}
			}
		}
		if (center == null) {
			AdditionalPipes.logger.severe("Recipe error");
			return null;
		}
		int[] result = BlockTeleportTether.getDirectionsForStack(center).clone();
		if (top == null && right == null && bottom == null && left == null) {
			int sum = APUtils.sum(result);
			return sum > 0 ? new int[] { 1 + sum } : null;
		}

		result = addResult(result, top,    true,  true);
		result = addResult(result, right,  false, true);
		result = addResult(result, bottom, true,  false);
		result = addResult(result, left,   false, false);
		/*if (result != null) {
			int area = (result[3] + 1 + result[1]) * (result[0] + 1 + result[2]);
			if (area > BlockTeleportTether.maxChunkDepth) {
				return null;
			}
		}*/
		return result;
	}

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		return getDirections(inventory) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		int[] dirs = getDirections(inventory);
		if (Arrays.equals(dirs, BlockTeleportTether.DEFAULT_AREA)) {
			return BlockTeleportTether.createStackSimple();
		}
		if (dirs.length == 1) {
			return BlockTeleportTether.createStackSingle(dirs[0]);
		}
		return BlockTeleportTether.createStackCustom(dirs);
	}

	@Override
	public int getRecipeSize() {
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

	private static boolean isTether(ItemStack stack) throws IllegalArgumentException {
		if (stack == null) {
			return false;
		}
		if (stack.itemID != AdditionalPipes.blockTeleportTether.blockID) {
			throw new IllegalArgumentException();
		}
		return true;
	}

	private static int[] addResult(int[] result, ItemStack stack, boolean vertical, boolean topOrRight) {
		if (stack == null || result == null) {
			return result;
		}
		int index = vertical ? 0 : 1;
		int other = vertical ? 1 : 0;
		int[] dirs = BlockTeleportTether.getDirectionsForStack(stack);
		if (dirs[other] > 0 || dirs[other + 2] > 0) {
			return null;
		}
		result[index] += dirs[index];
		result[index + 2] += dirs[index + 2];
		result[topOrRight ? index : index + 2] += 1;
		return result;
	}

	private static char rotatePatternChar(char c) {
		switch (c) {
			case 'T': return 'R';
			case 'R': return 'B';
			case 'B': return 'L';
			case 'L': return 'T';
			default:  return c;
		}
	}

	private static void addRecipe(int numOfRotate, String... layout) {
		int width = 0;
		int height = layout.length;
		for (String row : layout) {
			if (width < row.length()) {
				width = row.length();
			}
		}
		char[] pattern = new char[width * height];
		for (int y = 0; y < layout.length; y++) {
			String row = layout[y];
			for (int x = 0; x < row.length(); x++) {
				pattern[x + y * width] = row.charAt(x);
			}
			for (int x = row.length(); x < width; x++) {
				pattern[x + y * width] = ' ';
			}
		}

		GameRegistry.addRecipe(new RecipesTether(width, height, pattern.clone()));
		while (numOfRotate-- > 0) {
			char[] pattern2 = new char[height * width];
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					pattern2[(height - 1 - y) + x * height] = rotatePatternChar(pattern[x + y * width]);
				}
			}
			GameRegistry.addRecipe(new RecipesTether(height, width, pattern2));

			int tmp = width;
			width = height;
			height = tmp;
			pattern = pattern2;
		}
	}

	public static void addRecipes() {
		/*
		   -x-       -x              (x-x  x-  --x)
		   xxx  xxx  xx  xxx  xx  x  (-x-  -x  -x-)
		   -x-  -x-                  (x-x  x-  x--)
		*/
		addRecipe(0, " T ", "LCR", " B ");
		addRecipe(3, "LCR", " B ");
		addRecipe(3, " T", "LC");
		addRecipe(1, "LCR");
		addRecipe(3, "LC");
		addRecipe(0, "C");
	}

}
