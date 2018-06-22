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

package ParkingStrategy.ParkingInDepot.InsertionOptimizer;

import ParkingStrategy.VehicleData;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import Path.OneToManyPathSearch.PathData;
import Schedule.DrtRequest;

import java.util.Collection;
import java.util.Map;

/**
 * @author michalm
 */
public interface PrecalculatablePathDataProvider extends PathDataProvider {
	void precalculatePathData(DrtRequest drtRequest, Collection<VehicleData.Entry> vEntries);

	static PathDataSet getPathDataSet(DrtRequest drtRequest, VehicleData.Entry vEntry, Map<Id<Link>, PathData> pathsToPickupMap,
									  Map<Id<Link>, PathData> pathsFromPickupMap, Map<Id<Link>, PathData> pathsToDropoffMap,
									  Map<Id<Link>, PathData> pathsFromDropoffMap) {

		int length = vEntry.stops.size() + 1;
		PathData[] pathsToPickup = new PathData[length];
		PathData[] pathsFromPickup = new PathData[length];
		PathData[] pathsToDropoff = new PathData[length];
		PathData[] pathsFromDropoff = new PathData[length];

		pathsToPickup[0] = pathsToPickupMap.get(vEntry.start.link.getId());// start->pickup
		pathsFromPickup[0] = pathsFromPickupMap.get(drtRequest.getToLink().getId());// pickup->dropoff

		int i = 1;
		for (VehicleData.Stop s : vEntry.stops) {
			Id<Link> linkId = s.task.getLink().getId();
			pathsToPickup[i] = pathsToPickupMap.get(linkId);
			pathsFromPickup[i] = pathsFromPickupMap.get(linkId);
			pathsToDropoff[i] = pathsToDropoffMap.get(linkId);
			pathsFromDropoff[i] = pathsFromDropoffMap.get(linkId);
			i++;
		}

		return new PathDataSet(pathsToPickup, pathsFromPickup, pathsToDropoff, pathsFromDropoff);
	}
}