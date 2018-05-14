/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

package ParkingStrategy.ParkingInDepot.InsertionOptimizer;

import ParkingStrategy.DefaultDrtOptimizer;
import ParkingStrategy.MixedParkingStrategy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import Schedule.DrtStayTask;

/**
 * @author michalm
 */
// TODO move to DrtScheduler ??????????????
public class EmptyVehicleRelocator {
	private final TravelTime travelTime;
	private final DrtScheduler scheduler;
	private final LeastCostPathCalculator router;

	@Inject
	public EmptyVehicleRelocator(@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network,
                                 @Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime,
                                 @Named(DefaultDrtOptimizer.DRT_OPTIMIZER) TravelDisutility travelDisutility,
                                 DrtScheduler scheduler) {
		this.travelTime = travelTime;
		this.scheduler = scheduler;

		router = new FastAStarEuclideanFactory().createPathCalculator(network, travelDisutility, travelTime);
	}

	public void relocateVehicle(Vehicle vehicle, Link link, double time) {
		DrtStayTask currentTask = (DrtStayTask)vehicle.getSchedule().getCurrentTask();
		Link currentLink = currentTask.getLink();

		if (currentLink != link) {
			VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, link, time, router, travelTime);
			if (path.getArrivalTime() < vehicle.getServiceEndTime()) {
				scheduler.relocateEmptyVehicle(vehicle, path);
			}
		}
	}

	public void addVehicleToDepotOrStreet(Vehicle vehicle, double time) {
		Schedule schedule = vehicle.getSchedule();
		DrtStayTask stayTask = (DrtStayTask) schedule.getTasks().get(schedule.getTasks().size() - 1);
		if (time < MixedParkingStrategy.dayT0){
			stayTask.setEndTime(MixedParkingStrategy.dayT0);
		}else if(time < MixedParkingStrategy.dayT1){
			stayTask.setEndTime(MixedParkingStrategy.dayT1);
		}
	}
}
