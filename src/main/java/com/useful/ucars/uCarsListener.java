package com.useful.ucars;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.CarRespawnReason;
import com.useful.uCarsAPI.uCarRespawnEvent;
import com.useful.ucarsCommon.StatValue;

public class uCarsListener implements Listener {
	private ucars plugin;
	private List<String> ignoreJump = null;
	
	private Boolean carsEnabled = true;
	private Boolean licenseEnabled = false;
	private Boolean roadBlocksEnabled = false;
	private Boolean trafficLightsEnabled = true;
	private Boolean effectBlocksEnabled = true;
	private Boolean usePerms = false;
	private Boolean fuelEnabled = false;
	private Boolean fuelUseItems = false;
	
	private double defaultSpeed = 30;
	private double defaultHealth = 10;
	private double damage_water = 0;
	private double damage_lava = 10;
	private double damage_cactus = 5;
	private double uCar_jump_amount = 20;
	private double crash_damage = 0;
	
	private String fuelBypassPerm = "ufuel.bypass";
	
    private List<String> roadBlocks = new ArrayList<String>(); //Road blocks
    private List<String> trafficLightRawIds = new ArrayList<String>(); //Traffic lights
    private List<String> blockBoost = new ArrayList<String>(); //Gold booster blocks
    private List<String> highBlockBoost = new ArrayList<String>(); //Diamond booster blocks
    private List<String> resetBlockBoost = new ArrayList<String>(); //Emerald booster blocks
    private List<String> jumpBlock = new ArrayList<String>(); //Jump blocks (Iron)
    private List<String> teleportBlock = new ArrayList<String>(); //Teleport blocks (purple clay)
    private List<String> barriers = new ArrayList<String>();
    
    private ConcurrentHashMap<String, Double> speedMods = new ConcurrentHashMap<String, Double>();

	public uCarsListener(ucars plugin) {
		this.plugin = ucars.plugin;
		ignoreJump = new ArrayList<String>();
		ignoreJump.add("AIR"); //Air
		ignoreJump.add("LAVA"); //Lava
        ignoreJump.add("STATIONARY_LAVA"); //Lava
        ignoreJump.add("WATER"); //Water
        ignoreJump.add("STATIONARY_WATER"); //Water
        ignoreJump.add("COBBLE_WALL"); //Cobble wall
        ignoreJump.add("FENCE"); //fence
        ignoreJump.add("NETHER_FENCE"); //Nether fence
        ignoreJump.add("STONE_PLATE"); //Stone pressurepad
        ignoreJump.add("WOOD_PLATE"); //Wood pressurepad
		ignoreJump.add("TRIPWIRE"); // tripwires
		ignoreJump.add("TRIPWIRE_HOOK"); // tripwires
		ignoreJump.add("TORCH"); // torches
		ignoreJump.add("REDSTONE_TORCH_ON"); // redstone torches
		ignoreJump.add("REDSTONE_TORCH_OFF"); // redstone off torches
		ignoreJump.add("DIODE_BLOCK_OFF"); // repeater off
		ignoreJump.add("DIODE_BLOCK_ON"); // repeater on
		ignoreJump.add("REDSTONE_COMPARATOR_OFF"); // comparator off
		ignoreJump.add("REDSTONE_COMPARATOR_ON"); // comparator on
		ignoreJump.add("VINE"); // vines
		ignoreJump.add("LONG_GRASS"); // Tall grass
		ignoreJump.add("STONE_BUTTON"); // stone button
		ignoreJump.add("WOOD_BUTTON"); // wood button
		ignoreJump.add("FENCE_GATE"); // fence gate
		ignoreJump.add("LEVER"); // lever
		ignoreJump.add("SNOW"); // snow
		ignoreJump.add("DAYLIGHT_DETECTOR"); // daylight detector
		ignoreJump.add("SIGN_POST"); // sign
		ignoreJump.add("WALL_SIGN"); // sign on the side of a block
		ignoreJump.add("CARPET"); // carpet
		
		usePerms = ucars.config.getBoolean("general.permissions.enable");
		carsEnabled = ucars.config.getBoolean("general.cars.enable");
		defaultHealth = ucars.config.getDouble("general.cars.health.default");
		
		damage_water = ucars.config
				.getDouble("general.cars.health.underwaterDamage");
		damage_lava = ucars.config
				.getDouble("general.cars.health.lavaDamage");
		damage_cactus = ucars.config
				.getDouble("general.cars.health.cactusDamage");
		defaultSpeed = ucars.config
				.getDouble("general.cars.defSpeed");
		fuelBypassPerm = ucars.config
				.getString("general.cars.fuel.bypassPerm");
		uCar_jump_amount = ucars.config
				.getDouble("general.cars.jumpAmount");
		crash_damage = ucars.config
				.getDouble("general.cars.health.crashDamage");
		
		licenseEnabled = ucars.config.getBoolean("general.cars.licenses.enable");
		roadBlocksEnabled = ucars.config.getBoolean("general.cars.roadBlocks.enable");
		trafficLightsEnabled = ucars.config.getBoolean("general.cars.trafficLights.enable");
		effectBlocksEnabled = ucars.config.getBoolean("general.cars.effectBlocks.enable");
		fuelEnabled = ucars.config.getBoolean("general.cars.fuel.enable");
		fuelUseItems = ucars.config.getBoolean("general.cars.fuel.items.enable");
		
		if(roadBlocksEnabled){
		    List<String> ids = ucars.config
					.getStringList("general.cars.roadBlocks.ids");
			ids.addAll(ucars.config.getStringList("general.cars.blockBoost"));
			ids.addAll(ucars.config.getStringList("general.cars.HighblockBoost"));
			ids.addAll(ucars.config.getStringList("general.cars.ResetblockBoost"));
			ids.addAll(ucars.config.getStringList("general.cars.jumpBlock"));
			ids.add("AIR");
			ids.add("LAVA");
			ids.add("STATIONARY_LAVA");
			ids.add("WATER");
			ids.add("STATIONARY_WATER");
			roadBlocks = ids;
		}
		if(trafficLightsEnabled){
			trafficLightRawIds = ucars.config.getStringList("general.cars.trafficLights.waitingBlock");
		}
		if(effectBlocksEnabled){
			blockBoost = ucars.config.getStringList("general.cars.blockBoost");
			highBlockBoost = ucars.config.getStringList("general.cars.HighblockBoost");
			resetBlockBoost = ucars.config.getStringList("general.cars.ResetblockBoost");
			jumpBlock = ucars.config.getStringList("general.cars.jumpBlock");
			teleportBlock = ucars.config.getStringList("general.cars.teleportBlock");
		}
		
		barriers = ucars.config.getStringList("general.cars.barriers"); //Load specified barriers
		
		//SpeedMods
		List<String> units = ucars.config.getStringList("general.cars.speedMods");
		for (String unit : units) {
			String[] sections = unit.split("-");
			try {
				String rawMat = sections[0];
				double mult = Double.parseDouble(sections[1]);
				speedMods.put(rawMat, mult);
			} catch (NumberFormatException e) {
				//Invalid speed mod
			}
		}
		//No longer speedmods
	}

