/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportPower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipePowerTeleport extends Pipe<PipeTransportPower> implements ITeleportLogicProvider {

	private PipeLogicTeleport logic = new PipeLogicTeleport(this);

	public PipePowerTeleport(int itemID) {
		super(new PipeTransportPowerTeleport(), itemID);
		transport.initFromPipe(getClass());
	}

	@Override
	public void initialize() {
		super.initialize();
		logic.initialize();
	}

	@Override
	public void invalidate() {
		super.invalidate();
		logic.invalidate();
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		logic.onChunkUnload();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
	}

	/*@Override
	public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
		return logic.canPipeConnect(tile, side) && super.canPipeConnect(tile, side);
	}*/

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipePowerTeleport.ordinal();
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		logic.readFromNBT(data);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		logic.writeToNBT(data);
	}

	@Override
	public PipeLogicTeleport getLogic() {
		return logic;
	}

}
