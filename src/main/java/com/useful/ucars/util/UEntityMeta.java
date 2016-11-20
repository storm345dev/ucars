package com.useful.ucars.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import com.useful.ucars.ucars;

public class UEntityMeta {
	
	private static volatile Map<UUID, Object> entityMetaObjs = new ConcurrentHashMap<UUID, Object>(100, 0.75f, 2);
	private static volatile Map<UUID, WeakReference<Entity>> entityObjs = new ConcurrentHashMap<UUID, WeakReference<Entity>>(100, 0.75f, 2);
	
	public static void cleanEntityObjs(){
		Bukkit.getScheduler().runTaskAsynchronously(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				for(Entry<UUID, WeakReference<Entity>> entry:entityObjs.entrySet()){
					UUID entID = entry.getKey();
					WeakReference<Entity> val = entry.getValue();
					if(val == null || val.get() == null){
						entityObjs.remove(entID);
					}
				}
				return;
			}});
		/*Bukkit.getScheduler().runTask(ucars.plugin, new Runnable(){

			@Override
			public void run() {
				final List<Entity> allEntities = new ArrayList<Entity>();
				for(World w:Bukkit.getWorlds()){
					allEntities.addAll(w.getEntities());
				}
				Bukkit.getScheduler().runTaskAsynchronously(ucars.plugin, new Runnable(){

					@Override
					public void run() {
						for(final Entity e:new ArrayList<Entity>(entityObjs.values())){
							if(e.isDead() && !e.isValid()){
								synchronized(entityMetaObjs){
									Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

										@Override
										public void run() {
											entityObjs.remove(e.getUniqueId());
											entityMetaObjs.remove(e.getUniqueId());
											return;
										}}, 100l);
								}
							}
						}
						mainLoop: for(final UUID entID:new ArrayList<UUID>(entityMetaObjs.keySet())){
							for(Entity e:allEntities){
								if(e.getUniqueId().equals(entID)){
									continue mainLoop;
								}
							}
							Bukkit.getScheduler().runTaskLaterAsynchronously(ucars.plugin, new Runnable(){

								@Override
								public void run() {
									Object o = entityMetaObjs.get(entID);
									entityMetaObjs.remove(entID);
									if(o != null){
										UMeta.removeAllMeta(o);
									}
									return;
								}}, 100l);
						}
					}});
				return;
			}});*/
	}
	
	private static void setEntityObj(Entity e){
		if(e instanceof Player){
			return;
		}
		synchronized(entityObjs){
			entityObjs.put(e.getUniqueId(), new WeakReference<Entity>(e));
		}
	}
	
	private static void delEntityObj(Entity e){
		if(e instanceof Player){
			return;
		}
		synchronized(entityObjs){
			entityObjs.remove(e.getUniqueId());
		}
	}
	
	public static void printOutMeta(Entity e){
		StringBuilder sb = new StringBuilder();
		Map<String, List<MetadataValue>> metas = UMeta.getAllMeta(getMetaObj(e));
		for(String key:new ArrayList<String>(metas.keySet())){
			if(sb.length() > 0){
				sb.append(", ");
			}
			sb.append(key);
		}
		Bukkit.broadcastMessage(e.getUniqueId()+": "+sb.toString());
	}
	
	public static synchronized void removeAllMeta(Entity e){
		Object o = entityMetaObjs.get(e.getUniqueId());
		entityMetaObjs.remove(e.getUniqueId());
		delEntityObj(e);
		/*entityObjs.put(e.getUniqueId(), e);*/
		if(o != null){
			UMeta.removeAllMeta(o);
		}
	}
	
	private static Object getMetaObj(Entity e){
		if(e == null){
			return null;
		}
		synchronized(entityMetaObjs){
			/*entityObjs.put(e.getUniqueId(), e);*/
			Object obj = entityMetaObjs.get(e.getUniqueId());
			if(obj == null){
				obj = new Object();
				entityMetaObjs.put(e.getUniqueId(), obj);
				setEntityObj(e);
			}
			return obj;
		}
	}
	
	public static void setMetadata(Entity entity, String metaKey, MetadataValue value){
		/*entityObjs.put(entity.getUniqueId(), entity);*/
		setEntityObj(entity);
		UMeta.getMeta(getMetaObj(entity), metaKey).add(value);
	}
	
	public static List<MetadataValue> getMetadata(Entity entity, String metaKey){
		/*entityObjs.put(entity.getUniqueId(), entity);*/
		return UMeta.getAllMeta(getMetaObj(entity)).get(metaKey);
	}
	
	public static boolean hasMetadata(Entity entity, String metaKey){
		/*entityObjs.put(entity.getUniqueId(), entity);*/
		return UMeta.getAllMeta(getMetaObj(entity)).containsKey(metaKey);
	}
	
	public static void removeMetadata(Entity entity, String metaKey){
		/*entityObjs.put(entity.getUniqueId(), entity);*/
		UMeta.removeMeta(getMetaObj(entity), metaKey);
	}
}
