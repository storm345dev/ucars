package com.useful.uCarsAPI;

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

/**
 * A basic interface for modifying (multiplying)
 * the car speed/Vector after uCars has done it's
 * calculations. (Eg. Have cars with 2x speed upgrades)
 * 
 * @author storm345
 *
 */
public interface CarSpeedModifier {
	public Vector getModifiedSpeed(Minecart car, Vector travelVector);

}
