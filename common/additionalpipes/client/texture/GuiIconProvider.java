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

public class GuiIconProvider implements IIconProvider {

	public static final GuiIconProvider INSTANCE = new GuiIconProvider();

	public static final int SHARED 								= 0;
	public static final int RESTRICTED							= 1;
	public static final int PRIVATE								= 2;
	public static final int ON									= 3;
	public static final int OFF									= 4;

	public static final int MAX									= 5;

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int pipeIconIndex) {
		return icons[pipeIconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		icons = new Icon[MAX];

		icons[GuiIconProvider.SHARED] = iconRegister.registerIcon("buildcraft:icons/guiicons_0_2");
		icons[GuiIconProvider.RESTRICTED] = iconRegister.registerIcon("buildcraft:icons/guiicons_0_3");
		icons[GuiIconProvider.PRIVATE] = iconRegister.registerIcon("buildcraft:icons/guiicons_0_4");
		icons[GuiIconProvider.ON] = iconRegister.registerIcon("additionalpipes:icons/on");
		icons[GuiIconProvider.OFF] = iconRegister.registerIcon("additionalpipes:icons/off");
	}

}
