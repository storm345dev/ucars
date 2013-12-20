package com.useful.ucars;

import java.util.regex.Pattern;

public class PlaceManager {
	public static Boolean placeableOn(int id, byte data) {
		Boolean placeable = false;
		if (!ucars.config.getBoolean("general.cars.roadBlocks.enable")) {
			return true;
		}
		String[] rBlocks = ucars.config
				.getString("general.cars.roadBlocks.ids").split(
						Pattern.quote(","));
		for (String raw : rBlocks) {
			if (ItemStackFromId.equals(raw, id, data)) {
				placeable = true; // Placing on a road block
			}
		}
		return placeable;
	}

}
