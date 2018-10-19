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

package ParkingStrategy.InsertionOptimizer;

import Schedule.VehicleData;
import Schedule.AtodRequest;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import Path.OneToManyPathSearch.DrtPathData;


import java.util.Collection;
import java.util.Map;

/**
 * @author michalm
 */
public interface PrecalculatablePathDataProvider extends PathDataProvider {
	void precalculatePathData(AtodRequest drtRequest, Collection<VehicleData.Entry> vEntries);

	static PathDataSet getPathDataSet(AtodRequest drtRequest, VehicleData.Entry vEntry, Map<Id<Link>, DrtPathData> pathsToPickupMap,
									  Map<Id<Link>, DrtPathData> pathsFromPickupMap, Map<Id<Link>, DrtPathData> pathsToDropoffMap,
									  Map<Id<Link>, DrtPathData> pathsFromDropoffMap) {

		int length = vEntry.stops.size() + 1;
		DrtPathData[] pathsToPickup = new DrtPathData[length];
		DrtPathData[] pathsFromPickup = new DrtPathData[length];
		DrtPathData[] pathsToDropoff = new DrtPathData[length];
		DrtPathData[] pathsFromDropoff = new DrtPathData[length];

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
