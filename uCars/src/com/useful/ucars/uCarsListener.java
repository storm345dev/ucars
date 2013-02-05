package com.useful.ucars;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class uCarsListener implements Listener {
private ucars plugin;
	public uCarsListener(ucars plugin){
		this.plugin = ucars.plugin;
	}
public boolean isACar(Minecart cart){
	Location loc = cart.getLocation();
	float id = loc.getBlock().getTypeId();
	if(id == 27 || id == 66 || id == 28){
		return false;
	}
	return true;
}
public void ResetCarBoost(String playername, Minecart car, double defaultSpeed){
	String p = playername;
	World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
	w.playSound(plugin.getServer().getPlayer(p).getLocation(), Sound.BAT_TAKEOFF, 10, -2);
	ucars.carBoosts.put(p, defaultSpeed);
	return;
}
public boolean carBoost(String playerName, final double power, final long lengthMillis, double defaultSpeed){
	final String p = playerName;
	final double defMult = defaultSpeed;
	if(!ucars.carBoosts.containsKey(p)){
		ucars.carBoosts.put(p, (double)30);
	}
	final double Cur = ucars.carBoosts.get(p);
	if(Cur > defMult){
		//Already boosting!
		return false;
	}
	if(plugin == null){
		plugin.getLogger().log(Level.SEVERE , "Error in ucars: Caused by: plugin = null? Report on bukkitdev immediately!");
	}
//PLUGIN IS NULL??? //TODO
	plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
		public void run(){
			World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
			w.playSound(plugin.getServer().getPlayer(p).getLocation(), Sound.FIZZ, 10, -2);
			double speed = Cur + power;
			ucars.carBoosts.put(p, speed);
			//Boosting!
			try {
				Thread.sleep(lengthMillis);
			} catch (InterruptedException e) {
				ucars.carBoosts.put(p, defMult);
				return;
			}
			//paused for set time!
			ucars.carBoosts.put(p, defMult);
			//resumed normal speed!
			return;
		}
	});
	return true;
}

public boolean inACar(Player p){
	if(p == null){
		//Should NEVER happen(It means they r offline)
		return false;
	}
    if(!p.isInsideVehicle()){
    	return false;
    }
    Entity ent = p.getVehicle();
	if(!(ent instanceof Vehicle)){
		return false;
	}
	Vehicle veh = (Vehicle) ent;
	if(!(veh instanceof Minecart)){
		return false;
	}
	Minecart cart = (Minecart) veh;
	Location loc = cart.getLocation();
	float id = loc.getBlock().getTypeId();
	if(id == 27 || id == 66 || id == 28){
		return false;
	}
	return true;
}


