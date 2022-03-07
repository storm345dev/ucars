package com.useful.ucars;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;

public class CartOrientationUtil {
	public static interface CartOrientationUtilOverride {
		public void setPitch(Entity cart, float pitch);
		public void setYaw(Entity cart, float yaw);
		public void setRoll(Entity cart, float roll);
	}

	private static CartOrientationUtilOverride cartOrientationUtilOverride = null;

	public static void setCartOrientationUtilOverride(CartOrientationUtilOverride override){
		CartOrientationUtil.cartOrientationUtilOverride = override;
	}

	public static void setRoll(Entity cart, float roll){
		if(cartOrientationUtilOverride != null){
			cartOrientationUtilOverride.setRoll(cart,roll);
			return;
		}
	}

	public static void setPitch(Entity cart, float pitch){
		if(cartOrientationUtilOverride != null){
			cartOrientationUtilOverride.setPitch(cart,pitch);
			return;
		}
		if(!(cart instanceof Minecart)){
			throw new RuntimeException("Non Minecart cars not supported yet!");
		}
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = null;
			Class <?> ema = Reflect.getNMSClass("world.entity.","Entity");

			if(ucars.MCVersion.get(0) == 1) {
				if(ucars.MCVersion.get(1) >= 18) {
					p = ema.getDeclaredField("aB");
				} else if(ucars.MCVersion.get(1) == 17) {
					p = ema.getDeclaredField("az");
				} else {
					ema = Reflect.getNMSClass("world.entity.vehicle.","EntityMinecartAbstract");
					p = ema.getField("pitch");
				}
				p.setAccessible(true);
				p.set(ema.cast(nmsCart), -pitch);
				p.setAccessible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setYaw(Entity cart, float yaw){
		if(cartOrientationUtilOverride != null){
			cartOrientationUtilOverride.setYaw(cart,yaw);
			return;
		}
		if(!(cart instanceof Minecart)){
			throw new RuntimeException("Non Minecart cars not supported yet!");
		}
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("world.entity.","Entity");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = null;
			
			if(ucars.MCVersion.get(0) == 1) {
				if(ucars.MCVersion.get(1) >= 18) {
					p = ema.getDeclaredField("aA");
				} else if(ucars.MCVersion.get(1) == 17) {
					p = ema.getDeclaredField("ay");
				} else {
					ema = Reflect.getNMSClass("world.entity.vehicle.","EntityMinecartAbstract");
					p = ema.getField("yaw");	
				}
			}
			
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), yaw);
			p.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
