package com.useful.ucars;

import java.io.File;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class uCarsCommandExecutor implements CommandExecutor {
	private Plugin plugin;

	public uCarsCommandExecutor(ucars instance) {
		this.plugin = ucars.plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ucars")) {
			sender.sendMessage(ucars.colors.getInfo() + "Ucars v"
					+ plugin.getDescription().getVersion()
					+ " -by storm345- is working!");
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Low Boost:]"
					+ ucars.colors.getInfo()
					+ "Right click with "
					+ ucars.config.getString("general.cars.lowBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Medium Boost:]"
					+ ucars.colors.getInfo()
					+ "Right click with "
					+ ucars.config.getString("general.cars.medBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[High Boost:]"
					+ ucars.colors.getInfo()
					+ "Right click with "
					+ ucars.config.getString("general.cars.highBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Medium block Boost:]"
					+ ucars.colors.getInfo()
					+ "Drive over "
					+ ucars.config.getString("general.cars.blockBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[High block Boost:]"
					+ ucars.colors.getInfo()
					+ "Drive over "
					+ ucars.config.getString("general.cars.HighblockBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Reset block Boost:]"
					+ ucars.colors.getInfo()
					+ "Drive over "
					+ ucars.config
							.getString("general.cars.ResetblockBoost"));
			sender.sendMessage(ucars.colors.getTitle()
					+ "[Jump block:]"
					+ ucars.colors.getInfo()
					+ "Drive over "
					+ ucars.config.getString("general.cars.jumpBlock"));
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
						+ ucars.config.getDouble("general.cars.fuel.items.ids"));
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("ufuel")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Players only");
				return true;
			}
			if (!ucars.config.getBoolean("general.cars.fuel.enable")) {
				sender.sendMessage(ucars.colors.getError()
						+ "Fuel is not enabled!");
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
						+ ucars.colors.getInfo() + fuel + " litres");
				if (ucars.config.getBoolean("general.cars.fuel.items.enable")) {
					sender.sendMessage(ucars.colors.getTitle() + "[Important:]"
							+ ucars.colors.getInfo()
							+ "Item fuel is enabled-The above is irrelevant!");
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
							+ "Amount invalid!");
					return true;
				}
				double fuel = 0;
				if (ucars.fuel.containsKey(sender.getName())) {
					fuel = ucars.fuel.get(sender.getName());
				}
				if (!(ucars.economy.hasAccount(sender.getName()))) {
					sender.sendMessage(ucars.colors.getError()
							+ "You have no money!");
					return true;
				}
				double cost = ucars.config.getDouble("general.cars.fuel.price");
				double value = cost * amount;
				EconomyResponse resp = ucars.economy.bankBalance(sender
						.getName());
				double bal = resp.balance;
				if (bal < value) {
					sender.sendMessage(ucars.colors.getError()
							+ "That purchase costs " + value + " "
							+ ucars.economy.currencyNamePlural()
							+ "! But you only have " + bal + " "
							+ ucars.economy.currencyNamePlural() + "!");
					return true;
				}
				ucars.economy.bankWithdraw(sender.getName(), value);
				fuel = fuel + amount;
				ucars.fuel.put(sender.getName(), fuel);
				ucars.saveHashMap(ucars.fuel, plugin.getDataFolder()
						.getAbsolutePath() + File.separator + "fuel.bin");
				sender.sendMessage(ucars.colors.getSuccess()
						+ "Successfully purchased " + amount + " of fuel for "
						+ value + " " + ucars.economy.currencyNamePlural()
						+ "! You now have " + bal + " "
						+ ucars.economy.currencyNamePlural() + "!");
				return true;
			} else {
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("reloaducars")) {
			plugin.onDisable();
			plugin.onEnable();
			plugin.onLoad();
			sender.sendMessage(ucars.colors.getInfo()
					+ "The config has been reloaded!");
			return true;
		}
		return false;
	}
}
