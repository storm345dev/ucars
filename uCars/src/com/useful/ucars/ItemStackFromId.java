package com.useful.ucars;

import org.bukkit.inventory.ItemStack;

//TODO Make use new format
public class ItemStackFromId {
	public static ItemStack get(String raw) {
		String[] parts = raw.split(":");
		int id = Integer.parseInt(parts[0]);
		short data = 0;
		Boolean hasdata = false;
		if (parts.length > 1) {
			hasdata = true;
			data = Short.parseShort(parts[1]);
		}
		ItemStack item = new ItemStack(id);
		if (hasdata) {
			item.setDurability(data);
		}
		return item;
	}

	public static Boolean equals(String rawid, int tid, int tdata) {
		String[] parts = rawid.split(":");
		int id = Integer.parseInt(parts[0]);
		int data = 0;
		Boolean hasdata = false;
		if (parts.length > 1) {
			hasdata = true;
			data = Integer.parseInt(parts[1]);
		}
		if (tid == id) {
			Boolean valid = true;
			if (hasdata) {
				if (!(tdata == data)) {
					valid = false;
				}
			}
			if (valid) {
				return true;
			}
		}
		return false;
	}
}
