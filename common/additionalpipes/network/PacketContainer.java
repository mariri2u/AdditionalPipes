/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketContainer extends AdditionalPipesPacket {

	public int windowId;
	public int index;

	public PacketContainer() { }

	public PacketContainer(int id, int windowId, int index) {
		super(id);
		this.windowId = windowId;
		this.index = index;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		windowId = data.readByte();
		index = data.readShort();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeByte(windowId);
		data.writeShort(index);
	}

}
