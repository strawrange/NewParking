/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

import EAV.DischargingRate;
import Path.VrpPaths;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.ScheduleImpl;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import Vehicle.DynVehicleType;
import org.xml.sax.Attributes;

import javax.xml.stream.events.Attribute;
import java.util.List;
import java.util.Random;


/**
 * @author michalm
 */
public class VehicleImpl implements Vehicle {
	private final Id<Vehicle> id;
	private Link startLink;
	private final double capacity;

	// time window
	private final double serviceBeginTime;
	private double serviceEndTime;

	private Schedule schedule;

	private String mode;


	private DynVehicleType vehicleType;

	private double battery;

	boolean charging = false;

	boolean parking = false;

	public VehicleImpl(Id<Vehicle> id, Link startLink, double capacity, double serviceBeginTime,
					   double serviceEndTime, String mode, DynVehicleType vehicleType) {

		this.id = id;
		this.startLink = startLink;
		this.capacity = capacity;
		this.serviceBeginTime = serviceBeginTime;
		this.serviceEndTime = serviceEndTime;
		this.mode = mode;
		this.vehicleType = vehicleType;
		this.battery = vehicleType.getBatteryCapacity();
		schedule = new ScheduleImpl(this);
	}

	@Override
	public Id<Vehicle> getId() {
		return id;
	}

	@Override
	public Link getStartLink() {
		return startLink;
	}

	@Override
	public void setStartLink(Link link) {
		this.startLink = link;
	}

	@Override
	public double getCapacity() {
		return capacity;
	}

	@Override
	public double getServiceBeginTime() {
		return serviceBeginTime;
	}

	@Override
	public double getServiceEndTime() {
		return serviceEndTime;
	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	@Override
	public String toString() {
		return "Vehicle_" + id;
	}

	public void setServiceEndTime(double serviceEndTime) {
		this.serviceEndTime = serviceEndTime;
	}

	@Override
	public void resetSchedule() {
		schedule = new ScheduleImpl(this);
		Random random = new Random();
		double minBattery = DischargingRate.getMinBattery(vehicleType.getId());
		battery = (vehicleType.getBatteryCapacity() - minBattery) * Math.log10(random.nextInt(10) + 1) + minBattery;
		charging = false;
		parking = false;
	}


	public DynVehicleType getVehicleType() {
		return vehicleType;
	}

	public double getBattery() {
		return battery;
	}

	public void charge(double change){
		this.battery = this.battery + change;
	}

	public void discharge(double change) {
		this.battery = this.battery - change;
//		if (this.schedule.getCurrentTask() instanceof DrtDriveTask && DischargingRate.calculateDischargeByDistance(VrpPaths.calcDistance(((DrtDriveTask) this.schedule.getCurrentTask()).getPath())) + this.battery <= 7.0){
//			System.out.println();
//		}
		if (this.battery < 0){
			String s = "";
			for (int i = schedule.getCurrentTask().getTaskIdx(); i < schedule.getTasks().size();i++){
				Task task = schedule.getTasks().get(i);
					s +=  task.toString() + ";";
					if (task instanceof DrtStopTask){
						String drop = ((DrtStopTask) task).getDrtDropoffRequests().size() > 0 ? Double.toString(((DrtStopTask) task).getDrtDropoffRequests().get(0).getSubmissionTime()):"";
						String pick = ((DrtStopTask) task).getDrtPickupRequests().size() > 0 ? Double.toString(((DrtStopTask) task).getDrtPickupRequests().get(0).getSubmissionTime()):"";
						s += " submissionT " + drop + " " + pick + "\n";
					}
			}
			throw new RuntimeException(this.id + " is out of Power!!!" + s);
		}
	}

	public void changeStatus(boolean status){
		charging = status;
	}

	public boolean getStatus(){
		return charging;
	}

	public boolean isParking(){
		return parking;
	}

	public void changeParking(boolean parking){
		this.parking = parking;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
}
