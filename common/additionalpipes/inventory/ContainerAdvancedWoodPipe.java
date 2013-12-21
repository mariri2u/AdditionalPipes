/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import additionalpipes.inventory.components.AdditionalPipesContainer;
import additionalpipes.inventory.components.Property;
import additionalpipes.inventory.components.PropertyBoolean;
import additionalpipes.pipes.PipeLogicAdvancedWood;
import buildcraft.core.gui.slots.SlotPhantom;

public class ContainerAdvancedWoodPipe extends AdditionalPipesContainer {

	public final PropertyBoolean propExclude;

	private PipeLogicAdvancedWood logic;

	public ContainerAdvancedWoodPipe(IInventory playerInventory, PipeLogicAdvancedWood logic) {
		super(logic.getFilters().getSizeInventory());
		this.logic = logic;

		propExclude = addPropertyToContainer(new PropertyBoolean());

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new SlotPhantom(logic.getFilters(), i, 8 + i * 18, 18));
		}

		for (int l = 0; l < 3; l++) {
			for (int k1 = 0; k1 < 9; k1++) {
				addSlotToContainer(new Slot(playerInventory, k1 + l * 9 + 9, 8 + k1 * 18, 76 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; i1++) {
			addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 134));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return logic.pipe.container.isUseableByPlayer(player);
	}

	@Override
	protected boolean onChangeProperty(int index, Property prop, EntityPlayer player) {
		if (index == propExclude.index) {
			logic.exclude = ((PropertyBoolean) prop).value;
		}
		return true;
	}

	@Override
	protected Object getPropertyValue(int index) {
		if (index == propExclude.index) {
			return logic.exclude;
		}
		return null;
	}

}