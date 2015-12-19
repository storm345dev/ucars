package com.useful.ucars.controls;

import org.bukkit.entity.Player;

import com.useful.ucars.ucars;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class ControlSchemeManager {
	public static final String CONTROL_TYPE_META = "ucarsControlScheme";
	public static final String CONTROL_LOCK_META = "ucarsControlsLocked";
	
	public static ControlScheme getScheme(Player player){
		if(!UEntityMeta.hasMetadata(player, CONTROL_TYPE_META)){
			return ControlScheme.getDefault();
		}
		try {
			return (ControlScheme) UEntityMeta.getMetadata(player, CONTROL_TYPE_META).get(0).value();
		} catch (Exception e) {
			UEntityMeta.removeMetadata(player, CONTROL_TYPE_META);
			return ControlScheme.getDefault();
		}
	}
	
	public static void setControlScheme(Player player, ControlScheme scheme){
		UEntityMeta.removeMetadata(player, CONTROL_TYPE_META);
		UEntityMeta.setMetadata(player, CONTROL_TYPE_META, new StatValue(scheme, ucars.plugin));
	}
	
	public static ControlScheme toggleControlScheme(Player player){
		ControlScheme newScheme = getScheme(player).getNext();
		setControlScheme(player, newScheme);
		newScheme.showInfo(player);
		return newScheme;
	}
	
	public static boolean isControlsLocked(Player player){
		return UEntityMeta.hasMetadata(player, CONTROL_LOCK_META);
	}
	
	public static void setControlsLocked(Player player, boolean locked){
		UEntityMeta.removeMetadata(player, CONTROL_LOCK_META);
		if(locked){
			UEntityMeta.setMetadata(player, CONTROL_LOCK_META, new StatValue(null, ucars.plugin));
		}
	}
}
