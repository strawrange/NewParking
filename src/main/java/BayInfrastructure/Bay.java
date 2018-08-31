package BayInfrastructure;

import Run.DrtConfigGroup;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Bay {
    private final TransitStopFacility transitStop;
    private final Id<Link> linkId;
    private final double capacity;


    private Queue<Id<Vehicle>> vehicles = new ConcurrentLinkedQueue<>();
    private Queue<Id<Vehicle>> dwellingVehicles = new ConcurrentLinkedQueue<>();
    private double dwellLength = 0;

    public Bay(TransitStopFacility transitStop, double linkLength){
        this.transitStop = transitStop;
        this.linkId = transitStop.getLinkId();
        if (transitStop.getAttributes().getAttribute("capacity") == null){
            this.capacity = Double.POSITIVE_INFINITY;
        }else{
            this.capacity = Double.max((double) transitStop.getAttributes().getAttribute("capacity"), linkLength);
        }
    }

    public Bay(TransitStopFacility transitStop, double linkLength, DrtConfigGroup.Door2DoorStop door2DoorStop){
        this.transitStop =transitStop;
        this.linkId = transitStop.getLinkId();
        switch (door2DoorStop){
            case infinity:
                this.capacity = Double.POSITIVE_INFINITY;
                break;
            case linkLength:
                this.capacity = linkLength;
                break;
            default:
                throw new RuntimeException("No such door-to-door stop strategy!");
        }
    }

    public TransitStopFacility getTransitStop() {
        return transitStop;
    }

    public Id<Link> getLinkId() {
        return linkId;
    }

    public double getCapacity() {
        return capacity;
    }


    public void addVehicle(Id<Vehicle> vid){
        if (dwellLength > capacity){
            throw new RuntimeException("too many dwelling vehicles!");
        }
        if (dwellingVehicles.contains(vid)){
            return;
        }
        double vehicleLength = VehicleLength.lengthByVehicle.get(vid).getVehicle().getType().getLength();
        if (dwellLength +  vehicleLength >= capacity){
            if (!vehicles.contains(vid)) {
                vehicles.add(vid);
            }
        }else{
            dwellLength = dwellLength + vehicleLength;
            dwellingVehicles.add(vid);
        }
    }



    public void removeVehicle(Id<Vehicle> vid){
        if(!dwellingVehicles.contains(vid) && !vehicles.contains(vid)){
            String dwell = new String();
            String vehs = new String();
            for (Id<Vehicle> dvehid : dwellingVehicles){
                dwell = dwell + ";" + dvehid.toString();
            }
            for (Id<Vehicle> vehid : vehicles){
                vehs = vehs + ";" + vehid.toString();
            }
            throw new RuntimeException("vid: " + vid.toString() + ", transitStop: " + transitStop.getId().toString() + ", dwellV: " + dwell + ", v: " + vehs);
        }
        vehicles.remove(vid);
        if (dwellingVehicles.remove(vid)) {
            double vehicleLength = VehicleLength.lengthByVehicle.get(vid).getVehicle().getType().getLength();
            dwellLength = dwellLength - vehicleLength;
            if (isFull()) {
                Id<Vehicle> vehicleId = vehicles.peek();
                double vehLength = VehicleLength.lengthByVehicle.get(vehicleId).getVehicle().getType().getLength();
                if (dwellLength + vehLength <= capacity) {
                    dwellLength = dwellLength + vehLength;
                    dwellingVehicles.add(vehicleId);
                    vehicles.remove(vehicleId);
                }
            }
        }
    }


    public Queue<Id<Vehicle>> getVehicles() {
        return vehicles;
    }


    public boolean isFull() {
        return vehicles.size() > 0;
    }

}
