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

package ParkingStrategy.InsertionOptimizer;

import EAV.DischargingRate;
import Run.AtodConfigGroup;
import Schedule.VehicleData;
import Schedule.AtodRequest;
import Schedule.VehicleImpl;
import Vehicle.FleetImpl;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestRejectedEvent;


import org.matsim.contrib.drt.passenger.events.DrtRequestScheduledEvent;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.ScheduleImpl;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;

import java.util.*;

/**
 * @author michalm
 */
public class DefaultUnplannedRequestInserter implements UnplannedRequestInserter, MobsimBeforeCleanupListener {
	private static final Logger log = Logger.getLogger(DefaultUnplannedRequestInserter.class);

	private final DrtConfigGroup drtCfg;
	private final Fleet fleet;
	private final MobsimTimer mobsimTimer;
	private final EventsManager eventsManager;
	private final DrtScheduler scheduler;

	private final ParallelMultiVehicleInsertionProblem insertionProblem;

	@Inject
	public DefaultUnplannedRequestInserter(DrtConfigGroup drtCfg, Fleet fleet, MobsimTimer mobsimTimer,
										   EventsManager eventsManager, DrtScheduler scheduler, PrecalculatablePathDataProvider pathDataProvider, AtodConfigGroup atodConfigGroup) {
		this.drtCfg = drtCfg;
		this.fleet = fleet;
		this.mobsimTimer = mobsimTimer;
		this.eventsManager = eventsManager;
		this.scheduler = scheduler;

		insertionProblem = new ParallelMultiVehicleInsertionProblem(pathDataProvider, drtCfg, mobsimTimer);
	}

	@Override
	public void notifyMobsimBeforeCleanup(@SuppressWarnings("rawtypes") MobsimBeforeCleanupEvent e) {
		insertionProblem.shutdown();
	}

