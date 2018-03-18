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

package ParkingStrategy;


import ParkingStrategy.ParkingStrategy;
import com.google.inject.Inject;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.data.DrtRequest;
import org.matsim.contrib.drt.data.validator.DrtRequestValidator;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.optimizer.depot.DepotFinder;
import org.matsim.contrib.drt.optimizer.depot.Depots;
import org.matsim.contrib.drt.optimizer.insertion.UnplannedRequestInserter;
import org.matsim.contrib.drt.passenger.events.DrtRequestRejectedEvent;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.drt.scheduler.DrtScheduler;
import org.matsim.contrib.drt.scheduler.EmptyVehicleRelocator;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequests;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;

import java.util.Collection;
import java.util.TreeSet;

/**
 * @author michalm
 */
public class DefaultDrtOptimizer implements DrtOptimizer {
	public static final String DRT_OPTIMIZER = "drt_optimizer";

	private final DrtConfigGroup drtCfg;
	private final Fleet fleet;
	private final DrtScheduler scheduler;
	private final ParkingStrategy parkingStrategy;
	private final MobsimTimer mobsimTimer;
	private final EventsManager eventsManager;
	private final DrtRequestValidator requestValidator;
	private final DepotFinder depotFinder;
	private final EmptyVehicleRelocator relocator;
	private final UnplannedRequestInserter requestInserter;

	private final Collection<DrtRequest> unplannedRequests = new TreeSet<DrtRequest>(
			PassengerRequests.ABSOLUTE_COMPARATOR);
	private boolean requiresReoptimization = false;

	@Inject
	public DefaultDrtOptimizer(DrtConfigGroup drtCfg, Fleet fleet, MobsimTimer mobsimTimer, EventsManager eventsManager,
							   DrtRequestValidator requestValidator, DepotFinder depotFinder, ParkingStrategy parkingStrategy,
							   DrtScheduler scheduler, EmptyVehicleRelocator relocator, UnplannedRequestInserter requestInserter) {
		this.drtCfg = drtCfg;
		this.fleet = fleet;
		this.mobsimTimer = mobsimTimer;
		this.eventsManager = eventsManager;
		this.requestValidator = requestValidator;
		this.depotFinder = drtCfg.getIdleVehiclesReturnToDepots() ? depotFinder : null;
		this.parkingStrategy = drtCfg.getRebalancingInterval() != 0 ? parkingStrategy : null;
		this.scheduler = scheduler;
		this.relocator = relocator;
		this.requestInserter = requestInserter;
	}

	@Override
	public void notifyMobsimBeforeSimStep(@SuppressWarnings("rawtypes") MobsimBeforeSimStepEvent e) {
		if (requiresReoptimization) {
			for (Vehicle v : fleet.getVehicles().values()) {
				scheduler.updateTimings(v);
			}

			requestInserter.scheduleUnplannedRequests(unplannedRequests);
			requiresReoptimization = false;
		}
		if (mobsimTimer.getTimeOfDay() == 24 * 3600){
			System.out.println();
		}
//		if (parkingStrategy != null && e.getSimulationTime() % drtCfg.getRebalancingInterval() == 0) {
//			rebalanceFleet();
//		}
	}

//	private void rebalanceFleet() {
//		Stream<? extends Vehicle> rebalancableVehicles = fleet.getVehicles().values().stream()
//				.filter(scheduler::isIdle);
//		// right now we relocate only idle vehicles (vehicles that are being relocated cannot be relocated)
//		List<ParkingStrategy.Relocation> relocations = parkingStrategy.calcRelocations(rebalancableVehicles, mobsimTimer.getTimeOfDay());
//		for (ParkingStrategy.Relocation r : relocations) {
//			Vehicle veh = this.fleet.getVehicles().get(r.vid);
//			Link currentLink = ((DrtStayTask)veh.getSchedule().getCurrentTask()).getLink();
//			if (currentLink != r.link) {
//				relocator.relocateVehicle(veh, r.link, mobsimTimer.getTimeOfDay());
//			}
//		}
//	}

