package com.useful.uCarsAPI;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called when a new car is made in place of an old, identical one.
 * Eg. called when using teleport blocks.
 * 
 * @author storm345
 *
 */
public class uCarRespawnEvent extends Event implements Cancellable {
	private Entity newCar;
	private UUID oldId;
	private UUID newId;
	private Boolean cancelled = false;
	private CarRespawnReason reason;
	
	public static HandlerList handlers = new HandlerList();

	/**
	 * Called when a new car is made in place of an old, identical one.
	 * Eg. called when using teleport blocks.
	 * 
	 * @param newCar The new Car Entity.
	 * @param oldId The UUID of the old Car entity.
	 * @param newId The UUID of the new Car entity.
	 */
	public uCarRespawnEvent(Entity newCar, UUID oldId, UUID newId, CarRespawnReason reason) {
		this.newCar = newCar;
		this.oldId = oldId;
		this.newId = newId;
		this.reason = reason;
	}

	/**
	 * Get's the new car entity.
	 * 
	 * @return The new car entity.
	 */
	public Entity getNewCar() {
		return newCar;
	}

	/**
	 * Get's the old car entity's UUID
	 * 
	 * @return The old car entity's UUID
	 */
	public UUID getOldEntityId() {
		return oldId;
	}

	/**
	 * Get's the new car entity's UUID
	 * 
	 * @return The new car entity's UUID
	 */
	public UUID getNewEntityId() {
		return newId;
	}

	/**
	 * Is this event cancelled?
	 * 
	 */
	public boolean isCancelled() {
		return this.cancelled;
	}

	/**
	 * Set this event as cancelled?
	 * 
	 */
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}
	
	public CarRespawnReason getReason(){
		return reason;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
