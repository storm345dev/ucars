package com.useful.ucars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import com.useful.ucars.Colors;

public class ucars extends JavaPlugin {
	//added to github so users can see source code! :D
	public static HashMap<String, Double> carBoosts = new HashMap<String, Double>();
	public static ucars plugin;
	public static FileConfiguration config;
	public static Colors colors;
	public static String  colorise(String prefix){
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
	}
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
public void onEnable(){
	plugin = this;
	if(new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml").exists() == false){
		copy(getResource("config.yml"), new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml"));
	}
	config = getConfig();
	try{
		config.load(this.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
		if(!config.contains("general.cars.# description")) {
			config.set("general.cars.# description", "If enabled this will allow for drivable cars(Minecarts not on rails)");
			}
		if(!config.contains("general.cars.enable")) {
			config.set("general.cars.enable", true);
			}
		if(!config.contains("general.permissions.enable")) {
			config.set("general.permissions.enable", true);
			}
		if(!config.contains("general.cars.defSpeed")) {
			config.set("general.cars.defSpeed", (double)30);
			}
		if(!config.contains("general.cars.lowBoost")) {
			config.set("general.cars.lowBoost", 263);
			}
		if(!config.contains("general.cars.medBoost")) {
			config.set("general.cars.medBoost", 265);
			}
		if(!config.contains("general.cars.highBoost")) {
			config.set("general.cars.highBoost", 264);
			}
		if(!config.contains("general.cars.blockBoost")) {
			config.set("general.cars.blockBoost", 41);
			}
		if(!config.contains("general.cars.HighblockBoost")) {
			config.set("general.cars.HighblockBoost", 57);
			}
		if(!config.contains("general.cars.ResetblockBoost")) {
			config.set("general.cars.ResetblockBoost", 133);
			}
		if(!config.contains("colorScheme.success")) {
			config.set("colorScheme.success", "&a");
			}
        if(!config.contains("colorScheme.error")) {
			config.set("colorScheme.error", "&c");
			}
        if(!config.contains("colorScheme.info")) {
			config.set("colorScheme.info", "&e");
			}
        if(!config.contains("colorScheme.title")) {
			config.set("colorScheme.title", "&9");
			}
        if(!config.contains("colorScheme.tp")) {
			config.set("colorScheme.tp", "&5");
			}
	}
	catch (Exception e){
		//error
	}
	saveConfig();
	colors = new Colors(config.getString("colorScheme.success"), config.getString("colorScheme.error"), config.getString("colorScheme.info"), config.getString("colorScheme.title"), config.getString("colorScheme.title"));
	PluginDescriptionFile pldesc = plugin.getDescription();
    Map<String, Map<String, Object>> commands = pldesc.getCommands();
    Set<String> keys = commands.keySet();
    for(String k : keys){
    	try {
			getCommand(k).setExecutor(new uCarsCommandExecutor(this));
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Error registering command " + k.toString());
			e.printStackTrace();
		}
    }
    getServer().getPluginManager().registerEvents(new uCarsListener(null), this);
	getLogger().info("uCars has been enabled!");
	return;
}
public void onDisable(){
	getLogger().info("uCars has been disabled!");
	return;
}
}
