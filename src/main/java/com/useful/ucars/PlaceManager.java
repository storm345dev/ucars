package com.useful.ucars;

import java.util.List;

import org.bukkit.block.Block;

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
	@Deprecated
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
	
	public static Boolean placeableOn(Block block, ucars plugin) {
		if(ucars.listener.isMultiverse() && !ucars.listener.getWorldList().contains(block.getWorld().getName())) {
			return false;
		}
		
		if (!ucars.config.getBoolean("general.cars.roadBlocks.enable")) {
			return true;
		}
		
		List<String> rBlocks = ucars.config.getStringList("general.cars.roadBlocks.ids");
		return plugin.isBlockEqualToConfigIds(rBlocks,block);
	}
}