	private void parking(Vehicle vehicle) {
		ParkingStrategy.ParkingLocation r = parkingStrategy.parking(vehicle, mobsimTimer.getTimeOfDay());
		Link currentLink = ((DrtStayTask)vehicle.getSchedule().getCurrentTask()).getLink();
		if (r != null && currentLink != r.link) {
			relocator.relocateVehicle(vehicle, r.link, mobsimTimer.getTimeOfDay());
		}
	}

	@Override
	public void requestSubmitted(Request request) {
		DrtRequest drtRequest = (DrtRequest)request;
		if (!requestValidator.validateDrtRequest(drtRequest)) {
			drtRequest.setRejected(true);
			eventsManager.processEvent(new DrtRequestRejectedEvent(mobsimTimer.getTimeOfDay(), drtRequest.getId()));
			return;
		}

		unplannedRequests.add(drtRequest);
		requiresReoptimization = true;
	}

	@Override
	public void nextTask(Vehicle vehicle) {
		scheduler.updateBeforeNextTask(vehicle);

		vehicle.getSchedule().nextTask();

		// if STOP->STAY then choose the best depot
		if (depotFinder != null && Depots.isSwitchingFromStopToStay(vehicle)) {
			Link depotLink = depotFinder.findDepot(vehicle);
			if (depotLink != null) {
				relocator.relocateVehicle(vehicle, depotLink, mobsimTimer.getTimeOfDay());
			}
		}
		if (parkingStrategy != null && isIdle(vehicle)) {
			parking(vehicle);
		}

		if (parkingStrategy !=null && isParking(vehicle)){
			parkingStrategy.departing(vehicle, mobsimTimer.getTimeOfDay());
		}
	}

	private boolean isParking(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();

		// only active vehicles
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
			return false;
		}

		// previous task is STAY
		int previousTaskIdx = schedule.getCurrentTask().getTaskIdx() - 1;
		int nextTaskIdx = schedule.getCurrentTask().getTaskIdx() + 1;

		if (previousTaskIdx < 0){
			return false;
		}

		DrtTask previousTask = (DrtTask)schedule.getTasks().get(previousTaskIdx);
		if (previousTask.getDrtTaskType() != DrtTask.DrtTaskType.STAY) {
			return false;
		}
		if (schedule.getTasks().size() <= nextTaskIdx){
			throw new RuntimeException("The final task should be stay task!");
		}

		DrtTask nextTask = (DrtTask) schedule.getTasks().get(nextTaskIdx);
		if (nextTask.getDrtTaskType() == DrtTask.DrtTaskType.STAY){
			return false; // it is relocating not parking
		}
		// previous task was STOP
		//int previousTaskIdx = currentTask.getTaskIdx() - 1;
		//return (previousTaskIdx >= 0
		//		&& (((DrtTask)schedule.getTasks().get(previousTaskIdx)).getDrtTaskType() != DrtTask.DrtTaskType.STAY) );
		return true;
	}

	private static boolean isIdle(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();

		// only active vehicles
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
			return false;
		}

		// current task is STAY
		DrtTask currentTask = (DrtTask)schedule.getCurrentTask();
		if (currentTask.getDrtTaskType() != DrtTask.DrtTaskType.STAY) {
			return false;
		}

		// previous task was STOP
		//int previousTaskIdx = currentTask.getTaskIdx() - 1;
		//return (previousTaskIdx >= 0
		//		&& (((DrtTask)schedule.getTasks().get(previousTaskIdx)).getDrtTaskType() != DrtTask.DrtTaskType.STAY) );
		return true;
	}

	@Override
	public void vehicleEnteredNextLink(Vehicle vehicle, Link nextLink) {
		// scheduler.updateTimeline(vehicle);

		// TODO we may here possibly decide whether or not to reoptimize
		// if (delays/speedups encountered) {requiresReoptimization = true;}
	}
}
