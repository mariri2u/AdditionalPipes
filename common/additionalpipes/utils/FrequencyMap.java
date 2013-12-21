/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.common.DimensionManager;
import buildcraft.api.transport.IPipeTile.PipeType;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;

public class FrequencyMap {

	private final File file;
	private final Map<String, BiMap<Integer, String>> userMap = Maps.newHashMap();
	private final Map<String, ImmutableSortedMap<Integer, String>> userMapSorted = Maps.newHashMap();

	private FrequencyMap(File file) {
		this.file = file;
	}

	private BiMap<Integer, String> getBiMap(String username) {
		checkNotNull(username);
		BiMap<Integer, String> freqMap = userMap.get(username);
		if (freqMap != null) {
			return freqMap;
		}
		return ImmutableBiMap.of();
	}

	public String getFreqName(String username, int freq) {
		checkNotNull(username);
		return getBiMap(username).get(freq);
	}

	public int getFreq(String username, String freqName) {
		checkNotNull(username);
		return Objects.firstNonNull(getBiMap(username).inverse().get(freqName), Integer.valueOf(-1));
	}

	public void setFreqName(String username, int freq, String name) {
		checkNotNull(username);
		BiMap<Integer, String> freqMap = userMap.get(username);
		if (freqMap == null) {
			freqMap = HashBiMap.create();
			userMap.put(username, freqMap);
		}
		freqMap.put(freq, name);
		userMapSorted.remove(username);
	}

	public void removeFreqName(String username, int freq) {
		checkNotNull(username);
		BiMap<Integer, String> freqMap = userMap.get(username);
		if (freqMap != null) {
			freqMap.remove(freq);
			userMapSorted.remove(username);
		}
	}

	public ImmutableSortedMap<Integer, String> sortedMap(String username) {
		checkNotNull(username);
		ImmutableSortedMap<Integer, String> sortedMap = userMapSorted.get(username);
		if (sortedMap == null) {
			sortedMap = ImmutableSortedMap.copyOf(getBiMap(username));
			userMapSorted.put(username, sortedMap);
		}
		return sortedMap;
	}

	public ImmutableList<Integer> sortedFreqs(String username) {
		checkNotNull(username);
		return sortedMap(username).keySet().asList();
	}

	public ImmutableList<String> sortedNames(String username) {
		checkNotNull(username);
		return sortedMap(username).values().asList();
	}

	@SuppressWarnings("unchecked")
	public void readFromNBT(NBTTagCompound nbt) {
		for (NBTTagCompound usernameNBT : (Collection<NBTTagCompound>) nbt.getTags()) {
			BiMap<Integer, String> freqMap = HashBiMap.create();
			for (NBTTagInt freqNBT : (Collection<NBTTagInt>) usernameNBT.getTags()) {
				freqMap.put(freqNBT.data, freqNBT.getName());
			}
			userMap.put(usernameNBT.getName(), freqMap);
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		for (Entry<String, BiMap<Integer, String>> usernameEntry : userMap.entrySet()) {
			NBTTagCompound freqNBT = new NBTTagCompound();
			for (Entry<Integer, String> freqEntry : usernameEntry.getValue().entrySet()) {
				freqNBT.setInteger(freqEntry.getValue(), freqEntry.getKey());
			}
			nbt.setCompoundTag(usernameEntry.getKey(), freqNBT);
		}
	}

	private static final String FILE_PREFIX = "ap";
	private static final Map<PipeType, FrequencyMap> freqMaps = Maps.newHashMap();

	public static void clear() {
		freqMaps.clear();
	}

	public static FrequencyMap load(String name) {
		APUtils.checkIllegalClientAccess();

		File file = new File(DimensionManager.getWorld(0).getChunkSaveLocation(), FILE_PREFIX + name + "freqmap.dat");
		FrequencyMap result = new FrequencyMap(file);
		if (file.exists()) {
			try {
				result.readFromNBT(CompressedStreamTools.readCompressed(new FileInputStream(file)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static FrequencyMap load(PipeType type) {
		if (freqMaps.containsKey(type)) {
			return freqMaps.get(type);
		}

		FrequencyMap result = load(type.name().toLowerCase(Locale.ENGLISH) + "teleport");
		freqMaps.put(type, result);
		return result;
	}

	public void save() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		try {
			CompressedStreamTools.writeCompressed(nbt, new FileOutputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
