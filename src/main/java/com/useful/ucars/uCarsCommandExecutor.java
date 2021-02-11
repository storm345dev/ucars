package com.useful.ucars;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import com.useful.ucarsCommon.StatValue;

public class uCarsCommandExecutor implements CommandExecutor {
	private Plugin plugin;

	public uCarsCommandExecutor(ucars instance) {
		this.plugin = ucars.plugin;
	}

	// @Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ucars")) {
			sender.sendMessage(ucars.colors.getInfo() + "Ucars v"
					+ plugin.getDescription().getVersion()
					+ " -by storm345 (Bugfixes by Jakllp33)- is working!");
			sender.sendMessage(ucars.colors.getTitle() + "[Low Boost:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.rightClickWith")
					+ ucars.getIdList("general.cars.lowBoost"));
			sender.sendMessage(ucars.colors.getTitle() + "[Medium Boost:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.rightClickWith")
					+ ucars.getIdList("general.cars.medBoost"));
			sender.sendMessage(ucars.colors.getTitle() + "[High Boost:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.rightClickWith")
					+ ucars.getIdList("general.cars.highBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Medium block Boost:]" + ucars.colors.getInfo()
					+ Lang.get("lang.messages.driveOver")
					+ ucars.getIdList("general.cars.blockBoost"));
			sender.sendMessage(ucars.colors.getTitle() + "[High block Boost:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.driveOver")
					+ ucars.getIdList("general.cars.HighblockBoost"));
			sender.sendMessage(ucars.colors.getTitle() + "[Reset block Boost:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.driveOver")
					+ ucars.getIdList("general.cars.ResetblockBoost"));
			sender.sendMessage(ucars.colors.getTitle() + "[Jump block:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.driveOver")
					+ ucars.getIdList("general.cars.jumpBlock"));
			sender.sendMessage(ucars.colors.getTitle() + "[Teleport block:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.driveOver")
					+ ucars.getIdList("general.cars.teleportBlock"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Traffic light waiting block:]"
					+ ucars.colors.getInfo()
					+ Lang.get("lang.messages.driveOver")
					+ ucars.getIdList("general.cars.trafficLights.waitingBlock"));
			sender.sendMessage(ucars.colors.getTitle() + "[Default speed:]"
					+ ucars.colors.getInfo()
					+ ucars.config.getDouble("general.cars.defSpeed"));
			if (ucars.config.getBoolean("general.cars.fuel.enable")
					&& !ucars.config
							.getBoolean("general.cars.fuel.items.enable")) {
				sender.sendMessage(ucars.colors.getTitle()
						+ "[Fuel cost (Per litre):]" + ucars.colors.getInfo()
						+ ucars.config.getDouble("general.cars.fuel.price"));
			}
			if (ucars.config.getBoolean("general.cars.fuel.enable")
					&& ucars.config
							.getBoolean("general.cars.fuel.items.enable")) {
				sender.sendMessage(ucars.colors.getTitle() + "[Fuel items:]"
						+ ucars.colors.getInfo()
						+ ucars.getIdList("general.cars.fuel.items.ids"));
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("ufuel")) {
			if (!ucars.config.getBoolean("general.cars.fuel.enable")) {
				sender.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.fuel.disabled"));
				return true;
			}
			if (!sender.hasPermission(ucars.config
					.getString("general.cars.fuel.cmdPerm"))) {
				sender.sendMessage(ucars.colors.getError() + "No permission!");
				return true;
			}
			return ufuel(sender, args);
		} else if (cmd.getName().equalsIgnoreCase("reloaducars")) {
			// plugin.onDisable();
			ucars.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder()
						+ File.separator + "config.yml"));
			try {
				ucars.lang.load(new File(plugin.getDataFolder() + File.separator + "lang.yml"));
			} catch (Exception e1) {
				ucars.plugin.getLogger().log(Level.WARNING,
						"Error creating/loading lang file! Regenerating..");
			}
			
			ucars.listener.init();
			/*try {
				ucars.config.load(new File(plugin.getDataFolder()
						+ File.separator + "config.yml"));
			} catch (Exception e) {
				// Load config
				e.printStackTrace();
			} */
			// plugin.onEnable();
			// plugin.onLoad();
			sender.sendMessage(ucars.colors.getInfo()
					+ Lang.get("lang.messages.reload"));
			return true;
		} else if (cmd.getName().equalsIgnoreCase("cars")) {
			if (args.length < 1) {
				return false;
			}
			String action = args[0];
			if (action.equalsIgnoreCase("remove")) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(ucars.colors.getError()
							+ Lang.get("lang.messages.playersOnly"));
					return true;
				}
				Player player = (Player) sender;
				World world = player.getWorld();
				List<Entity> ents = world.getEntities();
				int removed = 0;
				for (Entity ent : ents) {
					if (ent instanceof Vehicle) {
						Vehicle cart = (Vehicle) ent;
						if (new uCarsListener(ucars.plugin).isACar(cart)) {
							ent.eject();
							if (ent.getPassenger() != null) {
								ent.getPassenger().eject();
							}
							ent.remove();
							removed++;
						}
					}
				}
				String success = Lang.get("lang.cars.remove");
				success = success.replaceAll("%world%", world.getName());
				success = success.replaceAll("%amount%", "" + removed);
				sender.sendMessage(ucars.colors.getSuccess() + success);
				return true;
			}
			return false;
		} else if (cmd.getName().equalsIgnoreCase("ulicense")) {
			if (!ucars.config.getBoolean("general.cars.licenses.enable") || !(sender instanceof Player)) {
				return true;
			}
			Player player = (Player) sender;
			int stage = 1;
			if (args.length > 0) {
				try {
					stage = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					return false;
				}
			}
			
			String noSkip = Lang.get("lang.licenses.nocheat");
			
			if (stage == 1) {
				sender.sendMessage(ucars.colors.getInfo()
						+ Lang.get("lang.licenses.basics"));
				String next = Lang.get("lang.licenses.next");
				next = next.replaceAll(Pattern.quote("%command%"),
						"/ulicense 2");
				sender.sendMessage(ucars.colors.getTitle() + next);
				player.setMetadata("ulicense1", new StatValue(true, ucars.plugin));
			} else if (stage == 2) {
				if(!player.hasMetadata("ulicense1")){
					noSkip = noSkip.replaceAll(Pattern.quote("%command%"), "/ulicense");
					sender.sendMessage(ucars.colors.getError()+noSkip);
					return true;
				}
				sender.sendMessage(ucars.colors.getInfo()
						+ Lang.get("lang.licenses.controls"));
				String next = Lang.get("lang.licenses.next");
				next = next.replaceAll(Pattern.quote("%command%"),
						"/ulicense 3");
				sender.sendMessage(ucars.colors.getTitle() + next);
				player.setMetadata("ulicense2", new StatValue(true, ucars.plugin));
			} else if (stage == 3) {
				if(!player.hasMetadata("ulicense2")){
					noSkip = noSkip.replaceAll(Pattern.quote("%command%"), "/ulicense 2");
					sender.sendMessage(ucars.colors.getError()+noSkip);
					return true;
				}
				sender.sendMessage(ucars.colors.getInfo()
						+ Lang.get("lang.licenses.effects"));
				String next = Lang.get("lang.licenses.next");
				next = next.replaceAll(Pattern.quote("%command%"),
						"/ulicense 4");
				sender.sendMessage(ucars.colors.getTitle() + next);
				player.setMetadata("ulicense3", new StatValue(true, ucars.plugin));
			} else if (stage == 4) {
				if(!player.hasMetadata("ulicense3")){
					noSkip = noSkip.replaceAll(Pattern.quote("%command%"), "/ulicense 3");
					sender.sendMessage(ucars.colors.getError()+noSkip);
					return true;
				}
				sender.sendMessage(ucars.colors.getInfo()
						+ Lang.get("lang.licenses.itemBoosts"));
				if (sender instanceof Player) {
					if (!((ucars) plugin).licensedPlayers.contains(sender
							.getName())) {
						sender.sendMessage(ucars.colors.getSuccess()
								+ Lang.get("lang.licenses.success"));
						((ucars) plugin).licensedPlayers.add(sender.getName());
						((ucars) plugin).licensedPlayers.save();
					}
				}
			} else {
				return false;
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("pigucart")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.messages.playersOnly"));
				return true;
			}
			Player player = (Player) sender;
			Location spawn = player.getLocation().add(0, 2, 0);
			Entity cart = player.getWorld().spawnEntity(spawn,
					EntityType.MINECART);
			if(args.length > 0 && player.isOp()){
				Material mat;
				try {
					mat = Material.valueOf(args[0]);
				} catch (Exception e) {
					mat = null;
				}
				if(mat != null && cart instanceof Minecart){
					((Minecart)cart).setDisplayBlock(new MaterialData(mat));
				}
			}
			else {
				Entity pig = player.getWorld().spawnEntity(spawn, EntityType.PIG);
				cart.setPassenger(pig);
				pig.setPassenger(player);
			}
			sender.sendMessage(ucars.colors.getSuccess() + "PiguCart!");
			return true;
		}
		return false;
	}

	public Boolean ufuel(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Lang.get("lang.messages.playersOnly"));
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		String action = args[0];
		if (action.equalsIgnoreCase("view")) {
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Fuel cost (Per litre):]" + ucars.colors.getInfo()
					+ ucars.config.getDouble("general.cars.fuel.price"));
			double fuel = 0;
			if (ucars.fuel.containsKey(sender.getName())) {
				fuel = ucars.fuel.get(sender.getName());
			}
			sender.sendMessage(ucars.colors.getTitle() + "[Your fuel:]"
					+ ucars.colors.getInfo() + fuel + " "
					+ Lang.get("lang.fuel.unit"));
			if (ucars.config.getBoolean("general.cars.fuel.items.enable")) {
				sender.sendMessage(ucars.colors.getTitle()
						+ Lang.get("lang.fuel.isItem"));
			}
			return true;
		} else if (action.equalsIgnoreCase("buy")) {
			if (args.length < 2) {
				return false;
			}
			double amount = 0;
			try {
				amount = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.fuel.invalidAmount"));
				return true;
			}
			double fuel = 0;
			if (ucars.fuel.containsKey(sender.getName())) {
				fuel = ucars.fuel.get(sender.getName());
			}
			double cost = ucars.config.getDouble("general.cars.fuel.price");
			double value = cost * amount;
			double bal = 0.0;
			try {
				bal = ucars.economy.getBalance(sender.getName());
			} catch (Exception e) {
				if (!ucars.plugin.setupEconomy()) {
					sender.sendMessage(ucars.colors.getError()
							+ "Error finding economy plugin");
					return true;
				} else {
					try {
						bal = ucars.economy.getBalance(sender.getName());
					} catch (Exception e1) {
						sender.sendMessage(ucars.colors.getError()
								+ "Error finding economy plugin");
						return true;
					}
				}
			}
			if (bal <= 0) {
				sender.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.fuel.noMoney"));
				return true;
			}
			if (bal < value) {
				String notEnough = Lang.get("lang.fuel.notEnoughMoney");
				notEnough = notEnough.replaceAll("%amount%", "" + value);
				notEnough = notEnough.replaceAll("%unit%",
						"" + ucars.economy.currencyNamePlural());
				notEnough = notEnough.replaceAll("%balance%", "" + bal);
				sender.sendMessage(ucars.colors.getError() + notEnough);
				return true;
			}
			ucars.economy.withdrawPlayer(sender.getName(), value);
			bal = bal - value;
			fuel = fuel + amount;
			ucars.fuel.put(sender.getName(), fuel);
			ucars.saveHashMap(ucars.fuel, plugin.getDataFolder()
					.getAbsolutePath() + File.separator + "fuel.bin");
			String success = Lang.get("lang.fuel.success");
			success = success.replaceAll("%amount%", "" + value);
			success = success.replaceAll("%unit%",
					"" + ucars.economy.currencyNamePlural());
			success = success.replaceAll("%balance%", "" + bal);
			success = success.replaceAll("%quantity%", "" + amount);
			sender.sendMessage(ucars.colors.getSuccess() + success);
			return true;
		} else if (action.equalsIgnoreCase("sell")) {
			if (!ucars.config.getBoolean("general.cars.fuel.sellFuel")) {
				sender.sendMessage(ucars.colors.getError()
						+ "Not allowed to sell fuel!");
				return true;
			}
			if (args.length < 2) {
				return false;
			}
			double amount = 0;
			try {
				amount = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.fuel.invalidAmount"));
				return true;
			}
			double fuel = 0;
			if (ucars.fuel.containsKey(sender.getName())) {
				fuel = ucars.fuel.get(sender.getName());
			}
			if ((fuel - amount) <= 0) {
				sender.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.fuel.empty"));
				return true;
			}
			double cost = ucars.config.getDouble("general.cars.fuel.price");
			double value = cost * amount;
			double bal = ucars.economy.getBalance(sender.getName());
			ucars.economy.depositPlayer(sender.getName(), value);
			bal = bal + value;
			fuel = fuel - amount;
			ucars.fuel.put(sender.getName(), fuel);
			ucars.saveHashMap(ucars.fuel, plugin.getDataFolder()
					.getAbsolutePath() + File.separator + "fuel.bin");
			String success = Lang.get("lang.fuel.sellSuccess");
			success = success.replaceAll("%amount%", "" + value);
			success = success.replaceAll("%unit%",
					"" + ucars.economy.currencyNamePlural());
			success = success.replaceAll("%balance%", "" + bal);
			success = success.replaceAll("%quantity%", "" + amount);
			sender.sendMessage(ucars.colors.getSuccess() + success);
			return true;
		} else {
			return false;
		}
	}
}
