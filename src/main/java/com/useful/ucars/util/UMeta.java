package com.useful.ucars.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.metadata.MetadataValue;

public class UMeta {
	private static volatile WeakHashMap<WeakKey, Map<String, List<MetadataValue>>> metadata = new WeakHashMap<WeakKey, Map<String, List<MetadataValue>>>();
	
	public static void removeAllMeta(Object key){
		synchronized(metadata){
			WeakKey weakKey = new WeakKey(key);
			metadata.remove(weakKey);
		}
	}
	
	public static Map<String, List<MetadataValue>> getAllMeta(Object key){
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
		synchronized(SchLocks.getMonitor(key)){
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
		synchronized(SchLocks.getMonitor(key)){
			meta.remove(metaKey);
		}
	}
	
	public static void gc(){
		System.gc();
		clean();
	}
	
	public static void clean(){
		synchronized(metadata){
			for(WeakKey ref:metadata.keySet()){
				try {
					if(ref.get() == null || ref == null){
						metadata.remove(ref);
					}
				} catch (Exception e) {
				}
			}
		}
	}
	
	private static class WeakKey extends WeakReference {

		public WeakKey(Object arg0) {
			super(arg0);
		}
		
		@Override
		public int hashCode(){
			Object val = get();
			if(val == null){
				return super.hashCode();
			}
			return val.hashCode();
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
