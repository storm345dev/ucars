package com.useful.ucars;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

public class ucarUpdateEvent extends VehicleUpdateEvent {
    public Vector toTravel = new Vector();
    public Boolean changePlayerYaw = false;
    public float yaw = 90;
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

}
