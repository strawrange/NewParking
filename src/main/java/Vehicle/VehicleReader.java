/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package Vehicle;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.vehicles.VehicleType;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Stack;

/**
 * @author michalm
 */
public class VehicleReader extends MatsimXmlParser {
	private static final String VEHICLE = "vehicle";

	private static final double DEFAULT_CAPACITY = 1;
	private static final double DEFAULT_T_0 = 0;
	private static final double DEFAULT_T_1 = 24 * 60 * 60;
	private static final double DEFAULT_BOARDING = 1.0;
	private static final double DEFAULT_ALIGHTING = 1.0;

	private FleetImpl fleet;
	private Map<Id<Link>, ? extends Link> links;
	private VehicleType vehicleType;

	public VehicleReader(Network network, FleetImpl fleet, VehicleType vehicleType) {
		this.fleet = fleet;
		links = network.getLinks();
		this.vehicleType = vehicleType;
	}

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (VEHICLE.equals(name)) {
			fleet.addVehicle(createVehicle(atts));
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}

	private Vehicle createVehicle(Attributes atts) {
		Id<Vehicle> id = Id.create(atts.getValue("id"), Vehicle.class);
		Link startLink = links.get(Id.createLinkId(atts.getValue("start_link")));
		double capacity = ReaderUtils.getDouble(atts, "capacity", DEFAULT_CAPACITY);
		double t0 = ReaderUtils.getDouble(atts, "t_0", DEFAULT_T_0);
		double t1 = ReaderUtils.getDouble(atts, "t_1", DEFAULT_T_1);
		vehicleType.setAccessTime(ReaderUtils.getDouble(atts, "boarding_time_per_person_s",DEFAULT_BOARDING));
		vehicleType.setEgressTime(ReaderUtils.getDouble(atts,"alighting_time_per_person_s", DEFAULT_ALIGHTING));
		if (vehicleType instanceof DynVehicleType) {
			if (capacity == 4.0){
				vehicleType.setLength(3.0);
			}
			if (capacity == 10.0){
				vehicleType.setLength(5.0);
			}
			if (capacity == 20.0){
				vehicleType.setLength(8.0);
			}
		}
		return createVehicle(id, startLink, capacity, t0, t1, atts);
	}

	protected Vehicle createVehicle(Id<Vehicle> id, Link startLink, double capacity, double t0, double t1,
			Attributes atts) {
		return new VehicleImpl(id, startLink, capacity, t0, t1);
	}
}
