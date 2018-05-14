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


import BayInfrastructure.Bay;
import BayInfrastructure.BayManager;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.ParkingInDepot.InsertionOptimizer.DrtScheduler;
import ParkingStrategy.ParkingInDepot.InsertionOptimizer.EmptyVehicleRelocator;
import ParkingStrategy.ParkingInDepot.InsertionOptimizer.UnplannedRequestInserter;
import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.passenger.events.DrtRequestRejectedEvent;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Request;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequests;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import Schedule.*;
import Schedule.validator.DrtRequestValidator;


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
	private final EmptyVehicleRelocator relocator;
	private final UnplannedRequestInserter requestInserter;

	private final Collection<DrtRequest> unplannedRequests = new TreeSet<DrtRequest>(
			PassengerRequests.ABSOLUTE_COMPARATOR);
	private boolean requiresReoptimization = false;
	private final DepotManager depotManager;
	private final BayManager bayManager;

	@Inject
	public DefaultDrtOptimizer(DrtConfigGroup drtCfg, Fleet fleet, MobsimTimer mobsimTimer, EventsManager eventsManager,
							   DrtRequestValidator requestValidator, ParkingStrategy parkingStrategy,
							   DrtScheduler scheduler, EmptyVehicleRelocator relocator, UnplannedRequestInserter requestInserter, DepotManager depotManager, BayManager bayManager) {
		this.drtCfg = drtCfg;
		this.fleet = fleet;
		this.mobsimTimer = mobsimTimer;
		this.eventsManager = eventsManager;
		this.requestValidator = requestValidator;
		this.parkingStrategy =  parkingStrategy;
		this.scheduler = scheduler;
		this.relocator = relocator;
		this.requestInserter = requestInserter;
		this.depotManager = depotManager;
		this.bayManager = bayManager;
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
		if (mobsimTimer.getTimeOfDay() == 30 * 3600 - 300){
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
		if (parkingStrategy instanceof MixedParkingStrategy){
			relocator.addVehicleToDepotOrStreet(vehicle, mobsimTimer.getTimeOfDay());
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
		Schedule schedule = vehicle.getSchedule();

		if (parkingStrategy instanceof MixedParkingStrategy && isRelocateToDepotOrStreet(vehicle)){
			schedule.addTask(new DrtStayTask(schedule.getCurrentTask().getEndTime(), vehicle.getServiceEndTime(), ((DrtStayTask)vehicle.getSchedule().getCurrentTask()).getLink()));
		}
		if (isCurrentStopTask(vehicle)){
			eventsManager.processEvent(new VehicleDepartsAtFacilityEvent(mobsimTimer.getTimeOfDay(),Id.createVehicleId(vehicle.getId().toString()),
					bayManager.getBayByLinkId(((DrtStopTask) schedule.getCurrentTask()).getLink().getId()).getTransitStop().getId(), 1));
			scheduler.modifyLanes(((DrtStopTask) schedule.getCurrentTask()).getLink(), mobsimTimer.getTimeOfDay(), 0.D);
		}

		if (isNextStopTask(vehicle)){
			int currentTaskIdx = schedule.getCurrentTask().getTaskIdx();
			DrtStopTask nextTask = (DrtStopTask) schedule.getTasks().get(currentTaskIdx + 1);
			if (isCurrentDriveOrStayTask(vehicle)){
				eventsManager.processEvent(new VehicleArrivesAtFacilityEvent(mobsimTimer.getTimeOfDay(),Id.createVehicleId(vehicle.getId().toString()),
					bayManager.getStopIdByLinkId(nextTask.getLink().getId()), 1));
			}
			Bay bay = bayManager.getBayByLinkId(nextTask.getLink().getId());
            bay.addVehicle(Id.createVehicleId(vehicle.getId()));
			if (bay.isFull() && bay.getVehicles().contains(vehicle.getId())) {
				scheduler.insertQuequingTask(vehicle);
				return;
			}
			if (isCurrentQueueTask(vehicle)){
				scheduler.updateQueue(vehicle);
			}
		}

		schedule.nextTask();
//		if (isCurrentStopTask(vehicle)){
//			eventsManager.processEvent(new VehicleArrivesAtFacilityEvent(mobsimTimer.getTimeOfDay(),Id.createVehicleId(vehicle.getId().toString()),
//					bayManager.getBayByLinkId(((DrtStopTask) schedule.getCurrentTask()).getLink().getId()).getTransitStop().getId(), 1));
//		}

		if (parkingStrategy != null && isIdle(vehicle)) {
			parking(vehicle);
		}

		if (parkingStrategy !=null && isParking(vehicle)){
			parkingStrategy.departing(vehicle, mobsimTimer.getTimeOfDay());
		}

		if (depotManager != null && isCancelReservation(vehicle)){
			depotManager.vehicleLeavingDepot(vehicle);
		}
	}

	private boolean isCurrentQueueTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (!(schedule.getCurrentTask() instanceof DrtQuequeTask)){
			return false;
		}
		return true;
	}

	private boolean isCurrentDriveOrStayTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if ((schedule.getCurrentTask() instanceof DrtDriveTask)){
			return true;
		}
		if (schedule.getCurrentTask() instanceof DrtStayTask){
			return true;
		}
		return false;
	}


	private boolean isNextStopTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (schedule.getCurrentTask().getTaskIdx() == schedule.getTaskCount() - 1){
			return false;
		}
		Task nextTask = schedule.getTasks().get(schedule.getCurrentTask().getTaskIdx() + 1);
		if (!(nextTask instanceof DrtStopTask)){
			return false;
		}
		return true;
	}

	private boolean isCurrentStopTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (!(schedule.getCurrentTask() instanceof DrtStopTask)){
			return false;
		}
		return true;
	}

	private boolean isRelocateToDepotOrStreet(Vehicle vehicle){
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (mobsimTimer.getTimeOfDay() != MixedParkingStrategy.dayT0 && mobsimTimer.getTimeOfDay() != MixedParkingStrategy.dayT1 ){
			return false;
		}
		if (!(schedule.getCurrentTask() instanceof DrtStayTask)){
			return false;
		}
		if (schedule.getCurrentTask().getTaskIdx() != schedule.getTaskCount() - 1){
			return false;
		}
		return true;
	}



	private boolean isCancelReservation(Vehicle vehicle){
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (! (vehicle.getSchedule().getCurrentTask() instanceof  DrtStopTask)){
			return false;
		}

		if ( !(vehicle.getSchedule().getTasks().get(vehicle.getSchedule().getCurrentTask().getTaskIdx() - 2) instanceof  DrtStayTask)){
			return false;
		}
		return true;
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
		if (!(previousTask instanceof DrtStayTask)) {
			return false;
		}
		if (schedule.getCurrentTask() instanceof DrtStayTask){
			return false;
		}
		if (schedule.getTasks().size() <= nextTaskIdx){
			throw new RuntimeException("The final task should be stay task!");
		}

		DrtTask nextTask = (DrtTask) schedule.getTasks().get(nextTaskIdx);
		if (nextTask instanceof DrtStayTask){
			return false; // it is relocating not parking
		}
		// previous task was STOP
		//int previousTaskIdx = currentTask.getTaskIdx() - 1;
		//return (previousTaskIdx >= 0
		//		&& (((DrtTask)Schedule.getTasks().get(previousTaskIdx)).getDrtTaskType() != DrtTask.DrtTaskType.STAY) );
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
		if (!(currentTask instanceof DrtStayTask)) {
			return false;
		}

		// previous task was STOP
		//int previousTaskIdx = currentTask.getTaskIdx() - 1;
		//return (previousTaskIdx >= 0
		//		&& (((DrtTask)Schedule.getTasks().get(previousTaskIdx)).getDrtTaskType() != DrtTask.DrtTaskType.STAY) );
		return true;
	}

	@Override
	public void vehicleEnteredNextLink(Vehicle vehicle, Link nextLink) {
		// scheduler.updateTimeline(vehicle);

		// TODO we may here possibly decide whether or not to reoptimize
		// if (delays/speedups encountered) {requiresReoptimization = true;}
	}
}
