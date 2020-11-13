package com.useful.ucars;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ucarDeathEvent extends Event implements Cancellable {
	public Boolean cancelled = false;
	private static final HandlerList handlers = new HandlerList();
	private Entity car = null;
	private Player player = null;
	
	public ucarDeathEvent(Entity vehicle) {
		this.car = vehicle;
	}
	
	public ucarDeathEvent(Entity vehicle, Player whoKilled) {
		this.car = vehicle;
		this.player = whoKilled;
	}
	
	public Player getPlayerWhoKilled(){
		return player;
	}
	
	public boolean didPlayerKill(){
		return player != null;
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

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
