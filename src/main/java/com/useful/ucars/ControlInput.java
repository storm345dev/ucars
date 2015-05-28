package com.useful.ucars;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucarsCommon.StatValue;

public class ControlInput {
	
	public static float getAccel(Player player){ //Returns a multiplier to multiply with the x and z of the movement vector so the car appears to accelerate smoothly
		if(!ucars.smoothDrive){ //Return "1" (No multiplier) if accelerating vehicles is disabled
			return 1;
		}
		float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			smooth = new SmoothMeta(accMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					smooth = new SmoothMeta(accMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				smooth = new SmoothMeta(accMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		smooth.updateAccelerationFactor(accMod); //Update onto the Acceleration meta (Which does all the calculation for smooth accelerating) what the API wants in terms of accelerating speed - Allows it to be dynamic
		
		return smooth.getFactor(); //Get the acceleration factor
	}
	
	public static void input(Minecart car, Vector travel, ucarUpdateEvent event){ //Take our inputted
		/*if(ucars.smoothDrive){
			float a = getAccel(event.getPlayer());
			travel.setX(travel.getX() * a);
			travel.setZ(travel.getZ() * a);
		}*/
		
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
