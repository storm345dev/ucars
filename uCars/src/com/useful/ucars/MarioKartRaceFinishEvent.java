package com.useful.ucars;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event to be used to dish out rewards, etc... at the end of a race
 */
public class MarioKartRaceFinishEvent extends Event implements Cancellable{
    public Boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();
    Player player = null;
    int position = 1;
	public MarioKartRaceFinishEvent(Player player, int position) {
		this.player = player;
		this.position = position;
	}
	public boolean isCancelled() {
		return this.cancelled;
	}
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}
	public Integer getFinishPosition(){
		return position;
	}
	public Player getPlayer(){
		return player;
	}
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
