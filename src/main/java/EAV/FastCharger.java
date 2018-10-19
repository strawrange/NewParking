package EAV;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

import java.util.ArrayList;

public class FastCharger implements Charger{
    private Id<Charger> id;
    private Link link;
    private double capacity;
    private ArrayList<Id<Vehicle>> vehicles = new ArrayList<>();


    public FastCharger(Id<Charger> depotId, Link link, double capacity){
        this.id = depotId;
        this.link = link;
        this.capacity = capacity;
    }


    @Override
    public double getChargingTime(VehicleImpl vehicle) {
        return 0;
    }

    @Override
    public double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery) {
        return 0;
    }


    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public boolean isQueue(Vehicle veh) {
        return false;
    }

    @Override
    public void removeVehicle(VehicleImpl vehicle, double timeOfDay) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Link getLink() {
        return null;
    }

    @Override
    public double getCapacity() {
        return 0;
    }

    @Override
    public TimeChargerPair calculateBestWaitTime(Id<Vehicle> vehicleId, double arrivalTime) {
        return null;
    }

    @Override
    public ChargerMode getChargerMode() {
        return null;
    }

    @Override
    public void addVehicle(VehicleImpl vehicle, double now) {

    }

    @Override
    public Id<Charger> getId() {
        return null;
    }
}
