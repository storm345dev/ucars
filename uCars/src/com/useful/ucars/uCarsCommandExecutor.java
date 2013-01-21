package com.useful.ucars;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class uCarsCommandExecutor implements CommandExecutor {
	private Plugin plugin;
public uCarsCommandExecutor(ucars instance){
	this.plugin = ucars.plugin;
}
@Override
public boolean onCommand(CommandSender sender, Command cmd, String commandLabel,
		String[] args) {
	if(cmd.getName().equalsIgnoreCase("ucars")){
		sender.sendMessage(ucars.colors.getInfo() + "Ucars -by storm345- is working!");
		sender.sendMessage(ucars.colors.getTitle() + "[Low Boost:]" + ucars.colors.getInfo() + "Right click with " + new ItemStack(ucars.config.getInt("general.cars.lowBoost")).getType().name());
		sender.sendMessage(ucars.colors.getTitle() + "[Medium Boost:]" + ucars.colors.getInfo() + "Right click with " + new ItemStack(ucars.config.getInt("general.cars.medBoost")).getType().name());
		sender.sendMessage(ucars.colors.getTitle() + "[High Boost:]" + ucars.colors.getInfo() + "Right click with " + new ItemStack(ucars.config.getInt("general.cars.highBoost")).getType().name());
		sender.sendMessage(ucars.colors.getTitle() + "[Medium block Boost:]" + ucars.colors.getInfo() + "Drive over " + new ItemStack(ucars.config.getInt("general.cars.blockBoost")).getType().name());
		sender.sendMessage(ucars.colors.getTitle() + "[High block Boost:]" + ucars.colors.getInfo() + "Drive over " + new ItemStack(ucars.config.getInt("general.cars.HighblockBoost")).getType().name());
		sender.sendMessage(ucars.colors.getTitle() + "[Reset block Boost:]" + ucars.colors.getInfo() + "Drive over " + new ItemStack(ucars.config.getInt("general.cars.ResetblockBoost")).getType().name());
		sender.sendMessage(ucars.colors.getTitle() + "[Default speed:]" + ucars.colors.getInfo() + ucars.config.getDouble("general.cars.defSpeed"));
		return true;
	}
	return false;
}
}
