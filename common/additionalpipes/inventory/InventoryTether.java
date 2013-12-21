/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import additionalpipes.block.BlockTeleportTether;
import additionalpipes.tileentity.TileTeleportTether;
import buildcraft.core.proxy.CoreProxy;

public class InventoryTether implements IInventory {

	private final EntityPlayer player;
	public final TileTeleportTether tether;
	private final ItemStack[] stacks = new ItemStack[5];

	public InventoryTether(EntityPlayer player, TileTeleportTether tether) {
		this.player = player;
		this.tether = tether;
		for (int i = 0; i < tether.directions.length; i++) {
			stacks[i] = BlockTeleportTether.createStackSingle(0);
		}
	}

	public ItemStack[] getStacks() {
		return stacks;
	}

	@Override
	public int getSizeInventory() {
		return stacks.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (i == 4) {
			return stacks[4] = BlockTeleportTether.getItemStack(tether);
		}
		ItemStack stack = stacks[i];
		stack.stackSize = tether.directions[i];
		return stack.stackSize > 0 ? stack : null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		//stacks[i].stackSize = tether.directions[i];
		if (stacks[i].stackSize == 0) {
			return null;
		}
		if (stacks[i].stackSize > j) {
			return stacks[i].splitStack(j);
		}
		ItemStack stack = stacks[i].copy();
		stacks[i].stackSize = 0;
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		if (i != 4) {
			stacks[i].stackSize = stack != null ? stack.stackSize : 0;
		}
	}

	@Override
	public String getInvName() {
		return "container.teleportTether";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {
		for (int i = 0; i < tether.directions.length; i++) {
			tether.directions[i] = stacks[i].stackSize;
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return tether.worldObj.getBlockTileEntity(tether.xCoord, tether.yCoord, tether.zCoord) == tether;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
		if (CoreProxy.proxy.isSimulating(tether.worldObj)) {
			tether.placedBy = player;
			tether.requestTicket();
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

}
