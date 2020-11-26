package com.useful.ucars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class ControlInput {
	
	public static CarDirection getCurrentDriveDir(Player player){
		if(!ucars.smoothDrive){
			return CarDirection.FORWARDS;
		}
		float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		float decMod = uCarsAPI.getAPI().getDeceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			smooth = new SmoothMeta(accMod, decMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					smooth = new SmoothMeta(accMod, decMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				smooth = new SmoothMeta(accMod, decMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		return smooth.getDirection();
	}
	
	public static long getFirstAirTime(Player player){
		if(!ucars.smoothDrive){
			return System.currentTimeMillis();
		}
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
			float decMod = uCarsAPI.getAPI().getDeceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
			smooth = new SmoothMeta(accMod, decMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
					float decMod = uCarsAPI.getAPI().getDeceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
					smooth = new SmoothMeta(accMod, decMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
				float decMod = uCarsAPI.getAPI().getDeceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
				smooth = new SmoothMeta(accMod, decMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		return smooth.getFirstAirTime();
	}
	
	public static void setFirstAirTime(Player player, long time){
		if(!ucars.smoothDrive){ //Return "1" (No multiplier) if accelerating vehicles is disabled
			return;
		}
		
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
			float decMod = uCarsAPI.getAPI().getDeceleration(player, 1);
			smooth = new SmoothMeta(accMod, decMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
					float decMod = uCarsAPI.getAPI().getDeceleration(player, 1);
					smooth = new SmoothMeta(accMod, decMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
				float decMod = uCarsAPI.getAPI().getDeceleration(player, 1);
				smooth = new SmoothMeta(accMod, decMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		smooth.setFirstAirTime(time);
	}
	
	public static float getCurrentAccel(Player player){
		if(!ucars.smoothDrive){
			return 1;
		}
		float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		float decMod = uCarsAPI.getAPI().getDeceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			smooth = new SmoothMeta(accMod, decMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					smooth = new SmoothMeta(accMod, decMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				smooth = new SmoothMeta(accMod, decMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		return smooth.getCurrentSpeedFactor();
	}
	
	public static void setAccel(Player player, float accel){
		if(!ucars.smoothDrive){ //Return "1" (No multiplier) if accelerating vehicles is disabled
			return;
		}
		
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
			float decMod = uCarsAPI.getAPI().getDeceleration(player, 1);
			smooth = new SmoothMeta(accMod, decMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
					float decMod = uCarsAPI.getAPI().getDeceleration(player, 1);
					smooth = new SmoothMeta(accMod, decMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
				float decMod = uCarsAPI.getAPI().getDeceleration(player, 1);
				smooth = new SmoothMeta(accMod, decMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		smooth.setCurrentSpeedFactor(accel);
	}
	
	public static float getAccel(Player player, CarDirection dir){ //Returns a multiplier to multiply with the x and z of the movement vector so the car appears to accelerate smoothly
		if(!ucars.smoothDrive){ //Return "1" (No multiplier) if accelerating vehicles is disabled
			return 1;
		}
		float accMod = uCarsAPI.getAPI().getAcceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		float decMod = uCarsAPI.getAPI().getDeceleration(player, 1); //The multiplier to multiply our acceleration by from the API (Eg. another plugin can say "0.5" as the value here for accelerating at half the usual speed)
		SmoothMeta smooth = null; //Metadata saved to the player for tracking their acceleration
		if(!player.hasMetadata("ucars.smooth")){ //Setting the metadata onto the player if it's not already set
			smooth = new SmoothMeta(accMod, decMod);
			player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
		}
		else { //Metadata already set, lets attempt to read it
			try {
				Object o = player.getMetadata("ucars.smooth").get(0).value(); //Get the smooth meta set on the player
				if(o instanceof SmoothMeta){
					smooth = (SmoothMeta) o;
				}
				else { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
					smooth = new SmoothMeta(accMod, decMod);
					player.removeMetadata("ucars.smooth", ucars.plugin);
					player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
				}
			} catch (Exception e) { //Meta incorrectly set, plugin conflict? Just overwriting it with out own, correct, meta
				smooth = new SmoothMeta(accMod, decMod);
				player.removeMetadata("ucars.smooth", ucars.plugin);
				player.setMetadata("ucars.smooth", new StatValue(smooth, ucars.plugin));
			}
		}
		
		smooth.updateAccelerationFactor(accMod); //Update onto the Acceleration meta (Which does all the calculation for smooth accelerating) what the API wants in terms of accelerating speed - Allows it to be dynamic
		smooth.updateDecelerationFactor(decMod);
		
		return smooth.getFactor(dir); //Get the acceleration factor
	}
	
	public static void input(Entity car, Vector travel, ucarUpdateEvent event){ //Take our inputted
		/*if(ucars.smoothDrive){
			float a = getAccel(event.getPlayer());
			travel.setX(travel.getX() * a);
			travel.setZ(travel.getZ() * a);
		}*/
		
		uCarsAPI api = uCarsAPI.getAPI();
		StatValue controlScheme = api.getUcarMeta(ucars.plugin, "car.controls", car.getUniqueId());
		if(controlScheme == null && !ucars.forceRaceControls){
			//Default control scheme
			if(!ucars.fireUpdateEvent){
				ucars.listener.onUcarUpdate(event);
			}
			else {
				ucars.plugin.getServer().getPluginManager().callEvent(event);
			}
			return;
		}
		else if (ucars.forceRaceControls || ((String)controlScheme.getValue()).equalsIgnoreCase("race")){
			//Use race oriented control scheme
			event.player = null; //Remove memory leak
			car.removeMetadata("car.vec", ucars.plugin); //Clear previous vector
			UEntityMeta.removeMetadata(car, "car.vec");
			car.setMetadata("car.vec", new StatValue(event, ucars.plugin));
			UEntityMeta.setMetadata(car, "car.vec", new StatValue(event, ucars.plugin));
			return;
		}
	}
}