	/*
	 * Performs on-tick calculations for if ucarsTrade is installed
	 */
	public Vector calculateCarStats(Minecart car, Player player,
			Vector velocity, double currentMult) {
		if (car.hasMetadata("car.frozen")) {
			velocity = new Vector(0, 0, 0);
			return velocity;
		}
		velocity = plugin.getAPI().getTravelVector(car, velocity, currentMult);
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
	 * Checks if the specified player is inside a ucars (public for traincarts
	 * support)
	 */
	public boolean inACar(String playername) {
		try {
			Player p = plugin.getServer().getPlayer(playername);
			return inACar(p);
		} catch (Exception e) {
			// Server reloading
			return false;
		}
	}

	/*
	 * Checks if a minecart is a car (Public for traincarts support)
	 */
	public boolean isACar(Minecart cart) {
		if(cart.hasMetadata("ucars.ignore")){
			return false; //Not a car
		}
		Location loc = cart.getLocation();
		Block b = loc.getBlock();
		String mat = b.getType().name().toUpperCase();
		String underMat = b.getRelative(BlockFace.DOWN).getType().name().toUpperCase();
		String underUnderMat = b.getRelative(BlockFace.DOWN, 2).getType().name().toUpperCase();
		List<String> checks = new ArrayList<String>();
		checks.add("POWERED_RAIL");
		checks.add("RAILS");
		checks.add("DETECTOR_RAIL");
		checks.add("ACTIVATOR_RAIL");
		if(checks.contains(mat) 
				|| checks.contains(underMat) 
				|| checks.contains(underUnderMat)){
			return false;
		}
		if (!plugin.getAPI().runCarChecks(cart)) {
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
			plugin.getLogger().log(Level.SEVERE,
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
	 * Checks if the specified player is inside a ucars (public for traincarts
	 * support)
	 */
	public boolean inACar(Player p) {
		try {
			if (p == null) {
				// Should NEVER happen(It means they r offline)
				return false;
			}
			if (p.getVehicle() == null) {
				return false;
			}
			Entity ent = p.getVehicle();
			if (!(ent instanceof Minecart)) {
				while (!(ent instanceof Minecart) && ent.getVehicle() != null) {
					ent = ent.getVehicle();
				}
				if (!(ent instanceof Minecart)) {
					return false;
				}
			}
			Minecart cart = (Minecart) ent;
			return isACar(cart);
		} catch (Exception e) {
			// Server reloading
			return false;
		}
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
	public void playerJoin(PlayerJoinEvent event) {

		if (event.getPlayer().isOp()) {
			if (!plugin.protocolLib) {
				event.getPlayer().sendMessage(
						ucars.colors.getError()
								+ Lang.get("lang.messages.noProtocolLib"));
			}
		}

	}

	/*
	 * Performs on-vehicle-tick calculations(even when stationary) and also
	 * allows for old versions of minecraft AND ucars to access the new features
	 * through this 'bridge' (in theory) But in practice they need protocol to
	 * get past the bukkit dependency exception so it uses that anyway! (Kept
	 * for old version hybrids, eg. tekkti)
	 */
	@EventHandler
	public void tickCalcsAndLegacy(VehicleUpdateEvent event) {
		// start vehicleupdate mechs
		Vehicle vehicle = event.getVehicle();
		Entity passenger = vehicle.getPassenger();
		Boolean driven = true;
		if (passenger == null || !(vehicle instanceof Minecart)) {
			return;
		}
		if (!(passenger instanceof Player)) {
			while (!(passenger instanceof Player)
					&& passenger.getPassenger() != null) {
				passenger = passenger.getPassenger();
			}
			if (!(passenger instanceof Player)) {
				driven = false;
			}
		}
		if(!driven){
			return; //Forget extra physics, takes too much strain with extra entities
		}
		if(!(event instanceof ucarUpdateEvent)){
			if(vehicle.hasMetadata("car.vec")){
				ucarUpdateEvent evt = (ucarUpdateEvent) vehicle.getMetadata("car.vec").get(0).value();
				evt.player = ((Player)passenger); //Make sure player is correct
				evt.incrementRead();
				vehicle.removeMetadata("car.vec", ucars.plugin);
				ucarUpdateEvent et = new ucarUpdateEvent(vehicle, evt.getTravelVector().clone(), null);
				et.setRead(evt.getReadCount());
				vehicle.setMetadata("car.vec", new StatValue(et, ucars.plugin));
				ucars.plugin.getServer().getPluginManager().callEvent(evt);
				return;
			}
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
		if (driven) {
			player = (Player) passenger;
		}
		if (vehicle instanceof Minecart) {
			if (!carsEnabled) {
				return;
			}

			Minecart car = (Minecart) vehicle;
			if (!isACar(car)) {
				return;
			}
			Vector vel = car.getVelocity();
			if (car.getVelocity().getY() > 0.1
					&& !car.hasMetadata("car.falling")
					&& !car.hasMetadata("car.ascending")) { // Fix jumping bug
															// in most occasions
				if (car.hasMetadata("car.jumping")) { //TODO
					vel.setY(2.5);
					car.removeMetadata("car.jumping", plugin);
				} else if (car.hasMetadata("car.jumpFull")) {
					// Jumping a full block
					if (car.getVelocity().getY() > 10) {
						vel.setY(5);
					}
					car.removeMetadata("car.jumpFull", plugin);
				} else {
					vel.setY(0);
				}
				car.setVelocity(vel);
			}
			// Make jumping work when not moving
			// Calculate jumping gravity
			if(car.hasMetadata("car.jumpUp")){
				double amt = (Double) car.getMetadata("car.jumpUp").get(0).value();
				car.removeMetadata("car.jumpUp", plugin);
				if(amt >= 1.5){
					double y = amt * 0.1;
					car.setMetadata("car.jumpUp", new StatValue(amt-y, plugin));
					vel.setY(y);
					car.setVelocity(vel);
					return; //We don't want any further calculations
				}
				else{ //At the peak of ascent
					car.setMetadata("car.falling", new StatValue(0.01, plugin));
					car.setMetadata("car.fallingPause", new StatValue(1, plugin));
				}
				
			}
			if (car.hasMetadata("car.falling")) {
				if(car.hasMetadata("car.fallingPause")){
					car.removeMetadata("car.fallingPause", plugin);
				}
				else{
					double gravity = (Double) car.getMetadata("car.falling").get(0).value();
					double newGravity = gravity + (gravity * 0.6);
					car.removeMetadata("car.falling", plugin);
					if ((gravity <= 0.6)) {
						car.setMetadata("car.falling", new StatValue(
								newGravity, ucars.plugin));
						vel.setY(-(gravity * 1.333 + 0.2d));
						car.setVelocity(vel);
					}
				}
			}

			/*
			 * Material carBlock = car.getLocation().getBlock().getType(); if
			 * (carBlock == Material.WOOD_STAIRS || carBlock ==
			 * Material.COBBLESTONE_STAIRS || carBlock == Material.BRICK_STAIRS
			 * || carBlock == Material.SMOOTH_STAIRS || carBlock ==
			 * Material.NETHER_BRICK_STAIRS || carBlock ==
			 * Material.SANDSTONE_STAIRS || carBlock ==
			 * Material.SPRUCE_WOOD_STAIRS || carBlock ==
			 * Material.BIRCH_WOOD_STAIRS || carBlock ==
			 * Material.JUNGLE_WOOD_STAIRS || carBlock ==
			 * Material.QUARTZ_STAIRS) { Vector vel = car.getVelocity();
			 * vel.setY(0.5); car.setVelocity(vel); }
			 */
			final Minecart cart = car;
			Runnable onDeath = new Runnable() {
				// @Override
				public void run() {
					plugin.getServer().getPluginManager()
							.callEvent(new ucarDeathEvent(cart));
				}
			};
			CarHealthData health = new CarHealthData(
					defaultHealth,
					onDeath, plugin);
			Boolean recalculateHealth = false;
			// It is a valid car!
			// START ON TICK CALCULATIONS
			if (car.hasMetadata("carhealth")) {
				List<MetadataValue> vals = car.getMetadata("carhealth");
				for (MetadataValue val : vals) {
					if (val instanceof CarHealthData) {
						health = (CarHealthData) val;
					}
				}
			}
			// Calculate health based on location
			if (normalblock.getType().equals(Material.WATER)
					|| normalblock.getType().equals(Material.STATIONARY_WATER)) {
				double damage = damage_water;
				if (damage > 0) {
					if (driven) {
						double max = defaultHealth;
						double left = health.getHealth() - damage;
						ChatColor color = ChatColor.YELLOW;
						if (left > (max * 0.66)) {
							color = ChatColor.GREEN;
						}
						if (left < (max * 0.33)) {
							color = ChatColor.RED;
						}
						player.sendMessage(ChatColor.RED + "-" + damage + "["
								+ Material.WATER.name().toLowerCase() + "]"
								+ color + " (" + left + ")");
					}
					health.damage(damage);
					recalculateHealth = true;
				}
			}
			if (normalblock.getType().equals(Material.LAVA)
					|| normalblock.getType().equals(Material.STATIONARY_LAVA)) {
				double damage = damage_lava;
				if (damage > 0) {
					if (driven) {
						double max = defaultHealth;
						double left = health.getHealth() - damage;
						ChatColor color = ChatColor.YELLOW;
						if (left > (max * 0.66)) {
							color = ChatColor.GREEN;
						}
						if (left < (max * 0.33)) {
							color = ChatColor.RED;
						}
						player.sendMessage(ChatColor.RED + "-" + damage + "["
								+ Material.LAVA.name().toLowerCase() + "]"
								+ color + " (" + left + ")");
					}
					health.damage(damage);
					recalculateHealth = true;
				}
			}
			if (recalculateHealth) {
				if (car.hasMetadata("carhealth")) {
					car.removeMetadata("carhealth", plugin);
				}
				car.setMetadata("carhealth", health);
			}
			// End health calculations
			if (!driven) {
				return;
			}
			// Do calculations for when driven
			// END ON TICK CALCULATIONS
			// end vehicleupdate mechs
			// start legacy controls
			if (plugin.protocolLib) {
				return;
			}
			// Attempt pre-1.6 controls

			Vector playerVelocity = car.getPassenger().getVelocity();
			ucarUpdateEvent ucarupdate = new ucarUpdateEvent(car,
					playerVelocity, player);
			plugin.getServer().getPluginManager().callEvent(ucarupdate);
			return;
		}
	}

	/*
	 * Performs the actually mechanic for making the cars move
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onUcarUpdate(ucarUpdateEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Boolean modY = true;
		Vehicle vehicle = event.getVehicle();
		
		if(event.getReadCount() > 2){
			vehicle.removeMetadata("car.vec", ucars.plugin);
		}
		
		Location under = vehicle.getLocation();
		under.setY(vehicle.getLocation().getY() - 1);
		Block underblock = under.getBlock();
		Block underunderblock = underblock.getRelative(BlockFace.DOWN);
		Block normalblock = vehicle.getLocation().getBlock();
		// Block up = normalblock.getLocation().add(0, 1, 0).getBlock();
		final Player player = event.getPlayer();
		if (player == null) {
			return;
		}
		if (vehicle instanceof Minecart) {
			if (!carsEnabled) {
				return;
			}
			try {
				if (!licenseEnabled
						&& plugin.licensedPlayers.contains(player.getName())) {
					player.sendMessage(ucars.colors.getError()
							+ Lang.get("lang.licenses.noLicense"));
					return;
				}
			} catch (Exception e1) {
			}
			Minecart car = (Minecart) vehicle;
			final Minecart cart = (Minecart) vehicle;
			Runnable onDeath = new Runnable() {
				// @Override
				public void run() {
					plugin.getServer().getPluginManager()
							.callEvent(new ucarDeathEvent(cart));
				}
			};
			CarHealthData health = new CarHealthData(
					defaultHealth,
					onDeath, plugin);
			Boolean recalculateHealth = false;
			// It is a valid car!
			if (car.getVelocity().getY() > 0.01
					&& !car.hasMetadata("car.falling")
					&& !car.hasMetadata("car.ascending")) {
				modY = false;
			}
			if (car.hasMetadata("car.jumping")) {
				if (!car.hasMetadata("car.ascending")) {
					modY = false;
				}
				car.removeMetadata("car.jumping", plugin);
			}
			car.setMaxSpeed(5); // Don't allow game breaking speed - but faster
								// than default
			if (car.hasMetadata("carhealth")) {
				List<MetadataValue> vals = car.getMetadata("carhealth");
				for (MetadataValue val : vals) {
					if (val instanceof CarHealthData) {
						health = (CarHealthData) val;
					}
				}
			}
			// Calculate road blocks
			if (roadBlocksEnabled) {
				Location loc = car.getLocation().getBlock()
						.getRelative(BlockFace.DOWN).getLocation();
				if(!plugin.isBlockEqualToConfigIds(roadBlocks, loc.getBlock())){
					//Not a road block being driven on
					return;
				}
			}
			Location loc = car.getLocation();
			if (atTrafficLight(car, underblock, underunderblock, loc)){
				return;
			}
			// Calculate default effect blocks
			if (effectBlocksEnabled) {
				if (plugin.isBlockEqualToConfigIds(blockBoost,
						underblock)
						|| plugin.isBlockEqualToConfigIds(
								blockBoost, underunderblock)) {
					if (inACar(player)) {
						carBoost(player.getName(), 20, 6000,
								defaultSpeed);
					}
				}
				if (plugin.isBlockEqualToConfigIds(
						highBlockBoost, underblock)
						|| plugin.isBlockEqualToConfigIds(
								highBlockBoost, underunderblock)) {
					if (inACar(player)) {
						carBoost(player.getName(), 50, 8000,
								defaultSpeed);
					}
				}
				if (plugin.isBlockEqualToConfigIds(
						resetBlockBoost, underblock)
						|| plugin
								.isBlockEqualToConfigIds(
										resetBlockBoost,
										underunderblock)) {
					if (inACar(player)) {
						ResetCarBoost(player.getName(), car,
								defaultSpeed);
					}
				}
			}
			Vector playerVelocity = event.getTravelVector(); // Travel Vector,
																// fixes
																// controls for
																// 1.6
			double multiplier = defaultSpeed;
			try {
				if (ucars.carBoosts.containsKey(player.getName())) { // Use the
																		// boost
																		// allocated
					multiplier = ucars.carBoosts.get(player.getName());
				}
			} catch (Exception e1) {
				return;
			}
			String underMat = under.getBlock().getType().name().toUpperCase();
			int underdata = under.getBlock().getData();
			// calculate speedmods
			String key = underMat+":"+underdata;
			if(speedMods.containsKey(key)){
				if(!ucars.carBoosts.containsKey(player.getName())){
					multiplier = speedMods.get(key);
				}
				else{
					multiplier = (speedMods.get(key)+multiplier)*0.5; //Mean Average of both
				}
			}
			if (event.getDoDivider()) { // Braking or going slower
				multiplier = multiplier * event.getDivider();
			}
			Vector Velocity = playerVelocity.multiply(multiplier);
			if (!(player.isInsideVehicle())) {
				return;
			}
			if (usePerms) {
				if (!player.hasPermission("ucars.cars")) {
					player.sendMessage(ucars.colors.getInfo()
							+ Lang.get("lang.messages.noDrivePerm"));
					return;
				}
			}
			if (normalblock.getType() != Material.AIR //Air
					&& normalblock.getType() != Material.WATER //Water
					&& normalblock.getType() != Material.STATIONARY_WATER //Water
					&& normalblock.getType() != Material.STEP //Slab
					&& normalblock.getType() != Material.DOUBLE_STEP //Double slab
					&& normalblock.getType() != Material.LONG_GRASS //Long grass
					&& !normalblock.getType().name().toLowerCase()
							.contains("stairs")) {
				// Stuck in a block
				car.setVelocity(new Vector(0, 1.1, 0));
			}
			Location before = car.getLocation();
			float dir = player.getLocation().getYaw();
			BlockFace faceDir = ClosestFace.getClosestFace(dir);
			// before.add(faceDir.getModX(), faceDir.getModY(),
			// faceDir.getModZ());
			double fx = Velocity.getX();
			if (Math.abs(fx) > 1) {
				fx = faceDir.getModX();
			}
			double fz = Velocity.getZ();
			if (Math.abs(fz) > 1) {
				fz = faceDir.getModZ();
			}
			before.add(new Vector(fx, faceDir.getModY(), fz));
			Block block = before.getBlock();
			// Calculate collision health
			if (block.getType().equals(Material.CACTUS)) {
				double damage = damage_cactus;
				if (damage > 0) {
					double max = defaultHealth;
					double left = health.getHealth() - damage;
					ChatColor color = ChatColor.YELLOW;
					if (left > (max * 0.66)) {
						color = ChatColor.GREEN;
					}
					if (left < (max * 0.33)) {
						color = ChatColor.RED;
					}
					player.sendMessage(ChatColor.RED + "-" + damage + "["
							+ Material.CACTUS.name().toLowerCase() + "]"
							+ color + " (" + left + ")");
					health.damage(damage);
					recalculateHealth = true;
				}
			}
			// End calculations for collision health
			if (fuelEnabled
					&& !fuelUseItems
					&& !player.hasPermission(fuelBypassPerm)) {
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
			else if (fuelEnabled
					&& fuelUseItems
					&& !player.hasPermission(fuelBypassPerm)) {
				// item fuel - Not for laggy servers!!!
				double fuel = 0;
				ArrayList<ItemStack> items = plugin.ufuelitems;
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
							item.getType();
						} catch (Exception e) {
							ignore = true;
						}
						if (!ignore) {
							if (!taken) {
								if(plugin.isItemOnList(items, item)){
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
					if (last) {
						inv.setItem(toUse, new ItemStack(Material.AIR));
					}
				}
			}
			if (Velocity.getY() < 0) { // Fix falling into ground and also allow use
										// custom gravity values (Eg. better jumping)
				double newy = Velocity.getY() + 2d;
				Velocity.setY(newy);
			}
			Material bType = block.getType();
			int bData = block.getData();
			Boolean fly = false; // Fly is the 'easter egg' slab elevator
			if (normalblock.getRelative(faceDir).getType() == Material.STEP) {
				// If looking at slabs
				fly = true;
			}
			/*
			 * if(bbb.getType()==Material.STEP && !(bbb.getData() != 0)){ //If
			 * in a slab block fly = true; }
			 */
			if (effectBlocksEnabled) {
				if (plugin.isBlockEqualToConfigIds(jumpBlock,
						underblock)
						|| plugin.isBlockEqualToConfigIds(
								jumpBlock, underunderblock)) { //TODO
					double y = Velocity.getY() + uCar_jump_amount;
					car.setMetadata("car.jumpUp", new StatValue(uCar_jump_amount, plugin));
					Velocity.setY(y);
					car.setVelocity(Velocity);
				}
				if (plugin.isBlockEqualToConfigIds(
						teleportBlock, underblock)
						|| plugin.isBlockEqualToConfigIds(
								teleportBlock, underunderblock)) {
					// teleport the player
					Sign s = null;
					if (underunderblock.getState() instanceof Sign) {
						s = (Sign) underunderblock.getState();
					}
					if (underunderblock.getRelative(BlockFace.DOWN).getState() instanceof Sign) {
						s = (Sign) underunderblock.getRelative(BlockFace.DOWN)
								.getState();
					}
					if (s != null) {
						String[] lines = s.getLines();
						if (lines[0].equalsIgnoreCase("[Teleport]")) {
							Boolean raceCar = false;
							if (car.hasMetadata("kart.racing")) {
								raceCar = true;
							}
							car.setMetadata("safeExit.ignore", new StatValue(null, plugin));
							car.eject();
							
							UUID carId = car.getUniqueId();
							
							car.remove();
							
							final Minecart ca = car;
							Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

								@Override
								public void run() {
									if(ca != null){
										ca.remove(); //For uCarsTrade
									}
									return;
								}}, 2l);
							
							String xs = lines[1];
							String ys = lines[2];
							String zs = lines[3];
							Boolean valid = true;
							double x = 0, y = 0, z = 0;
							try {
								x = Double.parseDouble(xs);
								y = Double.parseDouble(ys);
								y = y + 0.5;
								z = Double.parseDouble(zs);
							} catch (NumberFormatException e) {
								valid = false;
							}
							if (valid) {
								List<MetadataValue> metas = null;
								if (player.hasMetadata("car.stayIn")) {
									metas = player.getMetadata("car.stayIn");
									for (MetadataValue val : metas) {
										player.removeMetadata("car.stayIn",
												val.getOwningPlugin());
									}
								}
								Location toTele = new Location(s.getWorld(), x,
										y, z);
								Chunk ch = toTele.getChunk();
								if (ch.isLoaded()) {
									ch.load(true);
								}
								car = (Minecart) s.getWorld().spawnEntity(
										toTele, EntityType.MINECART);
								final Minecart v = car;
								car.setMetadata("carhealth", health);
								if (raceCar) {
									car.setMetadata("kart.racing",
											new StatValue(null, plugin));
								}
								health.onDeath = new Runnable() {
									public void run() {
										plugin.getServer()
												.getPluginManager()
												.callEvent(
														new ucarDeathEvent(
																v));
									}
								};
								uCarRespawnEvent evnt = new uCarRespawnEvent(car, carId, car.getUniqueId(),
										CarRespawnReason.TELEPORT);
								plugin.getServer().getPluginManager().callEvent(evnt);
								if(evnt.isCancelled()){
									car.remove();
								}
								else{
									player.sendMessage(ucars.colors.getTp()
											+ "Teleporting...");
									car.setPassenger(player);
									final Minecart ucar = car;
									Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){

										@Override
										public void run() {
											ucar.setPassenger(player); //For the sake of uCarsTrade
											return;
										}}, 2l);
									car.setVelocity(Velocity);
									if (metas != null) {
										for (MetadataValue val : metas) {
											player.setMetadata("car.stayIn", val);
										}
									}
									plugin.getAPI().updateUcarMeta(carId,
											car.getUniqueId());
								}
							}
						}
					}
				}
			}
			// actually jump
			Location theNewLoc = block.getLocation();
			Location bidUpLoc = block.getLocation().add(0, 1, 0);
			Material bidU = bidUpLoc.getBlock().getType();
			Boolean cont = true;
			// check it's not a barrier
			cont = !plugin.isBlockEqualToConfigIds(barriers, block);
			
			Boolean inStairs = false;
			Material carBlock = car.getLocation().getBlock().getType();
			if (carBlock.name().toLowerCase().contains("stairs")) {
				inStairs = true;
			}
			if (car.hasMetadata("car.ascending")) {
				car.removeMetadata("car.ascending", plugin);
			}
			// Make cars jump if needed
			if (inStairs ||
					 (!ignoreJump.contains(bType.name().toUpperCase()) && cont && modY)) { //Should jump
				if (bidU == Material.AIR || bidU == Material.LAVA 
						|| bidU == Material.STATIONARY_LAVA || bidU == Material.WATER
						|| bidU == Material.STATIONARY_WATER || bidU == Material.STEP 
						|| bidU == Material.DOUBLE_STEP || inStairs) { //Clear air above
					theNewLoc.add(0, 1.5d, 0);
					Boolean calculated = false;
					double y = 7;
					if (block.getType().name().toLowerCase().contains("step")) {
						calculated = true;
						y = 6;
					}
					if (carBlock.name().toLowerCase().contains("step")) { // In
																			// a
																			// step
																			// block
																			// and
																			// trying
																			// to
																			// jump
						calculated = true;
						y = 6;
					}
					if (carBlock.name().toLowerCase()
							.contains(Pattern.quote("stairs"))
							// ||
							// underblock.getType().name().toLowerCase().contains(Pattern.quote("stairs"))
							|| block.getType().name().toLowerCase()
									.contains(Pattern.quote("stairs"))
							|| inStairs) {
						calculated = true;
						y = 2.5;
						// ascend stairs
					}
					Boolean ignore = false;
					if (car.getVelocity().getY() > 4) {
						// if car is going up already then dont do ascent
						ignore = true;
					}
					if (!ignore) {
						// Do ascent
						Velocity.setY(y);
						if (calculated) {
							car.setMetadata("car.jumping", new StatValue(null,
									plugin));
						} else {
							car.setMetadata("car.jumpFull", new StatValue(null,
									plugin));
						}
					}
				}
				if (fly && cont) {
					// Make the car ascend (easter egg, slab elevator)
					Velocity.setY(0.6); // Make a little easier
					car.setMetadata("car.ascending",
							new StatValue(null, plugin));
				}
				// Move the car and adjust vector to fit car stats
				car.setVelocity(calculateCarStats(car, player, Velocity,
						multiplier));
			} else {
				if (fly) {
					// Make the car ascend (easter egg, slab elevator)
					Velocity.setY(0.6); // Make a little easier
					car.setMetadata("car.ascending",
							new StatValue(null, plugin));
				}
				// Move the car and adjust vector to fit car stats
				car.setVelocity(calculateCarStats(car, player, Velocity,
						multiplier));
			}
			// Recalculate car health
			if (recalculateHealth) {
				if (car.hasMetadata("carhealth")) {
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
	@EventHandler(priority = EventPriority.HIGHEST)
	void safeFly(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		Player p = (Player) event.getEntity();
		if (inACar(p.getName())) {
			Vector vel = p.getVehicle().getVelocity();
			if (!(vel.getY() > -0.1 && vel.getY() < 0.1)) {
				event.setCancelled(true);
			} else {
				try {
					p.damage(event.getDamage());
				} catch (Exception e) {
					// Damaging failed
				}
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
		if (cart.getPassenger() == null) { //Don't both to calculate with PiguCarts, etc...
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
		if (speed > 0) {
			Runnable onDeath = new Runnable() {
				// @Override
				public void run() {
					plugin.getServer().getPluginManager()
							.callEvent(new ucarDeathEvent(cart));
				}
			};
			CarHealthData health = new CarHealthData(
					defaultHealth,
					onDeath, plugin);
			// It is a valid car!
			if (cart.hasMetadata("carhealth")) {
				List<MetadataValue> vals = cart.getMetadata("carhealth");
				for (MetadataValue val : vals) {
					if (val instanceof CarHealthData) {
						health = (CarHealthData) val;
					}
				}
			}
			double dmg = crash_damage;
			if (dmg > 0) {
				if (cart.getPassenger() instanceof Player) {
					double max = defaultHealth;
					double left = health.getHealth() - dmg;
					ChatColor color = ChatColor.YELLOW;
					if (left > (max * 0.66)) {
						color = ChatColor.GREEN;
					}
					if (left < (max * 0.33)) {
						color = ChatColor.RED;
					}
					((Player) cart.getPassenger())
							.sendMessage(ChatColor.RED + "-" + dmg + "[crash]"
									+ color + " (" + left + ")");
				}
				health.damage(dmg);
			}
			if (cart.hasMetadata("carhealth")) {
				cart.removeMetadata("carhealth", plugin);
			}
			cart.setMetadata("carhealth", health);
		}
		if (!(speed > 0)) {
			return;
		}
		if (ucars.config.getBoolean("general.cars.hitBy.enableMonsterDamage")) {
			if (ent instanceof Monster) {
				double mult = ucars.config
						.getDouble("general.cars.hitBy.power") / 7;
				ent.setVelocity(cart.getVelocity().setY(0.5).multiply(mult));
				((Monster) ent).damage(0.75 * (speed * 100));
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
		p.sendMessage(ucars.colors.getInfo()
				+ Lang.get("lang.messages.hitByCar"));
		double damage = crash_damage;
		p.damage((int) (damage * speed));
		return;
	}

	/*
	 * This places cars and other interacting features
	 */
	@EventHandler
	void interact(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		if (event.getPlayer().getItemInHand().getType() == Material.MINECART) {
			// Its a minecart!
			Material iar = block.getType();
			if (iar == Material.RAILS || iar == Material.ACTIVATOR_RAIL 
					|| iar == Material.POWERED_RAIL || iar == Material.DETECTOR_RAIL) {
				return;
			}
			if (!PlaceManager.placeableOn(iar.name().toUpperCase(), block.getData())) {
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
            if(!plugin.API.runCarChecks(event.getPlayer().getItemInHand())){
				return;
			}
			Location loc = block.getLocation().add(0, 1.5, 0);
			loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			final Entity car = event.getPlayer().getWorld()
					.spawnEntity(loc, EntityType.MINECART);
			double health = ucars.config
					.getDouble("general.cars.health.default");
			Runnable onDeath = new Runnable() {
				// @Override
				public void run() {
					plugin.getServer().getPluginManager()
							.callEvent(new ucarDeathEvent((Minecart) car));
				}
			};
			car.setMetadata("carhealth", new CarHealthData(health, onDeath,
					plugin));
			/*
			 * Location carloc = car.getLocation();
			 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
			 * car.setVelocity(new Vector(0,0,0));
			 */
			event.getPlayer().sendMessage(
					ucars.colors.getInfo() + Lang.get("lang.messages.place"));
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				ItemStack placed = event.getPlayer().getItemInHand();
				placed.setAmount(placed.getAmount() - 1);
				event.getPlayer().getInventory().setItemInHand(placed);
			}
		}
		if (inACar(event.getPlayer())) {
			if (ucars.config.getBoolean("general.cars.fuel.enable")) {
				if (plugin.isItemEqualToConfigIds(ucars.config.getStringList(
						"general.cars.fuel.check"), event.getPlayer().getItemInHand())) {
					event.getPlayer().performCommand("ufuel view");
				}
			}
		}
		List<String> LowBoostRaw = ucars.config.getStringList("general.cars.lowBoost");
		List<String> MedBoostRaw = ucars.config.getStringList("general.cars.medBoost");
		List<String> HighBoostRaw = ucars.config.getStringList("general.cars.highBoost");
		// int LowBoostId = ucars.config.getInt("general.cars.lowBoost");
		// int MedBoostId = ucars.config.getInt("general.cars.medBoost");
		// int HighBoostId = ucars.config.getInt("general.cars.highBoost");
		ItemStack inHand = event.getPlayer().getItemInHand();
		String bid = inHand.getType().name().toUpperCase(); // booster material name
		int bdata = inHand.getDurability();
		ItemStack remove = inHand.clone();
		remove.setAmount(1);
		if (ItemStackFromId.equals(LowBoostRaw, bid, bdata)) {
			if (inACar(event.getPlayer())) {
				boolean boosting = carBoost(event.getPlayer().getName(), 10,
						3000, ucars.config.getDouble("general.cars.defSpeed"));
				if (boosting) {
					if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
						// they r in survival
						event.getPlayer().getInventory()
								.removeItem(remove);
					}
					event.getPlayer().sendMessage(
							ucars.colors.getSuccess()
									+ Lang.get("lang.boosts.low"));
					return;
				} else {
					event.getPlayer().sendMessage(
							ucars.colors.getError()
									+ Lang.get("lang.boosts.already"));
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
								.removeItem(remove);
					}
					event.getPlayer().sendMessage(
							ucars.colors.getSuccess()
									+ Lang.get("lang.boosts.med"));
					return;
				} else {
					event.getPlayer().sendMessage(
							ucars.colors.getError()
									+ Lang.get("lang.boosts.already"));
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
								.removeItem(remove);
					}
					event.getPlayer().sendMessage(
							ucars.colors.getSuccess()
									+ Lang.get("lang.boosts.high"));
					return;
				} else {
					event.getPlayer().sendMessage(
							ucars.colors.getError()
									+ Lang.get("lang.boosts.already"));
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
	void signInteract(PlayerInteractEvent event) {
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		// [ufuel]
		// buy/sell
		// how many litres
		if (!(block.getState() instanceof Sign)) {
			return;
		}
		Sign sign = (Sign) block.getState();
		String[] lines = sign.getLines();
		if (!lines[0].equalsIgnoreCase("[uFuel]")) {
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
		if (action.equalsIgnoreCase("buy")) {
			String[] args = new String[] { "buy", "" + amount };
			plugin.cmdExecutor.ufuel(event.getPlayer(), args);
		} else if (action.equalsIgnoreCase("sell")) {
			String[] args = new String[] { "sell", "" + amount };
			plugin.cmdExecutor.ufuel(event.getPlayer(), args);
		} else {
			return;
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	void minecartBreak(VehicleDamageEvent event) {
		if (!(event.getVehicle() instanceof Minecart)
				|| !(event.getAttacker() instanceof Player)) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		final Minecart car = (Minecart) event.getVehicle();
		Player player = (Player) event.getAttacker();
		if (!isACar(car)) {
			return;
		}
		if (!ucars.config.getBoolean("general.cars.health.overrideDefault")) {
			return;
		}
		if (car.hasMetadata("carhealth")) {
			car.removeMetadata("carhealth", plugin);
		}
		Runnable onDeath = new Runnable() {
			// @Override
			public void run() {
				plugin.getServer().getPluginManager()
						.callEvent(new ucarDeathEvent(car));
			}
		};
		CarHealthData health = new CarHealthData(
				ucars.config.getDouble("general.cars.health.default"), onDeath,
				plugin);
		// It is a valid car!
		// START ON TICK CALCULATIONS
		if (car.hasMetadata("carhealth")) {
			List<MetadataValue> vals = car.getMetadata("carhealth");
			for (MetadataValue val : vals) {
				if (val instanceof CarHealthData) {
					health = (CarHealthData) val;
				}
			}
		}
		double damage = ucars.config
				.getDouble("general.cars.health.punchDamage");
		if (event.getDamage() > 0 && damage > 0) {
			double max = ucars.config.getDouble("general.cars.health.default");
			double left = health.getHealth() - damage;
			ChatColor color = ChatColor.YELLOW;
			if (left > (max * 0.66)) {
				color = ChatColor.GREEN;
			}
			if (left < (max * 0.33)) {
				color = ChatColor.RED;
			}
			if (left < 0) {
				left = 0;
			}
			player.sendMessage(ChatColor.RED + "-" + damage + ChatColor.YELLOW
					+ "[" + player.getName() + "]" + color + " (" + left + ")");
			health.damage(damage);
			car.setMetadata("carhealth", health);
			event.setCancelled(true);
			event.setDamage(0);
		} else{
			event.setCancelled(true);
			event.setDamage(0);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void carDeath(ucarDeathEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Minecart cart = event.getCar();
		if(cart.hasMetadata("car.destroyed")){
			return;
		}
		cart.setMetadata("car.destroyed", new StatValue(true, ucars.plugin));
		cart.eject();
		Location loc = cart.getLocation();
		cart.remove();
		loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
		return;
	}
	
	public Boolean atTrafficLight(Minecart car, Block underblock, Block underunderblock, Location loc){
		if (trafficLightsEnabled) {
			if (plugin.isBlockEqualToConfigIds(
					trafficLightRawIds, underblock)
					|| plugin.isBlockEqualToConfigIds(
							trafficLightRawIds,
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
						return true;
					}
				}
			}
		}
		return false;
	}

}