	@Override
	public void scheduleUnplannedRequests(Collection<AtodRequest> unplannedRequests) {
		if (unplannedRequests.isEmpty()) {
			return;
		}
		//ArrayList<SingleVehicleInsertionProblem.BestInsertion> check = new ArrayList<>();
		//ArrayList<ArrayList<Task>> vehicles = new ArrayList<>();

		Iterator<AtodRequest> reqIter = unplannedRequests.iterator();
		while (reqIter.hasNext()) {
			AtodRequest req = reqIter.next();
			VehicleData vData = new VehicleData(mobsimTimer.getTimeOfDay(), ((FleetImpl)fleet).getVehicles(req.getMode()).values().stream());
			Optional<SingleVehicleInsertionProblem.BestInsertion> best = insertionProblem.findBestInsertion(req, vData.getEntries());
			if (!best.isPresent()) {
				req.setRejected(true);
				eventsManager.processEvent(new DrtRequestRejectedEvent(mobsimTimer.getTimeOfDay(), req.getId()));
				eventsManager.processEvent(new PersonStuckEvent(mobsimTimer.getTimeOfDay(), req.getPassenger().getId(), req.getFromLink().getId(),req.getMode()));
				if (drtCfg.isPrintDetailedWarnings()) {
					log.warn("No vehicle found for drt request " + req + " from passenger id="
							+ req.getPassenger().getId() + " fromLinkId=" + req.getFromLink().getId());
				}
			} else {
				SingleVehicleInsertionProblem.BestInsertion bestInsertion = best.get();
				//check.add(bestInsertion);
				//vehicles.add(new ArrayList<>(bestInsertion.vehicleEntry.vehicle.getSchedule().getTasks()));
				scheduler.insertPickup(bestInsertion.vehicleEntry, req, bestInsertion.insertion);
				vData.updateEntry(bestInsertion.vehicleEntry.vehicle);
				scheduler.insertDropoff(bestInsertion.vehicleEntry, req, bestInsertion.insertion);
				vData.updateEntry(bestInsertion.vehicleEntry.vehicle);
				eventsManager.processEvent(new DrtRequestScheduledEvent(mobsimTimer.getTimeOfDay(), req.getId(),
						bestInsertion.vehicleEntry.vehicle.getId(), req.getPickupTask().getEndTime(),
						req.getDropoffTask().getBeginTime()));
//				Task currentTask = bestInsertion.vehicleEntry.vehicle.getSchedule().getCurrentTask();
//				Double drive = 0.0;
//				//synchronized (drive) {
//				if (currentTask instanceof StayTaskImpl) {
//					drive = drive + ((StayTaskImpl) currentTask).getLink().getLength();
//				}
//				for (int i = bestInsertion.vehicleEntry.vehicle.getSchedule().getCurrentTask().getTaskIdx(); i < bestInsertion.vehicleEntry.vehicle.getSchedule().getTasks().size(); i++) {
//					Task drtTask = bestInsertion.vehicleEntry.vehicle.getSchedule().getTasks().get(i);
//					if (drtTask instanceof DrtDriveTask) {
//						drive = drive + VrpPaths.calcDistance(((DrtDriveTask) drtTask).getPath());
//					}
//				}
//				drive = drive + bestInsertion.insertion.pathToPickup.getPathDistance() + bestInsertion.insertion.pathFromPickup.getPathDistance() + (bestInsertion.insertion.dropoffIdx == bestInsertion.insertion.pickupIdx ? 0 : bestInsertion.insertion.pathToDropoff.getPathDistance()) +
//						(bestInsertion.insertion.dropoffIdx == bestInsertion.vehicleEntry.stops.size() ? 0 : bestInsertion.insertion.pathFromDropoff.getPathDistance());
//				//}
//				double estimatedBatteryAfterAccept = (((VehicleImpl) bestInsertion.vehicleEntry.vehicle).getBattery() - DischargingRate.calculateDischargeByDistance( drive));
////				if (estimatedBatteryAfterAccept < bestInsertion.insertion.estimatedBattery){
////					System.out.println();
////				}
			}

			reqIter.remove();
		}
//		for (int i = 0; i < check.size() - 1; i++){
//			SingleVehicleInsertionProblem.BestInsertion b = check.get(i);
//			for (int j = i + 1; j < check.size(); j++){
//				SingleVehicleInsertionProblem.BestInsertion a = check.get(j);
//				if (b.vehicleEntry.vehicle.getId().equals(a.vehicleEntry.vehicle.getId()) && b.insertion.pickupIdx == a.insertion.pickupIdx && b.insertion.dropoffIdx == a.insertion.dropoffIdx){
//					VehicleImpl vehicle = (VehicleImpl)b.vehicleEntry.vehicle;
//					Double drive = 0.0;
//					//synchronized (drive) {
//					Task currentTask = vehicle.getSchedule().getCurrentTask();
//					if (currentTask instanceof StayTaskImpl) {
//						drive = drive + ((StayTaskImpl) currentTask).getLink().getLength();
//					}
//					for (int m = vehicle.getSchedule().getCurrentTask().getTaskIdx(); m < vehicles.get(i).size(); m++) {
//						Task drtTask = vehicles.get(i).get(m);
//						if (drtTask instanceof DrtDriveTask) {
//							drive = drive + VrpPaths.calcDistance(((DrtDriveTask) drtTask).getPath());
//						}
//					}
//					double drivea = drive + a.insertion.pathToPickup.getPathDistance() + a.insertion.pathFromPickup.getPathDistance() + (a.insertion.dropoffIdx == a.insertion.pickupIdx ? 0 : a.insertion.pathToDropoff.getPathDistance()) +
//							(a.insertion.dropoffIdx == a.vehicleEntry.stops.size() ? 0 : a.insertion.pathFromDropoff.getPathDistance());
//					//}
//					double driveb = drive + b.insertion.pathToPickup.getPathDistance() + b.insertion.pathFromPickup.getPathDistance() + (b.insertion.dropoffIdx == b.insertion.pickupIdx ? 0 : b.insertion.pathToDropoff.getPathDistance()) +
//							(b.insertion.dropoffIdx == b.vehicleEntry.stops.size() ? 0 : b.insertion.pathFromDropoff.getPathDistance());
//					double estimatedBatteryAfterAccept = Double.min(vehicle.getBattery() - DischargingRate.calculateDischargeByDistance( drivea), vehicle.getBattery() - DischargingRate.calculateDischargeByDistance( driveb));
//					if (estimatedBatteryAfterAccept != b.insertion.estimatedBattery){
//						System.out.println();
//					}
//				}
//			}
//		}
	}
}
