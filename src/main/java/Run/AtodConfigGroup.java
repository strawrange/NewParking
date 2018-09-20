/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package Run;

import ParkingStrategy.ParkingStrategy;
import org.matsim.contrib.drt.optimizer.insertion.ParallelPathDataProvider;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.net.URL;
import java.util.Map;

public class AtodConfigGroup extends ReflectiveConfigGroup {
	
	public static final String GROUP_NAME = "atod";

	public AtodConfigGroup() {
		super(GROUP_NAME);
	}


	@SuppressWarnings("deprecation")
	public static AtodConfigGroup get(Config config) {
		return (AtodConfigGroup)config.getModule(GROUP_NAME);
	}

	public static final String PARKING_STRATEGY = "parkingStrategy";
	static final String PARKING = "Paring strategies, AlwaysRoaming, ParkingOntheRoad, ParkingInDepot, MixedParking";

	public static final String DEPOT_FILE = "depotFile";
	static final String DEPOT_FILE_EXP = "An XML file specifying the location of depots. The file format according to depot.dtd";


	public static final String DOOR_2_DOOR_STOP = "door2DoorStop";
	static final String DOOR_2_DOOR_STOP_EXP = "The bay length of the door-to-door AV, infinity means no bay length restriction, linkLength means length equals to the link length";

	public static final String MIN_BAY_SIZE = "minBaySize";
	static final String MIN_BAY_SIZE_EXP = "The minimum bay size for transit stop, 0.0 by default.";


	@NotNull
	private ParkingStrategy.Strategies parkingStrategy = ParkingStrategy.Strategies.NoParkingStrategy;

	@NotNull
	private String depotFile = null;


	@NotNull
	private Door2DoorStop door2DoorStop= Door2DoorStop.infinity;

	private double minBaySize = 0.0;

	public enum Door2DoorStop{
		infinity, linkLength
	}


	@Override
	public Map<String, String> getComments() {
		Map<String, String> map = super.getComments();
		map.put(PARKING_STRATEGY, PARKING);
		map.put(DEPOT_FILE, DEPOT_FILE_EXP);
		map.put(DOOR_2_DOOR_STOP, DOOR_2_DOOR_STOP_EXP);
		map.put(MIN_BAY_SIZE, MIN_BAY_SIZE_EXP);
		return map;
	}

		/**
	 *
	 * @return -- {@value #DEPOT_FILE_EXP}
	 */
	@StringGetter(DEPOT_FILE)
	public String getDepotFile() {
		return depotFile;
	}
	/**
	 * 
	 * @param depotFile -- {@value #DEPOT_FILE_EXP}
	 */
	@StringSetter(DEPOT_FILE)
	public void setDepotFile(String depotFile) {
		this.depotFile = depotFile;
	}
	/**
	 * 
	 * @return -- {@value #DEPOT_FILE_EXP}
	 */
	public URL getDepotFileUrl(URL context) {
		return ConfigGroup.getInputFileURL(context, this.depotFile);
	}


	/**
	 * 
	 * @return -- {@value #PARKING}
	 */
	@StringGetter(PARKING_STRATEGY)
	public ParkingStrategy.Strategies getParkingStrategy() {
		return parkingStrategy;
	}

	/**
	 * 
	 * @param parkingStrategy -- {@value #PARKING}
	 */
	@StringSetter(PARKING_STRATEGY)
	public void setParkingStrategy(String parkingStrategy) {

		this.parkingStrategy = ParkingStrategy.Strategies.valueOf(parkingStrategy);
	}


	@StringGetter(DOOR_2_DOOR_STOP)
	public Door2DoorStop getDoor2DoorStop() {
		return door2DoorStop;
	}

	@StringSetter(DOOR_2_DOOR_STOP)
	public void setDoor2DoorStop(String door2doorStop) {
		this.door2DoorStop = Door2DoorStop.valueOf(door2doorStop);
	}

	@StringGetter(MIN_BAY_SIZE)
	public double getMinBaySize(){
		return minBaySize;
	}
	@StringSetter(MIN_BAY_SIZE)
	public void setMinBaySize(double minBaySize){
		this.minBaySize = minBaySize;
	}
}
