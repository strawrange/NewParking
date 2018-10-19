package EAV;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import Vehicle.DynVehicleType;

public class Level2Charger implements Charger{
    private Id<Charger> id;
    private Link link;
    private int capacity;
    private ArrayList<VehicleImpl> chargingVehicles = new ArrayList<>();
    private Queue<VehicleImpl> waitingVehicles = new ConcurrentLinkedQueue<>();
    public static final double CHARGING_RATE_PER_SECOND =  8.0/75.0/60.0;
    private PriorityBlockingQueue<Double> endTime = new PriorityBlockingQueue<>();


    public Level2Charger(Id<Charger> id, Link link, int capacity){
        this.id = id;
        this.link = link;
        this.capacity = capacity;
    }



    @Override
    public double getChargingTime(VehicleImpl vehicle) {
        return (((DynVehicleType)vehicle.getVehicleType()).getBatteryCapacity() - vehicle.getBattery()) / CHARGING_RATE_PER_SECOND;
    }

    @Override
    public double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery) {
        return ((((DynVehicleType)vehicle.getVehicleType()).getBatteryCapacity()) - estimatedBattery) / CHARGING_RATE_PER_SECOND;
    }

    @Override
    public boolean isFull() {
        return waitingVehicles.size() > 0;
    }

    @Override
    public boolean isQueue(Vehicle veh) {
        return waitingVehicles.contains(veh);
    }

    @Override
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

    @Override
    public void clear() {
        waitingVehicles.clear();
        chargingVehicles.clear();
    }

    @Override
    public Link getLink() {
        return link;
    }

    @Override
    public double getCapacity() {
        return capacity;
    }

    @Override
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


    @Override
    public ChargerMode getChargerMode() {
        return ChargerMode.LEVEL2;
    }


    @Override
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


    @Override
    public Id<Charger> getId() {
        return id;
    }
}
