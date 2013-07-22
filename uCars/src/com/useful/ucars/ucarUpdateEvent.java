package com.useful.ucars;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.util.Vector;

public class ucarUpdateEvent extends VehicleUpdateEvent {
    public Vector toTravel = new Vector();
	public ucarUpdateEvent(Vehicle vehicle, Vector toTravel) {
		super(vehicle);
		this.toTravel = toTravel;
	}
	public Vector getTravelVector(){
		return this.toTravel;
	}

	

}
