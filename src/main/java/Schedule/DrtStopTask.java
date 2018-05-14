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

package Schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

import java.util.*;

/**
 * @author michalm
 */
public class DrtStopTask extends StayTaskImpl implements DrtTask {
	private final List<DrtRequest> dropoffRequests = new ArrayList<>();
	private final List<DrtRequest> pickupRequests = new ArrayList<>();
	private final List<DrtRequest> drop = new ArrayList<>();
	private final List<DrtRequest> pick = new ArrayList<>();

	public DrtStopTask(double beginTime, double endTime, Link link) {
		super(beginTime, endTime, link);
	}

	@Override
	public DrtTaskType getDrtTaskType() {
		return DrtTaskType.STOP;
	}

	public List<DrtRequest> getDropoffRequests() {
		return dropoffRequests;
	}

	public List<DrtRequest> getPickupRequests() {
		return pickupRequests;
	}

	public void addDropoffRequest(DrtRequest request) {
		dropoffRequests.add(request);
		drop.add(request);
	}

	public void addPickupRequest(DrtRequest request) {
		pickupRequests.add(request);
		pick.add(request);
	}

	@Override
	protected String commonToString() {
		return "[" + getDrtTaskType().name() + "]" + super.commonToString();
	}
}
