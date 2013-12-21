/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PropertyString extends Property {

	public String value = "";

	public PropertyString() {
	}

	@Override
	public void setValue(Object value) {
		this.value = (String) value;
	}

	@Override
	public boolean equalsValue(Object obj) {
		return value.equals(obj);
	}

	@Override
	public Object copy() {
		return value;// String is immutable
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		value = data.readUTF();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeUTF(value);
	}

}
