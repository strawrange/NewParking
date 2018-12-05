package EAV;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public abstract class Charger {

    private Id<Charger> id;
    private Link link;
    private int capacity;
    private ArrayList<VehicleImpl> chargingVehicles = new ArrayList<>();
    private Queue<VehicleImpl> waitingVehicles = new ConcurrentLinkedQueue<>();
    private PriorityBlockingQueue<Double> endTime = new PriorityBlockingQueue<>();


    public Charger(Id<Charger> id, Link link, int capacity){
        this.id = id;
        this.link = link;
        this.capacity = capacity;
    }
public enum ChargerMode{
        Level2,
    fast;
}



    public abstract double getChargingTime(VehicleImpl vehicle);

    public abstract double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery);


    public boolean isFull() {
        return waitingVehicles.size() > 0;
    }


    public boolean isQueue(Vehicle veh) {
        return waitingVehicles.contains(veh);
    }


    public void removeVehicle(VehicleImpl vehicle, double now) {
        if(!chargingVehicles.contains(vehicle) && !waitingVehicles.contains(vehicle)){
//            String dwell = new String();
//            String vehs = new String();
//            for (Id<org.matsim.vehicles.Vehicle> dvehid : chargingVehicles){
//                if (dvehid == null){
//                    dwell = dwell + ";null";
//                }
//                dwell = dwell + ";" + dvehid.toString();
//            }
//            for (Id<org.matsim.vehicles.Vehicle> vehid : vehicles){
//                if (vehid == null){
//                    vehs = vehs + ";null";
//                }
//                vehs = vehs + ";" + vehid.toString();
//            }
            //throw new RuntimeException("vid: " + vid.toString() + ", transitStop: " + transitStop.getId().toString() + ", dwellV: " + dwell + ", v: " + vehs);
            throw new RuntimeException("Vehicle is neither charging nor waiting! Cannot be removed!");
        }
        waitingVehicles.remove(vehicle);
        if (chargingVehicles.remove(vehicle) && isFull()){
            VehicleImpl veh = waitingVehicles.poll();
            chargingVehicles.add(veh);
        }else{
            endTime.poll();
        }

    }


    public void clear() {
        waitingVehicles.clear();
        chargingVehicles.clear();
    }


    public Link getLink() {
        return link;
    }


    public double getCapacity() {
        return capacity;
    }


    public TimeChargerPair calculateBestWaitTime(Id<Vehicle> vehicleId, double arrivalTime) {
        ArrayList<Double> check = new ArrayList(endTime);
        Collections.sort(check);
        TimeChargerPair best = new TimeChargerPair(Double.MAX_VALUE, this);
        if (chargingVehicles.size() < capacity){
            return new TimeChargerPair(0,this);
        }else{
            best.waitTime = Double.max(0, endTime.peek() - arrivalTime);
        }
        return best;
    }


    public abstract ChargerMode getChargerMode();


    public void addVehicle(VehicleImpl vehicle, double now) {
        if (chargingVehicles.size() > capacity){
            throw new RuntimeException("too many dwelling vehicles!");
        }
        if (chargingVehicles.contains(vehicle)){
            return;
        }
        if (chargingVehicles.size() == capacity){
            if (!waitingVehicles.contains(vehicle)) {
                waitingVehicles.add(vehicle);
                endTime.add(endTime.poll() + getChargingTime(vehicle));
            }
        }else{
            chargingVehicles.add(vehicle);
            endTime.add(now + getChargingTime(vehicle));
        }
    }


    public Id<Charger> getId() {
        return id;
    }
}


class TimeChargerPair{
    double waitTime;
    Charger charger;

    TimeChargerPair(double waitTime, Charger charger){
        this.waitTime = waitTime;
        this.charger = charger;
    }
}

//class VehiclesByStation{
//    double endTime = 0;
//    Id<Vehicle> vid;
//
//    public VehiclesByStation(double endTime, Id<Vehicle> vid){
//        this.endTime =endTime;
//        this.vid = vid;
//    }
//}
