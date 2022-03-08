package com.useful.ucars;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucars.util.UMeta;

import net.milkbowl.vault.economy.Economy;

public class ucars extends JavaPlugin {
	// The main file
	public static HashMap<String, Double> carBoosts = new HashMap<String, Double>();
	public static HashMap<String, Double> fuel = new HashMap<String, Double>();
	public static YamlConfiguration lang = new YamlConfiguration();
	public static ucars plugin;
	public static FileConfiguration config;
	public static Boolean vault = false;
	public static Economy economy = null;
	public static Colors colors;
	public static boolean ignoreRails = true;
	public Boolean protocolLib = false;
	public Object protocolManager = null;
	public ArrayList<ItemStack> ufuelitems = new ArrayList<ItemStack>();
	public ListStore licensedPlayers = null;
	public uCarsCommandExecutor cmdExecutor = null;
	public ArrayList<Plugin> hookedPlugins = new ArrayList<Plugin>();
	public Boolean ucarsTrade = false;
	public static uCarsListener listener = null;
	protected uCarsAPI API = null;
	public static boolean forceRaceControls = false;
	public static boolean smoothDrive = true;
	public static boolean playersIgnoreTrafficLights = false;
	public static boolean turningCircles = true;
	public static boolean fireUpdateEvent = false;
	public static ArrayList<Integer> MCVersion = new ArrayList<Integer>();

	public static String colorise(String prefix) {
		return ChatColor.translateAlternateColorCodes('&', prefix);
	}

	public ListStore getLicensedPlayers() {
		return this.licensedPlayers;
	}

