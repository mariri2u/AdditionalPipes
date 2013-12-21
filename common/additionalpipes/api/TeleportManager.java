/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes.api;

import java.util.Collections;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;

import com.google.common.collect.Sets;

public class TeleportManager {

	public static class EnergyProviderEvent extends Event {
		public final ITeleportEnergyProvider provider;
		public EnergyProviderEvent(ITeleportEnergyProvider provider) {
			this.provider = provider;
		}

		public static class Add extends EnergyProviderEvent {
			public Add(ITeleportEnergyProvider provider) { super(provider); }
		}

		public static class Remove extends EnergyProviderEvent {
			public Remove(ITeleportEnergyProvider provider) { super(provider); }
		}
	}

	public static class TeleportPipeEvent extends Event {
		public final ITeleportPipe pipe;
		public TeleportPipeEvent(ITeleportPipe pipe) {
			this.pipe = pipe;
		}

		public static class Add extends TeleportPipeEvent {
			public Add(ITeleportPipe pipe) { super(pipe); }
		}

		public static class Remove extends TeleportPipeEvent {
			public Remove(ITeleportPipe pipe) { super(pipe); }
		}

		public static class PreModify extends TeleportPipeEvent {
			public PreModify(ITeleportPipe pipe) { super(pipe); }
		}

		public static class Modify extends TeleportPipeEvent {
			public Modify(ITeleportPipe pipe) { super(pipe); }
		}
	}

	private static final Set<ITeleportEnergyProvider> energyProviders = Sets.newHashSet();
	private static final Set<ITeleportPipe> teleportPipes = Sets.newHashSet();

	public static void clear() {
		energyProviders.clear();
		teleportPipes.clear();
	}

	public static void addEnergyProvider(ITeleportEnergyProvider provider) {
		if (energyProviders.add(provider)) {
			MinecraftForge.EVENT_BUS.post(new EnergyProviderEvent.Add(provider));
		}
	}

	public static void removeEnergyProvider(ITeleportEnergyProvider provider) {
		if (energyProviders.remove(provider)) {
			MinecraftForge.EVENT_BUS.post(new EnergyProviderEvent.Remove(provider));
		}
	}

	public static void addTeleportPipe(ITeleportPipe pipe) {
		if (teleportPipes.add(pipe)) {
			MinecraftForge.EVENT_BUS.post(new TeleportPipeEvent.Add(pipe));
		}
	}

	public static void removeTeleportPipe(ITeleportPipe pipe) {
		if (teleportPipes.remove(pipe)) {
			MinecraftForge.EVENT_BUS.post(new TeleportPipeEvent.Remove(pipe));
		}
	}

	public static Set<ITeleportEnergyProvider> getEnergyProviders() {
		return Collections.unmodifiableSet(energyProviders);
	}

	public static Set<ITeleportPipe> getPipes() {
		return Collections.unmodifiableSet(teleportPipes);
	}

	public static boolean useEnergy(ITeleportPipe pipe, int amount) {
		for (ITeleportEnergyProvider provider : energyProviders) {
			if (provider.canTeleport(pipe) && provider.useEnergy(amount)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canUseEnergy(ITeleportPipe pipe, int amount) {
		for (ITeleportEnergyProvider provider : energyProviders) {
			if (provider.canTeleport(pipe) && provider.canUseEnergy(amount)) {
				return true;
			}
		}
		return false;
	}

	public static boolean canTeleport(ITeleportPipe pipe) {
		for (ITeleportEnergyProvider provider : energyProviders) {
			if (provider.canTeleport(pipe)) {
				return true;
			}
		}
		return false;
	}

	public static boolean useForcedEnergy(ITeleportPipe pipe, int amount, int factor) {
		for (ITeleportEnergyProvider provider : energyProviders) {
			if (provider.canTeleport(pipe)) {
				if (provider.useEnergy(amount)) {
					return true;
				}
			} else if (provider.canForcedTeleport(pipe)) {
				if (provider.useEnergy(amount * factor)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean canUseForcedEnergy(ITeleportPipe pipe, int amount, int factor) {
		for (ITeleportEnergyProvider provider : energyProviders) {
			if (provider.canTeleport(pipe)) {
				if (provider.canUseEnergy(amount)) {
					return true;
				}
			} else if (provider.canForcedTeleport(pipe)) {
				if (provider.canUseEnergy(amount * factor)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean canForcedTeleport(ITeleportPipe pipe) {
		for (ITeleportEnergyProvider provider : energyProviders) {
			if (provider.canTeleport(pipe) || provider.canForcedTeleport(pipe)) {
				return true;
			}
		}
		return false;
	}

}
