/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketRequestChunks extends AdditionalPipesPacket {

	public boolean showAllPersistentChunks;

	public PacketRequestChunks() {}

	public PacketRequestChunks(int id, boolean showAllPersistentChunks) {
		super(id);
		this.showAllPersistentChunks = showAllPersistentChunks;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeBoolean(showAllPersistentChunks);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		showAllPersistentChunks = data.readBoolean();
	}

}
