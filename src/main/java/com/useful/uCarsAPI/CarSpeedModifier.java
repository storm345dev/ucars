package com.useful.uCarsAPI;

import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * A basic interface for modifying (multiplying) the car speed/Vector after
 * uCars has done it's calculations. (Eg. Have cars with 2x speed upgrades)
 * 
 * @author storm345
 * 
 */
public interface CarSpeedModifier {
	/**
	 * Return the car Speed as a vector
	 * 
	 * @param car The Car to manipulate the speed of
	 * @param travelVector The Vector the car is travelling at
	 * @param currentMultiplier The rough speed of the car
	 * @return A manipulated vector of the Car's travel
	 */
	public Vector getModifiedSpeed(Entity car, Vector travelVector, double currentMultiplier);

}
