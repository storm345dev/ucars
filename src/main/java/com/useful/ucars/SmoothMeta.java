package com.useful.ucars;


public class SmoothMeta { //Performs all the calculations for actually making cars accelerate smoothly	
	private volatile long lastTime;
	private volatile float speedFactor = 0;
	private volatile float accFac = 1;
	private volatile float decFac = 1;
	private volatile CarDirection dir = CarDirection.FORWARDS;
	private volatile long firstAirTime = System.currentTimeMillis();
	
	public SmoothMeta(float accFac, float decFac){
		this.lastTime = System.currentTimeMillis();
		this.speedFactor = 0;
		this.accFac = accFac;
		this.decFac = decFac;
		firstAirTime = System.currentTimeMillis();
	}
	
	public void setFirstAirTime(long sf){
		this.firstAirTime = sf;
	}
	
	public long getFirstAirTime(){
		return this.firstAirTime;
	}
	
	public void setCurrentSpeedFactor(float sf){
		this.speedFactor = sf;
	}
	
	public float getCurrentSpeedFactor(){
		return this.speedFactor;
	}
	
	public CarDirection getDirection(){
		return this.dir;
	}
	
	public float getFactor(CarDirection dir){ //Return the multiplier to multiply the car's velocity (x & z only) by to make it appear to accelerate - Reliably called every control update (Eg. every tick holding down 'w' or 's')
		updateTime(); //Update how long since the user last tried to move and thus accelerated (Eg. if car hasn't moved in over 1.5s then start acceleration at 0 again)
		return incrementAndGetFactor(dir); //Increase our multiplier so the car gets faster
	}
	
	public void updateAccelerationFactor(float accFac){ //Update the new 'acceleration modifier' from the API so the API has more control over acceleration
		this.accFac = accFac;
	}
	
	public void updateDecelerationFactor(float decFac){ //Update the new 'acceleration modifier' from the API so the API has more control over acceleration
		this.decFac = decFac;
	}
	
	private float getA(){ //Get the multiplier for accelerating
		return (float) (0.025*accFac); //Our constant of 0.025 multiplied by whatever the API is asking for as a modification to the rate of acceleration
	}
	
	private float getDA(boolean reversing){ //Get the multiplier for deceleration (reversing = going in opposite dir to accel/decel)
		if(!reversing){
			return (float) (0.025*decFac); //Our constant of 0.045 multiplied by whatever the API is asking for as a modification to the rate of acceleration
		}
		else {
			return (float) (0.035*decFac); //Our constant of 0.045 multiplied by whatever the API is asking for as a modification to the rate of acceleration
		}
	}
	
	private float incrementAndGetFactor(CarDirection dir){
		if(dir.equals(CarDirection.NONE)){
			decrementFactor(false);
			if(this.dir.equals(CarDirection.BACKWARDS)){ //Normal Plugin is trying to move forwards still; so simply return - if it should be reversing
				return -speedFactor;
			}
			return speedFactor;
		}
		if(!this.dir.equals(dir) && speedFactor > 0){
			decrementFactor(true);
			return -speedFactor;
		}
		this.dir = dir;
		incrementFactor();
		return speedFactor;
	}
	
	private void incrementFactor(CarDirection dir){
		if(dir.equals(CarDirection.NONE)){
			decrementFactor(false);
			return;
		}
		if(!this.dir.equals(dir) && speedFactor > 0){
			decrementFactor(true);
		}
		this.dir = dir;
		incrementFactor();
	}
	
	private void decrementFactor(boolean reversing){
		float diff = speedFactor; //The difference between 1 (full speed) and the rate we want to accelerate by
		speedFactor -= (getDA(reversing)*diff); //Increase the speed by 'a' multiplied by the difference; eg. accelerates faster the slower the vehicle moves (Looks quite realistic)
		if(speedFactor <= 0.05){ //Close enough to 1; so just be 1 or else you get infinitely close to 1 without getting to it (Wasting time calculating for no visible reason)
			speedFactor = 0;
			return;
		}
	}
	
	private void incrementFactor(){ //Increases the multiplier
		if(speedFactor >= 0.97){ //Close enough to 1; so just be 1 or else you get infinitely close to 1 without getting to it (Wasting time calculating for no visible reason)
			speedFactor = 1;
			return;
		}
		float diff = 1-speedFactor; //The difference between 1 (full speed) and the rate we want to accelerate by
		speedFactor += (getA()*diff); //Increase the speed by 'a' multiplied by the difference; eg. accelerates faster the slower the vehicle moves (Looks quite realistic)
	}
	
	public void resetAcel(){ //Simulate the car being stationary again
		speedFactor = 0;
	}
	
	private void updateTime(){ //Set the car to 'stationary' if it hasn't moved in a while
		long now = System.currentTimeMillis();
		/*if((now-lastTime)>500){ //Havent moved in over 1.5s
			speedFactor = 0;
		}*/
		lastTime = now;
	}
}
