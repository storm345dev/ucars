package com.useful.ucars;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public class uCarsListener implements Listener {
	private ucars plugin;
	private List<String> ignoreJump = null;

	public uCarsListener(ucars plugin) {
		this.plugin = ucars.plugin;
		ignoreJump = new ArrayList<String>();
		ignoreJump.add("132"); // tripwires
		ignoreJump.add("50"); // torches
		ignoreJump.add("76"); // redstone torches
		ignoreJump.add("75"); // redstone off torches
		ignoreJump.add("93"); // repeater off
		ignoreJump.add("94"); // repeater on
		ignoreJump.add("149"); // comparator off
		ignoreJump.add("106"); // vines
		ignoreJump.add("31"); // Tall grass
		ignoreJump.add("77"); // stone button
		ignoreJump.add("143"); // wood button
		ignoreJump.add("107"); // fence gate
		ignoreJump.add("69"); // lever
		ignoreJump.add("157"); // activator rail
		ignoreJump.add("78"); // snow
		ignoreJump.add("151"); // daylight detector
		ignoreJump.add("63"); // sign
		ignoreJump.add("68"); // sign on the side of a block
		ignoreJump.add("171"); // carpet
	}
    
	/*
	 * Performs on-tick calculations for if ucarsTrade is installed
	 */
	public Vector calculateCarStats(Minecart car, Player player, Vector velocity){
		if(!plugin.ucarsTrade){
			return velocity;
		}
		//TODO Get UcarsTrade to modify velocity
		return velocity;
	}
	
	/*
	 * Checks if a trafficlight sign is attached to the given block
	 */
	public boolean trafficlightSignOn(Block block) {
		if (block.getRelative(BlockFace.NORTH).getState() instanceof Sign) {
			Sign sign = (Sign) block.getRelative(BlockFace.NORTH).getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
					ChatColor.stripColor("[TrafficLight]"))) {
				return true;
			}
		} else if (block.getRelative(BlockFace.EAST).getState() instanceof Sign) {
			Sign sign = (Sign) block.getRelative(BlockFace.EAST).getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
					ChatColor.stripColor("[TrafficLight]"))) {
				return true;
			}
		} else if (block.getRelative(BlockFace.SOUTH).getState() instanceof Sign) {
			Sign sign = (Sign) block.getRelative(BlockFace.SOUTH).getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
					ChatColor.stripColor("[TrafficLight]"))) {
				return true;
			}
		} else if (block.getRelative(BlockFace.WEST).getState() instanceof Sign) {
			Sign sign = (Sign) block.getRelative(BlockFace.WEST).getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
					ChatColor.stripColor("[TrafficLight]"))) {
				return true;
			}
		} else if (block.getRelative(BlockFace.DOWN).getState() instanceof Sign) {
			Sign sign = (Sign) block.getRelative(BlockFace.DOWN).getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
					ChatColor.stripColor("[TrafficLight]"))) {
				return true;
			}
		} else if (block.getRelative(BlockFace.UP).getState() instanceof Sign) {
			Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
			if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
					ChatColor.stripColor("[TrafficLight]"))) {
				return true;
			}
		}
		return false;
	}
	/*
     * Checks if the specified player is inside a ucars (public
     * for traincarts support)
     */
	public boolean inACar(String playername) {
		Player p = plugin.getServer().getPlayer(playername);
		if (p == null) {
			// Should NEVER happen(It means they r offline)
			return false;
		}
		if (!p.isInsideVehicle()) {
			return false;
		}
		Entity ent = p.getVehicle();
		if (!(ent instanceof Vehicle)) {
			return false;
		}
		Vehicle veh = (Vehicle) ent;
		if (!(veh instanceof Minecart)) {
			return false;
		}
		Minecart cart = (Minecart) veh;
		Location loc = cart.getLocation();
		float id = loc.getBlock().getTypeId();
		if (id == 27 || id == 66 || id == 28 || id == 157) {
			return false;
		}
		return true;
	}
    
	/*
	 * Checks if a minecart is a car
	 * (Public for traincarts support)
	 */
	public boolean isACar(Minecart cart) {
		Location loc = cart.getLocation();
		float id = loc.getBlock().getTypeId();
		if (id == 27 || id == 66 || id == 28 || id == 157) {
			return false;
		}
		id = loc.getBlock().getRelative(BlockFace.DOWN).getTypeId();
		if (id == 27 || id == 66 || id == 28 || id == 157) {
			return false;
		}
		id = loc.getBlock().getRelative(BlockFace.DOWN, 2).getTypeId();
		if (id == 27 || id == 66 || id == 28 || id == 157) {
			return false;
		}
		return true;
	}
    /*
     * Resets any boosts the given car may have
     */
	public void ResetCarBoost(String playername, Minecart car,
			double defaultSpeed) {
		String p = playername;
		World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
		w.playSound(plugin.getServer().getPlayer(p).getLocation(),
				Sound.BAT_TAKEOFF, 10, -2);
		if (ucars.carBoosts.containsKey(p)) {
			ucars.carBoosts.remove(p);
		}
		return;
	}
	
    /*
     * Applies a boost to the car mentioned
     */
	public boolean carBoost(String playerName, final double power,
			final long lengthMillis, double defaultSpeed) {
		final String p = playerName;
		final double defMult = defaultSpeed;
		double Cur = defMult;
		if (ucars.carBoosts.containsKey(p)) {
			Cur = ucars.carBoosts.get(p);
		}
		if (Cur > defMult) {
			// Already boosting!
			return false;
		}
		final double current = Cur;
		if (plugin == null) {
			plugin.getLogger()
					.log(Level.SEVERE,
							Lang.get("lang.error.pluginNull"));
		}
		plugin.getServer().getScheduler()
				.runTaskAsynchronously(plugin, new Runnable() {
					public void run() {
						World w = plugin.getServer().getPlayer(p).getLocation()
								.getWorld();
						w.playSound(plugin.getServer().getPlayer(p)
								.getLocation(), Sound.FIZZ, 10, -2);
						double speed = current + power;
						ucars.carBoosts.put(p, speed);
						// Boosting!
						try {
							Thread.sleep(lengthMillis);
						} catch (InterruptedException e) {
							ucars.carBoosts.remove(p);
							return;
						}
						// paused for set time!
						ucars.carBoosts.remove(p);
						// resumed normal speed!
						return;
					}
				});
		return true;
	}
    /*
     * Checks if the specified player is inside a ucars (public
     * for traincarts support)
     */
	public boolean inACar(Player p) {
		if (p == null) {
			// Should NEVER happen(It means they r offline)
			return false;
		}
		if (!p.isInsideVehicle()) {
			return false;
		}
		Entity ent = p.getVehicle();
		if (!(ent instanceof Vehicle)) {
			return false;
		}
		Vehicle veh = (Vehicle) ent;
		if (!(veh instanceof Minecart)) {
			return false;
		}
		Minecart cart = (Minecart) veh;
		Location loc = cart.getLocation();
		float id = loc.getBlock().getTypeId();
		if (id == 27 || id == 66 || id == 28) {
			return false;
		}
		return true;
	}
    /*
     * Standardises the text on some effect signs
     */
	@EventHandler
	public void signWriter(SignChangeEvent event) {
		String[] lines = event.getLines();
		if (ChatColor.stripColor(lines[1]).equalsIgnoreCase("[TrafficLight]")) {
			lines[1] = "[TrafficLight]";
		}
		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[uFuel]")) {
			lines[0] = "[uFuel]";
		}
		if (ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Teleport]")) {
			lines[0] = "[Teleport]";
		}
		return;
	}
	/*
	 * Alert op's if no protocolLib found
	 */
    @EventHandler
    public void playerJoin(PlayerJoinEvent event){
    	
    	if(event.getPlayer().isOp()){
    		if(!plugin.protocolLib){
    			event.getPlayer().sendMessage(ucars.colors.getError()+Lang.get("lang.messages.noProtocolLib"));
    		}
    	}
    
    
    }
    /*
     * Performs on-vehicle-tick calculations(even when stationary) 
     * and also allows for old versions of minecraft AND ucars 
     * to access the new features through this 'bridge' (in theory)
     * But in practice they need protocol to get past the bukkit
     * dependency exception so it uses that anyway! 
     * (Kept for old version hybrids, eg. tekkti)
     */
    @EventHandler 
    public void tickCalcsAndLegacy(VehicleUpdateEvent event){
    	//start vehicleupdate mechs
    	Vehicle vehicle = event.getVehicle();
    	Entity passenger = vehicle.getPassenger();
    	Boolean driven = false;
    	if (!(passenger instanceof Player)) {
			driven = false;
		}
		Location under = vehicle.getLocation();
		under.setY(vehicle.getLocation().getY() - 1);
		// Block underunderblock = underblock.getRelative(BlockFace.DOWN);
		Block normalblock = vehicle.getLocation().getBlock();
		/*
		 * if(underblock.getTypeId() == 0 || underblock.getTypeId() == 10 ||
		 * underblock.getTypeId() == 11 || underblock.getTypeId() == 8 ||
		 * underblock.getTypeId() == 9 && underunderblock.getTypeId() == 0 ||
		 * underunderblock.getTypeId() == 10 || underunderblock.getTypeId() ==
		 * 11 || underunderblock.getTypeId() == 8 || underunderblock.getTypeId()
		 * == 9){ return; }
		 */
	    Player player = null;
		if(driven){
	    player = (Player) passenger;
		}
		if (vehicle instanceof Minecart) {
			if (!ucars.config.getBoolean("general.cars.enable")) {
				return;
			}
			
			Minecart car = (Minecart) vehicle;
    	Material carBlock = car.getLocation().getBlock().getType();
		if (carBlock == Material.WOOD_STAIRS
				|| carBlock == Material.COBBLESTONE_STAIRS
				|| carBlock == Material.BRICK_STAIRS
				|| carBlock == Material.SMOOTH_STAIRS
				|| carBlock == Material.NETHER_BRICK_STAIRS
				|| carBlock == Material.SANDSTONE_STAIRS
				|| carBlock == Material.SPRUCE_WOOD_STAIRS
				|| carBlock == Material.BIRCH_WOOD_STAIRS
				|| carBlock == Material.JUNGLE_WOOD_STAIRS
				|| carBlock == Material.QUARTZ_STAIRS) {
			Vector vel = car.getVelocity();
			vel.setY(0.4);
			car.setVelocity(vel);
		}
		final Minecart cart = (Minecart) vehicle;
		Runnable onDeath = new Runnable(){
			//@Override
			public void run(){
				cart.eject();
				Location loc = cart.getLocation();
				cart.remove();
				loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
			}
		};
		CarHealthData health = new CarHealthData(ucars.config.getDouble("general.cars.health.default"), onDeath, plugin);
		Boolean recalculateHealth = false;
		// It is a valid car!
		//START ON TICK CALCULATIONS
        if(car.hasMetadata("carhealth")){
        	List<MetadataValue> vals = car.getMetadata("carhealth");
        	for(MetadataValue val:vals){
        		if(val instanceof CarHealthData){
        			health = (CarHealthData) val;
        		}
        	}
        }
        if(driven){
        driven = inACar(((Player)passenger));
        }
        //Calculate health based on location
        if(normalblock.getType().equals(Material.WATER) || normalblock.getType().equals(Material.STATIONARY_WATER)){
        	double damage = ucars.config.getDouble("general.cars.health.underwaterDamage");
        	if(damage > 0){
        		if(driven){
        			double max = ucars.config.getDouble("general.cars.health.default");
		    	    double left = health.getHealth() - damage;
		    	    ChatColor color = ChatColor.YELLOW;
		    	    if(left > (max*0.66)){
		    	    	color = ChatColor.GREEN;
		    	    }
		    	    if(left < (max*0.33)){
		    	    	color = ChatColor.RED;
		    	    }
        		player.sendMessage(ChatColor.RED+"-"+damage+"["+Material.WATER.name().toLowerCase()+"]" + color + " ("+left+")");
        		}
        		health.damage(damage);
        		recalculateHealth = true;
        	}
        }
        if(normalblock.getType().equals(Material.LAVA) || normalblock.getType().equals(Material.STATIONARY_LAVA)){
        	double damage = ucars.config.getDouble("general.cars.health.lavaDamage");
        	if(damage > 0){
        		if(driven){
        			double max = ucars.config.getDouble("general.cars.health.default");
		    	    double left = health.getHealth() - damage;
		    	    ChatColor color = ChatColor.YELLOW;
		    	    if(left > (max*0.66)){
		    	    	color = ChatColor.GREEN;
		    	    }
		    	    if(left < (max*0.33)){
		    	    	color = ChatColor.RED;
		    	    }
        		player.sendMessage(ChatColor.RED+"-"+damage+"["+Material.LAVA.name().toLowerCase()+"]" + color + " ("+left+")");
        		}
        		health.damage(damage);
        		recalculateHealth = true;
        	}
        }
        if(recalculateHealth){
        	if(car.hasMetadata("carhealth")){
        		car.removeMetadata("carhealth", plugin);
        	}
        	car.setMetadata("carhealth", health);
        }
        //End health calculations
        if(!driven){
			return;
		}
        //Do calculations for when driven
        //END ON TICK CALCULATIONS
    	//end vehicleupdate mechs
    	//start legacy controls
    	if(plugin.protocolLib){ 
    		return;
    	}
    	//Attempt pre-1.6 controls
    	
			Vector playerVelocity = car.getPassenger().getVelocity();
			ucarUpdateEvent ucarupdate = new ucarUpdateEvent(car, playerVelocity);
			plugin.getServer().getPluginManager().callEvent(ucarupdate);
			return;
		}
    }
    /*
     * Performs the actually mechanic for making the cars move
     */
	@EventHandler
	public void onUcarUpdate(ucarUpdateEvent event) {
		Vehicle vehicle = event.getVehicle();
		Location under = vehicle.getLocation();
		under.setY(vehicle.getLocation().getY() - 1);
		Block underblock = under.getBlock();
		Block underunderblock = underblock.getRelative(BlockFace.DOWN);
		Block normalblock = vehicle.getLocation().getBlock();
		//Block up = normalblock.getLocation().add(0, 1, 0).getBlock();
		Entity passenger = vehicle.getPassenger();
		Player player = (Player) passenger;
		if (vehicle instanceof Minecart) {
			if (!ucars.config.getBoolean("general.cars.enable")) {
				return;
			}
			if(!((ucars)plugin).licensedPlayers.contains(player.getName()) && ucars.config.getBoolean("general.cars.licenses.enable")){
				player.sendMessage(ucars.colors.getError()+Lang.get("lang.licenses.noLicense"));
				return;
			}
			Minecart car = (Minecart) vehicle;
			final Minecart cart = (Minecart) vehicle;
			Runnable onDeath = new Runnable(){
				//@Override
				public void run(){
					cart.eject();
					Location loc = cart.getLocation();
					cart.remove();
					loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
				}
			};
			CarHealthData health = new CarHealthData(ucars.config.getDouble("general.cars.health.default"), onDeath, plugin);
			Boolean recalculateHealth = false;
			// It is a valid car!
			car.setMaxSpeed(5);
            if(car.hasMetadata("carhealth")){
            	List<MetadataValue> vals = car.getMetadata("carhealth");
            	for(MetadataValue val:vals){
            		if(val instanceof CarHealthData){
            			health = (CarHealthData) val;
            		}
            	}
            }
			if (ucars.config.getBoolean("general.cars.roadBlocks.enable")) {
				Location loc = car.getLocation().getBlock()
						.getRelative(BlockFace.DOWN).getLocation();
				int id = loc.getBlock().getTypeId();
				Boolean valid = false;
				String idsRaw = ucars.config
						.getString("general.cars.roadBlocks.ids");
				String[] array = idsRaw.split(",");
				List<String> ids = new ArrayList<String>();
				for (String tid : array) {
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
				for (String tid : ids) {
					String[] parts = tid.split(":");
					if (parts.length > 1) {
						if (Integer.parseInt(parts[0]) == id) {
							// is same block type
							int data = Integer.parseInt(parts[1]);
							int tdata = loc.getBlock().getData();
							if (data == tdata) {
								valid = true;
							}
						}
					} else if (parts.length > 0) {
						if (Integer.parseInt(parts[0]) == id) {
							valid = true;
						}
					}
				}
				if (!valid) {
					return;
				}
			}
			Location loc = car.getLocation();
			if (ucars.config.getBoolean("general.cars.trafficLights.enable")) {
				if (plugin.isBlockEqualToConfigIds(
						"general.cars.trafficLights.waitingBlock", underblock)
						|| plugin.isBlockEqualToConfigIds(
								"general.cars.trafficLights.waitingBlock",
								underunderblock)) {
					Boolean found = false;
					Boolean on = false;
					int radius = 3;
					int radiusSquared = radius * radius;
					for (int x = -radius; x <= radius && !found; x++) {
						for (int z = -radius; z <= radius && !found; z++) {
							if ((x * x) + (z * z) <= radiusSquared) {
								double locX = loc.getX() + x;
								double locZ = loc.getZ() + z;
								for (int y = (int) Math.round((loc.getY() - 3)); y < (loc
										.getY() + 4) && !found; y++) {
									Location light = new Location(
											loc.getWorld(), locX, y, locZ);
									if (light.getBlock().getType() == Material.REDSTONE_LAMP_OFF) {
										if (trafficlightSignOn(light.getBlock())) {
											found = true;
											on = false;
										}
									} else if (light.getBlock().getType() == Material.REDSTONE_TORCH_ON) {
										if (trafficlightSignOn(light.getBlock())) {
											found = true;
											on = true;
										}
									}
								}
							}
						}
					}
					if (found) {
						if (!on) {
							return;
						}
					}
				}
			}
			//Calculate default effect blocks
			if(ucars.config.getBoolean("general.cars.effectBlocks.enable")){
			if (plugin.isBlockEqualToConfigIds("general.cars.blockBoost",
					underblock)
					|| plugin.isBlockEqualToConfigIds(
							"general.cars.blockBoost", underunderblock)) {
				if (inACar(player)) {
					carBoost(player.getName(), 20, 6000,
							ucars.config.getDouble("general.cars.defSpeed"));
				}
			}
			if (plugin.isBlockEqualToConfigIds("general.cars.HighblockBoost",
					underblock)
					|| plugin.isBlockEqualToConfigIds(
							"general.cars.HighblockBoost", underunderblock)) {
				if (inACar(player)) {
					carBoost(player.getName(), 50, 8000,
							ucars.config.getDouble("general.cars.defSpeed"));
				}
			}
			if (plugin.isBlockEqualToConfigIds("general.cars.ResetblockBoost",
					underblock)
					|| plugin.isBlockEqualToConfigIds(
							"general.cars.ResetblockBoost", underunderblock)) {
				if (inACar(player)) {
					ResetCarBoost(player.getName(), car,
							ucars.config.getDouble("general.cars.defSpeed"));
				}
			}
			}
			Vector playerVelocity = event.getTravelVector(); //Travel Vector, fixes controls for 1.6
			double defMultiplier = ucars.config
					.getDouble("general.cars.defSpeed");
			double multiplier = defMultiplier;
			String speedMods = ucars.config.getString("general.cars.speedMods");
			String[] units = speedMods.split(",");
			int underid = under.getBlock().getTypeId();
			int underdata = under.getBlock().getData();
			//calculate speedmods
			for (String unit : units) {
				String[] sections = unit.split("-");
				String rawid = sections[0];
				double mult = Double.parseDouble(sections[1]);
				if (ItemStackFromId.equals(rawid, underid, underdata)) {
					multiplier = mult;
				}
			}
			if (ucars.carBoosts.containsKey(player.getName())) { //Use the boost allocated
				multiplier = ucars.carBoosts.get(player.getName());
			}
			if(event.getDoDivider()){ //Braking or going slower
			multiplier = multiplier * event.getDivider();
			}
			Vector Velocity = playerVelocity.multiply(multiplier);
			if (!(player.isInsideVehicle())) {
				return;
			}
			if (ucars.config.getBoolean("general.permissions.enable")) {
				if (!player.hasPermission("ucars.cars")) {
					player.sendMessage(ucars.colors.getInfo()
							+ Lang.get("lang.messages.noDrivePerm"));
					return;
				}
			}
			if (normalblock.getTypeId() != 0 && normalblock.getTypeId() != 8
					&& normalblock.getTypeId() != 9
					&& normalblock.getTypeId() != 44
					&& normalblock.getTypeId() != 43
					&& normalblock.getTypeId() != 70
					&& normalblock.getTypeId() != 72
					&& normalblock.getTypeId() != 31) {
				//Stuck in a block
				car.setVelocity(new Vector(0, 1, 0));
			}
			if (playerVelocity.getX() == 0 && playerVelocity.getZ() == 0) {
				//Not moving
				return;
			}
			//definitely moving somewhere!
			Location before = car.getLocation();
			float dir = (float) player.getLocation().getYaw();
			BlockFace faceDir = ClosestFace.getClosestFace(dir);
			int modX = faceDir.getModX() * 1;
			int modY = faceDir.getModY() * 1;
			int modZ = faceDir.getModZ() * 1;
			before.add(modX, modY, modZ);
			Block block = before.getBlock();
			//Calculate collision health
			if(block.getType().equals(Material.CACTUS)){
	        	double damage = ucars.config.getDouble("general.cars.health.cactusDamage");
	        	if(damage > 0){
	        		double max = ucars.config.getDouble("general.cars.health.default");
		    	    double left = health.getHealth() - damage;
		    	    ChatColor color = ChatColor.YELLOW;
		    	    if(left > (max*0.66)){
		    	    	color = ChatColor.GREEN;
		    	    }
		    	    if(left < (max*0.33)){
		    	    	color = ChatColor.RED;
		    	    }
	        		player.sendMessage(ChatColor.RED+"-"+damage+"["+Material.CACTUS.name().toLowerCase()+"]" + color + " ("+left+")");
	        		health.damage(damage);
	        		recalculateHealth = true;
	        	}
	        }
			//End calculations for collision health
			if (ucars.config.getBoolean("general.cars.fuel.enable")
					&& !ucars.config
							.getBoolean("general.cars.fuel.items.enable") && !player.hasPermission(ucars.config.getString("general.cars.fuel.bypassPerm"))) {
				double fuel = 0;
				if (ucars.fuel.containsKey(player.getName())) {
					fuel = ucars.fuel.get(player.getName());
				}
				if (fuel < 0.1) {
					player.sendMessage(ucars.colors.getError()
							+ Lang.get("lang.fuel.empty"));
					return;
				}
				int amount = 0 + (int) (Math.random() * 250);
				if (amount == 10) {
					fuel = fuel - 0.1;
					fuel = (double) Math.round(fuel * 10) / 10;
					ucars.fuel.put(player.getName(), fuel);
				}
			}
			if (ucars.config.getBoolean("general.cars.fuel.enable")
					&& ucars.config
							.getBoolean("general.cars.fuel.items.enable")) {
				// item fuel - Not for laggy servers!!!
				double fuel = 0;
				List<ItemStack> items = plugin.ufuelitems;
				Inventory inv = player.getInventory();
				for (ItemStack item : items) {
					if (inv.contains(item.getType(), 1)) {
						fuel = fuel + 0.1;
					}
				}
				if (fuel < 0.1) {
					player.sendMessage(ucars.colors.getError()
							+ Lang.get("lang.fuel.empty"));
					return;
				}
				int amount = 0 + (int) (Math.random() * 150);
				if (amount == 10) {
					// remove item
					Boolean taken = false;
					Boolean last = false;
					int toUse = 0;
					for (int i = 0; i < inv.getContents().length; i++) {
						ItemStack item = inv.getItem(i);
						Boolean ignore = false;
						try {
							item.getTypeId();
						} catch (Exception e) {
							ignore = true;
						}
						if (!ignore) {
							if (!taken) {
								for (ItemStack titem : items) {
									if (titem.getTypeId() == item.getTypeId()) {
										taken = true;
										if (item.getAmount() < 2) {
											last = true;
											toUse = i;
										}
										item.setAmount((item.getAmount() - 1));
									}
								}
							}
						}
					}
					if (last) {
						inv.setItem(toUse, new ItemStack(Material.AIR));
					}
				}
			}
			if (Velocity.getY() < 0) { //Fix falling into ground
				double newy = Velocity.getY() + 2d;
				Velocity.setY(newy);
			}
			int bid = block.getTypeId();
			int bidData = block.getData();
			Boolean fly = false; //Fly is the 'easter egg' slab elevator
			if(block.getRelative(faceDir).getTypeId()==44 && !(block.getRelative(faceDir).getData() != 0)){
				//If looking at slabs
				fly = true;
			}
			
			if(block.getTypeId()==44 && !(block.getData() != 0)){
				//If in a slab block
				fly = true;
			}
			if(ucars.config.getBoolean("general.cars.effectBlocks.enable")){
			if (plugin.isBlockEqualToConfigIds("general.cars.jumpBlock",
					underblock)
					|| plugin.isBlockEqualToConfigIds("general.cars.jumpBlock",
							underunderblock)) {
				double jumpAmount = ucars.config
						.getDouble("general.cars.jumpAmount");
				double y = Velocity.getY() + jumpAmount;
				Velocity.setY(y);
				car.setVelocity(Velocity);
			}
			}
			if(ucars.config.getBoolean("general.cars.effectBlocks.enable")){
				if (plugin.isBlockEqualToConfigIds("general.cars.teleportBlock",
						underblock)
						|| plugin.isBlockEqualToConfigIds("general.cars.teleportBlock",
								underunderblock)) {
					//teleport the player
					Sign s = null;
					if(underunderblock.getState() instanceof Sign){
						s = (Sign) underunderblock.getState();
					}
					if(underunderblock.getRelative(BlockFace.DOWN).getState() instanceof Sign){
						s = (Sign) underunderblock.getRelative(BlockFace.DOWN).getState();
					}
					if(s!=null){
					String[] lines = s.getLines();
					if(lines[0].equalsIgnoreCase("[Teleport]")){
						car.eject();
						car.remove();
						String xs = lines[1];
						String ys = lines[2];
						String zs = lines[3];
						Boolean valid = true;
						double x = 0,y = 0,z = 0;
						try {
							x = Double.parseDouble(xs);
							y = Double.parseDouble(ys);
							y = y+0.5;
							z = Double.parseDouble(zs);
						} catch (NumberFormatException e) {
							valid = false;
						}
						if(valid){
						Location toTele = new Location(s.getWorld(),x,y,z);
						car = (Minecart) s.getWorld().spawnEntity(toTele, EntityType.MINECART);
					    car.setMetadata("carhealth", health);
					    player.sendMessage(ucars.colors.getTp()+"Teleporting...");
					    car.setPassenger(player);
					    car.setVelocity(Velocity);
						}
					}
					}
				}
				}
			/* -OLD
			if (block.getY() == under.getBlockY()
					|| block.getY() > normalblock.getY()) {
				// On the floor or too high to jump
				if (bid == 0 || bid == 10 || bid == 11 || bid == 8 || bid == 9
						|| bid == 139 || bid == 85 || bid == 107 || bid == 113
						|| bid == 70 || bid == 72) { //excluded jump blocks
					car.setVelocity(Velocity);
				} else if (block.getY() == under.getBlockY()) {
					car.setVelocity(Velocity);
				} else {
					return;// wall too high or on the floor
				}
				return;
			}
			*/
			//actually jump
			Location theNewLoc = block.getLocation();
			Location bidUpLoc = block.getLocation().add(0, 1, 0);
			int bidU = bidUpLoc.getBlock().getTypeId();
			Boolean cont = true;
			//check it's not a barrier
			String[] rawids = ucars.config.getString("general.cars.barriers")
					.split(",");
			for (String raw : rawids) {
				if (ItemStackFromId.equals(raw, bid, bidData)) {
					cont = false;
				}
			}
			for (String raw : ignoreJump) { //Check it's not a non-jumping block
				if (ItemStackFromId.equals(raw, bid, bidData)) {
					cont = false;
				}
			}
			//a list for grass, etc... so stop cars jumping
			if (bid != 0 && bid != 10 && bid != 11 && bid != 8 && bid != 9
					&& bid != 139 && bid != 85 && bid != 107 && bid != 113
					&& bid != 70 && bid != 72 && cont) {
				if (bidU == 0 || bidU == 10 || bidU == 11 || bidU == 8
						|| bidU == 9 || bidU == 44 || bidU == 43) {
					theNewLoc.add(0, 1.5d, 0);
					double y = 10;
					if (block.getType() == Material.STEP
							|| block.getType() == Material.DOUBLE_STEP) {
						y = 5;
					}
					Material carBlock = car.getLocation().getBlock().getType();
					if (carBlock == Material.WOOD_STAIRS
							|| carBlock == Material.COBBLESTONE_STAIRS
							|| carBlock == Material.BRICK_STAIRS
							|| carBlock == Material.SMOOTH_STAIRS
							|| carBlock == Material.NETHER_BRICK_STAIRS
							|| carBlock == Material.SANDSTONE_STAIRS
							|| carBlock == Material.SPRUCE_WOOD_STAIRS
							|| carBlock == Material.BIRCH_WOOD_STAIRS
							|| carBlock == Material.JUNGLE_WOOD_STAIRS
							|| carBlock == Material.QUARTZ_STAIRS) {
						y = 0.4;
						//stop cars getting stuck on stairs
					}
					Boolean ignore = false;
					if (car.getVelocity().getY() > 0) {
						//if car is going up already then dont do gravity 
						ignore = true;
					}
					if (!ignore) {
						//Do gravity
						Velocity.setY(y);
					}
					if(fly && cont){
						//Make the car ascend (easter egg, slab elevator)
						Velocity.setY(0.5);
					}
					//Move the car and adjust vector to fit car stats
					car.setVelocity(calculateCarStats(car, player, Velocity));
				}
			} else {
				//Move the car and adjust vector to fit car stats
				car.setVelocity(calculateCarStats(car, player, Velocity));
			}
			//Recalculate car health
			if(recalculateHealth){
	        	if(car.hasMetadata("carhealth")){
	        		car.removeMetadata("carhealth", plugin);
	        	}
	        	car.setMetadata("carhealth", health);
	        }
		}
		return;
	}
	
	/* 
	 * This disables fall damage whilst driving a car
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	void safeFly(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player p = (Player) event.getEntity();
		if (inACar(p.getName())) {
			Vector vel = p.getVehicle().getVelocity();
			if (!(vel.getY() > (double) -0.1 && vel.getY() < (double) 0.1)) {
				event.setCancelled(true);
			}
			else{
				p.damage(event.getDamage());
			}

		}
		return;
	}
  
	/*
	 * This provides effects and health changes when cars collide with entities
	 */
	@EventHandler
	void hitByCar(VehicleEntityCollisionEvent event) {
		Vehicle veh = event.getVehicle();
		if (!(veh instanceof Minecart)) {
			return;
		}
		final Minecart cart = (Minecart) veh;
		if (!isACar(cart)) {
			return;
		}
        Entity ent = event.getEntity();
		if (cart.getPassenger() == null) {
			return;
		}
		double x = cart.getVelocity().getX();
		double y = cart.getVelocity().getY();
		double z = cart.getVelocity().getZ();
		if (x < 0) {
			x = -x;
		}
		if (y < 0) {
			y = -y;
		}
		if (z < 0) {
			z = -z;
		}
		if (x < 0.3 && z < 0.3) {
			return;
		}
		double speed = (x * z) / 2;
		if(speed > 0){
			Runnable onDeath = new Runnable(){
				//@Override
				public void run(){
					cart.eject();
					Location loc = cart.getLocation();
					cart.remove();
					loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
				}
			};
			CarHealthData health = new CarHealthData(ucars.config.getDouble("general.cars.health.default"), onDeath, plugin);
			// It is a valid car!
	        if(cart.hasMetadata("carhealth")){
	        	List<MetadataValue> vals = cart.getMetadata("carhealth");
	        	for(MetadataValue val:vals){
	        		if(val instanceof CarHealthData){
	        			health = (CarHealthData) val;
	        		}
	        	}
	        }
	        double dmg = ucars.config.getDouble("general.cars.health.crashDamage");
	    	if(dmg > 0){
	    		if(cart.getPassenger() instanceof Player){
	    	    double max = ucars.config.getDouble("general.cars.health.default");
	    	    double left = health.getHealth() - dmg;
	    	    ChatColor color = ChatColor.YELLOW;
	    	    if(left > (max*0.66)){
	    	    	color = ChatColor.GREEN;
	    	    }
	    	    if(left < (max*0.33)){
	    	    	color = ChatColor.RED;
	    	    }
	    		((Player)cart.getPassenger()).sendMessage(ChatColor.RED+"-"+dmg+"[crash]" + color + " ("+left+")");
	    		}
	    		health.damage(dmg);
	    	}
	        	if(cart.hasMetadata("carhealth")){
	        		cart.removeMetadata("carhealth", plugin);
	        	}
	        	cart.setMetadata("carhealth", health);
		}
		if(!(speed > 0)){
			return;
		}
		if(ucars.config.getBoolean("general.cars.hitBy.enableMonsterDamage")){
    		if(ent instanceof Monster){
    			double mult = ucars.config.getDouble("general.cars.hitBy.power") / 7;
    			ent.setVelocity(cart.getVelocity().setY(0.5).multiply(mult));
    			((Monster) ent).damage(0.75*(speed*100));
    		}
        }
		if (!ucars.config.getBoolean("general.cars.hitBy.enable")) {
			return;
		}
		if (!(ent instanceof Player)) {
			return;
		}
		Player p = (Player) ent;
		if (inACar(p)) {
			return;
		}
		double mult = ucars.config.getDouble("general.cars.hitBy.power") / 5;
		p.setVelocity(cart.getVelocity().setY(0.5).multiply(mult));
		p.sendMessage(ucars.colors.getInfo() + Lang.get("lang.messages.hitByCar"));
		double damage = ucars.config.getDouble("general.cars.hitBy.damage");
		p.damage((int) (damage * speed));
		return;
	}

	/*
	 * This places cars and other interacting features
	 */
	@EventHandler
	void interact(PlayerInteractEvent event) {
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		if (event.getPlayer().getItemInHand().getTypeId() == 328) {
			// Its a minecart!
			int iar = block.getTypeId();
			if (iar == 66 || iar == 28 || iar == 27) {
				return;
			}
			if (!ucars.config.getBoolean("general.cars.enable")) {
				return;
			}
			if (ucars.config.getBoolean("general.cars.placePerm.enable")) {
				String perm = ucars.config
						.getString("general.cars.placePerm.perm");
				if (!event.getPlayer().hasPermission(perm)) {
					String noPerm = Lang.get("lang.messages.noPlacePerm");
					noPerm = noPerm.replaceAll("%perm%", perm);
					event.getPlayer().sendMessage(
							ucars.colors.getError() + noPerm);
					return;
				}
			}
			if (event.isCancelled()) {
				event.getPlayer().sendMessage(
						ucars.colors.getError()
								+ Lang.get("lang.messages.noPlaceHere"));
				return;
			}
			Location loc = block.getLocation().add(0, 1.5, 0);
			loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			final Entity car = event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
			double health = ucars.config.getDouble("general.cars.health.default");
			Runnable onDeath = new Runnable(){
				//@Override
				public void run(){
					car.eject();
					Location loc = car.getLocation();
					car.remove();
					loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
				}
			};
			car.setMetadata("carhealth", new CarHealthData(health, onDeath, plugin));
			/*
			 * Location carloc = car.getLocation();
			 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
			 * car.setVelocity(new Vector(0,0,0));
			 */
			event.getPlayer()
					.sendMessage(
							ucars.colors.getInfo()
									+ Lang.get("lang.messages.place"));
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				event.getPlayer().getInventory().removeItem(new ItemStack(328));
			}
		}
		if (inACar(event.getPlayer())) {
			if (ucars.config.getBoolean("general.cars.fuel.enable")) {
				String[] parts = ucars.config.getString(
						"general.cars.fuel.check").split(":");
				int id = Integer.parseInt(parts[0]);
				int data = 0;
				Boolean hasdata = false;
				if (parts.length > 1) {
					hasdata = true;
					data = Integer.parseInt(parts[1]);
				}
				if (event.getPlayer().getItemInHand().getTypeId() == id) {
					Boolean valid = true;
					if (hasdata) {
						int tdata = ((int) event.getPlayer().getItemInHand()
								.getData().getData());
						if (!(tdata == data)) {
							valid = false;
						}
					}
					if (valid) {
						event.getPlayer().performCommand("ufuel view");
					}
				}
			}
		}
		String LowBoostRaw = ucars.config.getString("general.cars.lowBoost");
		String MedBoostRaw = ucars.config.getString("general.cars.medBoost");
		String HighBoostRaw= ucars.config.getString("general.cars.highBoost");
		//int LowBoostId = ucars.config.getInt("general.cars.lowBoost");
		//int MedBoostId = ucars.config.getInt("general.cars.medBoost");
		//int HighBoostId = ucars.config.getInt("general.cars.highBoost");
		int bid = event.getPlayer().getItemInHand().getTypeId(); // booster id
		int bdata = event.getPlayer().getItemInHand().getDurability();
		if (ItemStackFromId.equals(LowBoostRaw, bid, bdata)) {
			if (inACar(event.getPlayer())) {
				boolean boosting = carBoost(event.getPlayer().getName(), 10,
						3000, ucars.config.getDouble("general.cars.defSpeed"));
				if (boosting) {
					if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
						// they r in survival
						event.getPlayer().getInventory()
								.removeItem(ItemStackFromId.get(LowBoostRaw));
					}
					event.getPlayer().sendMessage(
							ucars.colors.getSuccess()
									+ Lang.get("lang.boosts.low"));
					return;
				} else {
					event.getPlayer().sendMessage(
							ucars.colors.getError() + Lang.get("lang.boosts.already"));
				}
				return;
			}
		}
		if (ItemStackFromId.equals(MedBoostRaw, bid, bdata)) {
			if (inACar(event.getPlayer())) {
				boolean boosting = carBoost(event.getPlayer().getName(), 20,
						6000, ucars.config.getDouble("general.cars.defSpeed"));
				if (boosting) {
					if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
						// they r in survival
						event.getPlayer().getInventory()
								.removeItem(ItemStackFromId.get(MedBoostRaw));
					}
					event.getPlayer().sendMessage(
							ucars.colors.getSuccess()
									+ Lang.get("lang.boosts.med"));
					return;
				} else {
					event.getPlayer().sendMessage(
							ucars.colors.getError() + Lang.get("lang.boosts.already"));
				}
				return;
			}
		}
		if (ItemStackFromId.equals(HighBoostRaw, bid, bdata)) {
			if (inACar(event.getPlayer())) {
				boolean boosting = carBoost(event.getPlayer().getName(), 50,
						10000, ucars.config.getDouble("general.cars.defSpeed"));
				if (boosting) {
					if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
						// they r in survival
						event.getPlayer().getInventory()
								.removeItem(ItemStackFromId.get(HighBoostRaw));
					}
					event.getPlayer().sendMessage(
							ucars.colors.getSuccess()
									+ Lang.get("lang.boosts.high"));
					return;
				} else {
					event.getPlayer().sendMessage(
							ucars.colors.getError() + Lang.get("lang.boosts.already"));
				}
				return;
			}
		}

		return;
	}
	/*
	 * This controls the [ufuel] signs
	 */
	@EventHandler
	void signInteract(PlayerInteractEvent event){
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		// [ufuel]
		// buy/sell
		// how many litres
		if(!(block.getState() instanceof Sign)){
			return;
		}
		Sign sign = (Sign) block.getState();
		String[] lines = sign.getLines();
		if(!lines[0].equalsIgnoreCase("[uFuel]")){
			return;
		}
		event.setCancelled(true);
		String action = lines[1];
		String quantity = lines[2];
		double amount = 0;
		try {
			amount = Double.parseDouble(quantity);
		} catch (NumberFormatException e) {
			return;
		}
		if(action.equalsIgnoreCase("buy")){
			String[] args = new String[]{"buy",""+amount};
			plugin.cmdExecutor.ufuel(event.getPlayer(), args);
		}
		else if(action.equalsIgnoreCase("sell")){
			String[] args = new String[]{"sell",""+amount};
			plugin.cmdExecutor.ufuel(event.getPlayer(), args);
		}
		else{
			return;
		}
	}

}