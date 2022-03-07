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
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.CarRespawnReason;
import com.useful.uCarsAPI.uCarCrashEvent;
import com.useful.uCarsAPI.uCarRespawnEvent;
import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.controls.ControlSchemeManager;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class uCarsListener implements Listener {
	private ucars plugin;
	private List<String> ignoreJump = null;
	private List<String> softBlocks = null;
	
	private static double GRAVITY_Y_VELOCITY_MAGNITUDE = 0.04;
	private static double SIMULATED_FRICTION_SPEED_MULTIPLIER = 0.55;
	
	private Boolean carsEnabled = true;
	private Boolean licenseEnabled = false;
	private Boolean roadBlocksEnabled = false;
	private Boolean multiverseEnabled = false;
	private Boolean trafficLightsEnabled = true;
	private Boolean effectBlocksEnabled = true;
	private Boolean usePerms = false;
	private Boolean fuelEnabled = false;
	private Boolean fuelUseItems = false;
	private Boolean disableFallDamage = false;
	private Boolean pitchEnabled = true;
	
	private double defaultSpeed = 30;
	private static double defaultHealth = 10;
	private double damage_water = 0;
	private double damage_lava = 10;
	private double damage_cactus = 5;
	private double uCar_jump_amount = 5;
	private double crash_damage = 0;
	private double hitby_crash_damage = 0;
	
	private String fuelBypassPerm = "ufuel.bypass";
	
    private List<String> roadBlocks = new ArrayList<String>(); //Road blocks
    private List<String> trafficLightRawIds = new ArrayList<String>(); //Traffic lights
    private List<String> blockBoost = new ArrayList<String>(); //Gold booster blocks
    private List<String> highBlockBoost = new ArrayList<String>(); //Diamond booster blocks
    private List<String> resetBlockBoost = new ArrayList<String>(); //Emerald booster blocks
    private List<String> jumpBlock = new ArrayList<String>(); //Jump blocks (Iron)
    private List<String> teleportBlock = new ArrayList<String>(); //Teleport blocks (purple clay)
    private List<String> barriers = new ArrayList<String>();
    private List<String> ucarworlds = new ArrayList<String>();
    
    private ConcurrentHashMap<String, Double> speedMods = new ConcurrentHashMap<String, Double>();

	public uCarsListener(ucars plugin) {
		this.plugin = ucars.plugin;
		init();
	}

	/*
	 * Asks the API to calculate car stats (Such as velocity mods, etc...)
	 */
	public Vector calculateCarStats(Entity car, Player player,
			Vector velocity, double currentMult) {
		if (UEntityMeta.hasMetadata(car, "car.frozen") || car.hasMetadata("car.frozen")) {
			if(car.hasMetadata("car.inertialYAxis")) {
				velocity = new Vector(0, velocity.getY(), 0); // Don't freeze Y-velocity
				return velocity;
			}

			if(car.getType() == EntityType.MINECART) {
				velocity = new Vector(0, GRAVITY_Y_VELOCITY_MAGNITUDE, 0);
			} else {
				velocity = new Vector(0, 0, 0);
			}
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
	 * Checks if the specified player is inside a ucar (public for traincarts
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

	
	public boolean isACar(Entity cart) {
		if(cart.hasMetadata("ucars.ignore") || UEntityMeta.hasMetadata(cart, "ucars.ignore")){
			return false; //Not a car
		}
		if(multiverseEnabled && !ucarworlds.contains(cart.getWorld().getName())) {
			return false;
		}
		if((cart.getType().toString().toUpperCase().contains("MINECART") && cart.getType().toString().length() > 8) || cart.getType().toString().toUpperCase().contains("BOAT")) {
			return false; //Not a car but a Minecart_Something (only useful when derailed)
		}
		if(cart instanceof Animals) {
			return false;	//Animals are not cars...
		}
		
		
		Location loc = cart.getLocation();
		Block b = loc.getBlock();
		loc.setY(loc.getY() - 1);
		Block underblock = loc.getBlock();
		String mat = b.getType().name().toUpperCase();
		String underMat = b.getRelative(BlockFace.DOWN).getType().name().toUpperCase();
		String underUnderMat = b.getRelative(BlockFace.DOWN, 2).getType().name().toUpperCase();
		
		List<String> checks = new ArrayList<String>();
		if(ucars.ignoreRails){
			checks.add("POWERED_RAIL");
			checks.add("RAIL");
			checks.add("RAILS");
			checks.add("DETECTOR_RAIL");
			checks.add("ACTIVATOR_RAIL");
			
			List<String> newRoadBlocks = new ArrayList<>(roadBlocks);
			newRoadBlocks.remove("AIR");								//Keeping the metadata when falling off a cliff after rails
			if(UEntityMeta.hasMetadata(cart,"car.wasOnRails") && cart.getVelocity().getY() == 0 
					&& (plugin.isBlockEqualToConfigIds(newRoadBlocks, underblock) || 
							!ucars.config.getBoolean("general.cars.roadBlocks.enable") && cart.getVelocity().getX() == 0 && cart.getVelocity().getZ() == 0) 
					&& !checks.contains(mat) ) {
				UEntityMeta.removeMetadata(cart,"car.wasOnRails");
			}
			if(UEntityMeta.hasMetadata(cart, "car.wasOnRails")) {
				return false;
			}
		}
		if(checks.contains(mat)
				|| checks.contains(underMat) 
				|| checks.contains(underUnderMat)){
			UEntityMeta.setMetadata(cart, "car.wasOnRails", new StatValue(true, ucars.plugin));
			if(((Minecart)cart).getMaxSpeed() != 0.4 || ((Minecart)cart).getMaxSpeed() == 2.4) {
				((Minecart)cart).setMaxSpeed(0.4);
			}
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
	public void ResetCarBoost(String playername, Vehicle car,
			double defaultSpeed) {
		String p = playername;
		World w = plugin.getServer().getPlayer(p).getLocation().getWorld();
		w.playSound(plugin.getServer().getPlayer(p).getLocation(),
				Sound.ENTITY_BAT_TAKEOFF, 1.5f, -2);
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
								.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1.5f, -2);
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
			if (!(ent instanceof Vehicle)) {
				while (!(ent instanceof Vehicle) && ent.getVehicle() != null) {
					ent = ent.getVehicle();
				}
				if (!(ent instanceof Vehicle)) {
					return false;
				}
			}
			Vehicle cart = (Vehicle) ent;
			return isACar(cart);
		} catch (Exception e) {
			// Server reloading
			return false;
		}
	}
	
	public Entity getDrivingPassengerOfCar(Vehicle vehicle){ //Get the PLAYER passenger of the car
		if (vehicle.getPassengers().isEmpty() || !(vehicle instanceof Entity)) { //If it has nobody riding it, ignore it
			return null;
		}
		Entity passenger = vehicle.getPassengers().get(0); //The vehicle's lowest passenger; may be a pig, etc... if pigucarting
		if (!(passenger instanceof Player)) { //If not a player riding it; then keep looking until we find a player
			while (!(passenger instanceof Player)
					&& !passenger.getPassengers().isEmpty()) { //While there's more entities above this in the 'stack'
				passenger = passenger.getPassengers().get(0); //Keep iterating
			}
		}
		return passenger;
	}
	
	@EventHandler
	void carExit(VehicleExitEvent event){
		UEntityMeta.removeMetadata(event.getVehicle(), "car.vec");
		UEntityMeta.removeMetadata(event.getExited(), "ucars.smooth");
		event.getVehicle().removeMetadata("car.vec", ucars.plugin);
		event.getExited().removeMetadata("ucars.smooth", ucars.plugin);
		if(event.getVehicle().hasMetadata("safeExit.ignore")
				|| UEntityMeta.hasMetadata(event.getVehicle(), "safeExit.ignore")){
			return;
		}
		if(!(event.getVehicle() instanceof Vehicle) || !isACar((Vehicle) event.getVehicle())){
			return;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void carRemove(VehicleDestroyEvent event){
		if(event.isCancelled()){
			return;
		}
		UEntityMeta.removeMetadata(event.getVehicle(), "car.vec");
		event.getVehicle().removeMetadata("car.vec", ucars.plugin);
		final Vehicle v = event.getVehicle();
		Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				UEntityMeta.removeAllMeta(v);
				return;
			}}, 100l);
	}
	
	@EventHandler
	void entityDeath(EntityDeathEvent event){
		final Entity e = event.getEntity();
		if(e instanceof Player){
			return;
		}
		Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				UEntityMeta.removeAllMeta(e);
				return;
			}}, 100l);
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
		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[wir]")){
			if(!event.getPlayer().hasPermission("wirelessredstone")){
				event.getPlayer().sendMessage(ChatColor.RED+"Sorry you need the permisson 'wirelessredstone' to do this!");
				lines[0] = "";
			}
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //Called first
	public void playerJoinControlsUnlock(PlayerJoinEvent event){
		ControlSchemeManager.setControlsLocked(event.getPlayer(), false);
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
		if(!(vehicle instanceof Vehicle)){
			return;
		}
		Entity passenger = getDrivingPassengerOfCar(vehicle); //Gets the entity highest in the passenger 'stack' (Allows for pigucart, etc...)
		Boolean driven = passenger != null && passenger instanceof Player;
		if(!driven){
			return; //Forget extra car physics if minecart isn't manned, takes too much strain with extra entities
		}

		Vector travel = vehicle.getVelocity();

		if(event instanceof ucarUpdateEvent){
			travel = ((ucarUpdateEvent) event).getTravelVector().clone();
		}
		if(!(event instanceof ucarUpdateEvent)){ //If it's just the standard every tick vehicle update event...
			if(UEntityMeta.hasMetadata(vehicle, "car.vec")){ //If it has the 'car.vec' meta, we need to use RACE CONTROLS on this vehicle
				ucarUpdateEvent evt = (ucarUpdateEvent) UEntityMeta.getMetadata(vehicle, "car.vec").get(0).value(); //Handle the update event (Called here not directly because otherwise ppl with a better connection fire more control events and move marginally faster)
				evt.player = ((Player)passenger); //Set the player (in the car) onto the event so it can be handled by uCarUpdate handlers
				evt.incrementRead(); //Register that the control input update has been executed (So if no new control input event within 2 ticks; we know to stop the car)
				UEntityMeta.removeMetadata(vehicle, "car.vec"); //Update the 'car.vec' metadata with an otherwise identical event; but without the player object attached
				ucarUpdateEvent et = new ucarUpdateEvent(vehicle, evt.getTravelVector().clone(), null, evt.getDir()); //Clone of the other event, except no player object attached
				et.setRead(evt.getReadCount()); //Make sure it IS a clone (With correct variable values)
				UEntityMeta.setMetadata(vehicle, "car.vec", new StatValue(et, ucars.plugin)); //Update the meta on the car
				/*ucars.plugin.getServer().getPluginManager().callEvent(evt); //Actually handle the uCarUpdateEvent
*/
				if(!ucars.fireUpdateEvent){
					onUcarUpdate(evt);
				} else {
					ucars.plugin.getServer().getPluginManager().callEvent(evt);
				}
				/*return;*/
			}
		}	
		//Everything below this (in this method) is executed EVERY MC vehicle update (every tick) and every ucar update
		Block normalblock = vehicle.getLocation().getBlock();

		Player player = null;
		if (driven) {
			player = (Player) passenger;
		}
		if (!carsEnabled) {
			return;
		}

		Vehicle car = (Vehicle) vehicle;
		
		if (!isACar(car)) {
			return;
		}
		
		Vector vel = car.getVelocity();
		
		if (vel.getY() > 0.1
				&& !UEntityMeta.hasMetadata(car, "car.falling")
				&& !UEntityMeta.hasMetadata(car, "car.ascending")) { // Fix jumping bug (Where car just flies up infinitely high when clipping a block)
														// in most occasions
			if (UEntityMeta.hasMetadata(car, "car.jumping")) {
				/*vel.setY(2.5);*/
				UEntityMeta.removeMetadata(car, "car.jumping");
			} else if (UEntityMeta.hasMetadata(car, "car.jumpFull")) {
				// Jumping a full block
				if (vel.getY() > 10) {
					vel.setY(5);
				}
				UEntityMeta.removeMetadata(car, "car.jumpFull");
			} else if(car.hasMetadata("car.inertialYAxis")) {
				//Do nothing
			} else {
				vel.setY(0); 
			}
			car.setVelocity(vel);
		}
		
		// Make jumping work when not moving
		// Calculate jumping gravity
		if(UEntityMeta.hasMetadata(car, "car.jumpUp")){
			double amt = (Double) UEntityMeta.getMetadata(car, "car.jumpUp").get(0).value();
			UEntityMeta.removeMetadata(car, "car.jumpUp");
			if(amt >= 1.5){
				double y = amt * 0.1;
				UEntityMeta.setMetadata(car, "car.jumpUp", new StatValue(amt-y, plugin));
				vel.setY(y);
				car.setVelocity(vel);
				return; //We don't want any further calculations
			}
			else{ //At the peak of ascent
				UEntityMeta.setMetadata(car, "car.falling", new StatValue(0.01, plugin));
				//car.setMetadata("car.fallingPause", new StatValue(1, plugin));
			}
			
		}
		if (UEntityMeta.hasMetadata(car, "car.falling")) {
			double gravity = (Double) UEntityMeta.getMetadata(car, "car.falling").get(0).value();
			double newGravity = gravity + (gravity * 0.6);
			UEntityMeta.removeMetadata(car, "car.falling");
			if ((gravity <= 0.6)) {
				UEntityMeta.setMetadata(car, "car.falling", new StatValue(
						newGravity, ucars.plugin));
				vel.setY(-(gravity * 1.333 + 0.2d));
				car.setVelocity(vel);
			}
		}

		//Start health calculations
		CarHealthData health = getCarHealthHandler(car);
		Boolean recalculateHealth = false;
		// Calculate health based on location
		if (normalblock.getType().equals(Material.WATER)) {
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
					player.sendMessage(ChatColor.RED + "-" + damage + " ["
							+ Material.WATER.name().toLowerCase() + "]"
							+ color + " (" + left + ")");
				}
				health.damage(damage, car);
				recalculateHealth = true;
			}
		}
		if (normalblock.getType().equals(Material.LAVA)) {
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
					player.sendMessage(ChatColor.RED + "-" + damage + " ["
							+ Material.LAVA.name().toLowerCase() + "]"
							+ color + " (" + left + ")");
				}
				health.damage(damage, car);
				recalculateHealth = true;
			}
		}
		if (recalculateHealth) {
			updateCarHealthHandler(car, health);
		}

		float a = 1;
		if(event instanceof ucarUpdateEvent && ucars.smoothDrive){ //If acceleration is enabled
			a = ControlInput.getAccel(((ucarUpdateEvent)event).getPlayer(), ((ucarUpdateEvent)event).getDir()); //Find out the multiplier to use for accelerating the car 'naturally'
			CarDirection driveDir = ControlInput.getCurrentDriveDir(player);
			if(driveDir.equals(CarDirection.BACKWARDS)){
				a *= 0.2; //0.2 speed backwards
			}
			travel.setX(travel.getX() * a); //Multiple only x
			travel.setZ(travel.getZ() * a); //and z with it (No y acceleration)
		}
		Vector dirVec = travel.clone().setY(0).normalize();

		if(dirVec.lengthSquared() > 0.01 /*dirVec.lengthSquared() > 0.1 && Math.abs(a) > 0.2 && *//*event.getDir() != null && !event.getDir().equals(CarDirection.NONE)*/){
			Location dirLoc = new Location(car.getWorld(), 0, 0, 0); //Make sure car always faces the RIGHT "forwards"
			if(event instanceof ucarUpdateEvent && ((ucarUpdateEvent) event).getDir().equals(CarDirection.BACKWARDS)){
				dirVec = dirVec.multiply(-1);
			}
			CarDirection driveDir = ControlInput.getCurrentDriveDir(player);
			if(driveDir.equals(CarDirection.BACKWARDS)){
				dirVec = dirVec.multiply(-1);
			}
			dirLoc.setDirection(dirVec);
			float yaw = dirLoc.getYaw()+90;
			/*if(event.getDir().equals(CarDirection.BACKWARDS)){
				yaw += 180;
			}*/
			if(a < 0){
				yaw -= 180;
			}
			while(yaw < 0){
				yaw = 360 + yaw;
			}
			while(yaw >= 360){
				yaw = yaw - 360;
			}
			CartOrientationUtil.setYaw(car, yaw);
			/*WrapperPlayServerEntityLook p = new WrapperPlayServerEntityLook();
			p.setEntityID(car.getEntityId());
			p.setYaw(yaw);
			p.setPitch(car.getLocation().getPitch());
			p.sendPacket(player);*/
		}
		// End health calculations

		if (plugin.protocolLib) {
			return;
		}
		/*// Attempt pre-protocollib controls (Broken in MC 1.6.0 and probably above)

		Vector playerVelocity = car.getPassenger().getVelocity();
		ucarUpdateEvent ucarupdate = new ucarUpdateEvent(car,
				playerVelocity, player, CarDirection.NONE);
		plugin.getServer().getPluginManager().callEvent(ucarupdate);
		return;*/
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
			UEntityMeta.removeMetadata(vehicle, "car.vec");
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
		
		if(!(vehicle instanceof Vehicle)){
			return;
		}
		
		if (!carsEnabled) {
			return;
		}
		
		try {
			if (licenseEnabled
					&& !plugin.licensedPlayers.contains(player.getName())) {
				player.sendMessage(ucars.colors.getError()
						+ Lang.get("lang.licenses.noLicense"));
				return;
			}
		} catch (Exception e1) {
		}
		
		Vehicle car = (Vehicle) vehicle;

		if(!isACar(car)){
			return;
		}
		
		if (!(player.isInsideVehicle())) {
			return;
		}
				
		//Valid vehicle!
		
		/*Block next = car.getLocation().clone().add(event.getTravelVector().clone().setY(0)).getBlock();
		Block underNext = next.getRelative(BlockFace.DOWN);
		Block underunderNext = next.getRelative(BlockFace.DOWN, 2);*/
		
		CarHealthData health = this.getCarHealthHandler(car);
		Boolean recalculateHealth = false;
		
		if (car.getVelocity().getY() > 0.01
				&& !UEntityMeta.hasMetadata(car, "car.falling")
				&& !UEntityMeta.hasMetadata(car, "car.ascending")) {
			modY = false;
		}
		if (UEntityMeta.hasMetadata(car, "car.jumping")) {
			if (!UEntityMeta.hasMetadata(car, "car.ascending")) {
				modY = false;
			}
			UEntityMeta.removeMetadata(car, "car.jumping");
		}
		if(car instanceof Minecart && ((Minecart) car).getMaxSpeed() != 5) {
			((Minecart)car).setMaxSpeed(5); // Don't allow game breaking speed - but faster than default
		}
		
		// Calculate road blocks
		if (roadBlocksEnabled) {
			/*Location loc = car.getLocation().getBlock()
					.getRelative(BlockFace.DOWN).getLocation();*/
			
			if(!plugin.isBlockEqualToConfigIds(roadBlocks, underblock)){
				//Not a road block being driven on, so don't move
				return;
			}
		}
		
		Location loc = car.getLocation();
		if (!ucars.playersIgnoreTrafficLights && atTrafficLight(car, underblock, underunderblock, loc)){
			return; //Being told to wait at a traffic light, don't move
		}
		
		// Calculate default effect blocks
		if (effectBlocksEnabled) {
			if (plugin.isBlockEqualToConfigIds(blockBoost,
					underblock)
					|| plugin.isBlockEqualToConfigIds(
							blockBoost, underunderblock)) {
				carBoost(player.getName(), 20, 6000,
						defaultSpeed);
			}
			if (plugin.isBlockEqualToConfigIds(
					highBlockBoost, underblock)
					|| plugin.isBlockEqualToConfigIds(
							highBlockBoost, underunderblock)) {
				carBoost(player.getName(), 50, 8000,
						defaultSpeed);
			}
			if (plugin.isBlockEqualToConfigIds(
					resetBlockBoost, underblock)
					|| plugin
							.isBlockEqualToConfigIds(
									resetBlockBoost,
									underunderblock)) {
				ResetCarBoost(player.getName(), car,
						defaultSpeed);
			}
		}
		
		Vector travel = event.getTravelVector(); // Travel Vector,
															// fixes
															// controls for
															// 1.6
		
		if(car.hasMetadata("car.inertialYAxis")) {
			travel.setY(event.getVehicle().getVelocity().getY());
		}
			
		float a = 1;
		if(ucars.smoothDrive){ //If acceleration is enabled
			a = ControlInput.getAccel(event.getPlayer(), event.getDir()); //Find out the multiplier to use for accelerating the car 'naturally'
			CarDirection driveDir = ControlInput.getCurrentDriveDir(event.getPlayer());
			if(driveDir.equals(CarDirection.BACKWARDS)){
				a *= 0.2; //0.2 speed backwards
			}
			travel.setX(travel.getX() * a); //Multiple only x
			travel.setZ(travel.getZ() * a); //and z with it (No y acceleration)
		}
		
		//Vector dirVec = travel.clone().setY(0).normalize();
		/*try {
			dirVec = (Vector) (car.hasMetadata("ucarsSteeringDir") ? car.getMetadata("ucarsSteeringDir").get(0).value() : travel.clone().normalize());
		} catch (Exception e2) {
			dirVec = travel.clone().normalize();
		}*/

		
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
		String underunderMat = underunderblock.getType().name().toUpperCase();
		// calculate speedmods
		String key = underMat;
		if(speedMods.containsKey(key)){
			if(!ucars.carBoosts.containsKey(player.getName())){
				multiplier = speedMods.get(key);
			}
			else{
				multiplier = (speedMods.get(key)+multiplier)*0.5; //Mean Average of both
			}
		}
		key = underunderMat;
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
		
		travel = travel.setX(travel.getX()*multiplier);
		travel = travel.setZ(travel.getZ()*multiplier); 
		if (usePerms) {
			if (!player.hasPermission("ucars.cars")) {
				player.sendMessage(ucars.colors.getInfo()
						+ Lang.get("lang.messages.noDrivePerm"));
				return;
			}
		}
		
		/*if (normalblock.getType() != Material.AIR //Air
				&& normalblock.getType() != Material.WATER //Water
				&& normalblock.getType() != Material.STATIONARY_WATER //Water
				&& normalblock.getType() != Material.STEP //Slab
				&& normalblock.getType() != Material.DOUBLE_STEP //Double slab
				&& normalblock.getType() != Material.LONG_GRASS //Long grass
				&& !normalblock.getType().name().toLowerCase()
						.contains("stairs")) {
			// Stuck in a block
			car.setVelocity(new Vector(0, 0.5, 0));
		}*/
		
		Location before = car.getLocation();
		//float dir = player.getLocation().getYaw();
		float dir = car.getLocation().clone().setDirection(travel).getYaw();
		BlockFace faceDir = ClosestFace.getClosestFace(dir);
		// before.add(faceDir.getModX(), faceDir.getModY(),
		// faceDir.getModZ());

		//Read the vehicle length if it exists
		double length = car.getWidth();

		double fx = travel.getX()*1;
		if (Math.abs(fx) > 1) {
			fx = faceDir.getModX();
		}
		double fz = travel.getZ()*1;
		if (Math.abs(fz) > 1) {
			fz = faceDir.getModZ();
		}

		//Compute unit vector in car direction of travel
		Vector unitVec = new Vector(faceDir.getModX(),0,faceDir.getModZ());
		if(unitVec.lengthSquared() > 1){
			unitVec.multiply(1/(double)Math.sqrt(2));
		}

		Vector faceDirVec = new Vector(fx, faceDir.getModY(), fz);
		before=before.add(faceDirVec);
		//Add the length of the car in so that we are able to climb up blocks with an entity that has length
		before=before.add(unitVec.clone().multiply(length*0.5));
		Location frontRight = before.clone().add(unitVec.clone().multiply(length*0.5));
		Location frontLeft = before.clone().add(unitVec.clone().multiply(length*-0.5));
		Block block = before.getBlock(); //Block we're driving into
		Block frontRightInFront = frontRight.getBlock();
		Block frontLeftInFront = frontLeft.getBlock();
		//Hackish way to make this able to jump for wider vehicles
		boolean blockNoJump = noJump(block.getType().name());
		if(blockNoJump){
			if(!noJump(frontRightInFront.getType().name())){
				block = frontRightInFront;
			}
			else if(!noJump(frontLeftInFront.getType().name())){
				block = frontLeftInFront;
			}
		}
		/*Block above = block.getRelative(BlockFace.UP);
		
		if((!(block.isEmpty() || block.isLiquid())
				&& !(above.isEmpty() || above.isLiquid())
				&& !(block.getType().name().toLowerCase().contains("step"))
				*//*&& !(above.getType().name().toLowerCase().contains("step"))*//*)
		){
			*//*ControlInput.setAccel(player, 0); //They hit a wall head on*//*
		}*/
		
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
				player.sendMessage(ChatColor.RED + "-" + damage + " ["
						+ Material.CACTUS.name().toLowerCase() + "]"
						+ color + " (" + left + ")");
				health.damage(damage, car);
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
		
		/*if (travel.getY() < 0) { //Custom gravity
			double a1 = multiplier*a;
			if(a1 < 1){
				a1 = 1;
			}
			double newy = travel.getY() - (Math.abs(travel.getY())*0.02d)/a1;
			if(newy < -5){
				newy = -5;
			}
			if(newy > 0){
				newy = -0.2;
			}
			travel.setY(newy);
		}*/
		
		Boolean fly = false; // Fly is the 'easter egg' slab elevator
		if (normalblock.getRelative(faceDir).getType().name().toLowerCase().contains("slab")) {
			// If looking at slabs
			fly = true;
		}
		/*
		 * if(bbb.getType()==Material.STEP && !(bbb.getData() != 0)){ //If
		 * in a slab block fly = true; }
		 */
		if (effectBlocksEnabled) { //Has to be in this order for things to function properly - Cannot be merged with earlier effect block handling
			if (plugin.isBlockEqualToConfigIds(jumpBlock,
					underblock)
					|| plugin.isBlockEqualToConfigIds(
							jumpBlock, underunderblock)) {
				double y = 0;
				for(String bl : jumpBlock) {
					if(bl.contains(underblock.getType().name()) || bl.contains(underunderblock.getType().name())) {
						if(bl.contains("-")) {
							y = Double.valueOf(bl.split("-")[1]);
						} else {
							y = uCar_jump_amount;
						}
					}
					
				}
				UEntityMeta.setMetadata(car, "car.jumpUp", new StatValue(y, plugin));
				travel.setY(y);
				car.setVelocity(travel);
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
						UEntityMeta.setMetadata(car, "safeExit.ignore", new StatValue(null, plugin));
						
						String xs = lines[1];
						String ys = lines[2];
						String zs = lines[3];
						Boolean valid = true;
						double x = 0, y = 0, z = 0;
						
						try {
							if(xs.contains("~")) {
								x = loc.getX() + Double.parseDouble(xs.replace("~", "") + 0)/10;
							} else {x = Double.parseDouble(xs);};
							if(ys.contains("~")) {
								y = loc.getY() + Double.parseDouble(ys.replace("~", "") + 0)/10;
							} else {y = Double.parseDouble(ys);};
							if(zs.contains("~")) {
								z = loc.getZ() + Double.parseDouble(zs.replace("~", "") + 0)/10;
							} else {z = Double.parseDouble(zs);};
							
							y = y + 0.5;
						} catch (NumberFormatException e) {
							valid = false;
						}
						if (valid) {
							List<MetadataValue> normalMeta = null;
							List<MetadataValue> otherMeta = null;
							if (player.hasMetadata("car.stayIn") || UEntityMeta.hasMetadata(player, "car.stayIn")) {
								normalMeta = player.getMetadata("car.stayIn");
								otherMeta = UEntityMeta.getMetadata(player, "car.stayIn");
								for (MetadataValue val : normalMeta) {
									player.removeMetadata("car.stayIn", val.getOwningPlugin());
								}
								if(otherMeta != null) {
									for (MetadataValue val : otherMeta) {
										UEntityMeta.removeMetadata(player, "car.stayIn");
									}
								}
							}
							car.eject();
							
							UUID carId = car.getUniqueId();
							
							final Location toTele = new Location(s.getWorld(), x,
									y, z);
							Chunk ch = toTele.getChunk();
							if (!ch.isLoaded()) {
								ch.load(true);
							}
							player.teleport(toTele.clone().add(0,1,0));
							uCarRespawnEvent evnt = new uCarRespawnEvent(car, carId, car.getUniqueId(),
									CarRespawnReason.TELEPORT);
							plugin.getServer().getPluginManager().callEvent(evnt);
							if(evnt.isCancelled()){
								car.remove();
							} else{
								player.sendMessage(ucars.colors.getTp()
										+ "Teleporting...");
								final Vehicle ucar = car;
								Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
									@Override
									public void run() {
										ucar.teleport(toTele);
										ucar.addPassenger(player); //For the sake of uCarsTrade
										return;
									}}, 2l);
								car.setVelocity(travel);
								if (normalMeta != null) {
									for (MetadataValue val : normalMeta) {
										player.setMetadata("car.stayIn", val);
									}
								}
								if (otherMeta != null) {
									for (MetadataValue val : otherMeta) {
										UEntityMeta.setMetadata(player, "car.stayIn", val);
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
		
		// actually jump up a block if needed:
		Location theNewLoc = block.getLocation();
		Location bidUpLoc = block.getLocation().add(0, 1, 0);
		Material bidU = bidUpLoc.getBlock().getType();
		Boolean cont = true;
		// check it's not a barrier
		cont = !plugin.isBlockEqualToConfigIds(barriers, block) && !plugin.isBlockEqualToConfigIds(barriers, frontLeftInFront) && !plugin.isBlockEqualToConfigIds(barriers, frontRightInFront);
		
		Boolean inStairs = false;
		Block carBlock = car.getLocation().getBlock();
		Material carBlockType = carBlock.getType();
		if (carBlockType.name().toLowerCase().contains("stairs")) {
			inStairs = true;
		}
		if (UEntityMeta.hasMetadata(car, "car.ascending")) {
			UEntityMeta.removeMetadata(car, "car.ascending");
		}
		//player.sendMessage(block.getType().name()+" "+faceDir+" "+fx+" "+fz);
		// Make cars jump if needed
		if (inStairs ||
				 (!blockNoJump && !block.isPassable() && cont && modY &&
				 !(softBlocks.contains(block.getType().name()) && softBlocks.contains(carBlockType.name()) ) )) { //Softblocks floating problem
			//Should jump
			
			boolean calculated = false;
			if (bidU == Material.AIR || bidU == Material.LAVA || bidU == Material.WATER || bidUpLoc.getBlock().isPassable() || noJump(bidU.name()) || inStairs) { //Clear air above
				theNewLoc.add(0, 1.5d, 0);
				double y = 0.0;
				if(block.getBoundingBox().getMaxY() != car.getLocation().getBlock().getBoundingBox().getMaxY() ||
						(car.getBoundingBox().getMinY() < car.getLocation().getBlock().getBoundingBox().getMaxY() && !inStairs)) { //Check if we're staying on the same level (slabs, carpet etc -> no need to climb if not)
					calculated = true;
					y = block.getBoundingBox().getMaxY()-block.getLocation().getBlockY() + 0.2;
				}
				
				if (carBlockType.name().toLowerCase()
						.contains(Pattern.quote("stairs"))
						// ||
						// underblock.getType().name().toLowerCase().contains(Pattern.quote("stairs"))
						|| block.getType().name().toLowerCase()
								.contains(Pattern.quote("stairs"))
						|| inStairs) {
					calculated = true;
					y = 1.05;
					// ascend stairs
				}
				if (car.getFallDistance() > 1.5) { //Prevents Fall Distance stacking up causing fall-damage when climbing longer slopes
					y = y*0.95;
				}
				Boolean ignore = false;
				if (car.getVelocity().getY() > 4) {
					// if car is going up already then don't do ascent
					ignore = true;
				}
				if (!ignore) {
					// Do ascent
					travel.setY(block.getY()+y-car.getLocation().getY());
					if (calculated) {
						UEntityMeta.setMetadata(car, "car.jumping", new StatValue(null,
								plugin));
					} else {
						UEntityMeta.setMetadata(car, "car.jumpFull", new StatValue(null,
								plugin));
					}
				}
			}
			if (fly && cont && (bidUpLoc.getBlock().getType().name().toLowerCase().contains("slab") || underblock.isEmpty() && !carBlockType.name().toLowerCase().contains("slab"))) {
				// Make the car ascend (easter egg, slab elevator)
				travel.setY(0.1); // Make a little easier
				UEntityMeta.setMetadata(car, "car.ascending", new StatValue(null, plugin));
			}
			// Account for speed increase when climbing
			if(calculated && car.getType() == EntityType.MINECART) {
				travel.multiply(new Vector(SIMULATED_FRICTION_SPEED_MULTIPLIER,1,SIMULATED_FRICTION_SPEED_MULTIPLIER));
			}
			// Move the car and adjust vector to fit car stats
			car.setVelocity(calculateCarStats(car, player, travel,
					multiplier));
		} else {
			if (fly) {
				// Make the car ascend (easter egg, slab elevator)
				travel.setY(0.1); // Make a little easier
				UEntityMeta.setMetadata(car, "car.ascending", new StatValue(null, plugin));
			}
			// Account for speed increase when going down
			if(car.getFallDistance() > 0.0 && car.getType() == EntityType.MINECART) {
				travel.multiply(new Vector(SIMULATED_FRICTION_SPEED_MULTIPLIER,1,SIMULATED_FRICTION_SPEED_MULTIPLIER));
			}
			// Move the car and adjust vector to fit car stats
			car.setVelocity(calculateCarStats(car, player, travel,
					multiplier));
		}

		//Pitch the car
		if(travel.getY()!=0 && pitchEnabled) {
			if(travel.getY()<0) {
				CartOrientationUtil.setPitch(car, (float) (-35*travel.getY()));
			} else {
				CartOrientationUtil.setPitch(car, (float) (-20*travel.getY()));
			}
		} else {
			CartOrientationUtil.setPitch(car, 0);
		}

		// Recalculate car health
		if (recalculateHealth) {
			updateCarHealthHandler(car, health);
		}
		return;
	}

	/*
	 * This disables minor fall damage whilst driving a car
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	void safeFly(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.FALL) {
			return;
		}
		Player p = (Player) event.getEntity();
		if (inACar(p.getName())) {
			if(!disableFallDamage) {
				Vector vel = p.getVehicle().getVelocity();
				if (vel.getY() > -0.1 && vel.getY() < 0.1) {
					event.setCancelled(true);
				}/*else {
					try {
						p.damage(event.getDamage());
					} catch (Exception e) {
						// Damaging failed
					}
				}*/
			} else {
				event.setCancelled(true);
			}
		}
		return;
	}

	/*
	 * This provides effects and health changes when cars collide with entities
	 */
	@EventHandler
	void hitByCar(VehicleEntityCollisionEvent event) {
		if(event.isCancelled()){
			return;
		}
		Vehicle veh = event.getVehicle();
		if (!(veh instanceof Vehicle)) {
			return;
		}
		final Vehicle cart = (Vehicle) veh;
		if (!isACar(cart)) {
			return;
		}
		Entity ent = event.getEntity(); //copCar
		if(((cart.hasMetadata("trade.npc") && ent.hasMetadata("trade.npcvillager"))
				|| UEntityMeta.hasMetadata(cart, "trade.npc") && UEntityMeta.hasMetadata(ent, "trade.npcvillager"))
				|| ((cart.hasMetadata("trade.npc") && ent.getVehicle() != null && ent.getVehicle().hasMetadata("trade.npc"))
						&& UEntityMeta.hasMetadata(cart, "trade.npc") && ent.getVehicle() != null && UEntityMeta.hasMetadata(ent.getVehicle(), "trade.npc"))){
			event.setCancelled(true);
			event.setCollisionCancelled(false);
			return;
		}
		if(UEntityMeta.hasMetadata(ent, "IGNORE_COLLISIONS")){
			event.setCancelled(true);
			event.setCollisionCancelled(false);
			return;
		}
		/*if(cart.hasMetadata("copCar") || UEntityMeta.hasMetadata(cart, "copCar")){ 
			Bukkit.broadcastMessage("CANCELLED AS COP CAR");
			event.setCancelled(true);
			event.setCollisionCancelled(false);
			return;
		}*/
		if (cart.isEmpty()) { //Don't bother to calculate with PiguCarts, etc...
			return;
		}
		
		Entity passenger = getDrivingPassengerOfCar(cart);
		if(passenger.equals(ent) || cart.getPassengers().contains(ent)){
			return; //Player being hit is in the car
		}
		
		if(ent.hasMetadata("copCar") || UEntityMeta.hasMetadata(ent, "copCar") || (ent.getVehicle() != null && (ent.getVehicle().hasMetadata("copCar") || UEntityMeta.hasMetadata(ent.getVehicle(), "copCar")))){
			if(!(passenger instanceof Player)){
				event.setCancelled(true);
				event.setCollisionCancelled(false);
				return;
			}
		}
		
		if(UEntityMeta.hasMetadata(ent, "hitByLast")){
			try {
				long l = (Long) UEntityMeta.getMetadata(ent, "hitByLast").get(0).value();
				long pastTime = System.currentTimeMillis() - l;
				if(pastTime < 500){
					return; //Don't get hit by more than once at a time
				}
				else {
					UEntityMeta.removeMetadata(ent, "hitByLast");
				}
			} catch (Exception e) {
				UEntityMeta.removeMetadata(ent, "hitByLast");
			}
		}
		UEntityMeta.removeMetadata(ent, "hitByLast");
		UEntityMeta.setMetadata(ent, "hitByLast", new StatValue(System.currentTimeMillis(), ucars.plugin));
		
		/*double accel = 1;
		if(passenger instanceof Player){
			accel = ControlInput.getAccel(((Player)passenger), CarDirection.FORWARDS);
		}
		else {
			accel = UEntityMeta.hasMetadata(cart, "currentlyStopped") ? 0:1;
		}*/
		Vector vel = cart.getVelocity();
		double speed = vel.length() * 1.6; /*
		if(passenger instanceof Villager){ //NPC car from UT
			speed = cart.getVelocity().length()*1.6;
		}*/
		
		double damage = hitby_crash_damage;
		double pDmg = (damage * speed * 2);
		if(pDmg < 1){
			pDmg = 1;
		}
		if(pDmg > (hitby_crash_damage * 1.5)){
			pDmg = hitby_crash_damage * 1.5;
		}
		if(pDmg > 8){
			pDmg = 8;
		}
		
		Entity driver = getDrivingPassengerOfCar(veh);
		
		if (speed > 0) {
			CarHealthData health = getCarHealthHandler(cart);
			double dmg = crash_damage;
			if (dmg > 0) {
				if (cart.getPassengers().get(0) instanceof Player) {
					double max = defaultHealth;
					double left = health.getHealth() - dmg;
					ChatColor color = ChatColor.YELLOW;
					if (left > (max * 0.66)) {
						color = ChatColor.GREEN;
					}
					if (left < (max * 0.33)) {
						color = ChatColor.RED;
					}
					((Player) cart.getPassengers().get(0))
							.sendMessage(ChatColor.RED + "-" + ((int)dmg) + "[crash]"
									+ color + " (" + ((int)left) + ")");
				}
				health.damage(dmg, cart);
			}
			updateCarHealthHandler(cart, health);
		}
		if (speed <= 0) {
			return;
		}
		if (!ucars.config.getBoolean("general.cars.hitBy.enable")) {
			return;
		}
		if (ucars.config.getBoolean("general.cars.hitBy.enableMonsterDamage")) {
			if (ent instanceof Monster || (ucars.config.getBoolean("general.cars.hitBy.enableAllMonsterDamage") && ent instanceof Damageable)) {
				if(ent instanceof Villager && ent.getVehicle() != null && passenger instanceof Villager){
					return;
				}
				uCarCrashEvent evt = new uCarCrashEvent(cart, ent, pDmg);
				if(evt.isCancelled()){
					return;
				}
				pDmg = evt.getDamageToBeDoneToTheEntity();
				
				double mult = ucars.config
						.getDouble("general.cars.hitBy.power") / 7;
				ent.setVelocity(cart.getVelocity().clone().setY(0.5).multiply(mult));
				
				if(driver != null && driver.equals(ent)){
					
				}
				else if(driver != null){
					((Damageable) ent).damage(pDmg, driver);
				}
				else {
					((Damageable) ent).damage(pDmg);
				}
			}
		}
		
		boolean player = ent instanceof Player;
		Player p = null;
		if(player){
			p = (Player) ent;
		}
		if (!(player)) {
			return;
		}
		if(p != null){
			if (inACar(p)) {
				return;
			}
		}
		
		uCarCrashEvent evt = new uCarCrashEvent(cart, ent, pDmg);
		Bukkit.getPluginManager().callEvent(evt);
		if(evt.isCancelled()){
			return;
		}
		pDmg = evt.getDamageToBeDoneToTheEntity();
		
		double mult = ucars.config.getDouble("general.cars.hitBy.power") / 5;
		ent.setVelocity(cart.getVelocity().clone().setY(0.5).multiply(mult));
		if(p != null){
			p.sendMessage(ucars.colors.getInfo()
				+ Lang.get("lang.messages.hitByCar"));
		}
		/*p.sendMessage("Speed: "+speed);
		p.sendMessage("Crash dmg def: "+hitby_crash_damage);
		p.sendMessage("Damage to do: "+pDmg);*/
		if(ent instanceof LivingEntity){
			((LivingEntity)ent).damage(pDmg, driver);
		}
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
		if(event.getHand() == null){
			return;
		}
		if(event.getHand().equals(EquipmentSlot.OFF_HAND)){
			return;
		}
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		if (uCarsAPI.getAPI().isuCarsHandlingPlacingCars() && (plugin.API.hasItemCarCheckCriteria() || event.getPlayer().getInventory().getItemInMainHand().getType() == Material.MINECART)) {
			// Its a minecart!
			Material iar = block.getType();
			if (ucars.ignoreRails && (iar == Material.RAIL || iar == Material.ACTIVATOR_RAIL 
					|| iar == Material.POWERED_RAIL || iar == Material.DETECTOR_RAIL)) {
				return;
			}
			if (!PlaceManager.placeableOn(block, plugin)) {
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
			if(!plugin.API.runCarChecks(event.getPlayer().getInventory().getItemInMainHand())){
				return;
			}
			Location loc = block.getLocation().add(0.5, 1.5, 0.5);
			loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			final Minecart car = (Minecart) event.getPlayer().getWorld()
					.spawnEntity(loc, EntityType.MINECART);
			float yaw = event.getPlayer().getLocation().getYaw()+90;
			if(yaw < 0){
				yaw = 360 + yaw;
			}
			else if(yaw >= 360){
				yaw = yaw - 360;
			}
			CartOrientationUtil.setYaw(car, yaw);
			updateCarHealthHandler(car, getCarHealthHandler(car));
			/*
			 * Location carloc = car.getLocation();
			 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
			 * car.setVelocity(new Vector(0,0,0));
			 */
			event.getPlayer().sendMessage(
					ucars.colors.getInfo() + Lang.get("lang.messages.place"));
			event.getPlayer().sendMessage(ucars.colors.getInfo()+"You can also use 'jump' to change driving mode!");
			if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
				ItemStack placed = event.getPlayer().getInventory().getItemInMainHand();
				placed.setAmount(placed.getAmount() - 1);
				event.getPlayer().getInventory().setItemInMainHand(placed);
			}
		}
		if (inACar(event.getPlayer())) {
			if (ucars.config.getBoolean("general.cars.fuel.enable")) {
				if (plugin.isItemEqualToConfigIds(ucars.config.getStringList(
						"general.cars.fuel.check"), event.getPlayer().getInventory().getItemInMainHand())) {
					event.getPlayer().performCommand("ufuel view");
				}
			}
		}
		if(ucars.config.getBoolean("general.cars.boostsEnable")){
			return;
		}
		List<String> LowBoostRaw = ucars.config.getStringList("general.cars.lowBoost");
		List<String> MedBoostRaw = ucars.config.getStringList("general.cars.medBoost");
		List<String> HighBoostRaw = ucars.config.getStringList("general.cars.highBoost");
		// int LowBoostId = ucars.config.getInt("general.cars.lowBoost");
		// int MedBoostId = ucars.config.getInt("general.cars.medBoost");
		// int HighBoostId = ucars.config.getInt("general.cars.highBoost");
		ItemStack inHand = event.getPlayer().getInventory().getItemInMainHand();
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
		if (!(event.getVehicle() instanceof Vehicle)
				|| !(event.getAttacker() instanceof Player)) {
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		final Vehicle car = (Vehicle) event.getVehicle();
		Player player = (Player) event.getAttacker();
		if (!isACar(car)) {
			return;
		}
		if (!ucars.config.getBoolean("general.cars.health.overrideDefault")) {
			return;
		}
		CarHealthData health = getCarHealthHandler(car);
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
					+ " [" + player.getName() + "]" + color + " (" + left + ")");
			health.damage(damage, car, player);
			updateCarHealthHandler(car, health);
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
		Entity cart = event.getCar();
		if(cart.hasMetadata("car.destroyed") || UEntityMeta.hasMetadata(cart, "car.destroyed")){
			return;
		}
		UEntityMeta.setMetadata(cart, "car.destroyed", new StatValue(true, ucars.plugin));
		cart.removeMetadata("car.vec", ucars.plugin);
		UEntityMeta.removeMetadata(cart, "car.vec");
		cart.eject();
		Location loc = cart.getLocation();
		cart.remove();
		loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
		return;
	}
	
	@EventHandler
	void wirelessRedstone(BlockRedstoneEvent event){
		Block block = event.getBlock();
		if(!block.getType().equals(Material.REDSTONE_LAMP)){
			return;
		}
		boolean on = block.isBlockPowered();
		Sign sign = null;
		for(BlockFace dir:BlockFace.values()){
			Block bd = block.getRelative(dir);
			if(bd.getState() instanceof Sign){
				sign = (Sign) bd.getState();
			}
		}
		if(sign == null){
			return;
		}
		
		if(sign.getLine(0) == null || !sign.getLine(0).equalsIgnoreCase("[wir]")){ //Not wireless redstone
			return;
		}
		String otherLoc = sign.getLine(1);
		if(otherLoc == null){ //Match positive and negative numbers
			return; //Invalid sign
		}
		String[] parts = otherLoc.split(",");
		if(parts.length < 3){
			return;
		}
		try {
			int x,y,z;
			if(otherLoc.matches("-*\\d+,-*\\d+,-*\\d+")){
				x = Integer.parseInt(parts[0]);
				y = Integer.parseInt(parts[1]);
				z = Integer.parseInt(parts[2]);
			}
			else {
				//Invalid pattern
				return;
			}
			
			Block otherBlock = block.getWorld().getBlockAt(x, y, z);
			otherBlock.getLocation().getChunk(); //Make sure it's loaded
			if(on){ //Set to redstone block
				otherBlock.setType(Material.REDSTONE_BLOCK);
			}
			else { //Set to glass
				otherBlock.setType(Material.AIR);
			}
		} catch (Exception e) {
			//Not integers
			return;
		}
	}
	
	private int getCoord(String in, int current) throws Exception{
		if(in.matches("-*\\d+")){
			try {
				return Integer.parseInt(in);
			} catch (Exception e) {
				//Not an int
				throw new Exception();
			}
		}
		else if(in.matches("~-*\\d+") && in.length() > 1){
			try {
				return Integer.parseInt(in.substring(1))+current;
			} catch (Exception e) {
				//Not an int
				throw new Exception();
			}
		}
		else {
			//Not formatted right
			throw new Exception();
		}
	}
	
	@EventHandler
	void quit(PlayerQuitEvent event){
		final Player pl = event.getPlayer();
		Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				if(!pl.isOnline()) {
					UEntityMeta.removeAllMeta(pl);
				}
				return;
			}}, 100l);
	}

	@EventHandler
	void quit(PlayerKickEvent event){
		final Player pl = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				UEntityMeta.removeAllMeta(pl);
				return;
			}}, 100l);
	}
	
	@EventHandler
	void trafficIndicators(BlockRedstoneEvent event){
		Block block = event.getBlock();
		if(!block.getType().equals(Material.REDSTONE_LAMP)){
			return;
		}
		boolean on = block.isBlockPowered();
		Sign sign = null;
		for(BlockFace dir:dirs()){
			Block bd = block.getRelative(dir);
			if(bd.getState() instanceof Sign){
				sign = (Sign) bd.getState();
			}
		}
		if(sign == null){
			return;
		}
		
		if(sign.getLine(1) == null || !sign.getLine(1).equalsIgnoreCase("[trafficlight]")){ //Not wireless redstone
			return;
		}
		String otherLoc = sign.getLine(2);
		if(otherLoc == null){ //Match positive and negative numbers
			return; //Invalid sign
		}
		String[] parts = otherLoc.split(",");
		if(parts.length < 3){
			return;
		}
		try {
			int x,y,z;
			if(otherLoc.matches(".+,.+,.+")){
				try {
					x = getCoord(parts[0], sign.getX());
					y = getCoord(parts[1], sign.getY());
					z = getCoord(parts[2], sign.getZ());
				} catch (Exception e1) { //Badly formatted
					return;
				}
			}
			else {
				//Invalid pattern
				return;
			}
			
			Block otherBlock = block.getWorld().getBlockAt(x, y, z);
			for(Entity e:otherBlock.getLocation().getChunk().getEntities()){
				if(e.getLocation().distanceSquared(otherBlock.getLocation()) < 4){ //Within 2 blocks of the loc given
					if(e instanceof ItemFrame){
						ItemFrame ifr = (ItemFrame) e;
						if(on){
							ifr.setItem(new ItemStack(Material.EMERALD_BLOCK));
						}
						else {
							ifr.setItem(new ItemStack(Material.REDSTONE_BLOCK));
						}
					}
				}
			}
		} catch (Exception e) {
			//Not integers
			return;
		}
	}
	
	public Boolean atTrafficLight(Entity car, Block underblock, Block underunderblock, Location loc){
		if (trafficLightsEnabled) {
			if (plugin.isBlockEqualToConfigIds(
					trafficLightRawIds, underblock)
					|| plugin.isBlockEqualToConfigIds(
							trafficLightRawIds,
							underunderblock)
							|| plugin.isBlockEqualToConfigIds(
									trafficLightRawIds,
									underunderblock.getRelative(BlockFace.DOWN))
									|| plugin.isBlockEqualToConfigIds(
											trafficLightRawIds,
											underunderblock.getRelative(BlockFace.DOWN, 2))
											) {
				
				Boolean found = false;
				Boolean on = false;
				int radius = 3;
				int radiusSquared = radius * radius;
				for (int x = -radius; x <= radius && !found; x++) {
					for (int z = -radius; z <= radius && !found; z++) {
						if ((x * x) + (z * z) <= radiusSquared) {
							double locX = loc.getX() + x;
							double locZ = loc.getZ() + z;
							for (int y = (int) Math.round((loc.getY() - 4)); y < (loc
									.getY() + 4) && !found; y++) {
								Location light = new Location(
										loc.getWorld(), locX, y, locZ);
								Block lightBlock = light.getBlock();
								Lightable lightData = (Lightable) lightBlock.getBlockData();
								if (lightBlock.getType() == Material.REDSTONE_TORCH && !lightData.isLit()) {
									if (trafficlightSignOn(light.getBlock())) {
										found = true;
										on = false;
									}
								} else if (lightBlock.getType() == Material.REDSTONE_TORCH && lightData.isLit()) {
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
	
	private static BlockFace[] dirs(){
		return new BlockFace[]{
				BlockFace.NORTH,
				BlockFace.EAST,
				BlockFace.SOUTH,
				BlockFace.WEST,
				BlockFace.NORTH_WEST,
				BlockFace.NORTH_EAST,
				BlockFace.SOUTH_EAST,
				BlockFace.NORTH_WEST,
		};
	}
	
	public void updateCarHealthHandler(Entity car, CarHealthData handler){
		UEntityMeta.removeMetadata(car, "carhealth");
		UEntityMeta.setMetadata(car, "carhealth", new StatValue(handler, ucars.plugin));
	}
	
	public CarHealthData getCarHealthHandler(final Entity car){
		CarHealthData health = null;
		if (UEntityMeta.hasMetadata(car, "carhealth")) {
			try {
				List<MetadataValue> vals = UEntityMeta.getMetadata(car, "carhealth");
				for (MetadataValue val : vals) {
					if (val.value() != null && val.value() instanceof CarHealthData) {
						health = (CarHealthData) val.value();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				UEntityMeta.removeMetadata(car, "carhealth");
				health = null;
			}
		}
		if(health == null){ //Not yet set on cart
			health = new CarHealthData(
					defaultHealth,
					plugin);
		}
		return health;
	}
	
	public static void showCarDamageMessage(Player player, double damage, String cause, double remainingHealth){
		double max = defaultHealth;
		ChatColor color = ChatColor.YELLOW;
		if (remainingHealth > (max * 0.66)) {
			color = ChatColor.GREEN;
		}
		if (remainingHealth < (max * 0.33)) {
			color = ChatColor.RED;
		}
		player.sendMessage(ChatColor.RED + "-" + damage + " ["
				+ cause + "]"
				+ color + " (" + ((int)remainingHealth) + ")");
	}
	
	public static void showCarDamageMessage(Player player, double damage, double remainingHealth){
		double max = defaultHealth;
		ChatColor color = ChatColor.YELLOW;
		if (remainingHealth > (max * 0.66)) {
			color = ChatColor.GREEN;
		}
		if (remainingHealth < (max * 0.33)) {
			color = ChatColor.RED;
		}
		player.sendMessage(ChatColor.RED + "-" + damage + " ["
				+ "Car Health" + "]"
				+ color + " (" + ((int)remainingHealth) + ")");
	}
	
	/*public Runnable defaultDeathHandler(final Minecart cart){
		return new Runnable() {
			// @Override
			public void run() {
				plugin.getServer().getPluginManager()
						.callEvent(new ucarDeathEvent(cart));
			}
		};
	}*/
	
	public boolean noJump(String bName) {
		for(String str:ignoreJump) {
			if(bName.contains(str)) {return true;}
		}
		return false;
	}
	
	public void init() {
		ignoreJump = new ArrayList<String>();
		ignoreJump.add("WALL");
		ignoreJump.add("FENCE");
		ignoreJump.add("GATE");
		
		softBlocks = new ArrayList<String>();
		softBlocks.add("SNOW");
		softBlocks.add("SOUL_SAND");
		softBlocks.add("HONEY_BLOCK");
		
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
		
		hitby_crash_damage = ucars.config
				.getDouble("general.cars.hitBy.damage");
		
		licenseEnabled = ucars.config.getBoolean("general.cars.licenses.enable");
		roadBlocksEnabled = ucars.config.getBoolean("general.cars.roadBlocks.enable");
		multiverseEnabled = ucars.config.getBoolean("general.cars.worlds.enable");
		trafficLightsEnabled = ucars.config.getBoolean("general.cars.trafficLights.enable");
		effectBlocksEnabled = ucars.config.getBoolean("general.cars.effectBlocks.enable");
		fuelEnabled = ucars.config.getBoolean("general.cars.fuel.enable");
		fuelUseItems = ucars.config.getBoolean("general.cars.fuel.items.enable");
		disableFallDamage = ucars.config.getBoolean("general.cars.fallDamageDisabled");
		pitchEnabled = ucars.config.getBoolean("general.cars.enablePitch");
		
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
		if(multiverseEnabled) {
			ucarworlds.clear();
			ucarworlds.addAll(ucars.config.getStringList("general.cars.worlds.ids"));
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
	
	public List<String> getWorldList() {
		return ucarworlds;
	}
	
	public boolean isMultiverse() {
		return multiverseEnabled;
	}
}
