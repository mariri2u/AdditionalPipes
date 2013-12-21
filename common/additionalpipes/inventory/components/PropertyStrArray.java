/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class PropertyStrArray extends Property {

	public String[] value = ArrayUtils.EMPTY_STRING_ARRAY;

	public PropertyStrArray() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object value) {
		if (value instanceof List) {
			List<String> list = (List<String>) value;
			value = list.toArray(new String[list.size()]);
		}
		this.value = (String[]) value;
	}

	@Override
	public boolean equalsValue(Object obj) {
		if (obj instanceof List) {
			return Arrays.asList(value).equals(obj);
		}
		return obj instanceof String[] && Arrays.equals(value, (String[]) obj);
	}

	@Override
	public Object copy() {
		return value.clone();
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		int size = data.readInt();
		if (size > 0) {
			value = new String[size];
			for (int i = 0; i < value.length; i++) {
				value[i] = data.readUTF();
			}
		} else {
			value = ArrayUtils.EMPTY_STRING_ARRAY;
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(value.length);
		for (String v : value) {
			data.writeUTF(v);
		}
	}

}
