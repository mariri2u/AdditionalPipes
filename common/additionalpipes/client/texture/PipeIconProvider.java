/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.client.texture;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeIconProvider implements IIconProvider {

	public static final PipeIconProvider INSTANCE = new PipeIconProvider();

	public enum TYPE {
		PipeStructureTeleport("pipeStructureTeleport"),

		PipeItemsTeleport("pipeItemsTeleport"),

		PipeItemsDistributor_Down("pipeItemsDistributor_down"),
		PipeItemsDistributor_Up("pipeItemsDistributor_up"),
		PipeItemsDistributor_North("pipeItemsDistributor_north"),
		PipeItemsDistributor_South("pipeItemsDistributor_south"),
		PipeItemsDistributor_West("pipeItemsDistributor_west"),
		PipeItemsDistributor_East("pipeItemsDistributor_east"),

		PipeItemsAdvancedWood_Standard("pipeItemsAdvancedWood_standard"),
		PipeAllAdvancedWood_Solid("pipeAllAdvancedWood_solid"),

		PipeItemsAdvancedInsertion("pipeItemsAdvancedInsertion"),
		PipeItemsRedstone_Standard("pipeItemsRedstone_standard"),
		PipeItemsRedstone_Powered("pipeItemsRedstone_powered"),

		PipeFluidsTeleport("pipeFluidsTeleport"),
		PipeFluidsAdvancedInsertion("pipeFluidsAdvancedInsertion"),
		PipeFluidsRedstone_Standard("pipeFluidsRedstone_standard"),
		PipeFluidsRedstone_Powered("pipeFluidsRedstone_powered"),

		PipePowerTeleport("pipePowerTeleport"),
		PipePowerRedstone_Standard("pipePowerRedstone_standard"),
		PipePowerRedstone_Powered("pipePowerRedstone_powered"),
		PipePowerAdvancedWood_Standard("pipePowerAdvancedWood_standard");

		public static final TYPE[] VALUES = values();
		private final String iconTag;
		private Icon icon;

		private TYPE(String iconTag) {
			this.iconTag = iconTag;
		}

		private void registerIcon(IconRegister iconRegister) {
			icon = iconRegister.registerIcon("additionalpipes:" + iconTag);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int pipeIconIndex) {
		return TYPE.VALUES[pipeIconIndex].icon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		for (TYPE type : TYPE.VALUES) {
			type.registerIcon(iconRegister);
		}
	}

}
