package com.useful.ucars;

public class SmoothMeta { //Performs all the calculations for actually making cars accelerate smoothly
	private volatile long lastTime;
	private volatile float speedFactor = 0;
	private volatile float accFac = 1;
	
	public SmoothMeta(float accFac){
		this.lastTime = System.currentTimeMillis();
		this.speedFactor = 0;
		this.accFac = accFac;
	}
	
	public float getFactor(){ //Return the multiplier to multiply the car's velocity (x & z only) by to make it appear to accelerate - Reliably called every control update (Eg. every tick holding down 'w' or 's')
		updateTime(); //Update how long since the user last tried to move and thus accelerated (Eg. if car hasn't moved in over 1.5s then start acceleration at 0 again)
		incrementFactor(); //Increase our multiplier so the car gets faster
		return speedFactor;
	}
	
	public void updateAccelerationFactor(float accFac){ //Update the new 'acceleration modifier' from the API so the API has more control over acceleration
		this.accFac = accFac;
	}
	
	private float getA(){ //Get the multiplier for accelerating
		return (float) (0.025*accFac); //Our constant of 0.025 multiplied by whatever the API is asking for as a modification to the rate of acceleration
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
		if((now-lastTime)>500){ //Havent moved in over 1.5s
			speedFactor = 0;
		}
		lastTime = now;
	}
}
