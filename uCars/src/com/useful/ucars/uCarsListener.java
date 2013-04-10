package com.useful.ucars;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class uCarsListener implements Listener {
private ucars plugin;
	public uCarsListener(ucars plugin){
		this.plugin = ucars.plugin;
	}
	public boolean inACar(String playername){
		Player p = plugin.getServer().getPlayer(playername);
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
		if(id == 27 || id == 66 || id == 28 || id == 157){
			return false;
		}
		return true;
	}
public boolean isACar(Minecart cart){
	Location loc = cart.getLocation();
	float id = loc.getBlock().getTypeId();
	if(id == 27 || id == 66 || id == 28 || id == 157){
		return false;
	}
	return true;
}
public void ResetCarBoost(String playername, Minecart car, double defaultSpeed){
	String p = playername;
	World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
	w.playSound(plugin.getServer().getPlayer(p).getLocation(), Sound.BAT_TAKEOFF, 10, -2);
	if(ucars.carBoosts.containsKey(p)){
	ucars.carBoosts.remove(p);
	}
	return;
}
public boolean carBoost(String playerName, final double power, final long lengthMillis, double defaultSpeed){
	final String p = playerName;
	final double defMult = defaultSpeed;
	double Cur = defMult;
	if(ucars.carBoosts.containsKey(p)){
	Cur = ucars.carBoosts.get(p);
	}
	if(Cur > defMult){
		//Already boosting!
		return false;
	}
	final double current = Cur;
	if(plugin == null){
		plugin.getLogger().log(Level.SEVERE , "Error in ucars: Caused by: plugin = null? Report on bukkitdev immediately!");
	}
//PLUGIN IS NULL??? //TODO
	plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable(){
		public void run(){
			World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
			w.playSound(plugin.getServer().getPlayer(p).getLocation(), Sound.FIZZ, 10, -2);
			double speed = current + power;
			ucars.carBoosts.put(p, speed);
			//Boosting!
			try {
				Thread.sleep(lengthMillis);
			} catch (InterruptedException e) {
				ucars.carBoosts.remove(p);
				return;
			}
			//paused for set time!
			ucars.carBoosts.remove(p);
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
    //Block underunderblock = underblock.getRelative(BlockFace.DOWN);
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
    		
    		if(ucars.config.getBoolean("general.cars.roadBlocks.enable")){
    			Location loc = car.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
    			int id = loc.getBlock().getTypeId();
    			Boolean valid = false;
    			String idsRaw = ucars.config.getString("general.cars.roadBlocks.ids");
    			String[] array = idsRaw.split(",");
    			List<String> ids = new ArrayList<String>();
    			for(String tid:array){
    				ids.add(tid);
    			}
    			ids.add(ucars.config.getString("general.cars.blockBoost"));
    			ids.add(ucars.config.getString("general.cars.HighblockBoost"));
    			ids.add(ucars.config.getString("general.cars.ResetblockBoost"));
    			ids.add(ucars.config.getString("general.cars.jumpBlock"));
    			ids.add("0");
    			ids.add("10");
    			ids.add("11");
    			ids.add("8");
    			ids.add("9");
    			for(String tid:ids){
    				String[] parts = tid.split(":");
    				if(parts.length > 1){
    					if(Integer.parseInt(parts[0]) == id){
    						//is same block type
    						int data = Integer.parseInt(parts[1]);
    						int tdata = loc.getBlock().getData();
    						if(data == tdata){
    							valid = true;
    						}
    					}
    				}
    				else if(parts.length > 0){
    					if(Integer.parseInt(parts[0]) == id){
    						valid = true;
    					}
    				}
    			}
    			if(!valid){
    				return;
    			}
    		}
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
    		Vector cur = car.getVelocity();
    		//playerVelocity = playerVelocity.multiply(cur); //TODO velocity preservation
    		double defMultiplier = ucars.config.getDouble("general.cars.defSpeed");
    		double multiplier = defMultiplier;
    		String speedMods = ucars.config.getString("general.cars.speedMods");
    		String[] units = speedMods.split(",");
    		int underid = under.getBlock().getTypeId();
    		int underdata = under.getBlock().getData();
    		for(String unit:units){
    			String[] sections = unit.split("-");
    			String rawid = sections[0];
    			double mult = Double.parseDouble(sections[1]);
    			if(ItemStackFromId.equals(rawid, underid, underdata)){
    				multiplier = mult;
    			}
    		}
    		if(ucars.carBoosts.containsKey(player.getName())){
    		multiplier = ucars.carBoosts.get(player.getName());
    		}
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
    			car.setVelocity(new Vector(0, 1, 0));
    	    	//player.getWorld().createExplosion(loc, 0);
    	    }
    		if(up.getTypeId() != 0 && up.getTypeId() != 8 && up.getTypeId() != 9 && up.getTypeId() != 44 && up.getTypeId() != 43){
    			car.setVelocity(new Vector(0, 1, 0));
    	    	//player.getWorld().createExplosion(loc, 0);
    	    }
    		if(playerVelocity.getX() == 0 && playerVelocity.getZ() == 0){
    			return;
    		}
    		if(ucars.config.getBoolean("general.cars.fuel.enable") && !ucars.config.getBoolean("general.cars.fuel.items.enable")){
    			double fuel = 0;
    			if(ucars.fuel.containsKey(player.getName())){
    			fuel = ucars.fuel.get(player.getName());
    			}
    			if(fuel < 0.1){
    				player.sendMessage(ucars.colors.getError() + "You don't have any fuel left!");
    				return;
    			}
    			int amount = 0 + (int)(Math.random()*250);
    			if(amount == 10){
    				fuel = fuel - 0.1;
    				fuel = (double)Math.round(fuel*10)/10; 
    				ucars.fuel.put(player.getName(), fuel);
    			}
    		}
    		if(ucars.config.getBoolean("general.cars.fuel.enable") && ucars.config.getBoolean("general.cars.fuel.items.enable")){
    			//item fuel
    			double fuel = 0;
    			String idsraw = ucars.config.getString("general.cars.fuel.items.ids");
    			String[] ids = idsraw.split(",");
    			List<ItemStack> items = new ArrayList<ItemStack>();
    			for(String raw:ids){
    				ItemStack stack = ItemStackFromId.get(raw);
    				if(stack != null){
    					items.add(stack);
    				}
    			}
    			Inventory inv = player.getInventory();
    			for(ItemStack item:items){
    				if(inv.contains(item.getType(), 1)){
    					fuel = fuel + 0.1;
    				}
    			}
    			if(fuel < 0.1){
    				player.sendMessage(ucars.colors.getError() + "You don't have any fuel left!");
    				return;
    			}
    			int amount = 0 + (int)(Math.random()*150);
    			if(amount == 10){
    				//remove item
    				Boolean taken = false;
    				Boolean last = false;
    				int toUse = 0;
    				for(int i=0;i<inv.getContents().length;i++){
    					ItemStack item = inv.getItem(i);
    					Boolean ignore = false;
    					try {
							item.getTypeId();
						} catch (Exception e) {
							ignore = true;
						}
    					if(!ignore){
    					if(!taken){
    						for(ItemStack titem:items){
    							if(titem.getTypeId() == item.getTypeId()){
    								taken = true;
    								if(item.getAmount() < 2){
    									last = true;
    									toUse = i;
    								}
    								item.setAmount((item.getAmount()-1));
    							}
    						}
    					}
    					}
    				}
    				if(last){
    				inv.setItem(toUse, new ItemStack(Material.AIR));
    				}
    			}
    		}
    		if(Velocity.getY() < 0){
    			double newy = Velocity.getY() + 1d;
    			Velocity.setY(newy);
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
    		int bidData = block.getData();
    		int jumpBlock = ucars.config.getInt("general.cars.jumpBlock");
    		if(tid == jumpBlock){
    				double jumpAmount = ucars.config.getDouble("general.cars.jumpAmount");
    				double y = Velocity.getY() + jumpAmount;
       		     Velocity.setY(y);
       		     car.setVelocity(Velocity);
    			
    		}
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
		    Boolean cont = true;
		    String[] rawids = ucars.config.getString("general.cars.barriers").split(",");
		    for(String raw:rawids){
		    	if(ItemStackFromId.equals(raw, bid, bidData)){
		    		cont = false;
		    	}
		    }
		    List<String> ignoreJump = new ArrayList<String>();
		    ignoreJump.add("132"); //tripwires
		    ignoreJump.add("50"); //torches
		    ignoreJump.add("76"); //redstone torches
		    ignoreJump.add("75"); //redstone off torches
		    ignoreJump.add("93"); //repeater off
		    ignoreJump.add("94"); //repeater on
		    ignoreJump.add("149"); //comparator off
		    ignoreJump.add("106"); //vines
		    ignoreJump.add("31"); //Tall grass
		    ignoreJump.add("77"); //stone button
		    ignoreJump.add("143"); //wood button
		    ignoreJump.add("107"); //fence gate
		    ignoreJump.add("69"); //lever
		    ignoreJump.add("157"); //activator rail
		    ignoreJump.add("78"); //snow
		    ignoreJump.add("151"); //daylight detector
		    ignoreJump.add("63"); //sign
		    ignoreJump.add("68"); //sign on the side of a block
		    for(String raw:ignoreJump){
		    	if(ItemStackFromId.equals(raw, bid, bidData)){
		    		cont = false;
		    	}
		    }
		    //TODO have a list for grass, etc... so stop cars jumping
		    if(bid != 0 && bid != 10 && bid != 11 && bid != 8 && bid != 9 && bid != 139 && bid != 85 && bid != 107 && bid != 113 && bid != 70 && bid != 72 && cont){
		    	if(bidU == 0 || bidU == 10 || bidU == 11 || bidU == 8 || bidU == 9 || bidU == 44 || bidU == 43){
		    		//if(block.getTypeId() == 44 || block.getTypeId() == 43){
		    theNewLoc.add(0, 1.5d, 0);
		    double y = 10;
            if(block.getType() == Material.STEP || block.getType() == Material.DOUBLE_STEP){
           	 y = 5;
            }
            Boolean ignore = false;
		     if(car.getVelocity().getY() > 0){
		    	 ignore = true;
		     }
		     if(!ignore){
		     Velocity.setY(y);
		     }
		     car.setVelocity(Velocity);
		     
		    	//car.teleport(theNewLoc);
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

@EventHandler(priority = EventPriority.HIGHEST)
void safeFly(EntityDamageEvent event){
	if(!(event.getEntity() instanceof Player)){
		return;
	}
	Player p = (Player) event.getEntity();
	if(inACar(p.getName())){
		Vehicle veh = (Vehicle) p.getVehicle();
		Vector vel = veh.getVelocity();
		if(vel.getY() != (double)0){
			event.setCancelled(true);
		}
		
	}
	return;
}
@EventHandler
void hitByCar(VehicleEntityCollisionEvent event){
	if(!ucars.config.getBoolean("general.cars.hitBy.enable")){
		return;
	}
	Vehicle veh = event.getVehicle();
	if(!(veh instanceof Minecart)){
		return;
	}
	Minecart cart = (Minecart) veh;
	if(!isACar(cart)){
		return;
	}
	Entity ent = event.getEntity();
	if(!(ent instanceof Player)){
		return;
	}
	Player p = (Player) ent;
	if(inACar(p)){
		return;
	}
	if(cart.getPassenger() == null){
		return;
	}
	double x = cart.getVelocity().getX();
	double y = cart.getVelocity().getY();
    double z = cart.getVelocity().getZ();
    if(x < 0){
    	x = -x;
    }
    if(y < 0){
    	y = -y;
    }
    if(z < 0){
    	z = -z;
    }
	if(x < 0.3 && y <0.3 && z < 0.3){
		return;
	}
	double speed = (x*z)/2;
	double mult = ucars.config.getDouble("general.cars.hitBy.power")/5;
	p.setVelocity(cart.getVelocity().setY(0.5).multiply(mult));
	p.sendMessage(ucars.colors.getInfo()+"You were hit by a car!");
	double damage = ucars.config.getDouble("general.cars.hitBy.damage");
	p.damage((int) (damage*speed));
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
		if(ucars.config.getBoolean("general.cars.placePerm.enable")){
		    String perm = ucars.config.getString("general.cars.placePerm.perm");
		    if(!event.getPlayer().hasPermission(perm)){
		    	event.getPlayer().sendMessage(ucars.colors.getError() + "You do not have the "+perm+" permission needed to place cars!");
		    	return;
		    }
		}
		if(event.isCancelled()){
			event.getPlayer().sendMessage(ucars.colors.getError()+"You are not allowed to place a car here!");
			return;
		}
		Location loc = block.getLocation().add(0, 1, 0);
		loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
		event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
	    /*
	    Location carloc = car.getLocation();
	    carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
	    car.setVelocity(new Vector(0,0,0));
	    car.teleport(carloc);
	    car.setVelocity(new Vector(0,0,0));
	    */
		event.getPlayer().sendMessage(ucars.colors.getInfo() + "You placed a car! Cars can be driven with similar controls to a boat!");
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE){
			event.getPlayer().getInventory().removeItem(new ItemStack(328));
		}
	}
	if(inACar(event.getPlayer())){
	if(ucars.config.getBoolean("general.cars.fuel.enable")){
		String[] parts = ucars.config.getString("general.cars.fuel.check").split(":");
		int id = Integer.parseInt(parts[0]);
		int data = 0;
		Boolean hasdata = false;
		if(parts.length > 1){
			hasdata = true;
			data = Integer.parseInt(parts[1]);
		}
		if(event.getPlayer().getItemInHand().getTypeId() == id){
			Boolean valid = true;
			if(hasdata){
				int tdata = ((int)event.getPlayer().getItemInHand().getData().getData());
				if(!(tdata == data)){
					valid = false;
				}
			}
			if(valid){
	            event.getPlayer().performCommand("ufuel view");
			}
		}
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
