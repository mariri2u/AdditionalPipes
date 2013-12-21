/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import additionalpipes.AdditionalPipes;
import additionalpipes.inventory.APGuiIds;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;

public class PipeLogicAdvancedWood {

	public final Pipe<?> pipe;

	public boolean exclude = false;
	private final SimpleInventory filters = new SimpleInventory(9, "Filters", 1);

	public PipeLogicAdvancedWood(Pipe<?> pipe) {
		this.pipe = pipe;
	}

	public void toggleExclude() {
		exclude = !exclude;
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		if (!CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
			entityplayer.openGui(AdditionalPipes.instance, APGuiIds.PIPE_ADVANCED_WOOD, pipe.container.worldObj, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
		}

		return true;
	}

	/* SAVING & LOADING */
	public void writeToNBT(NBTTagCompound data) {
		data.setBoolean("exclude", exclude);
		filters.writeToNBT(data);
	}

	public void readFromNBT(NBTTagCompound data) {
		exclude = data.getBoolean("exclude");
		filters.readFromNBT(data);
	}

	public IInventory getFilters() {
		return filters;
	}

}
