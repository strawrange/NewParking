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
import EAV.*;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.InsertionOptimizer.DrtScheduler;
import ParkingStrategy.InsertionOptimizer.EmptyVehicleRelocator;
import ParkingStrategy.InsertionOptimizer.UnplannedRequestInserter;
import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.data.DrtRequest;
import org.matsim.contrib.drt.data.validator.DrtRequestValidator;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.passenger.events.DrtRequestRejectedEvent;
import Run.AtodConfigGroup;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.drt.schedule.DrtTask;
import Schedule.DrtStopTask;
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
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;
import Schedule.AtodRequest;
import Schedule.DrtQueueTask;
import Schedule.VehicleImpl;


import java.util.Collection;
import java.util.TreeSet;

/**
 * @author michalm
 */
public class DefaultDrtOptimizer implements DrtOptimizer, MobsimBeforeCleanupListener {
	public static final String DRT_OPTIMIZER = "drt_optimizer";

	private final AtodConfigGroup drtCfg;
	private final Fleet fleet;
	private final DrtScheduler scheduler;
	private final ParkingStrategy parkingStrategy;
	private final MobsimTimer mobsimTimer;
	private final EventsManager eventsManager;
	private final DrtRequestValidator requestValidator;
	private final EmptyVehicleRelocator relocator;
	private final UnplannedRequestInserter requestInserter;

	private final Collection<AtodRequest> unplannedRequests = new TreeSet<AtodRequest>(
			PassengerRequests.ABSOLUTE_COMPARATOR);
	private boolean requiresReoptimization = false;
	@Inject(optional = true)
	private DepotManager depotManager;
	@Inject(optional = true)
	private ChargerManager chargerManager;
	private final BayManager bayManager;
	@Inject(optional = true)
	private ChargingStrategy chargingStrategy;

	@Inject
	public DefaultDrtOptimizer(AtodConfigGroup drtCfg, Fleet fleet, MobsimTimer mobsimTimer, EventsManager eventsManager,
                               DrtRequestValidator requestValidator, ParkingStrategy parkingStrategy,
                               DrtScheduler scheduler, EmptyVehicleRelocator relocator, UnplannedRequestInserter requestInserter, BayManager bayManager) {
		this.drtCfg = drtCfg;
		this.fleet = fleet;
		this.mobsimTimer = mobsimTimer;
		this.eventsManager = eventsManager;
		this.requestValidator = requestValidator;
		this.parkingStrategy =  parkingStrategy;
		this.scheduler = scheduler;
		this.relocator = relocator;
		this.requestInserter = requestInserter;
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
		for (Id<Link> lid: bayManager.getLinksToBeUpdated()){
			scheduler.modifyLanes(lid, mobsimTimer.getTimeOfDay(), 0.D);
		}
		bayManager.clearLinksToBeUpdated();
//		if (parkingStrategy != null && e.getSimulationTime() % drtCfg.getRebalancingInterval() == 0) {
//			rebalanceMultiOperatorFleet();
//		}
	}

//	private void rebalanceMultiOperatorFleet() {
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
		AtodRequest drtRequest = (AtodRequest)request;
		if (!requestValidator.validateDrtRequest(drtRequest)) {
			drtRequest.setRejected(true);
			eventsManager.processEvent(new DrtRequestRejectedEvent(mobsimTimer.getTimeOfDay(), drtRequest.getId()));
			eventsManager.processEvent(new PersonStuckEvent(mobsimTimer.getTimeOfDay(),((AtodRequest) request).getPassenger().getId(),((AtodRequest) request).getFromLink().getId(),((AtodRequest) request).getMode()));
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
		}

		if (isCurrentChargeTask(vehicle)){
			DrtChargeTask currentTask = (DrtChargeTask) vehicle.getSchedule().getCurrentTask();
			Charger charger = currentTask.getCharger();
			charger.removeVehicle((VehicleImpl) vehicle, mobsimTimer.getTimeOfDay());
			if (charger.isEmpty()){
                    scheduler.modifyLanes(charger.getLink().getId(), mobsimTimer.getTimeOfDay(), 0.D);
            }
		}

