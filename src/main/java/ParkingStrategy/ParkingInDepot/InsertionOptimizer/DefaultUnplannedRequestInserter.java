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

import ParkingStrategy.VehicleData;
import Passenger.Event.DrtRequestScheduledEvent;
import Vehicle.FleetImpl;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestRejectedEvent;


import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;
import Schedule.DrtRequest;

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



		Iterator<DrtRequest> reqIter = unplannedRequests.iterator();
		while (reqIter.hasNext()) {
			DrtRequest req = reqIter.next();
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
				scheduler.insertPickup(bestInsertion.vehicleEntry, req, bestInsertion.insertion);
				vData.updateEntry(bestInsertion.vehicleEntry.vehicle);
				scheduler.insertDropoff(bestInsertion.vehicleEntry, req, bestInsertion.insertion);
				vData.updateEntry(bestInsertion.vehicleEntry.vehicle);
				eventsManager.processEvent(new DrtRequestScheduledEvent(mobsimTimer.getTimeOfDay(), req.getId(),
						bestInsertion.vehicleEntry.vehicle.getId(), req.getPickupTask().getEndTime(),
						req.getDropoffTask().getBeginTime()));
			}
			reqIter.remove();
		}
	}
}
