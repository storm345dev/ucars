package com.useful.ucars;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class CarHealthData implements MetadataValue {
	double health = 5;
	Runnable onDeath = null;
	Plugin plugin = null;
	public CarHealthData(double health, Runnable onDeath, Plugin plugin){
		this.health = health;
		this.onDeath = onDeath;
		this.plugin = plugin;
	}
	//@Override
	public boolean asBoolean() {
		return false;
	}

	//@Override
	public byte asByte() {
		return 0;
	}

	//@Override
	public double asDouble() {
		return health;
	}

	//@Override
	public float asFloat() {
		return (float) (health);
	}

	//@Override
	public int asInt() {
		return (int) Math.floor(health+0.5f);
	}

	//@Override
	public long asLong() {
		return Math.round(health);
	}

	//@Override
	public short asShort() {
		return Short.parseShort(""+health);
	}

	//@Override
	public String asString() {
		return ""+health;
	}

	//@Override
	public Plugin getOwningPlugin() {
		return plugin;
	}

	//@Override
	public void invalidate() {
		health = 0;
		die();
		return;
	}

	//@Override
	public Object value() {
		return health;
	}
	public void damage(double amount){
		health = this.health - amount;
		if(health<=0){
			die();
		}
		return;
	}
	public void setHealth(double amount){
		this.health = amount;
	}
	public double getHealth(){
		return this.health;
	}
	public void die(){
		this.onDeath.run();
		return;
	}
}
