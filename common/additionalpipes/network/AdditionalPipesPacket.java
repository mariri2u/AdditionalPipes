/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import additionalpipes.utils.APDefaultProps;
import buildcraft.core.network.BuildCraftPacket;

public abstract class AdditionalPipesPacket extends BuildCraftPacket {

	private int id;

	public AdditionalPipesPacket() {
		channel = APDefaultProps.NET_CHANNEL_NAME;
	}

	public AdditionalPipesPacket(int id) {
		this();
		this.id = id;
	}

	@Override
	public int getID() {
		return id;
	}

}
