/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;

public class PropertyItemStack extends Property {

	public ItemStack value;// nullable

	public PropertyItemStack() {
	}

	@Override
	public void setValue(Object value) {
		this.value = (ItemStack) value;
	}

	@Override
	public boolean equalsValue(Object obj) {
		return (obj == null || obj instanceof ItemStack) && ItemStack.areItemStacksEqual(value, (ItemStack) obj);
	}

	@Override
	public Object copy() {
		return value == null ? null : value.copy();
	}

	@Override
	public int hashCode() {
		return value == null ? 0 : value.hashCode();
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		value = Packet.readItemStack(data);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		Packet.writeItemStack(value, data);
	}

}
