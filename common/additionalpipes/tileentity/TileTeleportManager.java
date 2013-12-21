/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.tileentity;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.storage.MapData;
import additionalpipes.AdditionalPipes;
import additionalpipes.api.AccessRule;
import additionalpipes.api.IRestrictedTile;
import additionalpipes.api.ITeleportEnergyProvider;
import additionalpipes.api.ITeleportPipe;
import additionalpipes.api.TeleportManager;
import additionalpipes.utils.APUtils;
import additionalpipes.utils.RestrUtils;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import cpw.mods.fml.relauncher.ReflectionHelper;

//disableEnergyUsage=true
public class TileTeleportManager extends TileBuildCraft implements IRestrictedTile, ITeleportEnergyProvider {

	public String owner = "";
	public AccessRule accessRule = AccessRule.SHARED;
	public boolean isPublic = false;
	public final List<Integer> maps = Lists.newLinkedList();

	public boolean matchesOwner(ITeleportPipe pipe) {
		return isPublic ? pipe.isPublic() : !pipe.isPublic() && owner.equalsIgnoreCase(pipe.getOwner());
	}

	private static final Field mapDataDimension = ReflectionHelper.findField(MapData.class, "c", "field_76200_c", "dimension");
	private int getMapDataDimension(MapData mapData) {
		try {
			return mapDataDimension.getInt(mapData);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<Integer> getMapsLinkTo(final ITeleportPipe pipe) {
		if (!matchesOwner(pipe)) {
			return Collections.emptyList();
		}

		return Collections2.filter(maps, new Predicate<Integer>() {
			@Override
			public boolean apply(Integer input) {
				return hasLinkedWithMap(input, pipe);
			}
		});
	}

	private boolean hasLinkedWithMap(int map, ITeleportPipe pipe) {
		final int mapWidth = 128;
		final int mapHeight = 128;

		MapData mapData = Item.map.getMapData(APUtils.createMapStack(map, worldObj), worldObj);
		int size = 1 << mapData.scale;
		ChunkCoordinates pos = pipe.getPosition();
		int mapX = (pos.posX - mapData.xCenter) / size + mapWidth/2;
		int mapZ = (pos.posZ - mapData.zCenter) / size + mapHeight/2;
		int dimension = getMapDataDimension(mapData);
		if (pipe.getWorld().provider.dimensionId == dimension && mapX >= 0 && mapX < mapWidth && mapZ >= 0 && mapZ < mapHeight &&
				mapData.colors[mapX + mapZ*mapWidth] != 0) {
			return true;
		}
		return false;
	}

	public boolean hasLinked(ITeleportPipe pipe) {
		if (AdditionalPipes.disableLinkingUsage) {
			return true;
		}

		return !getMapsLinkTo(pipe).isEmpty();
	}

	@Override
	public void initialize() {
		super.initialize();
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			TeleportManager.addEnergyProvider(this);
		}
	}

	@Override
	public void invalidate() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			TeleportManager.removeEnergyProvider(this);
		}
		super.invalidate();
	}

	@Override
	public void onChunkUnload() {
		if (CoreProxy.proxy.isSimulating(worldObj)) {
			TeleportManager.removeEnergyProvider(this);
		}
		super.onChunkUnload();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		owner = nbttagcompound.getString("owner");
		accessRule = AccessRule.values()[nbttagcompound.getInteger("accessRule")];
		isPublic = nbttagcompound.getBoolean("isPublic");
		maps.addAll(Ints.asList(nbttagcompound.getIntArray("maps")));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setString("owner", owner);
		nbttagcompound.setInteger("accessRule", accessRule.ordinal());
		nbttagcompound.setBoolean("isPublic", isPublic);
		nbttagcompound.setIntArray("maps", Ints.toArray(maps));
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public void initOwner(String username) {
		if (Strings.isNullOrEmpty(owner)) {
			owner = username;
		}
	}

	@Override
	public AccessRule getAccessRule() {
		return accessRule;
	}

	@Override
	public void setAccessRule(AccessRule rule) {
		accessRule = rule;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return AdditionalPipes.disablePermissions || owner.equalsIgnoreCase(player.username);
	}

	@Override
	public boolean canAccess(EntityPlayer player) {
		return hasPermission(player) || accessRule != AccessRule.PRIVATE;
	}

	@Override
	public boolean canEdit(EntityPlayer player) {
		return hasPermission(player) || accessRule == AccessRule.SHARED;
	}

	@Override
	public boolean tryAccess(EntityPlayer player) {
		return RestrUtils.tryAccess(this, player);
	}

	@Override
	public boolean tryEdit(EntityPlayer player) {
		return RestrUtils.tryEdit(this, player);
	}

	@Override
	public boolean canTeleport(ITeleportPipe pipe) {
		return hasLinked(pipe);
	}

	@Override
	public boolean canForcedTeleport(ITeleportPipe pipe) {
		return false;
	}

	@Override
	public boolean useEnergy(int amount) {
		return true;
	}

	@Override
	public boolean canUseEnergy(int amount) {
		return true;
	}

}
