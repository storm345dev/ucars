package com.useful.ucars;

import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

public class ucarUpdateEvent extends VehicleUpdateEvent implements Cancellable{
    public Vector toTravel = new Vector();
    public Boolean changePlayerYaw = false;
    public float yaw = 90;
    public Boolean doDivider = false;;
    public double divider = 1;
    public Boolean cancelled = false;
    public Player player = null;
	public ucarUpdateEvent(Vehicle vehicle, Vector toTravel, Player player) {
		super(vehicle);
		this.toTravel = toTravel;
		this.player = player;
	}
	public Player getPlayer(){
		return player;
	}
	public Vector getTravelVector(){
		return this.toTravel;
	}
    public void setChangePlayerYaw(Boolean change){
    	this.changePlayerYaw = change;
    	return;
    }
	public Boolean getChangePlayerYaw(){
		return this.changePlayerYaw;
	}
	public void setDoDivider(Boolean doDivider){
		this.doDivider = doDivider;
		return;
	}
	public Boolean getDoDivider(){
		return this.doDivider;
	}
	public void setDivider(double divider){
	    this.divider = divider;
	    return;
	}
	public double getDivider(){
		return this.divider;
	}
	public boolean isCancelled() {
		return this.cancelled;
	}
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}

}
