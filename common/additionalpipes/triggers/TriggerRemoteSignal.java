/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.triggers;

import java.util.List;
import java.util.Locale;

import additionalpipes.client.texture.ActionTriggerIconProvider;
import additionalpipes.pipes.ITeleportLogicProvider;
import additionalpipes.pipes.PipeLogicTeleport;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.transport.IPipe;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerRemoteSignal extends APTrigger implements ITriggerPipe {

	public final boolean active;
	public final IPipe.WireColor color;

	public TriggerRemoteSignal(int legacyId, boolean active, IPipe.WireColor color) {
		super(legacyId, "additionalpipes.pipe.remote_wire." + color.name().toLowerCase(Locale.ENGLISH) + (active ? ".active" : ".inactive"));

		this.active = active;
		this.color = color;
	}

	@Override
	public String getDescription() {
		if (active) {
			switch (color) {
				case Red:
					return "Remote Red Pipe Signal On";
				case Blue:
					return "Remote Blue Pipe Signal On";
				case Green:
					return "Remote Green Pipe Signal On";
				case Yellow:
					return "Remote Yellow Pipe Signal On";
			}
		} else {
			switch (color) {
				case Red:
					return "Remote Red Pipe Signal Off";
				case Blue:
					return "Remote Blue Pipe Signal Off";
				case Green:
					return "Remote Green Pipe Signal Off";
				case Yellow:
					return "Remote Yellow Pipe Signal Off";
			}
		}
		return "";
	}

	private boolean canReceiveSignalFrom(Pipe<?> pipe) {
		if (pipe.gate != null) {
			for (ITrigger trigger : pipe.gate.triggers) {
				if (trigger instanceof TriggerRemoteSignal && ((TriggerRemoteSignal) trigger).color == color) {
					return false;
				}
			}
		}
		return true;
	}

	private Boolean isAnyPipeSignaling(PipeLogicTeleport logic) {
		List<PipeLogicTeleport> logics = logic.getConnectedPipeLogics(null);
		if (logics.isEmpty()) {
			return null;
		}
		boolean result = false;
		for (PipeLogicTeleport other : logics) {
			if (!canReceiveSignalFrom(other.pipe)) {
				return null;
			}
			if (other.pipe.wireSet[color.ordinal()] && other.pipe.signalStrength[color.ordinal()] > 0) {
				// To check canReceiveSignalFrom in all pipes.
				result = true;
			}
		}
		return result;
	}

	@Override
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
		Boolean isSignaling = isAnyPipeSignaling(((ITeleportLogicProvider) pipe).getLogic());
		if (isSignaling == null) {
			return false;
		} else if (isSignaling.booleanValue()) {
			return active;
		} else {
			return !active;
		}
	}

	@Override
	public int getIconIndex() {
		if (active) {
			switch (color) {
				case Red:
					return ActionTriggerIconProvider.Trigger_TPSignal_Red_Active;
				case Blue:
					return ActionTriggerIconProvider.Trigger_TPSignal_Blue_Active;
				case Green:
					return ActionTriggerIconProvider.Trigger_TPSignal_Green_Active;
				case Yellow:
				default:
					return ActionTriggerIconProvider.Trigger_TPSignal_Yellow_Active;
			}
		} else {
			switch (color) {
				case Red:
					return ActionTriggerIconProvider.Trigger_TPSignal_Red_Inactive;
				case Blue:
					return ActionTriggerIconProvider.Trigger_TPSignal_Blue_Inactive;
				case Green:
					return ActionTriggerIconProvider.Trigger_TPSignal_Green_Inactive;
				case Yellow:
				default:
					return ActionTriggerIconProvider.Trigger_TPSignal_Yellow_Inactive;
			}
		}
	}

}
