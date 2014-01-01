package com.useful.ucars;

import java.util.List;

import com.useful.ucarsCommon.IdMaterialConverter;

public class PlaceManager {
	/**
	 * Deprecated, use placeableOn(String materialName, byte data) instead.
	 * 
	 */
	@Deprecated
	public static Boolean placeableOn(int id, byte data) {
		String materialName = IdMaterialConverter.getMaterialById(id).name().toUpperCase();
		return placeableOn(materialName, data);
	}
	public static Boolean placeableOn(String materialName, byte data) {
		Boolean placeable = false;
		if (!ucars.config.getBoolean("general.cars.roadBlocks.enable")) {
			return true;
		}
		List<String> rBlocks = ucars.config
				.getStringList("general.cars.roadBlocks.ids");
		for (String raw : rBlocks) {
			if (ItemStackFromId.equals(raw, materialName, data)) {
				placeable = true; // Placing on a road block
			}
		}
		return placeable;
	}

}
