/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory;

import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import additionalpipes.AdditionalPipes;
import additionalpipes.inventory.components.ContainerRestrictedTile;
import additionalpipes.tileentity.TileTeleportTether;
import buildcraft.core.gui.slots.SlotUntouchable;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.primitives.Ints;

public class ContainerTeleportTether extends ContainerRestrictedTile {

	private final class SlotTetherSingle extends Slot {

		public SlotTetherSingle(InventoryTether inventory, int id, int x, int y) {
			super(inventory, id, x, y);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return stack.itemID == AdditionalPipes.blockTeleportTether.blockID && stack.hasTagCompound() &&
					Collections.frequency(Ints.asList(stack.stackTagCompound.getIntArray("directions")), 0) == 4;
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			return CoreProxy.proxy.isSimulating(tether.worldObj) ? tether.tryEdit(player) : canEdit(player.username);
		}

	}

	private final TileTeleportTether tether;
	public final InventoryTether tetherInventory;

	public ContainerTeleportTether(EntityPlayer player, TileTeleportTether tether) {
		super(tether, 4);
		this.tether = tether;

		tetherInventory = new InventoryTether(player, tether);
		addSlotToContainer(new SlotTetherSingle(tetherInventory, 0, 48, 17));
		addSlotToContainer(new SlotTetherSingle(tetherInventory, 1, 66, 35));
		addSlotToContainer(new SlotTetherSingle(tetherInventory, 2, 48, 53));
		addSlotToContainer(new SlotTetherSingle(tetherInventory, 3, 30, 35));
		addSlotToContainer(new SlotUntouchable(tetherInventory, 4, 124, 35));

		for (int l = 0; l < 3; l++) {
			for (int i1 = 0; i1 < 9; i1++) {
				addSlotToContainer(new Slot(player.inventory, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
			}
		}

		for (int l = 0; l < 9; l++) {
			addSlotToContainer(new Slot(player.inventory, l, 8 + l * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return tether.worldObj.getBlockTileEntity(tether.xCoord, tether.yCoord, tether.zCoord) == tether;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		tetherInventory.closeChest();
	}

}
