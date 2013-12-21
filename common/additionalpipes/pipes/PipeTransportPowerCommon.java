/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.core.DefaultProps;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.network.PacketPowerUpdate;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class PipeTransportPowerCommon extends PipeTransportPower {

	public static final short MAX_DISPLAY = 100;
	public static final int DISPLAY_SMOOTHING = 10;
	public static final int OVERLOAD_TICKS = 60;

	public final TileEntity[] tiles = ReflectionHelper.getPrivateValue(PipeTransportPower.class, this, "tiles");
	public final SafeTimeTracker tracker = ReflectionHelper.getPrivateValue(PipeTransportPower.class, this, "tracker");
	public final float[] prevDisplayPower = ReflectionHelper.getPrivateValue(PipeTransportPower.class, this, "prevDisplayPower");
	public float[] internalPower;
	public int[] powerQuery;

	public PipeTransportPowerCommon() {
		super();
		setInternalVar();
	}

	private void setInternalVar() {
		powerQuery = ReflectionHelper.getPrivateValue(PipeTransportPower.class, this, "powerQuery");
		internalPower = ReflectionHelper.getPrivateValue(PipeTransportPower.class, this, "internalPower");
	}

	private static final Method init = ReflectionHelper.findMethod(PipeTransportPower.class, null, new String[] { "init" });
	protected void init() {
		try {
			init.invoke(this);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Method step = ReflectionHelper.findMethod(PipeTransportPower.class, null, new String[] { "step" });
	protected void step() {
		try {
			step.invoke(this);
			setInternalVar();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Method getReceiverOnSide = ReflectionHelper.findMethod(PipeTransportPower.class, null, new String[] { "getReceiverOnSide" }, ForgeDirection.class);
	protected PowerReceiver getReceiverOnSide(ForgeDirection side) {
		try {
			return (PowerReceiver) getReceiverOnSide.invoke(this, side);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateEntity() {
		if (CoreProxy.proxy.isRenderWorld(container.worldObj))
			return;

		step();
		init();

		System.arraycopy(displayPower, 0, prevDisplayPower, 0, 6);
		Arrays.fill(displayPower, 0.0F);
		sendInternalPower();

		computeRequestedEnergy();
		transferRequestedEnergy();

		if (tracker.markTimeIfDelay(container.worldObj, 2 * BuildCraftCore.updateFactor)) {
			PacketPowerUpdate packet = new PacketPowerUpdate(container.xCoord, container.yCoord, container.zCoord);

			double displayFactor = MAX_DISPLAY / 1024.0;
			for (int i = 0; i < clientDisplayPower.length; i++) {
				clientDisplayPower[i] = (short) Math.min((displayPower[i] * displayFactor + .9999), Byte.MAX_VALUE);
			}

			packet.displayPower = clientDisplayPower;
			packet.overload = isOverloaded();
			CoreProxy.proxy.sendToPlayers(packet.getPacket(), container.worldObj, container.xCoord, container.yCoord, container.zCoord, DefaultProps.PIPE_CONTENTS_RENDER_DIST);
		}
	}

	protected void sendInternalPower() {
		for (int i = 0; i < 6; ++i) {
			if (internalPower[i] > 0) {
				float totalPowerQuery = 0F;

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0)
						if (tiles[j] instanceof TileGenericPipe || tiles[j] instanceof IPowerReceptor) {
							totalPowerQuery += powerQuery[j];
						}
				}

				for (int j = 0; j < 6; ++j) {
					if (j != i && powerQuery[j] > 0) {
						float watts = 0.0F;

						PowerReceiver prov = getReceiverOnSide(ForgeDirection.VALID_DIRECTIONS[j]);
						if (prov != null && prov.powerRequest() > 0) {
							watts = (internalPower[i] / totalPowerQuery) * powerQuery[j];
							watts = prov.receiveEnergy(Type.PIPE, watts, ForgeDirection.VALID_DIRECTIONS[j].getOpposite());
							internalPower[i] -= watts;
						} else if (tiles[j] instanceof TileGenericPipe) {
							watts = (internalPower[i] / totalPowerQuery) * powerQuery[j];
							TileGenericPipe nearbyTile = (TileGenericPipe) tiles[j];

							PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;

							watts = nearbyTransport.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[j].getOpposite(), watts);
							internalPower[i] -= watts;
						}

						displayPower[j] += watts;
						displayPower[i] += watts;
					}
				}
			}
		}

		double highestPower = 0;
		for (int i = 0; i < 6; i++) {
			displayPower[i] = (prevDisplayPower[i] * (DISPLAY_SMOOTHING - 1.0F) + displayPower[i]) / DISPLAY_SMOOTHING;
			if (displayPower[i] > highestPower) {
				highestPower = displayPower[i];
			}
		}

		overload += highestPower > maxPower * 0.95 ? 1 : -1;
		if (overload < 0) {
			overload = 0;
		}
		if (overload > OVERLOAD_TICKS) {
			overload = OVERLOAD_TICKS;
		}
	}

	protected void computeRequestedEnergy() {
		for (int i = 0; i < 6; ++i) {
			PowerReceiver prov = getReceiverOnSide(ForgeDirection.VALID_DIRECTIONS[i]);
			if (prov != null) {
				float request = prov.powerRequest();

				if (request > 0) {
					requestEnergy(ForgeDirection.VALID_DIRECTIONS[i], request);
				}
			}
		}
	}

	protected void transferRequestedEnergy() {
		int[] transferQuery = new int[6];

		for (int i = 0; i < 6; ++i) {
			transferQuery[i] = 0;

			for (int j = 0; j < 6; ++j) {
				if (j != i) {
					transferQuery[i] += powerQuery[j];
				}
			}

			transferQuery[i] = Math.min(transferQuery[i], maxPower);
		}

		for (int i = 0; i < 6; ++i) {
			if (transferQuery[i] != 0) {
				if (tiles[i] != null) {
					TileEntity entity = tiles[i];

					if (entity instanceof TileGenericPipe) {
						TileGenericPipe nearbyTile = (TileGenericPipe) entity;

						if (nearbyTile.pipe == null) {
							continue;
						}

						PipeTransportPower nearbyTransport = (PipeTransportPower) nearbyTile.pipe.transport;
						nearbyTransport.requestEnergy(ForgeDirection.VALID_DIRECTIONS[i].getOpposite(), transferQuery[i]);
					}
				}
			}
		}
	}

}
