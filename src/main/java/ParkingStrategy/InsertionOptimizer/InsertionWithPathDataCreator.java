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
import Path.OneToManyPathSearch.DrtPathData;

/**
 * @author michalm
 */
class InsertionWithPathDataCreator {
	private final PathDataProvider.PathDataSet set;
	private final int stopCount;

	InsertionWithPathDataCreator(PathDataProvider pathDataProvider, AtodRequest drtRequest, VehicleData.Entry vEntry) {
		set = pathDataProvider.getPathDataSet(drtRequest, vEntry);
		stopCount = vEntry.stops.size();
	}

	InsertionWithPathData create(InsertionGenerator.Insertion insertion) {
		int i = insertion.pickupIdx;
		int j = insertion.dropoffIdx;
		// i -> pickup
		DrtPathData toPickup = set.pathsToPickup[i]; // i -> pickup
		DrtPathData fromPickup = set.pathsFromPickup[i == j ? 0 : i + 1]; // pickup -> (dropoff | i+1)
		DrtPathData toDropoff = i == j ? null // pickup followed by dropoff
				: set.pathsToDropoff[j]; // j -> dropoff
		DrtPathData fromDropoff = j == stopCount ? null // dropoff inserted at the end
				: set.pathsFromDropoff[j + 1];
		return new InsertionWithPathData(i, j, toPickup, fromPickup, toDropoff, fromDropoff);
	}
}
