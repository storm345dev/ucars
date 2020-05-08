package com.useful.uCarsAPI;

import org.bukkit.entity.Entity;

/**
 * A basic interface for modifying (multiplying) the car turning amount after
 * uCars has done it's calculations. (Eg. Have cars with 2x will turn twice as fast)
 * 
 * @author storm345
 * 
 */
public interface CarTurningModifier {
	/**
	 * Return the maximum amount for the car to turn in degrees (Positive)
	 * 
	 * @param car The Car to manipulate the turning speed of
	 * @param currentRotationPerTick The number of degrees the car will turn each tick
	 * @return The maximum amount for the car to turn in degrees per tick (Positive)
	 */
	public double getModifiedTurningSpeed(Entity car, double currentRotationPerTick);

}
