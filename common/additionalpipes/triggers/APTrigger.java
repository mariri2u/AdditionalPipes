/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.triggers;

import net.minecraft.util.Icon;
import additionalpipes.client.texture.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCTrigger;

public abstract class APTrigger extends BCTrigger {

	public APTrigger(int legacyId, String tag) {
		super(legacyId, tag);
	}

	@Override
	public Icon getIcon() {
		return ActionTriggerIconProvider.INSTANCE.getIcon(getIconIndex());
	}

}
