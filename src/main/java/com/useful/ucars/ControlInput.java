package com.useful.ucars;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucarsCommon.StatValue;

public class ControlInput {
	
	private static float getAccel(Player player){
		if(!ucars.smoothDrive){
			return 1;
		}
		float accMod = uCarsAPI.getAPI().getAcceleration(player, 1);
		SmoothMeta smooth = null;
		if(!player.hasMetadata("ucars.smooth")){
			smooth = new SmoothMeta(accMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else {
			Object o = player.getMetadata("ucars.smooth").get(0).value();
			if(o instanceof SmoothMeta){
				smooth = (SmoothMeta) o;
			}
			else {
				smooth = new SmoothMeta(accMod);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		return smooth.getFactor();
	}
	
	public static void input(Minecart car, Vector travel, ucarUpdateEvent event){
		if(ucars.smoothDrive){
			float a = getAccel(event.getPlayer());
			travel.setX(travel.getX() * a);
			travel.setZ(travel.getZ() * a);
		}
		
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
