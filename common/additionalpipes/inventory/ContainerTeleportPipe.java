/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory;

import net.minecraft.entity.player.EntityPlayer;
import additionalpipes.inventory.components.ContainerRestrictedTile;
import additionalpipes.inventory.components.Property;
import additionalpipes.inventory.components.PropertyBoolean;
import additionalpipes.inventory.components.PropertyIntArray;
import additionalpipes.inventory.components.PropertyInteger;
import additionalpipes.inventory.components.PropertyStrArray;
import additionalpipes.inventory.components.PropertyString;
import additionalpipes.pipes.PipeLogicTeleport;
import additionalpipes.utils.FrequencyMap;
import buildcraft.api.transport.IPipeTile.PipeType;

import com.google.common.base.Strings;

public class ContainerTeleportPipe extends ContainerRestrictedTile {

	public final PropertyInteger propFreq;
	public final PropertyBoolean propCanReceive;
	public final PropertyBoolean propIsPublic;
	public final PropertyInteger propConnected;
	public final PropertyString propFreqName;
	public final PropertyStrArray propFreqMapNames;
	public final PropertyIntArray propFreqMapFreqs;
	public final PropertyInteger propPipeState;

	public final PipeLogicTeleport logic;

	public ContainerTeleportPipe(PipeLogicTeleport logic) {
		super(logic, 0);
		this.logic = logic;

		propFreq = addPropertyToContainer(new PropertyInteger());
		propCanReceive = addPropertyToContainer(new PropertyBoolean());
		propIsPublic = addPropertyToContainer(new PropertyBoolean());
		propConnected = addPropertyToContainer(new PropertyInteger());
		propFreqName = addPropertyToContainer(new PropertyString());
		propFreqMapNames = addPropertyToContainer(new PropertyStrArray());
		propFreqMapFreqs = addPropertyToContainer(new PropertyIntArray());
		propPipeState = addPropertyToContainer(new PropertyInteger());
	}

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return logic.pipe.container.isUseableByPlayer(var1);
	}

	private String getOwner() {
		return logic.isPublic ? "" : logic.owner;
	}

	@Override
	protected boolean onChangeProperty(int index, Property prop, EntityPlayer player) {
		if (index == propFreq.index) {
			if (logic.tryEdit(player)) {
				logic.doPreModify();
				logic.freq = ((PropertyInteger) prop).value;
				logic.doModify();
			}
		} else if (index == propCanReceive.index) {
			if (logic.tryEdit(player)) {
				logic.doPreModify();
				logic.canReceive = ((PropertyBoolean) prop).value;
				logic.doModify();
			}
		} else if (index == propIsPublic.index) {
			if (logic.tryEdit(player)) {
				logic.doPreModify();
				logic.isPublic = ((PropertyBoolean) prop).value;
				logic.doModify();
			}
		} else if (index == propFreqName.index) {
			if (logic.tryEdit(player)) {
				logic.doPreModify();
				String name = ((PropertyString) prop).value;
				FrequencyMap freqMap = FrequencyMap.load(logic.getType());
				if (name.isEmpty()) {
					freqMap.removeFreqName(getOwner(), logic.freq);
				} else {
					freqMap.setFreqName(getOwner(), logic.freq, name);
				}
				freqMap.save();
				logic.doModify();
			}
			return false;// To update freqmap-freqs at the same time
		} else {
			return super.onChangeProperty(index, prop, player);
		}
		return true;
	}

	@Override
	protected Object getPropertyValue(int index) {
		if (index == propFreq.index) {
			return logic.freq;
		} else if (index == propCanReceive.index) {
			return logic.canReceive;
		} else if (index == propIsPublic.index) {
			return logic.isPublic;
		} else if (index == propConnected.index) {
			return logic.getConnectedPipes(null).size();
		} else if (index == propFreqName.index) {
			return Strings.nullToEmpty(FrequencyMap.load(logic.getType()).getFreqName(getOwner(), logic.freq));
		} else if (index == propFreqMapNames.index) {
			return FrequencyMap.load(logic.getType()).sortedNames(getOwner());
		} else if (index == propFreqMapFreqs.index) {
			return FrequencyMap.load(logic.getType()).sortedFreqs(getOwner());
		} else if (index == propPipeState.index) {
			if (!logic.canTeleport()) {
				return 1;
			}
			if (logic.getType() != PipeType.STRUCTURE && !logic.canUseEnergy(1)) {
				return 2;
			}
			return 0;
		} else {
			return super.getPropertyValue(index);
		}
	}

}
