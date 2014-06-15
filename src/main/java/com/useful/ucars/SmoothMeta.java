package com.useful.ucars;

public class SmoothMeta {
	private volatile long lastTime;
	private volatile float speedFactor = 0;
	private volatile float accFac = 1;
	
	public SmoothMeta(float accFac){
		this.lastTime = System.currentTimeMillis();
		this.speedFactor = 0;
		this.accFac = accFac;
	}
	
	public float getFactor(){
		updateTime();
		incrementFactor();
		return speedFactor;
	}
	
	private float getA(){
		return (float) (0.025*accFac);
	}
	
	private void incrementFactor(){
		if(speedFactor >= 0.97){
			speedFactor = 1;
			return;
		}
		float diff = 1-speedFactor;
		speedFactor += (getA()*diff);
	}
	
	public void resetAcel(){
		speedFactor = 0;
	}
	
	private void updateTime(){
		long now = System.currentTimeMillis();
		if((now-lastTime)>500){ //Havent moved in over 1.5s
			speedFactor = 0;
		}
		lastTime = now;
	}
}
