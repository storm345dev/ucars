package com.useful.ucars;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class CartOrientationUtil {
	public static void setPitch(Entity cart, float pitch){
		if(!(cart instanceof Minecart)){
			throw new RuntimeException("Non Minecart cars not supported yet!");
		}
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityMinecartAbstract");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = ema.getField("pitch");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), -pitch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setYaw(Entity cart, float yaw){
		if(!(cart instanceof Minecart)){
			throw new RuntimeException("Non Minecart cars not supported yet!");
		}
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityMinecartAbstract");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = ema.getField("yaw");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), yaw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