@EventHandler
public void onVehicleUpdate(VehicleUpdateEvent event){
    Vehicle vehicle = event.getVehicle();
    Location under = vehicle.getLocation();
    		under.setY(vehicle.getLocation().getY() - 1);
    Block underblock = under.getBlock();
    Block underunderblock = underblock.getRelative(BlockFace.DOWN);
    Block normalblock = vehicle.getLocation().getBlock();
    Block up = normalblock.getLocation().add(0, 1, 0).getBlock();
    /*
    if(underblock.getTypeId() == 0 || underblock.getTypeId() == 10 || underblock.getTypeId() == 11 || underblock.getTypeId() == 8 || underblock.getTypeId() == 9 && underunderblock.getTypeId() == 0 || underunderblock.getTypeId() == 10 || underunderblock.getTypeId() == 11 || underunderblock.getTypeId() == 8 || underunderblock.getTypeId() == 9){
    	return;
    }
    */
    Entity passenger = vehicle.getPassenger();
    if (!(passenger instanceof Player)) {
      return;
    }
    

    Player player = (Player)passenger;
    	if (vehicle instanceof Minecart) {
    		if(!ucars.config.getBoolean("general.cars.enable")){
    			return;
    		}
    		Minecart car = (Minecart) vehicle;
    		// It is a valid car!
    		int blockBoostId = ucars.config.getInt("general.cars.blockBoost");
    		int tid = underblock.getTypeId();
    		if(tid == blockBoostId){
    			if(inACar(player)){
    				carBoost(player.getName(), 20, 6000, ucars.config.getDouble("general.cars.defSpeed"));
    			}
    		}
    		int HighblockBoostId = ucars.config.getInt("general.cars.HighblockBoost");
    		if(tid == HighblockBoostId){
    			if(inACar(player)){
    				carBoost(player.getName(), 50, 8000, ucars.config.getDouble("general.cars.defSpeed"));
    			}
    		}
    		int ResetblockBoostId = ucars.config.getInt("general.cars.ResetblockBoost");
    		if(tid == ResetblockBoostId){
    			if(inACar(player)){
    				ResetCarBoost(player.getName(), car, ucars.config.getDouble("general.cars.defSpeed"));
    			}
    		}
    		Location loc = car.getLocation();
    		Vector playerVelocity = car.getPassenger().getVelocity();
    		double defMultiplier = ucars.config.getDouble("general.cars.defSpeed");
    		double multiplier = 30;
    		if(!ucars.carBoosts.containsKey(player.getName())){
    			ucars.carBoosts.put(player.getName(), defMultiplier);
    		}
    		multiplier = ucars.carBoosts.get(player.getName());
    		double maxSpeed = 5;
    		Vector Velocity = playerVelocity.multiply(multiplier);
    		if(loc.getBlock().getTypeId() == 27 || loc.getBlock().getTypeId() == 27 || loc.getBlock().getTypeId() == 66){
    			return;
    		}
    		if(!(player.isInsideVehicle())){
    			return;
    		}
    		if(car.getLocation().add(0, -1, 0).getBlock().getTypeId() == 27 || car.getLocation().add(0, -1, 0).getBlock().getTypeId() == 28 || car.getLocation().add(0, -1, 0).getBlock().getTypeId() == 66){
    			return;	
    		}
    		if(ucars.config.getBoolean("general.permissions.enable")){
    		if(!player.hasPermission("ucars.cars")){
    			player.sendMessage(ucars.colors.getInfo() + "You don't have the permission ucars.cars required to drive a car!");
    			return;
    		}
    		}
    		if(normalblock.getTypeId() != 0 && normalblock.getTypeId() != 8 && normalblock.getTypeId() != 9 && normalblock.getTypeId() != 44 && normalblock.getTypeId() != 43 && normalblock.getTypeId() != 70 && normalblock.getTypeId() != 72 && normalblock.getTypeId() != 31){
    			car.setVelocity(new Vector(-0.5, 0, -0.5));
    	    	//player.getWorld().createExplosion(loc, 0);
    	    }
    		if(up.getTypeId() != 0 && up.getTypeId() != 8 && up.getTypeId() != 9 && up.getTypeId() != 44 && up.getTypeId() != 43){
    			car.setVelocity(new Vector(-0.5, 0, -0.5));
    	    	//player.getWorld().createExplosion(loc, 0);
    	    }
    		if(playerVelocity.getX() == 0 && playerVelocity.getZ() == 0){
    			return;
    		}
    		car.setMaxSpeed(maxSpeed);
    		Location before = car.getLocation();
    		float dir = (float)player.getLocation().getYaw();
    		BlockFace faceDir = ClosestFace.getClosestFace(dir);
    		int modX = faceDir.getModX() * 1;
    		int modY = faceDir.getModY() * 1;
    		int modZ = faceDir.getModZ() * 1;
    		before.add(modX, modY, modZ);
    		Block block = before.getBlock();
		    //Block block = car.getLocation().getBlock().getRelative(faceDir );
    		//Block block = normalblock.getRelative(modX, modY, modZ);
    		//Block block = player.getTargetBlock(null, 1);
    		int bid = block.getTypeId();
    		if(block.getY() == under.getBlockY() || block.getY() > normalblock.getY()){
    			//On the floor or too high to jump
    			if(bid == 0 || bid == 10 || bid == 11 || bid == 8 || bid == 9 || bid == 139 || bid == 85 || bid == 107 || bid == 113 || bid == 70 || bid == 72){
    			car.getLocation().setYaw(dir);
    		    car.setVelocity(Velocity);
    			}
    			else if(block.getY() == under.getBlockY()){
    				car.getLocation().setYaw(dir);
        		    car.setVelocity(Velocity);
    			}
    			else{
    				return;//wall to high or on the floor
    			}
    			return;
    		}
		    Location theNewLoc = block.getLocation();
		    Location bidUpLoc = block.getLocation().add(0, 1, 0);
		    int bidU = bidUpLoc.getBlock().getTypeId();
		    if(bid != 0 && bid != 10 && bid != 11 && bid != 8 && bid != 9 && bid != 139 && bid != 85 && bid != 107 && bid != 113 && bid != 70 && bid != 72){
		    	if(bidU == 0 || bidU == 10 || bidU == 11 || bidU == 8 || bidU == 9 || bidU == 44 || bidU == 43){
		    		//if(block.getTypeId() == 44 || block.getTypeId() == 43){
		    theNewLoc.add(0, 1.5d, 0);
		    	car.teleport(theNewLoc);
		    	}
		    }
		    else {
		    	car.getLocation().setYaw(dir);
    		    car.setVelocity(Velocity);
		    	//theNewLoc.add(0, 1d, 0);
		    }
			//player.getWorld().playEffect(exhaust, Effect.SMOKE, 1);
    	}
    	
    	return;
}




