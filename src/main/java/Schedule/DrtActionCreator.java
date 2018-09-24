/* *********************************************************************** *
 * project: org.matsim.*
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

package Schedule;

import Dwelling.BusStopActivity;
import ParkingStrategy.InsertionOptimizer.DrtScheduler;
import Passenger.PassengerEngine;
import com.google.inject.Inject;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizerWithOnlineTracking;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.vrpagent.VrpActivity;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.core.mobsim.qsim.QSim;
import DynAgent.*;

/**
 * @author michalm
 */
public class DrtActionCreator implements VrpAgentLogic.DynActionCreator {
	public static final String DRT_STAY_NAME = "DrtStay";
	public final static String DRT_STOP_NAME = "DrtBusStop";
	public static final String DRT_QUEUE_NAME = "DrtQueue";
	private final PassengerEngine passengerEngine;
	private final VrpLegs.LegCreator legCreator;
	private final DrtScheduler drtScheduler;

	@Inject
	public DrtActionCreator(PassengerEngine passengerEngine, VrpOptimizer optimizer, QSim qSim, DrtScheduler drtScheduler) {
		this.passengerEngine = passengerEngine;
		legCreator = VrpLegs.createLegWithOnlineTrackerCreator((VrpOptimizerWithOnlineTracking)optimizer,
				qSim.getSimTimer());
		this.drtScheduler = drtScheduler;
	}

	@Override
	public DynAction createAction(DynAgent dynAgent, Vehicle vehicle, double now) {
		DrtTask task = (DrtTask)vehicle.getSchedule().getCurrentTask();
		switch (task.getDrtTaskType()) {
			case DRIVE:
				return legCreator.createLeg(vehicle);

			case STOP:
				DrtStopTask t = (DrtStopTask)task;
				return new BusStopActivity(passengerEngine, dynAgent, t, t.getDropoffRequests(), t.getPickupRequests(),
						DRT_STOP_NAME, drtScheduler, vehicle, ((VehicleImpl)vehicle).getVehicleType().getAccessTime(), ((VehicleImpl)vehicle).getVehicleType().getEgressTime(), now);

			case STAY:
				return new VrpActivity(DRT_STAY_NAME, (StayTask)task);

            case QUEUE:
                return new VrpActivity(DRT_QUEUE_NAME, (StayTask)task);

			default:
				throw new IllegalStateException();
		}
	}
}
