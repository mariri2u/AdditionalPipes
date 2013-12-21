/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import additionalpipes.inventory.ContainerDistributionPipe;
import additionalpipes.pipes.PipeLogicDistributor;
import additionalpipes.utils.APDefaultProps;
import buildcraft.transport.TileGenericPipe;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDistributionPipe extends GuiAdditionalPipes<ContainerDistributionPipe> {

	private static final ResourceLocation TEXTURE = new ResourceLocation("additionalpipes", APDefaultProps.TEXTURE_PATH_GUI + "/distribution.png");
	protected static final RenderItem itemRenderer = new RenderItem();
	private PipeLogicDistributor logic;
	private GuiButton[] buttons = new GuiButton[18];
	private ItemStack indicator;

	public GuiDistributionPipe(PipeLogicDistributor logic) {
		super(new ContainerDistributionPipe(logic), null);
		this.logic = logic;
		xSize = 115;
		ySize = 130;
		// Show item randomly
		TileGenericPipe container = logic.pipe.container;
		List<ItemStack> itemList = Lists.newArrayList();
		do {
			Item item = Item.itemsList[container.worldObj.rand.nextInt(Item.itemsList.length)];
			if (item != null) {
				for (CreativeTabs tab : item.getCreativeTabs()) {
					if (tab == null) {
						break;
					}
					item.getSubItems(item.itemID, tab, itemList);
				}
				if (!itemList.isEmpty()) {
					indicator = itemList.get(container.worldObj.rand.nextInt(itemList.size()));
				}
			}
		} while (indicator == null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int cornerX = guiLeft + 32;
		int cornerY = guiTop + 5;
		for (int i = 0; i < 6; i++) {
			buttonList.add(buttons[i * 3 + 0] = new GuiButton(i * 3 + 0, cornerX + 1,      cornerY + 20 * i, 20, 20, "-"));
			buttonList.add(buttons[i * 3 + 1] = new GuiButton(i * 3 + 1, cornerX + 3 + 20, cornerY + 20 * i, 30, 20, "1000"));
			buttonList.add(buttons[i * 3 + 2] = new GuiButton(i * 3 + 2, cornerX + 5 + 50, cornerY + 20 * i, 20, 20, "+"));
		}
	}

	@Override
	public void updateInformation() {
		for (int i = 0; i < 6; i++) {
			buttons[i * 3 + 1].displayString = Integer.toString(clientProps.propsData[i].value);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		itemRenderer.zLevel = 200F;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, indicator, 9,
				7 + 20 * logic.pipe.container.getBlockMetadata());
		itemRenderer.zLevel = 0.0F;
	}

	private void incrementData(int id, int amount) {
		int oldData = clientProps.propsData[id].value;
		int newData = oldData + amount;
		if (newData < 0) {
			newData = 0;
		}
		if (oldData != newData) {
			clientProps.pushProperty(clientProps.propsData[id].index, newData);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		int distID = guibutton.id / 3;
		switch (guibutton.id % 3) {
			case 0:
				incrementData(distID, -1);
				break;
			case 1:
				return;
			case 2:
				incrementData(distID, 1);
				break;
		}
	}

	@Override
	protected void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);

		int x = i - guiLeft, y = j - guiTop;
		if (x >= 7 && x <= 27 && y >= 5 && y <= 125) {
			clientProps.pushProperty(clientProps.propSwitch.index, (y - 5) / 20);
			mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
		}
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if (wheel != 0) {
			int mouseX = Mouse.getEventX() * width / mc.displayWidth;
			int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
			for (int i = 0; i < 6; i++) {
				if (buttons[i * 3 + 1].mousePressed(mc, mouseX, mouseY)) {
					if (wheel > 0) {
						incrementData(i, 5);
					} else if (wheel < 0) {
						incrementData(i, -5);
					}
					break;
				}
			}
		}
	}

}
