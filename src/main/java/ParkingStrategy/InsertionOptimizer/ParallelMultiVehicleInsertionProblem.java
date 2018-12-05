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


import Schedule.VehicleData;

import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.core.mobsim.framework.MobsimTimer;
import Schedule.AtodRequest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

/**
 * @author michalm
 */
public class ParallelMultiVehicleInsertionProblem implements MultiVehicleInsertionProblem {
	private final PrecalculatablePathDataProvider pathDataProvider;
	private final InsertionCostCalculator insertionCostCalculator;
	private final ForkJoinPool forkJoinPool;

	public ParallelMultiVehicleInsertionProblem(PrecalculatablePathDataProvider pathDataProvider, DrtConfigGroup drtCfg,
                                                MobsimTimer timer) {
		this.pathDataProvider = pathDataProvider;
		insertionCostCalculator = new InsertionCostCalculator(timer);
		forkJoinPool = new ForkJoinPool(drtCfg.getNumberOfThreads());
	}

	@Override
	public Optional<SingleVehicleInsertionProblem.BestInsertion> findBestInsertion(AtodRequest drtRequest, Collection<VehicleData.Entry> vEntries) {
		pathDataProvider.precalculatePathData(drtRequest, vEntries);
		return forkJoinPool.submit(() -> vEntries.parallelStream()//
				.map(v -> new SingleVehicleInsertionProblem(pathDataProvider, insertionCostCalculator)
						.findBestInsertion(drtRequest, v))//
				.filter(Optional::isPresent)//
				.map(Optional::get)//
				.min(Comparator.comparing(i -> i.cost)))//
				.join();
	}

	public void shutdown() {
		forkJoinPool.shutdown();
	}
}
