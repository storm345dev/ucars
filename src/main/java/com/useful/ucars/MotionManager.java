package com.useful.ucars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.controls.ControlScheme;
import com.useful.ucars.controls.ControlSchemeManager;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class MotionManager {
	
	public static Vector rotateXZVector3dDegrees(Vector original, double degrees){
		double[] out = rotateVector2dRadians(original.getX(), original.getZ(), Math.toRadians(degrees));
		original.setX(out[0]);
		original.setZ(out[1]);
		return original;
	}
	
	public static double[] rotateVector2dDegrees(double x, double y, double degrees){
		return rotateVector2dRadians(x, y, Math.toRadians(degrees));
	}
	
	public static double[] rotateVector2dRadians(double x, double y, double radians){
	    double[] result = new double[2];
	    result[0] = x * Math.cos(radians) - y * Math.sin(radians);
	    result[1] = x * Math.sin(radians) + y * Math.cos(radians);
	    return result;
	}

	public static void move(Player player, float f, float s, boolean jumping) { // 'f' and 's' are values taken in by the vehicle control packet		
		Vector vec = new Vector();
		Entity ent = player.getVehicle();
		if (ent == null) {
			return;
		}
		while (!(ent instanceof Vehicle) && ent.getVehicle() != null) {
			ent = ent.getVehicle();
		}
		if(!ucars.listener.inACar(player)){
		}
		if (!ucars.listener.inACar(player) || !(ent instanceof Vehicle)) {
			return;
		}
		final Vehicle car = (Vehicle) ent;
		if(!player.equals(car.getPassenger())){
			return; //Only allow 1 driver
		}
		// Location loc = car.getLocation();
		// Vector carD = loc.getDirection();
		Vector plaD = player.getEyeLocation().getDirection();
		
		if(jumping){
			if(!UEntityMeta.hasMetadata(player, "ucarsToggleControls")){
				/*player.setMetadata("ucarsToggleControls", new StatValue(true, ucars.plugin));*/
				UEntityMeta.setMetadata(player, "ucarsToggleControls", new StatValue(true, ucars.plugin));
				if(ControlSchemeManager.isControlsLocked(player)){
					player.sendMessage(ucars.colors.getError()+"Cannot toggle control scheme right now! (It's been locked by another plugin)");
				}
				else {
					ControlSchemeManager.toggleControlScheme(player);
					if(!ucars.turningCircles && ControlSchemeManager.getScheme(player).equals(ControlScheme.KEYBOARD)){
						UEntityMeta.removeMetadata(car, "ucarsSteeringDir");
						UEntityMeta.setMetadata(car, "ucarsSteeringDir", new StatValue(plaD.clone().setY(0).normalize(), ucars.plugin));
					}
				}
			}
		}
		else { // !jumping
			if(UEntityMeta.hasMetadata(player, "ucarsToggleControls")){
				UEntityMeta.removeMetadata(player, "ucarsToggleControls");
			}
		}
		
		ControlScheme controls = ControlSchemeManager.getScheme(player);
		boolean keyboardSteering = controls.equals(ControlScheme.KEYBOARD);
		
		Vector carDirection = null;
		try {
			if(UEntityMeta.hasMetadata(car, "ucarsSteeringDir")/*car.hasMetadata("ucarsSteeringDir")*/){
				carDirection = (Vector) UEntityMeta.getMetadata(car, "ucarsSteeringDir")/*car.getMetadata("ucarsSteeringDir")*/.get(0).value();
			}
		} catch (Exception e) {
			carDirection = null;
		}
		if(carDirection == null){
			carDirection = car.getLocation().getDirection();
			//carDirection = plaD.clone().setY(0).normalize();
		}
		if(keyboardSteering || ucars.turningCircles){
			try {
				if(UEntityMeta.hasMetadata(car, "ucarsSteeringDir")){
					carDirection = (Vector) UEntityMeta.getMetadata(car, "ucarsSteeringDir").get(0).value();
				}
			} catch (Exception e) {
				carDirection = null;
			}
			if(carDirection == null){
				carDirection = car.getLocation().getDirection();
				//carDirection = plaD.clone().setY(0).normalize();
			}
		}
		
		CarDirection dir = CarDirection.NONE;
		
		if(f == 0 && !ucars.smoothDrive){
			return;
		}
		
		Location carLoc = car.getLocation().clone();
		boolean inAir = false;
		if(carLoc.getBlock().isEmpty() && carLoc.subtract(0, 1, 0).getBlock().isEmpty()) {
			inAir = true;
		}
		
		double len = car.getWidth() / 2;
		if(!car.getLocation().clone().add(len, -1, 0).getBlock().isEmpty() || !car.getLocation().clone().add(-len, -1, 0).getBlock().isEmpty() ||
				!car.getLocation().clone().add(0, -1, len).getBlock().isEmpty() || !car.getLocation().clone().add(0, -1, -len).getBlock().isEmpty() ||
				!car.getLocation().clone().add(len, -1, len).getBlock().isEmpty() || !car.getLocation().clone().add(len, -1, -len).getBlock().isEmpty() ||
				!car.getLocation().clone().add(-len, -1, len).getBlock().isEmpty() || !car.getLocation().clone().add(-len, -1, -len).getBlock().isEmpty()) { //Check area under car
			inAir = false;
		}
		if(ucars.smoothDrive && inAir){
			f = 0;
			s = 0;
		}
		else if(ucars.smoothDrive) {
			//Not in air
			ControlInput.setFirstAirTime(player, System.currentTimeMillis());
		}
		
		Boolean forwards = true; // if true, forwards, else backwards
		int side = 0; // -1=left, 0=straight, 1=right
		Boolean turning = false;
		if (f < 0) {
			dir = CarDirection.BACKWARDS;
			forwards = false;
		} else if (f > 0){
			dir = CarDirection.FORWARDS;
			forwards = true;
		}
		if (s > 0) {
			side = -1;
			turning = true;
		}
		if (s < 0) {
			side = 1;
			turning = true;
		}

		//player.sendMessage(""+ControlInput.getCurrentAccel(player)+" "+ControlInput.getCurrentDriveDir(player));
		
		long timeSinceOnGround = System.currentTimeMillis() - ControlInput.getFirstAirTime(player);
		
		double y = -0.1 + (-0.003*timeSinceOnGround); // rough gravity of minecraft
		if(y < -1){
			y = -1;
		}
		double d = 27;
		Boolean doDivider = false;
		Boolean doAction = false;
		double divider = 0.5; // x of the (1) speed
		double rotMod = uCarsAPI.getAPI().getMaxCarTurnAmountDegrees(car, 5);
		if (turning) {
			if (side < 0) {// do left action
				if(!keyboardSteering){
					doAction = true;
					UEntityMeta.setMetadata(car, "car.action", new StatValue(true, ucars.plugin));
				}
				else {
					carDirection = rotateXZVector3dDegrees(carDirection, ControlInput.getCurrentDriveDir(player).equals(CarDirection.BACKWARDS) ? rotMod : -rotMod);
				}
			} else if (side > 0) {// do right action
				if(!keyboardSteering){
					doDivider = true;
					UEntityMeta.setMetadata(car, "car.action", new StatValue(true, ucars.plugin));
				}
				else {
					carDirection = rotateXZVector3dDegrees(carDirection, ControlInput.getCurrentDriveDir(player).equals(CarDirection.BACKWARDS) ? -rotMod : rotMod);
				}
			}
		}
		if(!keyboardSteering && ucars.turningCircles && (!ucars.smoothDrive || !inAir)){
			//Rotate 'carDirection' vector according to where they're looking; max of rotMod degrees
			float pYaw = (float) Math.toDegrees(Math.atan2(plaD.getX() , -plaD.getZ())); //Calculate yaw from 'player direction' vector
			float cYaw = (float) Math.toDegrees(Math.atan2(carDirection.getX() , -carDirection.getZ())); //Calculate yaw from 'carDirection' vector
			/*if(ControlInput.getCurrentDriveDir(player).equals(CarDirection.BACKWARDS)*//* && ControlInput.getCurrentAccel(player) > 0*//*){
				pYaw += 180;
			}*/
			float yawDiff = pYaw - cYaw;
			if(yawDiff <= -180){
				yawDiff += 360;
			}
			else if(yawDiff > 180){
				yawDiff -= 360;
			}
			/*Bukkit.broadcastMessage(yawDiff+"");*/
			if(yawDiff < -rotMod){
				yawDiff = (float) -rotMod;
			}
			else if(yawDiff > rotMod){
				yawDiff = (float) rotMod;
			}
			carDirection = rotateXZVector3dDegrees(carDirection, yawDiff/*ControlInput.getCurrentDriveDir(player).equals(CarDirection.BACKWARDS) ? -yawDiff : yawDiff*/);
		}
		if(keyboardSteering || ucars.turningCircles){
			UEntityMeta.removeMetadata(car, "ucarsSteeringDir");
			UEntityMeta.setMetadata(car, "ucarsSteeringDir", new StatValue(carDirection.normalize(), ucars.plugin));
			plaD = carDirection.clone();
		}
		if (forwards) {
			double x = plaD.getX() / d;
			double z = plaD.getZ() / d;
			if (!doDivider) {
				if (UEntityMeta.hasMetadata(car, "car.braking")) {
					UEntityMeta.removeMetadata(car, "car.braking");
				}
			}
			if(!doAction){
				if (UEntityMeta.hasMetadata(car, "car.action")) {
					UEntityMeta.removeMetadata(car, "car.action");
				}
			}
			vec = new Vector(x, y, z);
			final ucarUpdateEvent event = new ucarUpdateEvent(car, vec, player, dir);
			event.setDoDivider(doDivider);
			event.setDivider(divider);
			final Vector v = vec;
			ucars.plugin.getServer().getScheduler()
					.runTask(ucars.plugin, new Runnable() {

						public void run() {
							ControlInput.input(car, v, event);
						}
					});
			return;
		}
		if (!forwards) {
			double x = plaD.getX() / d;
			double z = plaD.getZ() / d;
			if (!doDivider) {
				if (UEntityMeta.hasMetadata(car, "car.braking")) {
					UEntityMeta.removeMetadata(car, "car.braking");
				}
			}
			x = 0 - x;
			z = 0 - z;
			vec = new Vector(x, y, z);
			final Vector v = vec;
			final ucarUpdateEvent event = new ucarUpdateEvent(car, vec, player, dir);
			event.setDoDivider(doDivider);
			event.setDivider(divider);
			Bukkit.getScheduler().runTask(ucars.plugin, new Runnable(){

				@Override
				public void run() {
					ControlInput.input(car, v, event);
					return;
				}});
			return;
		}
	}

}
