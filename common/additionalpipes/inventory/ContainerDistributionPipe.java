/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.inventory.components.AdditionalPipesContainer;
import additionalpipes.inventory.components.Property;
import additionalpipes.inventory.components.PropertyInteger;
import additionalpipes.pipes.PipeLogicDistributor;

public class ContainerDistributionPipe extends AdditionalPipesContainer {

	public final PropertyInteger[] propsData = new PropertyInteger[6];
	public final PropertyInteger propSwitch;

	private PipeLogicDistributor logic;

	public ContainerDistributionPipe(PipeLogicDistributor logic) {
		super(0);
		this.logic = logic;

		for (int i = 0; i < propsData.length; i++) {
			propsData[i] = addPropertyToContainer(new PropertyInteger());
		}
		propSwitch = addPropertyToContainer(new PropertyInteger());
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return logic.pipe.container.isUseableByPlayer(player);
	}

	@Override
	protected boolean onChangeProperty(int index, Property prop, EntityPlayer player) {
		int newValue = ((PropertyInteger) prop).value;
		if (index >= propsData[0].index && index <= propsData[5].index) {
			logic.distData[index] = newValue;

			boolean found = false;
			for (int data : logic.distData) {
				if (data > 0) {
					found = true;
					break;
				}
			}

			if (!found) {
				for (int i = 0; i < logic.distData.length; i++) {
					logic.distData[i] = 1;
				}
			}
			logic.enforceValidSide();
		} else if (index == propSwitch.index) {
			if (logic.distData[newValue] > 0) {
				logic.switchTo(ForgeDirection.VALID_DIRECTIONS[newValue], false);
			}
		}
		return true;
	}

	@Override
	protected Object getPropertyValue(int index) {
		if (index >= propsData[0].index && index <= propsData[5].index) {
			return logic.distData[index];
		} else if (index == propSwitch.index) {
			return logic.pipe.container.getBlockMetadata();
		}
		return null;
	}

}
