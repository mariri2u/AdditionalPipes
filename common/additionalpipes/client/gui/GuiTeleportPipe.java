/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.client.gui;

import java.util.Arrays;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import additionalpipes.AdditionalPipes;
import additionalpipes.client.gui.components.GuiBetterTextField;
import additionalpipes.client.gui.components.GuiButtonShorter;
import additionalpipes.client.gui.components.GuiRestrictedTile;
import additionalpipes.client.gui.components.GuiStringSlot;
import additionalpipes.client.gui.components.IGuiIndirectSlots;
import additionalpipes.inventory.ContainerTeleportPipe;
import additionalpipes.pipes.PipeLogicTeleport;
import additionalpipes.utils.APDefaultProps;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.core.utils.StringUtils;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTeleportPipe extends GuiRestrictedTile<ContainerTeleportPipe> implements IGuiIndirectSlots {

	private static final ResourceLocation TEXTURE = new ResourceLocation("additionalpipes", APDefaultProps.TEXTURE_PATH_GUI + "/teleport.png");

	private GuiBetterTextField textboxFreq;
	private GuiBetterTextField textboxName;

	private GuiButton buttonSetFreq;
	private GuiButton buttonCanReceive;
	private GuiButton buttonSetName;
	private GuiButton buttonRemoveName;
	private GuiButton buttonIsPublic;

	private GuiStringSlot slotFreqNames;

	private PipeLogicTeleport logic;
	private int currentFreq = 0;
	private String currentName = "";
	private String[] currentNames = ArrayUtils.EMPTY_STRING_ARRAY;
	private int[] showingFreqs = ArrayUtils.EMPTY_INT_ARRAY;

	public GuiTeleportPipe(PipeLogicTeleport logic) {
		super(new ContainerTeleportPipe(logic), null, TEXTURE);
		this.logic = logic;
		xSize = 244;
		ySize = 116;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);

		buttonList.add(buttonSetFreq = new GuiButtonShorter(0, guiLeft + 50, guiTop + 7, 22, 16, StringUtils.localize("gui.set")));

		textboxFreq = new GuiBetterTextField(fontRenderer, guiLeft + 7, guiTop + 7, 40, 16);
		textboxFreq.setText("0");
		textboxFreq.setMaxStringLength(5);
		textboxFreq.setReturnButton(this, buttonSetFreq);
		textboxFreq.setAllowedCharacters("0123456789");

		buttonList.add(new GuiButton(1, guiLeft + 6, guiTop + 28, 30, 20, "-"));
		buttonList.add(new GuiButton(2, guiLeft + 42, guiTop + 28, 30, 20, "+"));
		buttonList.add(new GuiButton(3, guiLeft + 6, guiTop + 52, 30, 20, "-10"));
		buttonList.add(new GuiButton(4, guiLeft + 42, guiTop + 52, 30, 20, "+10"));
		buttonList.add(new GuiButton(5, guiLeft + 6, guiTop + 76, 30, 20, "-100"));
		buttonList.add(new GuiButton(6, guiLeft + 42, guiTop + 76, 30, 20, "+100"));

		buttonList.add(buttonCanReceive = new GuiButtonShorter(10, guiLeft + 204, guiTop + 7, 34, 16, ""));
		buttonCanReceive.drawButton = logic.getType() != PipeType.STRUCTURE;

		buttonList.add(buttonSetName = new GuiButtonShorter(7, guiLeft + 196, guiTop + 29, 42, 16, StringUtils.localize("gui.add")));
		buttonList.add(buttonRemoveName = new GuiButtonShorter(8, guiLeft + 196, guiTop + 29, 42, 16, StringUtils.localize("gui.remove")));
		buttonRemoveName.drawButton = false;

		textboxName = new GuiBetterTextField(fontRenderer, guiLeft + 80, guiTop + 29, 114, 16);
		textboxName.setMaxStringLength(20);
		textboxName.setReturnButton(this, buttonSetName);

		slotFreqNames = new GuiStringSlot(mc, this, guiLeft + 80, guiTop + 49, 157, 46);
		slotFreqNames.registerReturnButton(new GuiButton(9, 0, 0, 0, 0, ""));

		buttonList.add(buttonIsPublic = new GuiButtonShorter(11, guiLeft + 6, guiTop + 99, 66, 12, ""));

		currentFreq = 0;
		currentName = "";
		currentNames = ArrayUtils.EMPTY_STRING_ARRAY;
	}

	@Override
	public void updateInformation() {
		int freq = clientProps.propFreq.value;
		if (currentFreq != freq) {
			if (Integer.toString(currentFreq).equals(textboxFreq.getText())) {
				// unedited
				textboxFreq.setText(Integer.toString(freq));
			} else {
				textboxFreq.setDefaultText(Integer.toString(freq));
			}
			currentFreq = freq;
		}

		String freqName = clientProps.propFreqName.value;
		if (!Objects.equal(currentName, freqName)) {
			if (Objects.equal(currentName, textboxName.getText())) {
				// unedited
				textboxName.setText(freqName);
			} else {
				textboxName.setDefaultText(freqName);
			}
			currentName = freqName;
			updateSlotStrings();
		}

		String[] freqNames = clientProps.propFreqMapNames.value;
		if (currentNames != freqNames) {
			currentNames = freqNames;
			updateSlotStrings();
		}

		String freqText = textboxFreq.getText();
		buttonSetFreq.enabled = !Strings.isNullOrEmpty(freqText) && currentFreq != Integer.parseInt(freqText);

		String typedName = textboxName.getText();
		boolean isEmpty = Strings.isNullOrEmpty(typedName);
		boolean equalsName = !isEmpty && typedName.equals(currentName);
		buttonSetName.enabled = !isEmpty && !equalsName && ArrayUtils.indexOf(currentNames, typedName) == -1;
		buttonSetName.drawButton = !equalsName;
		buttonRemoveName.enabled = equalsName;
		buttonRemoveName.drawButton = equalsName;

		buttonCanReceive.displayString = clientProps.propCanReceive.value ? StringUtils.localize("gui.receive.true") : StringUtils.localize("gui.receive.false");
		buttonIsPublic.displayString = clientProps.propIsPublic.value ? StringUtils.localize("gui.public") : StringUtils.localize("gui.private");
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		fontRenderer.drawString(StringUtils.localize("gui.connected") + ": " + clientProps.propConnected.value, 80, 11,
				clientProps.propIsPublic.value ? 0x0000FF : 0x404040);
		String title;
		if (logic.getType() != PipeType.STRUCTURE) {
			title = StringUtils.localize("gui.receive") + ":";
			fontRenderer.drawString(title, 200 - fontRenderer.getStringWidth(title), 11, 0x404040);
		}

		switch (clientProps.propPipeState.value) {
			case 1:
				title = StringUtils.localize("gui.notLinked");
				break;
			case 2:
				title = StringUtils.localize("gui.noEnergy");
				break;
			default:
				title = null;
				break;
		}
		if (title != null) {
			fontRenderer.drawString(title, 236 - fontRenderer.getStringWidth(title), 102, 0xFF0000);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		overlayBackground(guiLeft, guiTop, xSize, ySize);

		slotFreqNames.drawScreen(par2, par3, par1);
		textboxFreq.drawTextBox();
		textboxName.drawTextBox();
	}

	@Override
	public void overlayBackground(int xPos, int yPos, int width, int height) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);

		drawTexturedModalRect(xPos, yPos, xPos - guiLeft, yPos - guiTop, width, height);
	}

	@Override
	public void buttonPressed(GuiButton button) {
		actionPerformed(button);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
			case 0:
				pressSetFreq();
				break;
			case 1: case 2: case 3:
			case 4: case 5: case 6:
				pressIncrementFreq(button.id);
				break;
			case 7:
				pressSetName();
				break;
			case 8:
				pressRemoveName();
				break;
			case 9:
				selectSlotName();
				break;
			case 10:
				pressCanReceive();
				break;
			case 11:
				pressIsPublic();
				break;
		}
	}

	private void setFrequency(int frequency) {
		if (frequency < 0) {
			frequency = 0;
		} else if (frequency > 99999) {
			frequency = 99999;
		}
		if (clientProps.propFreq.value != frequency) {
			clientProps.pushProperty(clientProps.propFreq.index, frequency);
			slotFreqNames.lastSelected = -1;
		}
	}

	private void updateSlotStrings() {
		showingFreqs = Ints.toArray(Collections2.filter(Ints.asList(clientProps.propFreqMapFreqs.value), Predicates.not(Predicates.equalTo(currentFreq))));
		slotFreqNames.setStrings(ImmutableList.copyOf(Iterables.filter(Arrays.asList(currentNames), Predicates.not(Predicates.equalTo(currentName)))));
	}

	private void pressSetFreq() {
		String freqText = textboxFreq.getText();
		if (Strings.isNullOrEmpty(freqText)) {
			return;
		}

		setFrequency(Integer.parseInt(freqText));
	}

	private void pressIncrementFreq(int id) {
		int newFreq = currentFreq;
		switch (id) {
			case 1:
				newFreq--;
				break;
			case 2:
				newFreq++;
				break;
			case 3:
				newFreq -= 10;
				break;
			case 4:
				newFreq += 10;
				break;
			case 5:
				newFreq -= 100;
				break;
			case 6:
				newFreq += 100;
				break;
		}
		setFrequency(newFreq);
	}

	private void setFreqName(String name) {
		clientProps.pushProperty(clientProps.propFreqName.index, name);
		clientProps.propFreqName.setValue(currentName);// Not to update freqname immediately
	}

	private void pressSetName() {
		setFreqName(textboxName.getText());
	}

	private void pressRemoveName() {
		setFreqName("");
	}

	private void selectSlotName() {
		if (slotFreqNames.lastSelected != -1 && showingFreqs.length > slotFreqNames.lastSelected) {
			setFrequency(showingFreqs[slotFreqNames.lastSelected]);
		}
	}

	private void pressCanReceive() {
		clientProps.pushProperty(clientProps.propCanReceive.index, !clientProps.propCanReceive.value);
	}

	private void pressIsPublic() {
		clientProps.pushProperty(clientProps.propIsPublic.index, !clientProps.propIsPublic.value);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textboxFreq.mouseClicked(mouseX, mouseY, mouseButton);
		textboxName.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char c, int key) {
		if (!textboxFreq.textboxKeyTyped(c, key) && !textboxName.textboxKeyTyped(c, key)) {
			super.keyTyped(c, key);
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public String getPropOwner() {
		String owner = super.getPropOwner();
		if (mc.isSingleplayer() && !mc.getIntegratedServer().getPublic() && !Strings.isNullOrEmpty(AdditionalPipes.fakedUserName)) {
			owner = AdditionalPipes.fakedUserName;
		}
		return owner;
	}

}
