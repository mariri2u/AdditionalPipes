/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.client.gui;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

import additionalpipes.client.gui.components.GuiRestrictedTile;
import additionalpipes.inventory.ContainerTeleportTether;
import additionalpipes.tileentity.TileTeleportTether;
import additionalpipes.utils.APDefaultProps;
import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTeleportTether extends GuiRestrictedTile<ContainerTeleportTether> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("additionalpipes", APDefaultProps.TEXTURE_PATH_GUI + "/teleporttether.png");
	protected static final RenderItem itemRenderer = new RenderItem();

	public GuiTeleportTether(EntityPlayer player, TileTeleportTether tether) {
		super(new ContainerTeleportTether(player, tether), null);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		String title = StringUtils.localize("container.teleportTether");
		fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, ySize - 96 + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);

		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void drawItemStackTooltip(ItemStack stack, int x, int y) {
		int side = ArrayUtils.indexOf(clientProps.tetherInventory.getStacks(), stack);
		if (side != -1) {
			List<String> list = stack.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

			String type;
			switch (side) {
				case 0: type = "north"; break;
				case 1: type = "east"; break;
				case 2: type = "south"; break;
				case 3: type = "west"; break;
				default: type = "self"; break;
			}
			list.add(1, EnumChatFormatting.AQUA + StringUtils.localize("tile.teleportTether." + type));

			for (int i = 0; i < list.size(); ++i) {
				if (i == 0) {
					list.set(i, "\u00a7" + Integer.toHexString(stack.getRarity().rarityColor) + list.get(i));
				} else {
					list.set(i, EnumChatFormatting.GRAY + list.get(i));
				}
			}

			FontRenderer font = stack.getItem().getFontRenderer(stack);
			drawHoveringText(list, x, y, font == null ? fontRenderer : font);
		} else {
			super.drawItemStackTooltip(stack, x, y);
		}
	}

}
