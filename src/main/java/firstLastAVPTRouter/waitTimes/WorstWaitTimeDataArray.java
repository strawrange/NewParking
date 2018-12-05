/* *********************************************************************** *
 * project: org.matsim.*
 * WaitTimeCalculator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package firstLastAVPTRouter.waitTimes;

import org.matsim.contrib.eventsBasedPTRouter.waitTimes.WaitTimeData;

/**
 * Array implementation of the structure for saving wait times
 * 
 * @author sergioo
 */

public class WorstWaitTimeDataArray implements WaitTimeData {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	//Attributes
	private double[] worstWaitTimes;

	//Constructors
	public WorstWaitTimeDataArray(int numSlots) {
		worstWaitTimes = new double[numSlots];
		resetWaitTimes();
	}

	//Methods
	@Override
	public void resetWaitTimes() {
		for(int i = 0; i< worstWaitTimes.length; i++) {
			worstWaitTimes[i] = 0;
		}
	}
	@Override
	public synchronized void addWaitTime(int timeSlot, double waitTime) {
		if(waitTime>worstWaitTimes[timeSlot])
			worstWaitTimes[timeSlot] = waitTime;
	}
	@Override
	public double getWaitTime(int timeSlot) {
		return worstWaitTimes[timeSlot< worstWaitTimes.length?timeSlot:(worstWaitTimes.length-1)];
	}
	@Override
	public int getNumData(int timeSlot) {
		return worstWaitTimes[timeSlot]==0?0:1;
	}

}
