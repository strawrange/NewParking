/* *********************************************************************** *
 *                                                                         *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package Dwelling;


import ParkingStrategy.ParkingInDepot.InsertionOptimizer.DrtScheduler;
import Passenger.PassengerEngine;
import Schedule.DrtRequest;
import org.matsim.contrib.dvrp.passenger.PassengerPickupActivity;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.AbstractDynActivity;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.contrib.dvrp.data.Vehicle;
import DynAgent.DynAgent;
import java.util.List;


/**
 * Multiple passenger dropoff and pickup activity
 * 
 * @author michalm
 */
public class BusStopActivity extends AbstractDynActivity implements PassengerPickupActivity, DrtPassengerAccessEgress{
	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final List<DrtRequest> dropoffRequests;
	private final List<DrtRequest> pickupRequests;

	private double endTime = END_ACTIVITY_LATER;
	private StayTask task;
	private DrtStopHandler handler;
	private DrtScheduler drtScheduler;
	private Vehicle vehicle;

	public BusStopActivity(PassengerEngine passengerEngine, DynAgent driver, StayTask task, List<DrtRequest> dropoffRequests,
						   List<DrtRequest> pickupRequests, String activityType, DrtScheduler drtScheduler, Vehicle vehicle, double accessTime, double egressTime, double now) {
		super(activityType);

		this.passengerEngine = passengerEngine;
		this.driver = driver;
		this.dropoffRequests = dropoffRequests;
		this.pickupRequests = pickupRequests;
		this.task = task;
		this.handler = new DrtStopHandler(accessTime, egressTime);
		this.drtScheduler = drtScheduler;
		this.vehicle = vehicle;
		doSimStep(now);
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

	@Override
	public void doSimStep(double now) {
		double delay = handler.handleDrtTransitStop(now, dropoffRequests, pickupRequests, this);
		if (delay == 0){
			task.setEndTime(now);
			drtScheduler.updateQueue(vehicle);
			endTime = now;
		}
	}

	@Override
	public void notifyPassengerIsReadyForDeparture(MobsimPassengerAgent passenger, double now) {
//		if (now < departureTime) {
//			return;// pick up only at the end of stop activity
//		}
//
//		PassengerRequest request = getRequestForPassenger(passenger);
//		if (passengerEngine.pickUpPassenger(this, driver, request, now)) {
//			passengersAboard++;
//		} else {
//			throw new IllegalStateException("The passenger is not on the link or not available for departure!");
//		}
	}

//	private PassengerRequest getRequestForPassenger(MobsimPassengerAgent passenger) {
//		for (PassengerRequest request : pickupRequests) {
//			if (passenger == request.getPassenger()) {
//				return request;
//			}
//		}
//		throw new IllegalArgumentException("I am waiting for different passengers!");
//	}


	@Override
	public boolean handlePassengerEntering(PassengerRequest request, double time) {
		if(passengerEngine.pickUpPassenger(this,driver,request,time)){
			return true;
		}
		return false;
	}

	@Override
	public boolean handlePassengerLeaving(PassengerRequest request,  double time) {
		passengerEngine.dropOffPassenger(driver, request, time);
		return true;
	}
}
