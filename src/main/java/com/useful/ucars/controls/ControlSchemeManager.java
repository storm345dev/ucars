package com.useful.ucars.controls;

import org.bukkit.entity.Player;

import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class ControlSchemeManager {
	public static final String CONTROL_TYPE_META = "ucarsControlScheme";
	
	public static ControlScheme getScheme(Player player){
		if(!player.hasMetadata(CONTROL_TYPE_META)){
			return ControlScheme.getDefault();
		}
		try {
			return (ControlScheme) player.getMetadata(CONTROL_TYPE_META).get(0).value();
		} catch (Exception e) {
			player.removeMetadata(CONTROL_TYPE_META, ucars.plugin);
			return ControlScheme.getDefault();
		}
	}
	
	public static void setControlScheme(Player player, ControlScheme scheme){
		player.removeMetadata(CONTROL_TYPE_META, ucars.plugin);
		player.setMetadata(CONTROL_TYPE_META, new StatValue(scheme, ucars.plugin));
	}
	
	public static ControlScheme toggleControlScheme(Player player){
		ControlScheme newScheme = getScheme(player).getNext();
		setControlScheme(player, newScheme);
		newScheme.showInfo(player);
		return newScheme;
	}
}
