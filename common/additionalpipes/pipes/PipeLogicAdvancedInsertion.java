/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.pipes;

import java.util.LinkedList;
import java.util.Map;

import additionalpipes.AdditionalPipes;
import additionalpipes.triggers.ActionDisableInsertion;
import buildcraft.api.gates.IAction;

public class PipeLogicAdvancedInsertion {

	private boolean disabled = false;

	public LinkedList<IAction> getActions(LinkedList<IAction> result) {
		if (AdditionalPipes.actionDisableInsertion != null) {
			result.add(AdditionalPipes.actionDisableInsertion);
		}

		return result;
	}

	public void actionsActivated(Map<IAction, Boolean> actions) {
		disabled = false;
		for (Map.Entry<IAction, Boolean> e : actions.entrySet()) {
			if (e.getValue() && e.getKey() instanceof ActionDisableInsertion) {
				disabled = true;
			}
		}
	}

	public boolean isDisabled() {
		return disabled;
	}

}