@EventHandler
void interact(PlayerInteractEvent event){
	if(!(event.getAction() == Action.RIGHT_CLICK_BLOCK)){
		return;
	}
	Block block = event.getClickedBlock();
	if(event.getPlayer().getItemInHand().getTypeId() == 328){
		//Its a minecart!
		int iar = block.getTypeId();
		if(iar == 66 || iar == 28 || iar == 27){
			return;
		}
		if(!ucars.config.getBoolean("general.cars.enable")){
			return;
		}
		Location loc = block.getLocation().add(0, 1, 0);
		Entity ent = event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
	    Minecart car = (Minecart) ent;
	    Location carloc = car.getLocation();
	    carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
	    car.setVelocity(new Vector(0,0,0));
	    car.teleport(carloc);
	    car.setVelocity(new Vector(0,0,0));
		event.getPlayer().sendMessage(plugin.colors.getInfo() + "You placed a car! Cars can be driven with similar controls to a boat!");
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
			event.getPlayer().getInventory().removeItem(new ItemStack(328));
		}
	}
	int LowBoostId = ucars.config.getInt("general.cars.lowBoost");
	int MedBoostId = ucars.config.getInt("general.cars.medBoost");
	int HighBoostId = ucars.config.getInt("general.cars.highBoost");
	float bid = event.getPlayer().getItemInHand().getTypeId(); // booster id
	if(bid == LowBoostId){
		if(inACar(event.getPlayer())){
			boolean boosting = carBoost(event.getPlayer().getName(), 10, 3000, ucars.config.getDouble("general.cars.defSpeed"));
			if(boosting){
				if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
					// they r in survival
					event.getPlayer().getInventory().removeItem(new ItemStack(LowBoostId));
				}
				event.getPlayer().sendMessage(ucars.colors.getSuccess() + "Initiated low level boost!");
				return;
			}
			else {
				event.getPlayer().sendMessage(ucars.colors.getError() + "Already boosting!");
			}
			return;
		}
	}
	if(bid == MedBoostId){
		if(inACar(event.getPlayer())){
			boolean boosting = carBoost(event.getPlayer().getName(), 20, 6000, ucars.config.getDouble("general.cars.defSpeed"));
			if(boosting){
				if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
					// they r in survival
					event.getPlayer().getInventory().removeItem(new ItemStack(MedBoostId));
				}
				event.getPlayer().sendMessage(ucars.colors.getSuccess() + "Initiated medium level boost!");
				return;
			}
			else {
				event.getPlayer().sendMessage(ucars.colors.getError() + "Already boosting!");
			}
			return;
		}
	}
	if(bid == HighBoostId){
		if(inACar(event.getPlayer())){
			boolean boosting = carBoost(event.getPlayer().getName(), 50, 10000, ucars.config.getDouble("general.cars.defSpeed"));
			if(boosting){
				if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
					// they r in survival
					event.getPlayer().getInventory().removeItem(new ItemStack(HighBoostId));
				}
				event.getPlayer().sendMessage(ucars.colors.getSuccess() + "Initiated high level boost!");
				return;
			}
			else {
				event.getPlayer().sendMessage(ucars.colors.getError() + "Already boosting!");
			}
			return;
		}
	}
	
	return;
}

}