		if (isNextStopTask(vehicle)){
			int currentTaskIdx = schedule.getCurrentTask().getTaskIdx();
			DrtStopTask nextTask = (DrtStopTask) schedule.getTasks().get(currentTaskIdx + 1);
			Bay bay = bayManager.getBayByLinkId(nextTask.getLink().getId());
			bay.addVehicle(Id.createVehicleId(vehicle.getId()));
			if  (isCurrentDriveOrStayTask(vehicle)){
				eventsManager.processEvent(new VehicleArrivesAtFacilityEvent(mobsimTimer.getTimeOfDay(),Id.createVehicleId(vehicle.getId().toString()),
						bayManager.getStopIdByLinkId(nextTask.getLink().getId()), 1));
			}
			if (bay.isFull() && bay.getVehicles().contains(vehicle.getId())) {
				scheduler.insertQuequingTask(vehicle, 1.0);
				return;
			}
			if (isCurrentQueueTask(vehicle)){
				scheduler.updateQueue(vehicle);
			}
		}

		if (isNextChargeTask(vehicle)){
			int currentTaskIdx = schedule.getCurrentTask().getTaskIdx();
			DrtChargeTask nextTask = (DrtChargeTask) schedule.getTasks().get(currentTaskIdx + 1);
			Charger charger = nextTask.getCharger();
			if (!charger.checkChargerAvailability() && !charger.isQueue(vehicle) && !charger.isCharging(vehicle)){
				scheduler.changeCharger(vehicle, mobsimTimer.getTimeOfDay());
				schedule.nextTask();
			}else {
				charger.addVehicle((VehicleImpl) vehicle, mobsimTimer.getTimeOfDay());
				if (!charger.isOpen(mobsimTimer.getTimeOfDay())){
					scheduler.insertQuequingTask(vehicle, charger.getStartTime() - mobsimTimer.getTimeOfDay());
                        scheduler.modifyLanes(charger.getLink().getId(), mobsimTimer.getTimeOfDay(), -1.D);
					return;
				}
				if (charger.isFull() && charger.isQueue(vehicle)) {
					scheduler.insertQuequingTask(vehicle, 1.0);
                        scheduler.modifyLanes(charger.getLink().getId(), mobsimTimer.getTimeOfDay(), -1.D);
					return;
				}
				if (!charger.isEmpty()){
                        scheduler.modifyLanes(charger.getLink().getId(), mobsimTimer.getTimeOfDay(), -1.D);
                }
			}
            if (isCurrentQueueTask(vehicle)) {
                scheduler.updateQueue(vehicle);
                if (charger.isEmpty()){
                    if (charger.getLink().getCapacity(mobsimTimer.getTimeOfDay()) != charger.getLink().getCapacity()) {
                        scheduler.modifyLanes(charger.getLink().getId(), mobsimTimer.getTimeOfDay(), 0.D);
                    }
                }
            }

		}

		schedule.nextTask();
//		if (isCurrentStopTask(vehicle)){
//			eventsManager.processEvent(new VehicleArrivesAtFacilityEvent(mobsimTimer.getTimeOfDay(),Id.createVehicleId(vehicle.getId().toString()),
//					bayManager.getBayByLinkId(((DrtStopTask) schedule.getCurrentTask()).getLink().getId()).getTransitStop().getId(), 1));
//		}

		if (chargingStrategy != null && isIdle(vehicle)) {
			if (((VehicleImpl)vehicle).getBattery() <= DischargingRate.getMinBattery(((VehicleImpl) vehicle).getVehicleType().getId())){
				if (charging(vehicle)) {
					return;
				}
			}
		}

		if (parkingStrategy !=null && isIdle(vehicle) && isNotChargingTransit(vehicle)){
			parking(vehicle);
			((VehicleImpl)vehicle).changeParking(true);
		}

		if (parkingStrategy !=null && isParking((VehicleImpl) vehicle) && !isNextChargeTask(vehicle)){
			parkingStrategy.departing(vehicle, mobsimTimer.getTimeOfDay());
			((VehicleImpl)vehicle).changeParking(false);
		}

