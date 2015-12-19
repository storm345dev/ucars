package com.useful.ucars.util;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

public class UEntityMeta {
	public static void setMetadata(Entity entity, String metaKey, MetadataValue value){
		UMeta.getMeta(entity, metaKey).add(value);
	}
	
	public static List<MetadataValue> getMetadata(Entity entity, String metaKey){
		return UMeta.getAllMeta(entity).get(metaKey);
	}
	
	public static boolean hasMetadata(Entity entity, String metaKey){
		return UMeta.getAllMeta(entity).containsKey(metaKey);
	}
	
	public static void removeMetadata(Entity entity, String metaKey){
		UMeta.removeMeta(entity, metaKey);
	}
}
