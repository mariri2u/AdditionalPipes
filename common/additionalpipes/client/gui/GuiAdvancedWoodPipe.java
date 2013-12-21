/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import additionalpipes.inventory.ContainerAdvancedWoodPipe;
import additionalpipes.pipes.PipeLogicAdvancedWood;
import additionalpipes.utils.APDefaultProps;
import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAdvancedWoodPipe extends GuiAdditionalPipes<ContainerAdvancedWoodPipe> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("additionalpipes", APDefaultProps.TEXTURE_PATH_GUI + "/advancedwood.png");

	private IInventory filterIInventory;
	private GuiButton buttonToggle;

	public GuiAdvancedWoodPipe(IInventory playerInventory, PipeLogicAdvancedWood filterInventory) {
		super(new ContainerAdvancedWoodPipe(playerInventory, filterInventory), filterInventory.getFilters(), TEXTURE);
		filterIInventory = filterInventory.getFilters();
		xSize = 176;
		ySize = 158;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(buttonToggle = new GuiButton(1, guiLeft + 8, guiTop + 40, 140, 20, StringUtils.localize("gui.advancedWood.required")));
	}

	@Override
	public void updateInformation() {
		buttonToggle.displayString = clientProps.propExclude.value ? StringUtils.localize("gui.advancedWood.excluded")
				: StringUtils.localize("gui.advancedWood.required");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		fontRenderer.drawString(StringUtils.localize(filterIInventory.getInvName()), 8, 6, 0x404040);
		fontRenderer.drawString(StringUtils.localize("gui.inventory"), 8, 66, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 1) {
			clientProps.pushProperty(clientProps.propExclude.index, !clientProps.propExclude.value);
		}
	}

}
