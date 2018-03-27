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

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.contrib.drt.data.DrtRequest;
import org.matsim.contrib.drt.optimizer.VehicleData;
import org.matsim.contrib.drt.optimizer.insertion.PrecalculatablePathDataProvider;
import org.matsim.contrib.drt.optimizer.insertion.UnplannedRequestInserter;
import org.matsim.contrib.drt.passenger.events.DrtRequestRejectedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestScheduledEvent;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

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
                                           EventsManager eventsManager, DrtScheduler scheduler, PrecalculatablePathDataProvider pathDataProvider) {
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
	public void scheduleUnplannedRequests(Collection<DrtRequest> unplannedRequests) {
		if (unplannedRequests.isEmpty()) {
			return;
		}

		VehicleData vData = new VehicleData(mobsimTimer.getTimeOfDay(), fleet.getVehicles().values().stream());

		Iterator<DrtRequest> reqIter = unplannedRequests.iterator();
		while (reqIter.hasNext()) {
			DrtRequest req = reqIter.next();
			Optional<SingleVehicleInsertionProblem.BestInsertion> best = insertionProblem.findBestInsertion(req, vData.getEntries());
			if (!best.isPresent()) {
				req.setRejected(true);
				eventsManager.processEvent(new DrtRequestRejectedEvent(mobsimTimer.getTimeOfDay(), req.getId()));
				if (drtCfg.isPrintDetailedWarnings()) {
					log.warn("No vehicle found for drt request " + req + " from passenger id="
							+ req.getPassenger().getId() + " fromLinkId=" + req.getFromLink().getId());
				}
			} else {
				SingleVehicleInsertionProblem.BestInsertion bestInsertion = best.get();
				Schedule schedule = bestInsertion.vehicleEntry.vehicle.getSchedule();
				if (schedule.getCurrentTask() instanceof DrtDriveTask && schedule.getTasks().get(schedule.getCurrentTask().getTaskIdx() + 1) instanceof DrtStayTask){

				}
				scheduler.insertRequest(bestInsertion.vehicleEntry, req, bestInsertion.insertion);
				vData.updateEntry(bestInsertion.vehicleEntry.vehicle);
				eventsManager.processEvent(new DrtRequestScheduledEvent(mobsimTimer.getTimeOfDay(), req.getId(),
						bestInsertion.vehicleEntry.vehicle.getId(), req.getPickupTask().getEndTime(),
						req.getDropoffTask().getBeginTime()));
			}
			reqIter.remove();
		}
	}
}
