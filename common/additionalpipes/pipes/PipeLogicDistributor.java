/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.AdditionalPipes;
import additionalpipes.inventory.APGuiIds;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.TileBuffer;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;

public abstract class PipeLogicDistributor {

	public final Pipe<?> pipe;

	public int distData[] = { 1, 1, 1, 1, 1, 1 };
	public int curTick = 0;

	public PipeLogicDistributor(Pipe<?> pipe) {
		this.pipe = pipe;
	}

	private boolean isValidFacing(ForgeDirection side) {
		TileBuffer[] tileBuffer = pipe.container.getTileCache();
		if (tileBuffer == null)
			return true;

		if (!tileBuffer[side.ordinal()].exists())
			return true;

		TileEntity tile = tileBuffer[side.ordinal()].getTile();
		return isValidConnectingTile(tile);
	}

	protected abstract boolean isValidConnectingTile(TileEntity tile);

	public void initialize() {
		//enforceValidSide();
	}

	public void onBlockPlaced() {
		pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, 1, 3);
		enforceValidSide();
	}

	public void onNeighborBlockChange(int blockId) {
		enforceValidSide();
	}

	public ForgeDirection getNextSide(ForgeDirection side) {
		return getNextSide(side, ForgeDirection.UNKNOWN);
	}

	public ForgeDirection getNextSide(ForgeDirection side, ForgeDirection initSide) {
		int nextSide = side.ordinal();

		for (int l = 0; l < 6; ++l) {
			nextSide++;

			if (nextSide > 5) {
				nextSide = 0;
			}

			if (nextSide == initSide.ordinal()) {
				return ForgeDirection.UNKNOWN;
			}
			if (distData[nextSide] > 0) {
				return ForgeDirection.VALID_DIRECTIONS[nextSide];
			}
		}
		return ForgeDirection.UNKNOWN;
	}

	public void switchTo(ForgeDirection side, boolean force) {
		if (force || pipe.container.getBlockMetadata() != side.ordinal()) {
			pipe.container.worldObj.setBlockMetadataWithNotify(pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord, side.ordinal(), 3);
			curTick = 0;
			pipe.container.markBlockForUpdate();
		} else if (curTick >= distData[side.ordinal()]) {
			curTick = 0;
		}
	}

	public void switchPosition() {
		ForgeDirection initSide = ForgeDirection.VALID_DIRECTIONS[pipe.container.getBlockMetadata()];
		ForgeDirection side = getNextSide(initSide);
		while (side != ForgeDirection.UNKNOWN && !isValidFacing(side)) {
			side = getNextSide(side, initSide);
		}
		if (side == ForgeDirection.UNKNOWN) {
			side = initSide;
		}
		switchTo(side, true);
	}

	public void switchIfNeeded() {
		enforceValidSide();
		ForgeDirection initSide = ForgeDirection.VALID_DIRECTIONS[pipe.container.getBlockMetadata()];

		for (ForgeDirection side = initSide; side != ForgeDirection.UNKNOWN; side = getNextSide(side, initSide)) {
			if (isValidFacing(side)) {
				switchTo(side, false);
				return;
			}
		}
	}

	public void enforceValidSide() {
		ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[pipe.container.getBlockMetadata()];
		if (distData[side.ordinal()] == 0) {
			switchTo(getNextSide(side), false);
		}
	}

	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench
				&& ((IToolWrench) equipped).canWrench(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord)) {
			switchPosition();
			((IToolWrench) equipped).wrenchUsed(entityplayer, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
			return true;
		}

		if (CoreProxy.proxy.isSimulating(pipe.container.worldObj)) {
			entityplayer.openGui(AdditionalPipes.instance, APGuiIds.PIPE_DISTRIBUTOR, pipe.container.worldObj, pipe.container.xCoord, pipe.container.yCoord, pipe.container.zCoord);
		}

		return true;
	}

	public boolean outputOpen(ForgeDirection to) {
		return distData[to.ordinal()] > 0;
	}

	public void readFromNBT(NBTTagCompound data) {
		curTick = data.getInteger("curTick");

		boolean found = false;
		for (int i = 0; i < distData.length; i++) {
			int tick = data.getInteger("distData" + i);
			if (tick > 0) {
				found = true;
			}
			distData[i] = tick;
		}

		if (!found) {
			Arrays.fill(distData, 1);
		}
	}

	public void writeToNBT(NBTTagCompound data) {
		data.setInteger("curTick", curTick);
		for (int i = 0; i < distData.length; i++) {
			data.setInteger("distData" + i, distData[i]);
		}
	}

}
