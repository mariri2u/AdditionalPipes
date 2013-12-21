/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.world.ChunkCoordIntPair;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class PacketChunkCoordList extends AdditionalPipesPacket {

	public Multiset<ChunkCoordIntPair> coordSet;

	public PacketChunkCoordList() {}

	public PacketChunkCoordList(int id, Multiset<ChunkCoordIntPair> coordSet) {
		super(id);
		this.coordSet = coordSet;
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(coordSet.entrySet().size());
		for (Multiset.Entry<ChunkCoordIntPair> e : coordSet.entrySet()) {
			data.writeInt(e.getElement().chunkXPos);
			data.writeInt(e.getElement().chunkZPos);
			data.writeInt(e.getCount());
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		coordSet = HashMultiset.create();
		int size = data.readInt();
		for (int i = 0; i < size; i++) {
			coordSet.add(new ChunkCoordIntPair(data.readInt(), data.readInt()), data.readInt());
		}
	}

}
