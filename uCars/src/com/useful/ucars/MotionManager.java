package com.useful.ucars;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;

public class MotionManager extends Event {

	public MotionManager(Player player, float f, float s){
		Vector vec = new Vector();
		if(!ucars.listener.inACar(player)){
			return;
		}
		Minecart car = (Minecart) player.getVehicle();
		Location loc = car.getLocation();
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
		double y = -0.4; //rough gravity of minecraft
	    if(forwards && !turning){ //Mouse controls please
	    	double x = plaD.getX() / 25;
	    	double z = plaD.getZ() / 25;
	    	vec = new Vector(x,y,z);
	    	ucarUpdateEvent event = new ucarUpdateEvent(car, vec);
	    	ucars.plugin.getServer().getPluginManager().callEvent(event);
	    	return;
	    }
	    if(!forwards && !turning){ //Mouse controls please
	    	double x = plaD.getX() / 25;
	    	double z = plaD.getZ() / 25;
	    	x = 0-x;
	    	z = 0-z;
	    	vec = new Vector(x,y,z);
	    	ucarUpdateEvent event = new ucarUpdateEvent(car, vec);
	    	ucars.plugin.getServer().getPluginManager().callEvent(event);
	    	return;
	    }
	    else{ //Do complicated vector math
	    	double x = 0;
	    	double z = 0;
	    	if(forwards){
	    		x = plaD.getX() / 25;
		    	z = plaD.getZ() / 25;
	    	}
	    	else if(!forwards){
	    		x = plaD.getX() / 25;
		    	z = plaD.getZ() / 25;
		    	x = 0-x;
		    	z = 0-z;	
	    	}
	    	else{
	    		x = plaD.getX();
		    	z = plaD.getZ();
	    	}
	    	if(side < 0){ //go left
	    		//double yaw  = ((player.getLocation().getYaw() + 90)  * Math.PI) / 180;
	    		//double pitch = ((0 + 90) * Math.PI) / 180;
	    		//x = Math.sin(pitch) * Math.cos(yaw);
	    		//z = Math.cos(pitch);
	    		vec = new Vector(x,y,z);
	    		ucarUpdateEvent event = new ucarUpdateEvent(car, vec);
		    	ucars.plugin.getServer().getPluginManager().callEvent(event);
	    	}
	    	else if(side > 0){ //go right
	    		//double yaw  = ((player.getLocation().getYaw() + 90)  * Math.PI) / 180;
	    		//double pitch = ((0 + 90) * Math.PI) / 180;
	    		//x = Math.sin(pitch) * Math.cos(yaw);
	    		//z = Math.cos(pitch);
	    		vec = new Vector(x,y,z);
	    		ucarUpdateEvent event = new ucarUpdateEvent(car, vec);
		    	ucars.plugin.getServer().getPluginManager().callEvent(event);
	    	}
	    	else { //should never happen
	    		return;
	    	}
	    	
	    }
	}
	
	@Override
	public HandlerList getHandlers() {
		return null;
	}
      
	

}
