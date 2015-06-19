package com.useful.uCarsAPI;

import org.bukkit.entity.Player;

/**
 * A basic interface for modifying (multiplying) the car acceleration after
 * uCars has done it's calculations. (Eg. Have cars with 2x speed upgrades)
 * 
 * @author storm345
 * 
 */
public interface CarDecelerationModifier {
	/**
	 * Return the car's acceleration as a decimal, eg. 0.5 for half normal acceleration or 2.0 for 2.0x normal acceleration
	 * 
	 * @param driver The driver of the car
	 * @param current The current acceleration decimal calculated by uCars and other plugins
	 */
	public float getAccelerationDecimal(Player driver, float current);

}
