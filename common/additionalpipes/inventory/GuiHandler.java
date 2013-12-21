/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import additionalpipes.client.gui.GuiAdvancedWoodPipe;
import additionalpipes.client.gui.GuiDistributionPipe;
import additionalpipes.client.gui.GuiTeleportManager;
import additionalpipes.client.gui.GuiTeleportPipe;
import additionalpipes.client.gui.GuiTeleportTether;
import additionalpipes.pipes.ITeleportLogicProvider;
import additionalpipes.pipes.PipeItemsAdvancedWood;
import additionalpipes.pipes.PipeItemsDistributor;
import additionalpipes.tileentity.TileTeleportManager;
import additionalpipes.tileentity.TileTeleportTether;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		Pipe<?> pipe = null;
		if (tile instanceof TileGenericPipe) {
			pipe = ((TileGenericPipe) tile).pipe;

			if (pipe == null) {
				return null;
			}
		}

		switch (ID) {
			case APGuiIds.PIPE_TELEPORT:
				return new ContainerTeleportPipe(((ITeleportLogicProvider) pipe).getLogic());

			case APGuiIds.PIPE_DISTRIBUTOR:
				if (pipe instanceof PipeItemsDistributor)
					return new ContainerDistributionPipe(((PipeItemsDistributor) pipe).logic);
				break;

			case APGuiIds.PIPE_ADVANCED_WOOD:
				if (pipe instanceof PipeItemsAdvancedWood)
					return new ContainerAdvancedWoodPipe(player.inventory, ((PipeItemsAdvancedWood) pipe).logic);
				break;

			case APGuiIds.TELEPORT_MANAGER:
				return new ContainerTeleportManager(player.inventory, (TileTeleportManager) tile);

			case APGuiIds.TELEPORT_TETHER:
				return new ContainerTeleportTether(player, (TileTeleportTether) tile);
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		Pipe<?> pipe = null;
		if (tile instanceof TileGenericPipe) {
			pipe = ((TileGenericPipe) tile).pipe;

			if (pipe == null) {
				return null;
			}
		}

		switch (ID) {
			case APGuiIds.PIPE_TELEPORT:
				return new GuiTeleportPipe(((ITeleportLogicProvider) pipe).getLogic());

			case APGuiIds.PIPE_DISTRIBUTOR:
				if (pipe instanceof PipeItemsDistributor)
					return new GuiDistributionPipe(((PipeItemsDistributor) pipe).logic);
				break;

			case APGuiIds.PIPE_ADVANCED_WOOD:
				if (pipe instanceof PipeItemsAdvancedWood)
					return new GuiAdvancedWoodPipe(player.inventory, ((PipeItemsAdvancedWood) pipe).logic);
				break;

			case APGuiIds.TELEPORT_MANAGER:
				return new GuiTeleportManager(player.inventory, (TileTeleportManager) tile);

			case APGuiIds.TELEPORT_TETHER:
				return new GuiTeleportTether(player, (TileTeleportTether) tile);
		}

		return null;
	}

}
