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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

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
	public Boolean protocolLib = false;
	public Object  protocolManager = null;
	public List<ItemStack> ufuelitems = new ArrayList<ItemStack>();
    public static uCarsListener listener = null;
    
	public static String colorise(String prefix) {
                /*
		prefix = prefix.replace("&0", "" + ChatColor.BLACK);
		prefix = prefix.replace("&1", "" + ChatColor.DARK_BLUE);
		prefix = prefix.replace("&2", "" + ChatColor.DARK_GREEN);
		prefix = prefix.replace("&3", "" + ChatColor.DARK_AQUA);
		prefix = prefix.replace("&4", "" + ChatColor.DARK_RED);
		prefix = prefix.replace("&5", "" + ChatColor.DARK_PURPLE);
		prefix = prefix.replace("&6", "" + ChatColor.GOLD);
		prefix = prefix.replace("&7", "" + ChatColor.GRAY);
		prefix = prefix.replace("&8", "" + ChatColor.DARK_GRAY);
		prefix = prefix.replace("&9", "" + ChatColor.BLUE);
		prefix = prefix.replace("&a", "" + ChatColor.GREEN);
		prefix = prefix.replace("&b", "" + ChatColor.AQUA);
		prefix = prefix.replace("&c", "" + ChatColor.RED);
		prefix = prefix.replace("&d", "" + ChatColor.LIGHT_PURPLE);
		prefix = prefix.replace("&e", "" + ChatColor.YELLOW);
		prefix = prefix.replace("&f", "" + ChatColor.WHITE);
		prefix = prefix.replace("&r", "" + ChatColor.RESET);
		prefix = prefix.replace("&l", "" + ChatColor.BOLD);
		prefix = prefix.replace("&i", "" + ChatColor.ITALIC);
		prefix = prefix.replace("&m", "" + ChatColor.MAGIC);
		return prefix;
                */
		 return ChatColor.translateAlternateColorCodes('&', prefix);
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

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
    private Boolean setupProtocol(){
    	try {
			this.protocolLib = true;
			this.protocolManager = ProtocolLibrary.getProtocolManager();
			((ProtocolManager)this.protocolManager).addPacketListener(new PacketAdapter(plugin,
			        ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL, 
			        0x1b) {
			    @Override
			    public void onPacketReceiving(PacketEvent event) {
			        if (event.getPacketID() == 0x1b) {
			            PacketContainer packet = event.getPacket();	
			            float sideways = packet.getFloat().read(0);
			            float forwards = packet.getFloat().read(1);  
			            new MotionManager(event.getPlayer(), forwards, sideways);
			        }
			    }
			});
		} catch (Exception e) {
			return false;
		}
    	return true;
    }
	public void onEnable() {
		plugin = this;
		File langFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		if (langFile.exists() == false
				|| langFile.length() < 1) {
			try {
				langFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}
			
		}
		try {
			lang.load(langFile);
		} catch (Exception e1) {
			getLogger().info("Error creating/loading lang file!");
		}
		if (new File(getDataFolder().getAbsolutePath() + File.separator
				+ "config.yml").exists() == false
				|| new File(getDataFolder().getAbsolutePath() + File.separator
						+ "config.yml").length() < 1) {
			// YamlConfiguration newC = new YamlConfiguration();
			// newC.set("time.created", System.currentTimeMillis());
			File configFile = new File(getDataFolder().getAbsolutePath()
					+ File.separator + "config.yml");
			try {
				configFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}
			copy(getResource("ucarsConfigHeader.yml"), configFile);
		}

		config = getConfig();
		try {
			// config.load(this.getDataFolder().getAbsolutePath() +
			// File.separator + "config.yml");
			if (!config.contains("general.cars.# description")) {
				config.set("general.cars.# description",
						"If enabled this will allow for drivable cars(Minecarts not on rails)");
			}
			if(!lang.contains("lang.messages.place")){
				lang.set("lang.messages.place", "&eYou placed a car! Cars can be driven with similar controls to a horse!");
			}
			if(!lang.contains("lang.error.pluginNull")){
				lang.set("lang.error.pluginNull", "&4Error in ucars: Caused by: plugin = null? Report on bukkitdev immediately!");
			}
			if(!lang.contains("lang.messages.noDrivePerm")){
				lang.set("lang.messages.noDrivePerm", "You don't have the permission ucars.cars required to drive a car!");
			}
			if(!lang.contains("lang.messages.noPlacePerm")){
				lang.set("lang.messages.noPlacePerm", "You don't have the permission %perm% required to place a car!");
			}
			if(!lang.contains("lang.messages.noPlaceHere")){
				lang.set("lang.messages.noPlaceHere", "&4You are not allowed to place a car here!");
			}
			if(!lang.contains("lang.messages.hitByCar")){
				lang.set("lang.messages.hitByCar", "You were hit by a car!");
			}
			if(!lang.contains("lang.cars.remove")){
				lang.set("lang.cars.remove", "&e%amount%&a cars in world &e%world%&a were removed!");
			}
			if(!lang.contains("lang.boosts.already")){
				lang.set("lang.boosts.already", "&4Already boosting!");
			}
			if(!lang.contains("lang.boosts.low")){
				lang.set("lang.boosts.low", "Initiated low level boost!");
			}
			if(!lang.contains("lang.boosts.med")){
				lang.set("lang.boosts.med", "Initiated medium level boost!");
			}
			if(!lang.contains("lang.boosts.high")){
				lang.set("lang.boosts.high", "Initiated high level boost!");
			}
			if(!lang.contains("lang.fuel.empty")){
				lang.set("lang.fuel.empty", "You don't have any fuel left!");
			}
			if(!lang.contains("lang.fuel.disabled")){
				lang.set("lang.fuel.disabled", "Fuel is not enabled!");
			}
			if(!lang.contains("lang.fuel.unit")){
				lang.set("lang.fuel.unit", "litres");
			}
			if(!lang.contains("lang.fuel.isItem")){
				lang.set("lang.fuel.isItem", "&9[Important:]&eItem fuel is enabled-The above is irrelevant!");
			}
			if(!lang.contains("lang.fuel.invalidAmount")){
				lang.set("lang.fuel.invalidAmount", "Amount invalid!");
			}
			if(!lang.contains("lang.fuel.noMoney")){
				lang.set("lang.fuel.noMoney", "You have no money!");
			}
			if(!lang.contains("lang.fuel.notEnoughMoney")){
				lang.set("lang.fuel.notEnoughMoney", "That purchase costs %amount% %unit%! You only have %balance% %unit%!");
			}
			if(!lang.contains("lang.fuel.success")){
				lang.set("lang.fuel.success", "Successfully purchased %quantity% of fuel for %amount% %unit%! You now have %balance% %unit% left!");
			}
			if(!lang.contains("lang.messages.rightClickWith")){
				lang.set("lang.messages.rightClickWith", "Right click with ");
			}
			if(!lang.contains("lang.messages.driveOver")){
				lang.set("lang.messages.driveOver", "Drive over ");
			}
			if(!lang.contains("lang.messages.playersOnly")){
				lang.set("lang.messages.playersOnly", "Players only!");
			}
			if(!lang.contains("lang.messages.reload")){
				lang.set("lang.messages.reload", "The config has been reloaded!");
			}
			if(!lang.contains("lang.messages.noProtocolLib")){
				lang.set("lang.messages.noProtocolLib", "Hello operator, ProtocolLib (http://dev.bukkit.org/bukkit-plugins/protocollib/) was not detected and is required for ucars in MC 1.6 or higher. Please install it if necessary!");
			}
			if (!config.contains("general.cars.enable")) {
				config.set("general.cars.enable", true);
			}
			if (!config.contains("general.permissions.enable")) {
				config.set("general.permissions.enable", true);
			}
			if (!config.contains("general.cars.defSpeed")) {
				config.set("general.cars.defSpeed", (double) 30);
			}
			if (!config.contains("general.cars.effectBlocks.enable")) {
				config.set("general.cars.effectBlocks.enable", true);
			}
			if (!config.contains("general.cars.lowBoost")) {
				config.set("general.cars.lowBoost", "263");
			}
			if (!config.contains("general.cars.medBoost")) {
				config.set("general.cars.medBoost", "265");
			}
			if (!config.contains("general.cars.highBoost")) {
				config.set("general.cars.highBoost", "264");
			}
			if (!config.contains("general.cars.blockBoost")) {
				config.set("general.cars.blockBoost", "41");
			}
			if (!config.contains("general.cars.HighblockBoost")) {
				config.set("general.cars.HighblockBoost", "57");
			}
			if (!config.contains("general.cars.ResetblockBoost")) {
				config.set("general.cars.ResetblockBoost", "133");
			}
			if (!config.contains("general.cars.turret")) {
				config.set("general.cars.turret", false);
			}
			if (!config.contains("general.cars.jumpBlock")) {
				config.set("general.cars.jumpBlock", "42");
			}
			if (!config.contains("general.cars.trafficLights.enable")) {
				config.set("general.cars.trafficLights.enable", true);
			}
			if (!config.contains("general.cars.trafficLights.waitingBlock")) {
				config.set("general.cars.trafficLights.waitingBlock", "155");
			}
			if (!config.contains("general.cars.hitBy.enable")) {
				config.set("general.cars.hitBy.enable", false);
			}
			if (!config.contains("general.cars.hitBy.enableMonsterDamage")) {
				config.set("general.cars.hitBy.enableMonsterDamage", true);
			}
			if (!config.contains("general.cars.hitBy.power")) {
				config.set("general.cars.hitBy.power", (double) 5);
			}
			if (!config.contains("general.cars.hitBy.damage")) {
				config.set("general.cars.hitBy.damage", (double) 1.5);
			}
			if (!config.contains("general.cars.roadBlocks.enable")) {
				config.set("general.cars.roadBlocks.enable", false);
			}
			if (!config.contains("general.cars.roadBlocks.ids")) {
				config.set("general.cars.roadBlocks.ids",
						"35:15,35:8,35:0,35:7");
			}
			if (!config.contains("general.cars.fuel.enable")) {
				config.set("general.cars.fuel.enable", false);
			}
			if (!config.contains("general.cars.fuel.price")) {
				config.set("general.cars.fuel.price", (double) 2);
			}
			if (!config.contains("general.cars.fuel.check")) {
				config.set("general.cars.fuel.check", "288:0");
			}
			if (!config.contains("general.cars.fuel.bypassPerm")) {
				config.set("general.cars.fuel.bypassPerm", "ucars.bypassfuel");
			}
			if (!config.contains("general.cars.fuel.items.enable")) {
				config.set("general.cars.fuel.items.enable", false);
			}
			if (!config.contains("general.cars.fuel.items.ids")) {
				config.set("general.cars.fuel.items.ids", "5,263:0,263:1");
			}
			if (!config.contains("general.cars.barriers")) {
				config.set("general.cars.barriers", "139,85,107,113");
			}
			if (!config.contains("general.cars.speedMods")) {
				config.set("general.cars.speedMods", "88:0-10,19:0-20");
			}
			if (!config.contains("general.cars.placePerm.enable")) {
				config.set("general.cars.placePerm.enable", false);
			}
			if (!config.contains("general.cars.placePerm.perm")) {
				config.set("general.cars.placePerm.perm", "ucars.place");
			}
			if (!config.contains("general.cars.health.default")) {
				config.set("general.cars.health.default", (double)10.0);
			}
			if (!config.contains("general.cars.health.underwaterDamage")) {
				config.set("general.cars.health.underwaterDamage", (double)0.0);
			}
			if (!config.contains("general.cars.health.lavaDamage")) {
				config.set("general.cars.health.lavaDamage", (double)0.0);
			}
			if (!config.contains("general.cars.health.cactusDamage")) {
				config.set("general.cars.health.cactusDamage", (double)0.0);
			}
			if (!config.contains("general.cars.health.crashDamage")) {
				config.set("general.cars.health.crashDamage", (double)0.0);
			}
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

		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		String idsraw = ucars.config
				.getString("general.cars.fuel.items.ids");
		String[] ids = idsraw.split(",");
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
				getCommand(k).setExecutor(new uCarsCommandExecutor(this));
			} catch (Exception e) {
				getLogger().log(Level.SEVERE,
						"Error registering command " + k.toString());
				e.printStackTrace();
			}
		}
		ucars.listener = new uCarsListener(null);
		getServer().getPluginManager().registerEvents(ucars.listener,
				this);
		if(getServer().getPluginManager().getPlugin("ProtocolLib")!=null){
			Boolean success = setupProtocol();
			if(!success){
				this.protocolLib = false;
				getLogger().log(Level.WARNING, "ProtocolLib (http://http://dev.bukkit.org/bukkit-plugins/protocollib/) was not found! For servers running MC 1.6 or above this is required for ucars to work!");	
			}
		}
		else{
			this.protocolLib = false;
			getLogger().log(Level.WARNING, "ProtocolLib (http://http://dev.bukkit.org/bukkit-plugins/protocollib/) was not found! For servers running MC 1.6 or above this is required for ucars to work!");	    
		}
		getLogger().info("uCars has been enabled!");
		return;
	}

	public void onDisable() {
		saveHashMap(fuel, plugin.getDataFolder().getAbsolutePath()
				+ File.separator + "fuel.bin");
		getLogger().info("uCars has been disabled!");
		return;
	}

	public Boolean isBlockEqualToConfigIds(String configPath, Block block) {
		// split by , then split by : then compare!
		String ids = config.getString(configPath);
		String[] rawids = ids.split(",");
		for (String raw : rawids) {
			String[] parts = raw.split(":");
			if (parts.length < 1) {
			} else if (parts.length < 2) {
				int id = Integer.parseInt(parts[0]);
				if (id == block.getTypeId()) {
					return true;
				}
			} else {
				int id = Integer.parseInt(parts[0]);
				int data = Integer.parseInt(parts[1]);
				int bdata = block.getData();
				if (id == block.getTypeId() && bdata == data) {
					return true;
				}
			}
		}
		return false;
	}
}
