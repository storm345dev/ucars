package com.useful.ucars;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

public class MotionManager {

	public MotionManager(Player player, float f, float s){
		Vector vec = new Vector();
		if(!ucars.listener.inACar(player)){
			return;
		}
		Minecart car = (Minecart) player.getVehicle();
		//Location loc = car.getLocation();
		//Vector carD = loc.getDirection();
		Vector plaD = player.getEyeLocation().getDirection();
		if(f==0){
			return;
		}
		Boolean forwards = true; //if true, forwards, else backwards
		int side = 0; //-1=left, 0=straight, 1=right
		Boolean turning = false;
		if(f < 0){forwards=false;}else{forwards=true;}
		if(s>0){side=-1;turning=true;}if(s<0){side=1;turning=true;}
		double y = -0.35; //rough gravity of minecraft
		double d = 27;
		if(turning){
			if(side<0){//do left action
				if(ucars.config.getBoolean("general.cars.turret")){
				Vector arrowVel = plaD.clone();
				arrowVel.setY(-0.01);
				Boolean doArrow = true;
				if(!player.hasMetadata("firing")){
					if(player.getGameMode() != GameMode.CREATIVE){
			    if(player.getInventory().contains(Material.ARROW)){
			    player.getInventory().removeItem(new ItemStack(Material.ARROW,1));
			    }
			    else{
			    	doArrow = false;
			    }
					}
			    if(doArrow){
				player.getWorld().spawnArrow(car.getLocation().add(0, 0.7, 0), arrowVel, 2, 1);
				final String playername = player.getName();
				player.setMetadata("firing", new FixedMetadataValue(ucars.plugin,true));
				ucars.plugin.getServer().getScheduler().runTaskLater(ucars.plugin, new Runnable(){

					@Override
					public void run() {
						Player p = ucars.plugin.getServer().getPlayer(playername);
						if(p.hasMetadata("firing")){
						p.removeMetadata("firing", ucars.plugin);
						return;
						}
					}}, 10l);
			    }
				}
				}
				
		    }
			else if(side>0){//do right action
				
			}
		}
	    if(forwards){ //Mouse controls please
	    	double x = plaD.getX() / d;
	    	double z = plaD.getZ() / d;
	    	vec = new Vector(x,y,z);
	    	ucarUpdateEvent event = new ucarUpdateEvent(car, vec);
	    	ucars.plugin.getServer().getPluginManager().callEvent(event);
	    	return;
	    }
	    if(!forwards){ //Mouse controls please
	    	double x = plaD.getX() / d;
	    	double z = plaD.getZ() / d;
	    	x = 0-x;
	    	z = 0-z;
	    	vec = new Vector(x,y,z);
	    	ucarUpdateEvent event = new ucarUpdateEvent(car, vec);
	    	ucars.plugin.getServer().getPluginManager().callEvent(event);
	    	return;
	    }
	}

	

      
	

}