	public void setLicensedPlayers(ListStore licensed) {
		this.licensedPlayers = licensed;
		return;
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				// System.out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Double> loadHashMapDouble(String path) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					path));
			Object result = ois.readObject();
			ois.close();
			// you can feel free to cast result to HashMap<String, Integer> if
			// you know there's that HashMap in the file
			return (HashMap<String, Double>) result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void saveHashMap(HashMap<String, Double> map, String path) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			// Handle I/O exceptions
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	private Boolean setupProtocol() {
		try {
			this.protocolLib = true;
			this.protocolManager = ProtocolLibrary.getProtocolManager();
			/*
			 * ((ProtocolManager)this.protocolManager).addPacketListener(new
			 * PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE,
			 * ListenerPriority.NORMAL, 0x1b) {
			 */
			
			((ProtocolManager) this.protocolManager).addPacketListener(
					  new PacketAdapter(this, PacketType.Play.Client.STEER_VEHICLE) {
						  @Override
						  public void onPacketReceiving(final PacketEvent event) {
								PacketContainer packet = event.getPacket();
								final float sideways = packet.getFloat().read(0);
								final float forwards = packet.getFloat().read(1);
								final boolean jumping = packet.getBooleans().read(0);
								Bukkit.getScheduler().runTask(ucars.plugin, new Runnable(){

									@Override
									public void run() {
										MotionManager.move(event.getPlayer(), forwards,
												sideways, jumping);
										return;
									}});
								
						  }
					});
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public void onEnable() {
		plugin = this;

		Pattern pattern = Pattern.compile(".v(.*?)_R");		//Get MC-Version
		Matcher matcher = pattern.matcher(Bukkit.getServer().getClass().getPackage().getName());
		if(matcher.find()) {
			String[] MCVersionStr = matcher.group(1).split("_");
			for(String s:MCVersionStr) {
				MCVersion.add(Integer.parseInt(s));
			}
		}
		
		File langFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		if (langFile.exists() == false || langFile.length() < 1) {
			try {
				langFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}

		}
		try {
			lang.load(langFile);
		} catch (Exception e1) {
			getLogger().log(Level.WARNING,
					"Error creating/loading lang file! Regenerating..");
		}
		File configFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "config.yml");
		if (configFile.exists() == false || configFile.length() < 1) {
			// YamlConfiguration newC = new YamlConfiguration();
			// newC.set("time.created", System.currentTimeMillis());
			try {
				configFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}
			copy(getResource("ucarsConfigHeader.yml"), configFile);
		}

		try {
			config = getConfig();
		} catch (Exception e2) {
			try {
				configFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}
			copy(getResource("ucarsConfigHeader.yml"), configFile);
		}
		try {
			// config.load(this.getDataFolder().getAbsolutePath() +
			// File.separator + "config.yml");
			if (!config.contains("general.cars.# description")) {
				config.set("general.cars.# description",
						"If enabled this will allow for drivable cars(Minecarts not on rails)");
			}
			if (!lang.contains("lang.messages.place")) {
				lang.set("lang.messages.place",
						"&eYou placed a car! Cars can be driven with similar controls to a horse!");
			}
			if (!lang.contains("lang.error.pluginNull")) {
				lang.set("lang.error.pluginNull",
						"&4Error in ucars: Caused by: plugin = null? Report on bukkitdev immediately!");
			}
			if (!lang.contains("lang.messages.noDrivePerm")) {
				lang.set("lang.messages.noDrivePerm",
						"You don't have the permission ucars.cars required to drive a car!");
			}
			if (!lang.contains("lang.messages.noPlacePerm")) {
				lang.set("lang.messages.noPlacePerm",
						"You don't have the permission %perm% required to place a car!");
			}
			if (!lang.contains("lang.messages.noPlaceHere")) {
				lang.set("lang.messages.noPlaceHere",
						"&4You are not allowed to place a car here!");
			}
			if (!lang.contains("lang.messages.hitByCar")) {
				lang.set("lang.messages.hitByCar", "You were hit by a car!");
			}
			if (!lang.contains("lang.cars.remove")) {
				lang.set("lang.cars.remove",
						"&e%amount%&a cars in world &e%world%&a were removed!");
			}
			if (!lang.contains("lang.boosts.already")) {
				lang.set("lang.boosts.already", "&4Already boosting!");
			}
			if (!lang.contains("lang.boosts.low")) {
				lang.set("lang.boosts.low", "Initiated low level boost!");
			}
			if (!lang.contains("lang.boosts.med")) {
				lang.set("lang.boosts.med", "Initiated medium level boost!");
			}
			if (!lang.contains("lang.boosts.high")) {
				lang.set("lang.boosts.high", "Initiated high level boost!");
			}
			if (!lang.contains("lang.fuel.empty")) {
				lang.set("lang.fuel.empty", "You don't have any fuel left!");
			}
			if (!lang.contains("lang.fuel.disabled")) {
				lang.set("lang.fuel.disabled", "Fuel is not enabled!");
			}
			if (!lang.contains("lang.fuel.unit")) {
				lang.set("lang.fuel.unit", "litres");
			}
			if (!lang.contains("lang.fuel.isItem")) {
				lang.set("lang.fuel.isItem",
						"&9[Important:]&eItem fuel is enabled-The above is irrelevant!");
			}
			if (!lang.contains("lang.fuel.invalidAmount")) {
				lang.set("lang.fuel.invalidAmount", "Amount invalid!");
			}
			if (!lang.contains("lang.fuel.noMoney")) {
				lang.set("lang.fuel.noMoney", "You have no money!");
			}
			if (!lang.contains("lang.fuel.notEnoughMoney")) {
				lang.set("lang.fuel.notEnoughMoney",
						"That purchase costs %amount% %unit%! You only have %balance% %unit%!");
			}
			if (!lang.contains("lang.fuel.success")) {
				lang.set(
						"lang.fuel.success",
						"Successfully purchased %quantity% of fuel for %amount% %unit%! You now have %balance% %unit% left!");
			}
			if (!lang.contains("lang.fuel.sellSuccess")) {
				lang.set(
						"lang.fuel.sellSuccess",
						"Successfully sold %quantity% of fuel for %amount% %unit%! You now have %balance% %unit% left!");
			}
			if (!lang.contains("lang.messages.rightClickWith")) {
				lang.set("lang.messages.rightClickWith", "Right click with ");
			}
			if (!lang.contains("lang.messages.driveOver")) {
				lang.set("lang.messages.driveOver", "Drive over ");
			}
			if (!lang.contains("lang.messages.playersOnly")) {
				lang.set("lang.messages.playersOnly", "Players only!");
			}
			if (!lang.contains("lang.messages.reload")) {
				lang.set("lang.messages.reload",
						"The config has been reloaded!");
			}
			if (!lang.contains("lang.messages.noProtocolLib")) {
				lang.set(
						"lang.messages.noProtocolLib",
						"Hello operator, ProtocolLib (http://dev.bukkit.org/bukkit-plugins/protocollib/) was not detected and is required for ucars in MC 1.6 or higher. Please install it if necessary!");
			}
			if (!lang.contains("lang.licenses.next")) {
				lang.set("lang.licenses.next", "Now do %command% to continue!");
			}
			if (!lang.contains("lang.licenses.nocheat")) {
				lang.set("lang.licenses.nocheat", "You need to do all the stages of ulicense to obtain a license! You need to do %command%!");
			}
			if (!lang.contains("lang.licenses.basics")) {
				lang.set(
						"lang.licenses.basics",
						"A car is just a minecart placed on the ground, not rails. To place a car simply look and the floor while holding a minecart and right click!");
			}
			if (!lang.contains("lang.licenses.controls")) {
				lang.set(
						"lang.licenses.controls",
						"1) Look where you would like to go. 2) Use the 'w' key to go forward and 's' to go backwards. 3) Use the 'd' key to slow down/brake and the 'a' key to activate any action assgined to the car!");
			}
			if (!lang.contains("lang.licenses.effects")) {
				lang.set(
						"lang.licenses.effects",
						"Car speed can change depending on what block you may drive over. These can be short term boosts or a speedmod block. Do /ucars for more info on boosts!");
			}
			if (!lang.contains("lang.licenses.itemBoosts")) {
				lang.set(
						"lang.licenses.itemBoosts",
						"Right clicking with certain items can give you different boosts. Do /ucars for more info!");
			}
			if (!lang.contains("lang.licenses.success")) {
				lang.set("lang.licenses.success",
						"Congratulations! You can now drive a ucar!");
			}
			if (!lang.contains("lang.licenses.noLicense")) {
				lang.set("lang.licenses.noLicense",
						"To drive a car you need a license, do /ulicense to obtain one!");
			}
			if (!config.contains("general.cars.enable")) {
				config.set("general.cars.enable", true);
			}
			else{
				//Existing config
				if(!config.contains("misc.configVersion")){
					//Config part of old format and mark as so to convert it later
					config.set("misc.configVersion", 1.0);
				}
			}
            if(!config.contains("misc.configVersion")){
				config.set("misc.configVersion", 1.1);
			}
			if (!config.contains("general.permissions.enable")) {
				config.set("general.permissions.enable", true);
			}
			if (!config.contains("general.cars.enablePitch")) {
				config.set("general.cars.enablePitch", true);
			}
			if (!config.contains("general.cars.defSpeed")) {
				config.set("general.cars.defSpeed", (double) 30);
			}
			if (!config.contains("general.cars.smooth")) {
				config.set("general.cars.smooth", true);
			}
			if (!config.contains("general.cars.turningCircles")) {
				config.set("general.cars.turningCircles", true);
			}
			turningCircles = config.getBoolean("general.cars.turningCircles");
			if (!config.contains("general.cars.effectBlocks.enable")) {
				config.set("general.cars.effectBlocks.enable", true);
			}
			if (!config.contains("general.cars.boostsEnable")) {
				config.set("general.cars.boostsEnable", true);
			}
			if (!config.contains("general.cars.lowBoost")) {
				config.set("general.cars.lowBoost", new String[]{"COAL"});
			}
			if (!config.contains("general.cars.medBoost")) {
				config.set("general.cars.medBoost", new String[]{"IRON_INGOT"});
			}
			if (!config.contains("general.cars.highBoost")) {
				config.set("general.cars.highBoost", new String[]{"DIAMOND"});
			}
			if (!config.contains("general.cars.blockBoost")) {
				config.set("general.cars.blockBoost", new String[]{"GOLD_BLOCK"});
			}
			if (!config.contains("general.cars.HighblockBoost")) {
				config.set("general.cars.HighblockBoost", new String[]{"DIAMOND_BLOCK"});
			}
			if (!config.contains("general.cars.ResetblockBoost")) {
				config.set("general.cars.ResetblockBoost", new String[]{"EMERALD_BLOCK"});
			}
			if (!config.contains("general.cars.turret")) {
				config.set("general.cars.turret", null); //Remove if set
			}
			if (!config.contains("general.cars.ignoreVehiclesOnRails")) {
				config.set("general.cars.ignoreVehiclesOnRails", true);
			}
			else {
				ucars.ignoreRails = config.getBoolean("general.cars.ignoreVehiclesOnRails");
			}
			if (!config.contains("general.cars.jumpBlock")) {
				config.set("general.cars.jumpBlock", new String[]{"IRON_BLOCK"});
			}
			if (!config.contains("general.cars.jumpAmount")) {
				config.set("general.cars.jumpAmount", (double) 30);
			}
			if (!config.contains("general.cars.teleportBlock")) {
				config.set("general.cars.teleportBlock", new String[]{"STAINED_CLAY:2"});
			}
			if(!config.contains("general.cars.fireUpdateEvent")){
				config.set("general.cars.fireUpdateEvent", fireUpdateEvent);
			}
			else {
				fireUpdateEvent = config.getBoolean("general.cars.fireUpdateEvent");
			}
			if (!config.contains("general.cars.trafficLights.enable")) {
				config.set("general.cars.trafficLights.enable", true);
			}
			if (!config.contains("general.cars.trafficLights.waitingBlock")) {
				config.set("general.cars.trafficLights.waitingBlock", new String[]{"QUARTZ_BLOCK"});
			}
			if (!config.contains("general.cars.hitBy.enable")) {
				config.set("general.cars.hitBy.enable", false);
			}
			if (!config.contains("general.cars.hitBy.enableMonsterDamage")) {
				config.set("general.cars.hitBy.enableMonsterDamage", true);
			}
			if (!config.contains("general.cars.hitBy.enableAllMonsterDamage")) {
				config.set("general.cars.hitBy.enableAllMonsterDamage", true);
			}
			if (!config.contains("general.cars.hitBy.power")) {
				config.set("general.cars.hitBy.power", (double) 5);
			}
			if (!config.contains("general.cars.hitBy.damage")) {
				config.set("general.cars.hitBy.damage", 1.5);
			}
			if (!config.contains("general.cars.fallDamageDisabled")) {
				config.set("general.cars.fallDamageDisabled", false);
			}
			if (!config.contains("general.cars.worlds.enable")) {
				config.set("general.cars.worlds.enable", false);
			}
			if (!config.contains("general.cars.worlds.ids")) {
				config.set("general.cars.worlds.ids", new String[]{
						"world"});
			}
			if (!config.contains("general.cars.roadBlocks.enable")) {
				config.set("general.cars.roadBlocks.enable", false);
			}
			if (!config.contains("general.cars.roadBlocks.ids")) {
				config.set("general.cars.roadBlocks.ids", new String[]{
						"black_wool","white_wool","gray_wool","light_gray_wool"});
			}
			if (!config.contains("general.cars.licenses.enable")) {
				config.set("general.cars.licenses.enable", false);
			}
			if (!config.contains("general.cars.fuel.enable")) {
				config.set("general.cars.fuel.enable", false);
			}
			if (!config.contains("general.cars.fuel.price")) {
				config.set("general.cars.fuel.price", (double) 2);
			}
			if (!config.contains("general.cars.fuel.check")) {
				config.set("general.cars.fuel.check", new String[]{"FEATHER"});
			}
			if (!config.contains("general.cars.fuel.cmdPerm")) {
				config.set("general.cars.fuel.cmdPerm", "ucars.ucars");
			}
			if (!config.contains("general.cars.fuel.bypassPerm")) {
				config.set("general.cars.fuel.bypassPerm", "ucars.bypassfuel");
			}
			if (!config.contains("general.cars.fuel.items.enable")) {
				config.set("general.cars.fuel.items.enable", false);
			}
			if (!config.contains("general.cars.fuel.items.ids")) {
				config.set("general.cars.fuel.items.ids", new String[]{
						"WOOD","COAL:0","COAL:1"});
			}
			if (!config.contains("general.cars.fuel.sellFuel")) {
				config.set("general.cars.fuel.sellFuel", true);
			}
			if (!config.contains("general.cars.barriers")) {
				config.set("general.cars.barriers", new String[]{
						"COBBLE_WALL","FENCE","FENCE_GATE","NETHER_FENCE"});
			}
			if (!config.contains("general.cars.speedMods")) {
				config.set("general.cars.speedMods", new String[]{
						"SOUL_SAND:0-10","SPONGE:0-20"});
			}
			if (!config.contains("general.cars.placePerm.enable")) {
				config.set("general.cars.placePerm.enable", false);
			}
			if (!config.contains("general.cars.placePerm.perm")) {
				config.set("general.cars.placePerm.perm", "ucars.place");
			}
			if (!config.contains("general.cars.health.default")) {
				config.set("general.cars.health.default", 10.0);
			}
			if (!config.contains("general.cars.health.max")) {
				config.set("general.cars.health.max", 100.0);
			}
			if (!config.contains("general.cars.health.min")) {
				config.set("general.cars.health.min", 5.0);
			}
			if (!config.contains("general.cars.health.overrideDefault")) {
				config.set("general.cars.health.overrideDefault", true);
			}
			if (!config.contains("general.cars.health.underwaterDamage")) {
				config.set("general.cars.health.underwaterDamage", 0.0);
			}
			if (!config.contains("general.cars.health.lavaDamage")) {
				config.set("general.cars.health.lavaDamage", 0.0);
			}
			if (!config.contains("general.cars.health.punchDamage")) {
				config.set("general.cars.health.punchDamage", 50.0);
			}
			if (!config.contains("general.cars.health.cactusDamage")) {
				config.set("general.cars.health.cactusDamage", 0.0);
			}
			if (!config.contains("general.cars.health.crashDamage")) {
				config.set("general.cars.health.crashDamage", 0.0);
			}
			if (!config.contains("general.cars.forceRaceControlSystem")) {
				config.set("general.cars.forceRaceControlSystem", false);
			}
			forceRaceControls = config.getBoolean("general.cars.forceRaceControlSystem");
			if (!config.contains("general.cars.playersIgnoreTrafficLights")) {
				config.set("general.cars.playersIgnoreTrafficLights", false);
			}
			playersIgnoreTrafficLights = config.getBoolean("general.cars.playersIgnoreTrafficLights");
			if (!config.contains("colorScheme.success")) {
				config.set("colorScheme.success", "&a");
			}
			if (!config.contains("colorScheme.error")) {
				config.set("colorScheme.error", "&c");
			}
			if (!config.contains("colorScheme.info")) {
				config.set("colorScheme.info", "&e");
			}
			if (!config.contains("colorScheme.title")) {
				config.set("colorScheme.title", "&9");
			}
			if (!config.contains("colorScheme.tp")) {
				config.set("colorScheme.tp", "&5");
			}

			if (config.getBoolean("general.cars.fuel.enable")
					&& !config.getBoolean("general.cars.fuel.items.enable")) {
				try {
					if (!setupEconomy()) {
						plugin.getLogger()
								.warning(
										"Attempted to enable fuel but vault NOT found. Please install vault to use fuel!");
						plugin.getLogger().warning("Disabling fuel system...");
						config.set("general.cars.fuel.enable", false);
					} else {
						vault = true;
						fuel = new HashMap<String, Double>();
						File fuels = new File(plugin.getDataFolder()
								.getAbsolutePath()
								+ File.separator
								+ "fuel.bin");
						if (fuels.exists() && fuels.length() > 1) {
							fuel = loadHashMapDouble(plugin.getDataFolder()
									.getAbsolutePath()
									+ File.separator
									+ "fuel.bin");
							if (fuel == null) {
								fuel = new HashMap<String, Double>();
							}
						}
					}
				} catch (Exception e) {
					plugin.getLogger()
							.warning(
									"Attempted to enable fuel but vault NOT found. Please install vault to use fuel!");
					plugin.getLogger().warning("Disabling fuel system...");
					config.set("general.cars.fuel.enable", false);
				}
			}
		} catch (Exception e) {
		}
		//Before saving, convert old configs
		double latestConfigVersion = 1.1;
		double configVersion = config.getDouble("misc.configVersion");
        while(configVersion<latestConfigVersion){
        	configVersion+=0.1; //Add 0.1 to config version
        	config = ConfigVersionConverter.convert(config, configVersion); //Convert to next increment in config versioning
        }
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		List<String> ids = ucars.config.getStringList("general.cars.fuel.items.ids");
		ufuelitems = new ArrayList<ItemStack>();
		for (String raw : ids) {
			ItemStack stack = ItemStackFromId.get(raw);
			if (stack != null) {
				ufuelitems.add(stack);
			}
		}
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		PluginDescriptionFile pldesc = plugin.getDescription();
		Map<String, Map<String, Object>> commands = pldesc.getCommands();
		Set<String> keys = commands.keySet();
		for (String k : keys) {
			try {
				cmdExecutor = new uCarsCommandExecutor(this);
				getCommand(k).setExecutor(cmdExecutor);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE,
						"Error registering command " + k.toString());
				e.printStackTrace();
			}
		}
		if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
			Boolean success = setupProtocol();
			if (!success) {
				this.protocolLib = false;
				getLogger()
						.log(Level.WARNING,
								"ProtocolLib (http://http://dev.bukkit.org/bukkit-plugins/protocollib/) was not found! For servers running MC 1.6 or above this is required for ucars to work!");
			}
		} else {
			this.protocolLib = false;
			getLogger()
					.log(Level.WARNING,
							"ProtocolLib (http://http://dev.bukkit.org/bukkit-plugins/protocollib/) was not found! For servers running MC 1.6 or above this is required for ucars to work!");
		}
		this.licensedPlayers = new ListStore(new File(getDataFolder()
				+ File.separator + "licenses.txt"));
		this.licensedPlayers.load();
		ucars.listener = new uCarsListener(this);
		getServer().getPluginManager().registerEvents(listener, this);
		this.API = new uCarsAPI();
		smoothDrive = config.getBoolean("general.cars.smooth");
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable(){

			@Override
			public void run() {
				UEntityMeta.cleanEntityObjs();
				UMeta.clean();
				return;
			}}, 20*20l, 20*20l);
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable(){

			@Override
			public void run() {
				if(Runtime.getRuntime().maxMemory()-Runtime.getRuntime().freeMemory() > 1000){
					System.gc();
				}
				return;
			}}, 20*20l, 120*20l);
		
		getLogger().info("uCars has been enabled!");
		return;
	}

	@Override
	public void onDisable() {
		saveHashMap(fuel, plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "fuel.bin");
		this.licensedPlayers.save();
		unHookPlugins();
		getLogger().info("uCars has been disabled!");
		return;
	}
    public static String getIdList(final String configKey){
    	final List<String> s = config.getStringList(configKey);
    	String msg = "";
    	for(String str:s){
    		if(msg.length() < 1){
    			msg = str;
    			continue; //Next iteration
    		}
    		msg+=", "+str; //Append it
    	}
    	return msg;
    }
	public final Boolean isBlockEqualToConfigIds(final String configKey, Block block){
		return isBlockEqualToConfigIds(config.getStringList(configKey), block);
	}
	public final Boolean isBlockEqualToConfigIds(List<String> rawIds, Block block) {
		// split by : then compare!
		for (String raw : rawIds) {
			if(raw.contains("-")) {
				raw = raw.split("-")[0];
			}
			final String[] parts = raw.split(":");
			if (parts.length < 1) {
			} else if (parts.length < 2) { //New configs and blocknames
				if (ItemStackFromId.equals(raw,block.getType().name().toUpperCase(),block.getData())) {
					return true;
				}
			} else { //old configs and block names
				final String mat = parts[0];
				final int data = Integer.parseInt(parts[1]);
				final int bdata = block.getData(); //TODO Alternative to .getData()
				if (mat.equalsIgnoreCase(block.getType().name().substring(block.getType().name().indexOf("_")+1)) && bdata == data) {
					return true;
				}
			}
		}
		return false;
	}
	
	public final Boolean isItemEqualToConfigIds(List<String> rawIds, ItemStack item) {
		// split by : then compare!
		for (String raw : rawIds) {
			final String[] parts = raw.split(":");
			if (parts.length < 1) {
			} else if (parts.length < 2) {
				if (parts[0].equalsIgnoreCase(item.getType().name())) {
					return true;
				}
			} else {
				final String mat = parts[0];
				final int data = Integer.parseInt(parts[1]);
				final int bdata = item.getDurability();
				if (mat.equalsIgnoreCase(item.getType().name()) && bdata == data) {
					return true;
				}
			}
		}
		return false;
	}
	public final Boolean isItemOnList(ArrayList<ItemStack> items, ItemStack item) {
		// split by : then compare!
		for (ItemStack raw : items) {
			final String mat = raw.getType().name().toUpperCase();
			final int data = raw.getDurability();
			final int bdata = item.getDurability();
			if (mat.equalsIgnoreCase(item.getType().name()) && bdata == data) {
				return true;
			}
		}
		return false;
	}

	public uCarsAPI getAPI() {
		return API;
	}

	public void hookPlugin(Plugin plugin) {
		getAPI().hookPlugin(plugin);
	}

	public void unHookPlugin(Plugin plugin) {
		getAPI().unHookPlugin(plugin);
	}

	public void unHookPlugins() {
		getAPI().unHookPlugins();
	}

	public Boolean isPluginHooked(Plugin plugin) {
		return getAPI().isPluginHooked(plugin);
	}
	
	public Plugin getPlugin(String name){
		try {
			for(Plugin p:this.hookedPlugins){
				if(p.getName().equalsIgnoreCase(name)){
					return p;
				}
			}
		} catch (Exception e) {
			//Concurrent error
			return null;
		}
		return null;
	}
}
