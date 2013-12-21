/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PropertyBoolean extends Property {

	public boolean value = false;

	public PropertyBoolean() {
	}

	@Override
	public void setValue(Object value) {
		this.value = ((Boolean) value).booleanValue();
	}

	@Override
	public boolean equalsValue(Object obj) {
		return obj instanceof Boolean && value == ((Boolean) obj).booleanValue();
	}

	@Override
	public Object copy() {
		return Boolean.valueOf(value);
	}

	@Override
	public int hashCode() {
		return Boolean.valueOf(value).hashCode();
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		value = data.readBoolean();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeBoolean(value);
	}

}
