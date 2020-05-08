package com.useful.uCarsAPI;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class uCarCrashEvent extends Event implements Cancellable {
	private boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();
	private Entity car = null;
	private Entity hit;
	private double damageToEntity = 0;

	public uCarCrashEvent(Entity vehicle, Entity hit, double damageToEntity) {
		this.car = vehicle;
		this.hit = hit;
		this.damageToEntity = damageToEntity;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public Entity getCar() {
		return car;
	}
	
	public Entity getEntityCrashedInto(){
		return this.hit;
	}
	
	public double getDamageToBeDoneToTheEntity(){
		return this.damageToEntity;
	}
	
	public void setDamageToBeDoneToTheEntity(double dmg){
		this.damageToEntity = dmg;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
