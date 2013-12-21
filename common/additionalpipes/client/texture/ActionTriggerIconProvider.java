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

public class ActionTriggerIconProvider implements IIconProvider {

	public static final ActionTriggerIconProvider INSTANCE = new ActionTriggerIconProvider();

	public static final int Action_DisableInsertion 			= 0;

	public static final int Trigger_TPSignal_Red_Active 		= 1;
	public static final int Trigger_TPSignal_Blue_Active 		= 2;
	public static final int Trigger_TPSignal_Green_Active 		= 3;
	public static final int Trigger_TPSignal_Yellow_Active 		= 4;
	public static final int Trigger_TPSignal_Red_Inactive 		= 5;
	public static final int Trigger_TPSignal_Blue_Inactive 		= 6;
	public static final int Trigger_TPSignal_Green_Inactive 	= 7;
	public static final int Trigger_TPSignal_Yellow_Inactive 	= 8;

	public static final int MAX									= 9;

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

		icons[ActionTriggerIconProvider.Action_DisableInsertion] = iconRegister.registerIcon("additionalpipes:triggers/action_disableinsertion");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Red_Active] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_red_active");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Red_Inactive] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_red_inactive");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Blue_Active] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_blue_active");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Blue_Inactive] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_blue_inactive");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Green_Active] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_green_active");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Green_Inactive] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_green_inactive");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Yellow_Active] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_yellow_active");
		icons[ActionTriggerIconProvider.Trigger_TPSignal_Yellow_Inactive] = iconRegister.registerIcon("additionalpipes:triggers/trigger_tpsignal_yellow_inactive");
	}

}
