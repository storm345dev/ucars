package com.useful.ucars;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

public class ucarUpdateEvent extends VehicleUpdateEvent {
    public Vector toTravel = new Vector();
    public Boolean changePlayerYaw = false;
    public float yaw = 90;
    public Boolean doDivider = false;;
    public double divider = 1;
	public ucarUpdateEvent(Vehicle vehicle, Vector toTravel) {
		super(vehicle);
		this.toTravel = toTravel;
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

}
