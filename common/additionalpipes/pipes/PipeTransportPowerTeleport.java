/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import additionalpipes.AdditionalPipes;
import additionalpipes.utils.APUtils;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;

import com.google.common.collect.Lists;

public class PipeTransportPowerTeleport extends PipeTransportPowerCommon {

	protected float[] displayPower2 = new float[] { 0, 0, 0, 0, 0, 0 };

	@Override
	protected void sendInternalPower() {
		PipeLogicTeleport logic = ((PipePowerTeleport) container.pipe).getLogic();

		List<Pair<PipeTransportPowerTeleport, Float>> powerQueryList = Lists.newLinkedList();
		float totalPowerQuery = 0;

		for (PipePowerTeleport pipe : logic.<PipePowerTeleport>getConnectedPipes(true)) {
			PipeTransportPowerTeleport target = (PipeTransportPowerTeleport) pipe.transport;
			float totalQuery = APUtils.sum(target.powerQuery);
			if (totalQuery > 0) {
				powerQueryList.add(Pair.of(target, totalQuery));
				totalPowerQuery += totalQuery;
			}
		}

		for (int i = 0; i < 6; i++) {
			displayPower[i] += displayPower2[i];
			displayPower2[i] = 0;

			float totalWatt = internalPower[i];
			if (totalWatt > 0) {
				for (Pair<PipeTransportPowerTeleport, Float> query : powerQueryList) {
					float watts = totalWatt / totalPowerQuery * query.getRight();
					int energy = MathHelper.ceiling_double_int(watts / AdditionalPipes.unitPower);
					if (logic.useEnergy(energy)) {
						PipeTransportPowerTeleport target = query.getLeft();
						watts -= target.receiveTeleportedEnergy(watts);

						displayPower[i] += watts;
						internalPower[i] -= watts;
					}
				}
			}
		}

		super.sendInternalPower();
	}

	public float receiveTeleportedEnergy(float totalWatt) {
		float remaining = totalWatt;
		float totalPowerQuery = 0;

		for (int i = 0; i < 6; ++i) {
			if (powerQuery[i] > 0) {
				if (tiles[i] instanceof TileGenericPipe || tiles[i] instanceof IPowerReceptor) {
					totalPowerQuery += powerQuery[i];
				}
			}
		}

		for (int i = 0; i < 6; i++) {
			if (powerQuery[i] > 0) {
				float watts = 0.0F;

				PowerReceiver prov = getReceiverOnSide(ForgeDirection.VALID_DIRECTIONS[i]);
				if (prov != null && prov.powerRequest() > 0) {
					watts = (totalWatt / totalPowerQuery) * powerQuery[i];
					watts = prov.receiveEnergy(Type.PIPE, watts, ForgeDirection.VALID_DIRECTIONS[i].getOpposite());
					remaining -= watts;
				} else if (tiles[i] instanceof TileGenericPipe) {
					watts = (totalWatt / totalPowerQuery * powerQuery[i]);
					TileGenericPipe nearbyTile = (TileGenericPipe) tiles[i];

					PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

					watts = nearbyTransport.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i].getOpposite(), watts);
					remaining -= watts;
				}
				displayPower2[i] += watts;
				if (watts > 0 && displayPower2[i] < watts) {
					AdditionalPipes.logger.warning("Power losts");
				}
			}
		}

		return remaining;
	}

	@Override
	public void requestEnergy(ForgeDirection from, float amount) {
		step();

		if (CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			return;
		}

		PipeLogicTeleport logic = ((PipePowerTeleport) container.pipe).getLogic();
		if (!logic.canReceive || !logic.canUseEnergy(1)) { // No need to request
			return;
		}

		nextPowerQuery[from.ordinal()] += amount;

		for (PipePowerTeleport pipe : logic.<PipePowerTeleport>getConnectedPipes(false)) {
			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = pipe.container.getTile(o);
				if (Utils.checkPipesConnections(pipe.container, tile) && tile instanceof TileGenericPipe) {
					// AdditionalPipes.logger.info(getPosition().toString() +
					// " RequestEnergy: " + from.toString() + " - Val: " + i +
					// " - Dest: " + (new Position(tile)).toString());

					TileGenericPipe nearbyTile = (TileGenericPipe) tile;
					if (nearbyTile.pipe == null) {
						continue;
					}

					PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
					nearbyTransport.requestEnergy(o.getOpposite(), amount);
				}
			}
		}
	}

}
