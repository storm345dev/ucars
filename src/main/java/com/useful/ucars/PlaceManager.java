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
	
	public static Boolean placeableOn(Block block) {
		Boolean placeable = false;
		if (!ucars.config.getBoolean("general.cars.roadBlocks.enable")) {
			return true;
		}
		List<String> rBlocks = ucars.config
				.getStringList("general.cars.roadBlocks.ids");
		for (String raw : rBlocks) {
			final String[] parts = raw.split(":");
			if (parts.length < 1) {
			} else if (parts.length < 2) { //New configs and blocknames
				if (ItemStackFromId.equals(raw,block.getType().name().toUpperCase(),block.getData()) || block.getType().name().toUpperCase().contains(raw)) {
					return true;
				}
			} else { //old configs and block names
				final String mat = parts[0];
				final int data = Integer.parseInt(parts[1]);
				final int bdata = block.getData(); //TODO Alternative to .getData()
				if (mat.equalsIgnoreCase(block.getType().name().substring(block.getType().name().indexOf("_")+1)) && bdata == data) {
					return true;
				}
			}
		}
		return placeable;
	}
}
