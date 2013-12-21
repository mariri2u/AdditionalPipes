/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import additionalpipes.AdditionalPipes;
import additionalpipes.block.components.BlockRestricted;
import additionalpipes.inventory.APGuiIds;
import additionalpipes.tileentity.TileTeleportTether;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.proxy.CoreProxy;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTeleportTether extends BlockRestricted {

	public static final int LOADING_THRESHOLD = 1000;

	public static final int[] DEFAULT_AREA = new int[] { 1, 1, 1, 1 };
	public static int maxChunkDepth = 25;
	public static int maxTicketLength = 200;

	public BlockTeleportTether(int blockID) {
		super(blockID, Material.cloth);
		setHardness(0.2F);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	public static int[] getDirectionsForStack(ItemStack stack) {
		if (!stack.hasTagCompound() || !stack.stackTagCompound.hasKey("directions")) {
			return DEFAULT_AREA;
		}
		return stack.stackTagCompound.getIntArray("directions");
	}

	public static ItemStack createStackSimple() {
		return new ItemStack(AdditionalPipes.blockTeleportTether);
	}

	public static ItemStack createStackCustom(int[] dirs) {
		ItemStack result = createStackSimple();
		result.setTagCompound(new NBTTagCompound("tag"));
		result.stackTagCompound.setIntArray("directions", dirs);
		return result;
	}

	public static ItemStack createStackExtended(int dir, int length) {
		int[] dirs = new int[] { 0, 0, 0, 0 };
		dirs[dir] = length;
		return createStackCustom(dirs);
	}

	public static ItemStack createStackSingle(int size) {
		ItemStack stack = createStackCustom(new int[] { 0, 0, 0, 0 });
		stack.stackSize = size;
		return stack;
	}

	public static ItemStack getItemStack(TileTeleportTether tether) {
		if (Arrays.equals(tether.directions, DEFAULT_AREA)) {
			return createStackSimple();
		}
		return createStackCustom(tether.directions.clone());
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLiving, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entityLiving, stack);

		TileTeleportTether tether = (TileTeleportTether) world.getBlockTileEntity(x, y, z);
		if (stack.hasTagCompound() && stack.stackTagCompound.hasKey("directions")) {
			tether.directions = stack.stackTagCompound.getIntArray("directions");
		}
		if (entityLiving instanceof EntityPlayer) {
			tether.placedBy = (EntityPlayer) entityLiving;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileTeleportTether();
	}

	@Override
	protected void onBlockAccessed(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset) {
		player.openGui(AdditionalPipes.instance, APGuiIds.TELEPORT_TETHER, world, x, y, z);
	}

	@Override
	public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		if (CoreProxy.proxy.isSimulating(world)) {
			if (!player.capabilities.isCreativeMode) {
				TileTeleportTether tether = (TileTeleportTether) world.getBlockTileEntity(x, y, z);
				if (!tether.tryEdit(player)) {
					return false;
				}
				dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			}
			world.setBlockToAir(x, y, z);
		}
		return true;
	}

	@Override
	public int idDropped(int par1, Random par2Random, int par3) {
		return 0;
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random) {
		return 0;
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> result = Lists.newArrayList();
		if (CoreProxy.proxy.isSimulating(world)) {
			TileTeleportTether tether = (TileTeleportTether) world.getBlockTileEntity(x, y, z);
			if (tether != null) {
				result.add(getItemStack(tether));
			}
		}
		return result;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		TileTeleportTether tether = (TileTeleportTether) world.getBlockTileEntity(x, y, z);
		return getItemStack(tether);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon("additionalpipes:tether");
	}

}