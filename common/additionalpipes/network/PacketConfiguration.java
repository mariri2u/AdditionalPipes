/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketConfiguration extends AdditionalPipesPacket {

	public boolean disablePermissions;
	public int managerCapacity;

	public PacketConfiguration() {}

	public PacketConfiguration(int id, boolean disablePermissions, int managerCapacity) {
		super(id);
		this.disablePermissions = disablePermissions;
		this.managerCapacity = managerCapacity;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		disablePermissions = data.readBoolean();
		managerCapacity = data.readInt();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeBoolean(disablePermissions);
		data.writeInt(managerCapacity);
	}

}
