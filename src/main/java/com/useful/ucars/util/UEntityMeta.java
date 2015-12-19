package com.useful.ucars.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.MetadataValue;

import com.useful.ucars.ucars;

public class UEntityMeta {
	
	private static Map<UUID, Object> entityMetaObjs = new ConcurrentHashMap<UUID, Object>(100, 0.75f, 2);
	
	public static void cleanEntityObjs(){
		Bukkit.getScheduler().runTask(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				final List<Entity> allEntities = new ArrayList<Entity>();
				for(World w:Bukkit.getWorlds()){
					allEntities.addAll(w.getEntities());
				}
				Bukkit.getScheduler().runTaskAsynchronously(ucars.plugin, new Runnable(){

					@Override
					public void run() {
						mainLoop: for(UUID entID:new ArrayList<UUID>(entityMetaObjs.keySet())){
							for(Entity e:allEntities){
								if(e.getUniqueId().equals(entID)){
									continue mainLoop;
								}
							}
							Object o = entityMetaObjs.get(entID);
							entityMetaObjs.remove(entID);
							if(o != null){
								UMeta.removeAllMeta(o);
							}
						}
					}});
				return;
			}});
	}
	
	public static void removeAllMeta(Entity e){
		Object o = entityMetaObjs.get(e.getUniqueId());
		entityMetaObjs.remove(e.getUniqueId());
		if(o != null){
			UMeta.removeAllMeta(o);
		}
	}
	
	private static Object getMetaObj(Entity e){
		if(e == null){
			return null;
		}
		synchronized(entityMetaObjs){
			Object obj = entityMetaObjs.get(e.getUniqueId());
			if(obj == null){
				obj = new Object();
				entityMetaObjs.put(e.getUniqueId(), obj);
			}
			return obj;
		}
	}
	
	public static void setMetadata(Entity entity, String metaKey, MetadataValue value){
		UMeta.getMeta(getMetaObj(entity), metaKey).add(value);
	}
	
	public static List<MetadataValue> getMetadata(Entity entity, String metaKey){
		return UMeta.getAllMeta(getMetaObj(entity)).get(metaKey);
	}
	
	public static boolean hasMetadata(Entity entity, String metaKey){
		return UMeta.getAllMeta(getMetaObj(entity)).containsKey(metaKey);
	}
	
	public static void removeMetadata(Entity entity, String metaKey){
		UMeta.removeMeta(getMetaObj(entity), metaKey);
	}
}
