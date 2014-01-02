package com.useful.ucars;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public class MotionManager {

	public static void move(Player player, float f, float s) {
		Vector vec = new Vector();
		Entity ent = player.getVehicle();
		if (ent == null) {
			return;
		}
		while (!(ent instanceof Minecart) && ent.getVehicle() != null) {
			ent = ent.getVehicle();
		}
		if(!ucars.listener.inACar(player)){
		}
		if (!ucars.listener.inACar(player) || !(ent instanceof Minecart)) {
			return;
		}
		Minecart car = (Minecart) ent;
		// Location loc = car.getLocation();
		// Vector carD = loc.getDirection();
		Vector plaD = player.getEyeLocation().getDirection();
		if (f == 0) {
			return;
		}
		Boolean forwards = true; // if true, forwards, else backwards
		int side = 0; // -1=left, 0=straight, 1=right
		Boolean turning = false;
		if (f < 0) {
			forwards = false;
		} else {
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
		double y = -0.35; // rough gravity of minecraft
		double d = 27;
		Boolean doDivider = false;
		Boolean doAction = false;
		double divider = 0.5; // x of the (1) speed
		if (turning) {
			if (side < 0) {// do left action
				doAction = true;
				car.setMetadata("car.action", new StatValue(true, ucars.plugin));
				/* Turrets removed
				if (ucars.config.getBoolean("general.cars.turret")) {
					Vector arrowVel = plaD.clone();
					arrowVel.setY(-0.01);
					Boolean doArrow = true;
					if (!player.hasMetadata("firing")) {
						if (player.getGameMode() != GameMode.CREATIVE) {
							if (player.getInventory().contains(Material.ARROW)) {
								player.getInventory().removeItem(
										new ItemStack(Material.ARROW, 1));
							} else {
								doArrow = false;
							}
						}
						if (doArrow) {
							player.getWorld().spawnArrow(
									car.getLocation().add(0, 0.7, 0), arrowVel,
									2, 1);
							final String playername = player.getName();
							player.setMetadata("firing",
									new FixedMetadataValue(ucars.plugin, true));
							ucars.plugin.getServer().getScheduler()
									.runTaskLater(ucars.plugin, new Runnable() {

										// @Override
										public void run() {
											Player p = ucars.plugin.getServer()
													.getPlayer(playername);
											if (p.hasMetadata("firing")) {
												p.removeMetadata("firing",
														ucars.plugin);
												return;
											}
										}
									}, 10l);
						}
					}
					
				}
				*/
			} else if (side > 0) {// do right action
				doDivider = true;
				car.setMetadata("car.braking",
						new StatValue(true, ucars.plugin));
			}
		}
		if (forwards) { // Mouse controls please
			double x = plaD.getX() / d;
			double z = plaD.getZ() / d;
			if (!doDivider) {
				if (car.hasMetadata("car.braking")) {
					car.removeMetadata("car.braking", ucars.plugin);
				}
			}
			if(!doAction){
				if(car.hasMetadata("car.action")){
					car.removeMetadata("car.action", ucars.plugin);
				}
			}
			vec = new Vector(x, y, z);
			final ucarUpdateEvent event = new ucarUpdateEvent(car, vec, player);
			event.setDoDivider(doDivider);
			event.setDivider(divider);
			ucars.plugin.getServer().getScheduler()
					.runTask(ucars.plugin, new Runnable() {

						public void run() {
							ucars.plugin.getServer().getPluginManager()
									.callEvent(event);
						}
					});
			return;
		}
		if (!forwards) { // Mouse controls please
			double x = plaD.getX() / d;
			double z = plaD.getZ() / d;
			if (!doDivider) {
				if (car.hasMetadata("car.braking")) {
					car.removeMetadata("car.braking", ucars.plugin);
				}
			}
			x = 0 - x;
			z = 0 - z;
			vec = new Vector(x, y, z);
			ucarUpdateEvent event = new ucarUpdateEvent(car, vec, player);
			event.setDoDivider(doDivider);
			event.setDivider(divider);
			ucars.plugin.getServer().getPluginManager().callEvent(event);
			return;
		}
	}

}
