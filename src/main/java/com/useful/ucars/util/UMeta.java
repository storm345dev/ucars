package com.useful.ucars.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.metadata.MetadataValue;

public class UMeta {
	private static volatile HashMap<WeakKey, Map<String, List<MetadataValue>>> metadata = new HashMap<WeakKey, Map<String, List<MetadataValue>>>();
	
	public static void removeAllMeta(Object key){
		synchronized(metadata){
			WeakKey weakKey = new WeakKey(key);
			metadata.remove(weakKey);
		}
	}
	
	public static Map<String, List<MetadataValue>> getAllMeta(Object key){
		if(key == null){
			return new ConcurrentHashMap<String, List<MetadataValue>>(10, 0.75f, 2);
		}
		synchronized(metadata){
			WeakKey weakKey = new WeakKey(key);
			Map<String, List<MetadataValue>> res = metadata.get(weakKey);
			if(res == null){
				res = new ConcurrentHashMap<String, List<MetadataValue>>(10, 0.75f, 2);
				metadata.put(weakKey, res);
			}
			return res;
		}
	}
	
	public static List<MetadataValue> getMeta(Object key, String metaKey){
		Map<String, List<MetadataValue>> meta = getAllMeta(key);
		List<MetadataValue> list;
		synchronized(USchLocks.getMonitor(key)){
			list = meta.get(metaKey);
			if(list == null){
				list = new ArrayList<MetadataValue>();
				meta.put(metaKey, list);
			}
		}
		return list;
	}
	
	public static void removeMeta(Object key, String metaKey){
		Map<String, List<MetadataValue>> meta = getAllMeta(key);
		synchronized(USchLocks.getMonitor(key)){
			meta.remove(metaKey);
		}
	}
	
	public static void gc(){
		System.gc();
		clean();
	}
	
	public static void clean(){
		synchronized(metadata){
			for(WeakKey ref:new ArrayList<WeakKey>(metadata.keySet())){
				try {
					if(ref.get() == null || ref == null){
						metadata.remove(ref);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class WeakKey extends WeakReference {

		private int hash = -1;
		
		public WeakKey(Object arg0) {
			super(arg0);
			this.hash = arg0.hashCode();
		}
		
		@Override
		public int hashCode(){
			return hash;
		}
		
		@Override
		public boolean equals(Object o){
			if(!(o instanceof WeakKey)){
				return false;
			}
			Object self = get();
			Object other = ((WeakKey)o).get();
			if(self == null || other == null){
				return super.equals(o);
			}
			return self.equals(other);
		}
		
	}
	
	public static int getTotalMetaSize(){
		clean();
		return metadata.size();
	}
}
