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


import Schedule.AtodRequest;
import Schedule.VehicleData;
import Path.OneToManyPathSearch.DrtPathData;

/**
 * @author michalm
 */
public interface PathDataProvider {
	class PathDataSet {
		// path[0] is a special entry; path[i] corresponds to stop i-1, for 1 <= i <= stopCount
		public final DrtPathData[] pathsToPickup;//path[0] start->pickup
		public final DrtPathData[] pathsFromPickup;//path[0] pickup->dropoff
		public final DrtPathData[] pathsToDropoff;//path[0] null
		public final DrtPathData[] pathsFromDropoff;//path[0] null

		public PathDataSet(DrtPathData[] pathsToPickup, DrtPathData[] pathsFromPickup, DrtPathData[] pathsToDropoff,
						   DrtPathData[] pathsFromDropoff) {
			this.pathsToPickup = pathsToPickup;
			this.pathsFromPickup = pathsFromPickup;
			this.pathsToDropoff = pathsToDropoff;
			this.pathsFromDropoff = pathsFromDropoff;
		}
	}

	PathDataSet getPathDataSet(AtodRequest drtRequest, VehicleData.Entry vEntry);
}