		if (parkingStrategy.getCurrentStrategy(vehicle.getId()) != null && parkingStrategy.getCurrentStrategy(vehicle.getId()).equals(ParkingStrategy.Strategies.ParkingInDepot) && isCancelReservation(vehicle)){
			depotManager.vehicleLeavingDepot(vehicle);
		}
	}

	private boolean isNotChargingTransit(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		int previousTaskIdx = schedule.getCurrentTask().getTaskIdx() - 1;
		if (previousTaskIdx > 0 &&  schedule.getTasks().get(previousTaskIdx) instanceof DrtChargeTask){
			return schedule.getCurrentTask().getBeginTime() != schedule.getCurrentTask().getEndTime();
		}
		return true;
	}

	private boolean isCurrentChargeTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		return (schedule.getCurrentTask() instanceof DrtChargeTask);
	}

	private boolean isNextChargeTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (schedule.getCurrentTask().getTaskIdx() == schedule.getTaskCount() - 1){
			return false;
		}
		Task nextTask = schedule.getTasks().get(schedule.getCurrentTask().getTaskIdx() + 1);
		if (!(nextTask instanceof DrtChargeTask)){
			return false;
		}
		return true;
	}

	private boolean charging(Vehicle vehicle) {
		ChargerPathPair r = chargingStrategy.charging(vehicle, mobsimTimer.getTimeOfDay());
		if (r != null) {
			scheduler.chargingVehicle(vehicle, r);
			return true;
		}
		return false;
	}

	private boolean isCurrentQueueTask(Vehicle vehicle) {
		Schedule schedule = vehicle.getSchedule();
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED){
			return false;
		}
		if (!(schedule.getCurrentTask() instanceof DrtQueueTask)){
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
		return (schedule.getCurrentTask() instanceof DrtStopTask);
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
		if (! (schedule.getCurrentTask() instanceof  DrtStopTask)){
			return false;
		}
		if (vehicle.getSchedule().getCurrentTask().getTaskIdx() - 2 < 0){
			return false;
		}
		if ( !(schedule.getTasks().get(vehicle.getSchedule().getCurrentTask().getTaskIdx() - 2) instanceof  DrtStayTask)){
			return false;
		}
		return true;
	}

	private boolean isParking(VehicleImpl vehicle) {
		Schedule schedule = vehicle.getSchedule();

		// only active vehicles
		if (schedule.getStatus() != Schedule.ScheduleStatus.STARTED) {
			return false;
		}

		// previous task is STAY
		int previousTaskIdx = schedule.getCurrentTask().getTaskIdx() - 1;
		int nextTaskIdx = schedule.getCurrentTask().getTaskIdx() + 1;
//
		if (previousTaskIdx < 0){
			return false;
		}
//
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
		//		&& (((DrtTask)getTasks().get(previousTaskIdx)).getDrtTaskType() != DrtTask.DrtTaskType.STAY) );
//		return true;
		return vehicle.isParking();
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

//		if (currentTask.getTaskIdx() != schedule.getTasks().size() - 1){
//			return false;
//		}


		//return (previousTaskIdx >= 0
		//		&& (((DrtTask)getTasks().get(previousTaskIdx)).getDrtTaskType() != DrtTask.DrtTaskType.STAY) );
		return currentTask.getBeginTime() != currentTask.getEndTime();
	}

	@Override
	public void vehicleEnteredNextLink(Vehicle vehicle, Link nextLink) {
		// scheduler.updateTimeline(vehicle);

		// TODO we may here possibly decide whether or not to reoptimize
		// if (delays/speedups encountered) {requiresReoptimization = true;}
	}

	@Override
	public void notifyMobsimBeforeCleanup(MobsimBeforeCleanupEvent e) {
		for (AtodRequest drtRequest: unplannedRequests){
			eventsManager.processEvent(new PersonStuckEvent(mobsimTimer.getTimeOfDay(), drtRequest.getPassenger().getId(),drtRequest.getFromLink().getId(),drtRequest.getMode()));
		}
		for (Vehicle vehicle: fleet.getVehicles().values()) {
			if (vehicle.getSchedule().getStatus() != Schedule.ScheduleStatus.STARTED){
				continue;
			}
			for (int i = vehicle.getSchedule().getCurrentTask().getTaskIdx(); i < vehicle.getSchedule().getTasks().size(); i++) {
				Task task = vehicle.getSchedule().getTasks().get(i);
				if (task instanceof DrtStopTask)
					for (DrtRequest drtRequest : ((DrtStopTask) task).getPickupRequests())
						eventsManager.processEvent(new PersonStuckEvent(mobsimTimer.getTimeOfDay(), drtRequest.getPassenger().getId(), drtRequest.getFromLink().getId(), ((AtodRequest)drtRequest).getMode()));
			}
		}
	}
}
