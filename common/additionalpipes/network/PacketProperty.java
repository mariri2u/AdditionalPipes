/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import additionalpipes.inventory.components.Property;

public class PacketProperty extends PacketContainer {

	public Property prop;

	public PacketProperty() { }

	public PacketProperty(int id, int windowId, int index, Property prop) {
		super(id, windowId, index);
		this.prop = prop;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		prop = Property.readPacket(data);
		prop.index = index;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		Property.writePacket(prop, data);
	}

}
