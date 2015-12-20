package com.useful.ucars.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Super awesome memory-leak-free (TESTED) class for allowing any objects to be used as synchronized code block monitor objects
 * This WILL work IF (and only if) the objects used return true for .equals()
 *
 */
public class USchLocks {
	private static volatile HashMap<WeakKey, WeakReference> monitors = new HashMap<WeakKey, WeakReference>();
	
	public static Object getMonitor(Object key){
		clean();
		synchronized(monitors){
			@SuppressWarnings("rawtypes")
			WeakKey weakKey = new WeakKey(key);
			WeakReference monitorWeak = monitors.get(weakKey);
			Object monitor = monitorWeak == null ? null : monitorWeak.get();
			if(monitor == null){
				monitor = new Object();
				monitors.put(weakKey, new WeakReference(monitor));
			}
			return monitor;
		}
	}
	
	public static void gc(){
		System.gc();
		clean();
	}
	
	private static void clean(){
		synchronized(monitors){
			for(WeakKey ref:new ArrayList<WeakKey>(monitors.keySet())){
				try {
					if(ref.get() == null || monitors.get(ref).get() == null){
						monitors.remove(ref);
					}
				} catch (Exception e) {
					monitors.remove(ref);
				}
			}
		}
	}
	
	private static class WeakKey extends WeakReference {

		private int hash;
		
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
	
	public static int getTotalMonitors(){
		clean();
		return monitors.size();
	}
}
