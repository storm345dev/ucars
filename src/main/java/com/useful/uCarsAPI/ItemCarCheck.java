package com.useful.uCarsAPI;

import org.bukkit.inventory.ItemStack;

/**
 * Provides an interfact to add custom checking to cars
 * 
 * @author storm345
 * 
 */
public interface ItemCarCheck {
	/**
	 * Called to check if a (uCars checked and valid) car is a car (According to
	 * your plugin)
	 * 
	 * @param car
	 *            The uCars-valid car
	 * @return True if it is a car
	 */
	public Boolean isACar(ItemStack carStack);
}
