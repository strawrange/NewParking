/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
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

package ParkingStrategy.InsertionOptimizer;

import Path.OneToManyPathSearch.DrtPathData;

/**
 * @author michalm
 */
public class InsertionWithPathData {
	public final int pickupIdx;
	public final int dropoffIdx;
	public final DrtPathData pathToPickup;
	public final DrtPathData pathFromPickup;
	public final DrtPathData pathToDropoff;// null if dropoff inserted directly after pickup
	public final DrtPathData pathFromDropoff;// null if dropoff inserted at the end
	//TODO
	public double estimatedBattery = 0.0;

	InsertionWithPathData(int pickupIdx, int dropoffIdx, DrtPathData pathToPickup, DrtPathData pathFromPickup,
                          DrtPathData pathToDropoff, DrtPathData pathFromDropoff) {
		this.pickupIdx = pickupIdx;
		this.dropoffIdx = dropoffIdx;
		this.pathToPickup = pathToPickup;
		this.pathFromPickup = pathFromPickup;
		this.pathToDropoff = pathToDropoff;
		this.pathFromDropoff = pathFromDropoff;
	}

	@Override
	public String toString() {
		return "Insertion: pickupIdx=" + pickupIdx + ", dropoffIdx=" + dropoffIdx;
	}
}
