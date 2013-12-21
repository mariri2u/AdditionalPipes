/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import additionalpipes.client.texture.PipeIconProvider;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IAction;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PipeFluidsAdvancedInsertion extends Pipe<PipeTransportFluids> {

	private PipeLogicAdvancedInsertion logic = new PipeLogicAdvancedInsertion();
	private EnumSet<ForgeDirection> outputOpenCache = EnumSet.noneOf(ForgeDirection.class);

	public PipeFluidsAdvancedInsertion(int itemID) {
		super(new PipeTransportFluids(), itemID);

		transport.flowRate = 25;
		transport.travelDelay = 8;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return PipeIconProvider.INSTANCE;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return PipeIconProvider.TYPE.PipeFluidsAdvancedInsertion.ordinal();
	}

	private void updateCache() {
		FluidStack pushStack = transport.internalTanks[ForgeDirection.UNKNOWN.ordinal()].getFluid();
		if (pushStack == null || pushStack.amount < 1) {
			return;
		}
		FluidStack testStack = pushStack.copy();
		testStack.amount = 1;

		outputOpenCache.clear();
		EnumSet<ForgeDirection> connectedPipes = !logic.isDisabled() ? EnumSet.noneOf(ForgeDirection.class) : outputOpenCache;

		for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = container.getTile(o);
			if (tile instanceof TileGenericPipe) {
				if (Utils.checkPipesConnections(tile, container)) {
					connectedPipes.add(o);
				}
			} else if (!logic.isDisabled() && tile instanceof IFluidHandler) {
				IFluidHandler tank = (IFluidHandler) tile;
				if (tank.fill(o, testStack, false) > 0) {
					outputOpenCache.add(o);
				}
			}
		}

		if (outputOpenCache.isEmpty()) {
			outputOpenCache = connectedPipes;
		}
	}

	@Override
	public void updateEntity() {
		updateCache();
		super.updateEntity();
	}

	@Override
	public boolean outputOpen(ForgeDirection to) {
		return outputOpenCache.contains(to);
	}

	@Override
	public LinkedList<IAction> getActions() {
		return logic.getActions(super.getActions());
	}

	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);
		logic.actionsActivated(actions);
	}

}
