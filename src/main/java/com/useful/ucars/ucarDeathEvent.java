package com.useful.ucars;

import org.bukkit.entity.Minecart;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ucarDeathEvent extends Event implements Cancellable {
	public Boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();
	Minecart car = null;

	public ucarDeathEvent(Minecart vehicle) {
		this.car = vehicle;
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

	public Minecart getCar() {
		return car;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
