package com.useful.ucars;

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucarsCommon.StatValue;

public class ControlInput {
	public static void input(Minecart car, Vector travel, ucarUpdateEvent event){
		uCarsAPI api = uCarsAPI.getAPI();
		StatValue controlScheme = api.getUcarMeta(ucars.plugin, "car.controls", car.getUniqueId());
		if(controlScheme == null && !ucars.forceRaceControls){
			//Default control scheme
			ucars.plugin.getServer().getPluginManager().callEvent(event);
			return;
		}
		else if (ucars.forceRaceControls || ((String)controlScheme.getValue()).equalsIgnoreCase("race")){
			//Use race oriented control scheme
			event.player = null; //Remove memory leak
			car.removeMetadata("car.vec", ucars.plugin); //Clear previous vector
			car.setMetadata("car.vec", new StatValue(event, ucars.plugin));
			return;
		}
	}
}
