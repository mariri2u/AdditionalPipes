/**
 * Additional Pipes is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package additionalpipes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import logisticspipes.interfaces.routing.ISpecialPipedConnection;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import additionalpipes.api.TeleportManager;
import additionalpipes.block.BlockTeleportManager;
import additionalpipes.block.BlockTeleportTether;
import additionalpipes.blueprints.BptItemPipeAdvancedWooden;
import additionalpipes.blueprints.BptItemPipeDistributor;
import additionalpipes.blueprints.BptItemPipeTeleport;
import additionalpipes.chunk.ChunkManager;
import additionalpipes.client.texture.ActionTriggerIconProvider;
import additionalpipes.client.texture.GuiIconProvider;
import additionalpipes.inventory.GuiHandler;
import additionalpipes.item.ItemTeleportManager;
import additionalpipes.item.ItemTeleportTether;
import additionalpipes.item.crafting.RecipesRestore;
import additionalpipes.item.crafting.RecipesTether;
import additionalpipes.network.PacketHandler;
import additionalpipes.network.PlayerTracker;
import additionalpipes.pipes.ITeleportLogicProvider;
import additionalpipes.pipes.PipeFluidsAdvancedInsertion;
import additionalpipes.pipes.PipeFluidsRedstone;
import additionalpipes.pipes.PipeFluidsTeleport;
import additionalpipes.pipes.PipeItemsAdvancedInsertion;
import additionalpipes.pipes.PipeItemsAdvancedWood;
import additionalpipes.pipes.PipeItemsDistributor;
import additionalpipes.pipes.PipeItemsRedstone;
import additionalpipes.pipes.PipeItemsTeleport;
import additionalpipes.pipes.PipeLogicTeleport;
import additionalpipes.pipes.PipePowerAdvancedWood;
import additionalpipes.pipes.PipePowerRedstone;
import additionalpipes.pipes.PipePowerTeleport;
import additionalpipes.pipes.PipeStructureTeleport;
import additionalpipes.proxy.APProxy;
import additionalpipes.tileentity.TileTeleportManager;
import additionalpipes.tileentity.TileTeleportManagerEnergy;
import additionalpipes.tileentity.TileTeleportTether;
import additionalpipes.triggers.ActionDisableInsertion;
import additionalpipes.triggers.PipeTriggerProvider;
import additionalpipes.triggers.TriggerRemoteSignal;
import additionalpipes.utils.APDefaultProps;
import additionalpipes.utils.FrequencyMap;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.transport.IPipe;
import buildcraft.core.blueprints.BptItem;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.Localization;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeConnectionBans;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsQuartz;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipePowerWood;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = APDefaultProps.ID, name = APDefaultProps.NAME, version = APDefaultProps.VERSION, dependencies = "required-after:BuildCraft|Core@[%BC_VERSION%,);required-after:BuildCraft|Transport;required-after:BuildCraft|Silicon;after:LogisticsPipes|Main;after:IC2", useMetadata = false)
@NetworkMod(channels = { APDefaultProps.NET_CHANNEL_NAME }, packetHandler = PacketHandler.class)
public class AdditionalPipes {

	static {
		// Fluid pipes
		PipeConnectionBans.banConnection(PipeFluidsAdvancedInsertion.class, PipeFluidsCobblestone.class);
		PipeConnectionBans.banConnection(PipeFluidsTeleport.class);

		// Item pipes
		PipeConnectionBans.banConnection(PipeItemsAdvancedWood.class);
		PipeConnectionBans.banConnection(PipeItemsAdvancedWood.class, PipeItemsWood.class);
		PipeConnectionBans.banConnection(PipeItemsAdvancedInsertion.class, PipeItemsCobblestone.class, PipeItemsQuartz.class);
		PipeConnectionBans.banConnection(PipeItemsTeleport.class);

		// Power pipes
		PipeConnectionBans.banConnection(PipePowerAdvancedWood.class);
		PipeConnectionBans.banConnection(PipePowerAdvancedWood.class, PipePowerWood.class);
		PipeConnectionBans.banConnection(PipePowerTeleport.class);

		// Structure pipes
		PipeConnectionBans.banConnection(PipeStructureTeleport.class);

		PipeTransportPower.powerCapacities.put(PipePowerTeleport.class, 4096);
		PipeTransportPower.powerCapacities.put(PipePowerRedstone.class, 256);
		PipeTransportPower.powerCapacities.put(PipePowerAdvancedWood.class, 128);
	}

	public static ItemPipe pipeItemsTeleport;
	public static ItemPipe pipeFluidsTeleport;
	public static ItemPipe pipePowerTeleport;
	public static ItemPipe pipeStructureTeleport;
	public static ItemPipe pipeItemsDistribution;
	public static ItemPipe pipeItemsAdvancedWood;
	public static ItemPipe pipePowerAdvancedWood;
	public static ItemPipe pipeItemsAdvancedInsertion;
	public static ItemPipe pipeFluidsAdvancedInsertion;
	public static ItemPipe pipeItemsRedstone;
	public static ItemPipe pipeFluidsRedstone;
	public static ItemPipe pipePowerRedstone;

	public static String CATEGORY_TRIGGER = "trigger";
	public static BCTrigger triggerRemoteRedSignalActive;
	public static BCTrigger triggerRemoteRedSignalInactive;
	public static BCTrigger triggerRemoteBlueSignalActive;
	public static BCTrigger triggerRemoteBlueSignalInactive;
	public static BCTrigger triggerRemoteGreenSignalActive;
	public static BCTrigger triggerRemoteGreenSignalInactive;
	public static BCTrigger triggerRemoteYellowSignalActive;
	public static BCTrigger triggerRemoteYellowSignalInactive;

	public static BCAction actionDisableInsertion;

	public static BlockTeleportTether blockTeleportTether;
	public static Block blockTeleportManager;

	private static Configuration configuration;

	public static boolean enableRestoreRecipes;
	public static boolean distributorSplitsStack;
	public static String fakedUserName;
	public static boolean showAllPersistentChunks;
	public static boolean disablePermissions;

	public static String CATEGORY_ENERGY = "energy";
	public static boolean disableEnergyUsage;
	public static boolean disableLinkingUsage;
	public static int forcedEnergyFactor;
	public static int unitLP;
	public static int unitItems;
	public static int unitFluids;
	public static int unitPower;
	public static int managerCapacity;

	public static boolean enableTeleportTether;
	public static boolean enableTeleportManager;

	public static boolean remoteDisablePermissions;
	public static int remoteManagerCapacity;

	public static Logger logger = Logger.getLogger(APDefaultProps.ID);

	@Mod.Instance(APDefaultProps.ID)
	public static AdditionalPipes instance;

	private static final class PipeRecipe {
		boolean isShapeless = false; // pipe recipes come shaped and unshaped.
		ItemStack result;
		Object[] input;
	}

	private static List<PipeRecipe> pipeRecipes = Lists.newLinkedList();

	private static boolean getProp(String category, String key, boolean defaultValue, String comment) {
		Property prop = configuration.get(category, key, defaultValue);
		prop.comment = comment;
		return prop.getBoolean(defaultValue);
	}

	private static int getProp(String category, String key, int defaultValue, int minimalValue, String comment) {
		Property prop = configuration.get(category, key, defaultValue);
		prop.comment = comment;
		int result = prop.getInt(defaultValue);
		if (result >= minimalValue) {
			return result;
		}
		prop.set(minimalValue);
		return minimalValue;
	}

	private static Integer getDefinedProp(String category, String key, int defaultValue) {
		if (!configuration.hasKey(category, key)) {
			return null;
		}
		Property prop = configuration.get(category, key, defaultValue);
		return prop.getInt(defaultValue);
	}

	private static String getProp(String category, String key, String defaultValue, String comment) {
		Property prop = configuration.get(category, key, defaultValue);
		prop.comment = comment;
		return prop.getString();
	}

	private static int getBlockProp(String key, int defaultID) {
		Property prop = configuration.getBlock(key, defaultID);
		return prop.getInt(defaultID);
	}

	@Mod.EventHandler
	public void preInitialize(FMLPreInitializationEvent event) {
		logger.setParent(FMLLog.getLogger());

		configuration = new Configuration(event.getSuggestedConfigurationFile());
		try {
			configuration.load();

			enableRestoreRecipes = getProp(Configuration.CATEGORY_GENERAL, "enableRestoreRecipes", APDefaultProps.ENABLE_RESTORE_RECIPES,
					"Set to true to enable restoring Fluid/Kinesis/Structure Pipe recipes to Items Pipe.");
			distributorSplitsStack = getProp(Configuration.CATEGORY_GENERAL, "distributorSplitsStack", APDefaultProps.DISTRIBUTOR_SPLITS_STACK,
					"Set to true to distribute by the number of items instead of the number of stacks.");
			fakedUserName = getProp(Configuration.CATEGORY_GENERAL, "fakedUserName", APDefaultProps.FAKED_USER_NAME,
					"This user name is shown in the GUI on SSP.");
			showAllPersistentChunks = getProp(Configuration.CATEGORY_GENERAL, "showAllPersistentChunks", APDefaultProps.SHOW_ALL_PERSISTENT_CHUNKS,
					"Set to true to show chunks that is loaded by other mods when you press F9.");
			disablePermissions = getProp(Configuration.CATEGORY_GENERAL, "disablePermissions", APDefaultProps.DISABLE_PERMISSIONS,
					"Set to true to disable access restrictions.");

			disableEnergyUsage = getProp(CATEGORY_ENERGY, "disableEnergyUsage", APDefaultProps.DISABLE_ENERGY_USAGE,
					"Set to false to use energy with Teleport Manager when the item of pipe is teleporting. This function is for the masochists");
			disableLinkingUsage = getProp(CATEGORY_ENERGY, "disableLinkingUsage", APDefaultProps.DISABLE_LINKING_USAGE,
					"Set to false to link teleport pipes with Teleport Manager. This function is for the masochists");
			forcedEnergyFactor = getProp(CATEGORY_ENERGY, "forcedEnergyFactor", APDefaultProps.FORCED_ENERGY_FACTOR, 1,
					"Teleport Pipe will use energy increased by this number if no Teleport Manager has linked pipes");
			unitLP = getProp(CATEGORY_ENERGY, "unit.LP", APDefaultProps.UNIT_LP, 1, null);
			unitItems = getProp(CATEGORY_ENERGY, "unit.items", APDefaultProps.UNIT_ITEMS, 1, null);
			unitFluids = getProp(CATEGORY_ENERGY, "unit.fluids", APDefaultProps.UNIT_FLUIDS, 1, null);
			unitPower = getProp(CATEGORY_ENERGY, "unit.power", APDefaultProps.UNIT_POWER, 1, null);
			managerCapacity = getProp(CATEGORY_ENERGY, "managerCapacity", APDefaultProps.MANAGER_CAPACITY, 0, null);

			pipeItemsTeleport = createPipe(APDefaultProps.PIPE_ITEMS_TELEPORT, PipeItemsTeleport.class, "Item Teleport Pipe", BuildCraftCore.diamondGearItem, Item.eyeOfEnder, BuildCraftCore.diamondGearItem);
			pipeFluidsTeleport = createPipe(APDefaultProps.PIPE_FLUIDS_TELEPORT, PipeFluidsTeleport.class, "Waterproof Teleport Pipe", BuildCraftTransport.pipeWaterproof, pipeItemsTeleport);
			pipePowerTeleport = createPipe(APDefaultProps.PIPE_POWER_TELEPORT, PipePowerTeleport.class, "Power Teleport Pipe", Item.redstone, pipeItemsTeleport);
			pipeStructureTeleport = createPipe(APDefaultProps.PIPE_STRUCTURE_TELEPORT, PipeStructureTeleport.class, "Structure Teleport Pipe", Block.gravel, pipeItemsTeleport);

			pipeItemsDistribution = createPipe(APDefaultProps.PIPE_ITEMS_DISTRIBUTION, PipeItemsDistributor.class, "Distribution Transport Pipe", Item.redstone, Item.ingotIron, Block.glass, Item.ingotIron);

			pipeItemsAdvancedWood = createPipe(APDefaultProps.PIPE_ITEMS_ADVANCED_WOOD, PipeItemsAdvancedWood.class, "Advanced Wooden Transport Pipe", Item.emerald, "plankWood", Block.glass, "plankWood");
			if (pipeItemsAdvancedWood != null) {
				PipeRecipe recipe = new PipeRecipe();
				recipe.result = new ItemStack(pipeItemsAdvancedWood, 2);
				recipe.input = new Object[] { " r ", "wgw", Character.valueOf('r'), Item.redstone, Character.valueOf('w'), "plankWood", Character.valueOf('g'), Block.glass };
				pipeRecipes.add(recipe);
			}
			pipePowerAdvancedWood = createPipe(APDefaultProps.PIPE_POWER_ADVANCED_WOOD, PipePowerAdvancedWood.class, "Advanced Wooden Conductive Pipe", Item.redstone, pipeItemsAdvancedWood);

			pipeItemsAdvancedInsertion = createPipe(APDefaultProps.PIPE_ITEMS_ADVANCED_INSERTION, PipeItemsAdvancedInsertion.class, "Item Advanced Insertion Pipe", Item.redstone, Block.stone, Block.glass, Block.stone);
			pipeFluidsAdvancedInsertion = createPipe(APDefaultProps.PIPE_FLUIDS_ADVANCED_INSERTION, PipeFluidsAdvancedInsertion.class, "Advanced Insertion Waterproof Pipe", BuildCraftTransport.pipeWaterproof, pipeItemsAdvancedInsertion);

			pipeItemsRedstone = createPipe(APDefaultProps.PIPE_ITEMS_REDSTONE, PipeItemsRedstone.class, "Redstone Transport Pipe", Item.redstone, Block.glass, Item.redstone);
			pipeFluidsRedstone = createPipe(APDefaultProps.PIPE_FLUIDS_REDSTONE, PipeFluidsRedstone.class, "Redstone Waterproof Pipe", BuildCraftTransport.pipeWaterproof, pipeItemsRedstone);
			pipePowerRedstone = createPipe(APDefaultProps.PIPE_POWER_REDSTONE, PipePowerRedstone.class, "Redstone Conductive Pipe", Item.redstone, pipeItemsRedstone);

			triggerRemoteRedSignalActive = createTrigger(APDefaultProps.TRIGGER_REMOTE_RED_SIGNAL_ACTIVE, true, IPipe.WireColor.Red);
			triggerRemoteRedSignalInactive = createTrigger(APDefaultProps.TRIGGER_REMOTE_RED_SIGNAL_INACTIVE, false, IPipe.WireColor.Red);
			triggerRemoteBlueSignalActive = createTrigger(APDefaultProps.TRIGGER_REMOTE_BLUE_SIGNAL_ACTIVE, true, IPipe.WireColor.Blue);
			triggerRemoteBlueSignalInactive = createTrigger(APDefaultProps.TRIGGER_REMOTE_BLUE_SIGNAL_INACTIVE, false, IPipe.WireColor.Blue);
			triggerRemoteGreenSignalActive = createTrigger(APDefaultProps.TRIGGER_REMOTE_GREEN_SIGNAL_ACTIVE, true, IPipe.WireColor.Green);
			triggerRemoteGreenSignalInactive = createTrigger(APDefaultProps.TRIGGER_REMOTE_GREEN_SIGNAL_INACTIVE, false, IPipe.WireColor.Green);
			triggerRemoteYellowSignalActive = createTrigger(APDefaultProps.TRIGGER_REMOTE_YELLOW_SIGNAL_ACTIVE, true, IPipe.WireColor.Yellow);
			triggerRemoteYellowSignalInactive = createTrigger(APDefaultProps.TRIGGER_REMOTE_YELLOW_SIGNAL_INACTIVE, false, IPipe.WireColor.Yellow);

			Integer actionId = getDefinedProp(CATEGORY_TRIGGER, "actionDisableInsertion.id", APDefaultProps.ACTION_DISABLE_INSERTION);
			if (actionId == null || actionId.intValue() != 0) {
				actionDisableInsertion = new ActionDisableInsertion(actionId != null ? actionId.intValue() : APDefaultProps.ACTION_DISABLE_INSERTION);
			}

			int tetherId = getBlockProp("teleportTether.id", APDefaultProps.BLOCK_TELEPORT_TETHER);
			enableTeleportTether = getProp(Configuration.CATEGORY_BLOCK, "teleportTether.enabled", true, null);
			if (tetherId != 0) {
				CoreProxy.proxy.registerTileEntity(TileTeleportTether.class, "Teleport Tether");

				blockTeleportTether = new BlockTeleportTether(tetherId);
				GameRegistry.registerBlock(blockTeleportTether.setUnlocalizedName("teleportTether"), ItemTeleportTether.class, "teleportTether");
				CoreProxy.proxy.addName(blockTeleportTether, "Teleport Tether");
				net.kyprus.additionalpipes.AdditionalPipes.blockChunkLoader = blockTeleportTether;
			}

			int managerId = getBlockProp("teleportManager.id", APDefaultProps.BLOCK_TELEPORT_MANAGER);
			enableTeleportManager = getProp(Configuration.CATEGORY_BLOCK, "teleportManager.enabled", true, null);
			if (managerId != 0) {
				CoreProxy.proxy.registerTileEntity(disableEnergyUsage ? TileTeleportManager.class : TileTeleportManagerEnergy.class,
						"Teleport Manager");

				blockTeleportManager = new BlockTeleportManager(managerId);
				GameRegistry.registerBlock(blockTeleportManager.setUnlocalizedName("teleportManager"), ItemTeleportManager.class, "teleportManager");
				CoreProxy.proxy.addName(blockTeleportManager, "Teleport Manager");
			}

			MinecraftForge.EVENT_BUS.register(this);
		} finally {
			if (configuration.hasChanged()) {
				configuration.save();
			}
		}

		BlockTeleportTether.maxChunkDepth = ForgeChunkManager.getMaxChunkDepthFor(APDefaultProps.ID);
		BlockTeleportTether.maxTicketLength = ForgeChunkManager.getMaxTicketLengthFor(APDefaultProps.ID);
	}

	@Mod.EventHandler
	public void initialize(FMLInitializationEvent event) {
		setPipeBpt(pipeItemsTeleport, new BptItemPipeTeleport());
		setPipeBpt(pipeFluidsTeleport, new BptItemPipeTeleport());
		setPipeBpt(pipePowerTeleport, new BptItemPipeTeleport());
		setPipeBpt(pipeItemsDistribution, new BptItemPipeDistributor());
		setPipeBpt(pipeItemsAdvancedWood, new BptItemPipeAdvancedWooden());

		ActionManager.registerTriggerProvider(new PipeTriggerProvider());

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		Localization.addLocalization("/lang/additionalpipes/", APDefaultProps.DEFAULT_LANGUAGE);
		APProxy.proxy.init();
		ChunkManager.instance();
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
		GameRegistry.registerPlayerTracker(new PlayerTracker());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// For Logistics Pipes compatibility
		try {
			Class<?> SimpleServiceLocator = Class.forName("logisticspipes.proxy.SimpleServiceLocator");
			logger.fine("Logistics Pipes is detected");
			Object specialconnection = SimpleServiceLocator.getField("specialpipeconnection").get(null);
			Method registerHandler = specialconnection.getClass().getMethod("registerHandler", ISpecialPipedConnection.class);
			registerHandler.invoke(specialconnection, new ISpecialPipedConnection() {

				@Override
				public boolean init() {
					return true;
				}

				@Override
				public boolean isType(TileGenericPipe tile) {
					return tile.pipe instanceof ITeleportLogicProvider;
				}

				@Override
				public List<TileGenericPipe> getConnections(TileGenericPipe tile) {
					return Lists.transform(((ITeleportLogicProvider) tile.pipe).getLogic().getConnectedPipeLogics(true), new Function<PipeLogicTeleport, TileGenericPipe>() {
						@Override
						public TileGenericPipe apply(PipeLogicTeleport input) {
							return input.pipe.container;
						}
					});
				}

			});
			logger.fine("Logistics Pipes is compatible now!");
		} catch (ClassNotFoundException e) {
		} catch (ReflectiveOperationException e) {
			logger.warning("This Logistics Pipes is not supported");
			e.printStackTrace();
		}
	}

	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		remoteDisablePermissions = disablePermissions;
		remoteManagerCapacity = managerCapacity;
	}

	@Mod.EventHandler
	public void serverStopped(FMLServerStoppedEvent event) {
		FrequencyMap.clear();
		TeleportManager.clear();
	}

	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.textureType == 1) {
			GuiIconProvider.INSTANCE.registerIcons(event.map);
			ActionTriggerIconProvider.INSTANCE.registerIcons(event.map);
		}
	}

	public void loadRecipes() {
		if (enableRestoreRecipes) {
			GameRegistry.addRecipe(new RecipesRestore());
		}
		if (enableTeleportTether) {
			GameRegistry.addRecipe(new ItemStack(blockTeleportTether, 4), new Object[] { "iii", "iLi", "iii", Character.valueOf('i'), Item.ingotIron, Character.valueOf('L'), new ItemStack(Item.dyePowder, 1, 4) });
			RecipesTether.addRecipes();
		}
		if (enableTeleportManager) {
			GameRegistry.addRecipe(new ItemStack(blockTeleportManager, 1), new Object[] { "iGi", "eTe", "iii", Character.valueOf('G'), BuildCraftCore.diamondGearItem, Character.valueOf('e'), Item.eyeOfEnder, Character.valueOf('T'), pipeItemsTeleport, Character.valueOf('i'), Item.ingotIron });
			GameRegistry.addRecipe(new ItemStack(blockTeleportManager, 1), new Object[] { "iGi", "eTe", "iii", Character.valueOf('G'), new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3), Character.valueOf('e'), Item.eyeOfEnder, Character.valueOf('T'), pipeItemsTeleport, Character.valueOf('i'), Item.ingotIron });
		}

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes) {
			if (pipe.isShapeless) {
				GameRegistry.addShapelessRecipe(pipe.result, pipe.input);
			} else {
				CoreProxy.proxy.addCraftingRecipe(pipe.result, pipe.input);
			}
		}
		pipeRecipes = null;
	}

	public static ItemPipe createPipe(int defaultID, Class<? extends Pipe<?>> clas, String descr, Object... ingredients) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0)) + clas.getSimpleName().substring(1);
		boolean enabled = AdditionalPipes.configuration.get(Configuration.CATEGORY_ITEM, name + ".enabled", true).getBoolean(true);
		int id = AdditionalPipes.configuration.getItem(Configuration.CATEGORY_ITEM, name + ".id", defaultID).getInt(defaultID);
		if (id == 0) {
			return null;
		}

		ItemPipe res = BlockGenericPipe.registerPipe(id, clas);
		res.setUnlocalizedName(clas.getSimpleName());
		LanguageRegistry.addName(res, descr);
		APProxy.proxy.registerRenderer(res);

		if (enabled) {
			// Add appropriate recipe to temporary list
			PipeRecipe recipe = new PipeRecipe();

			if (ingredients.length == 4) {
				recipe.result = new ItemStack(res, 8);
				recipe.input = new Object[] { " A ", "BCD", Character.valueOf('A'), ingredients[0], Character.valueOf('B'), ingredients[1], Character.valueOf('C'), ingredients[2], Character.valueOf('D'), ingredients[3] };

				pipeRecipes.add(recipe);
			} else if (ingredients.length == 3) {
				recipe.result = new ItemStack(res, 8);
				recipe.input = new Object[] { "ABC", Character.valueOf('A'), ingredients[0], Character.valueOf('B'), ingredients[1], Character.valueOf('C'), ingredients[2] };

				pipeRecipes.add(recipe);
			} else if (ingredients.length == 2) {
				recipe.isShapeless = true;
				recipe.result = new ItemStack(res, 1);
				recipe.input = new Object[] { ingredients[0], ingredients[1] };

				pipeRecipes.add(recipe);
			}
		}

		return res;
	}

	private BCTrigger createTrigger(int defaultID, boolean active, IPipe.WireColor color) {
		String name = "trigger" + color.toString() + "Signal" + (active ? "Active" : "Inactive") + "Teleport";

		int id = configuration.hasKey(name, CATEGORY_TRIGGER) ? getProp(CATEGORY_TRIGGER, name + ".id", defaultID, 0, null) : defaultID;
		return id == 0 ? null : new TriggerRemoteSignal(id, active, color);
	}

	private void setPipeBpt(ItemPipe pipe, BptItem bpt) {
		if (pipe != null) {
			BuildCraftCore.itemBptProps[pipe.itemID] = bpt;
		}
	}

}
