package EAV;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;

public interface Charger {

    double getChargingTime(VehicleImpl vehicle);

    double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery);

    boolean isFull();


    boolean isQueue(Vehicle veh);

    void removeVehicle(VehicleImpl vehicle, double timeOfDay);

    void clear();

    public static enum ChargerMode{
        LEVEL2,
        FAST;
    }
    Link getLink();

    double getCapacity();

    TimeChargerPair calculateBestWaitTime(Id<Vehicle> vehicleId, double arrivalTime);


    ChargerMode getChargerMode();


    void addVehicle(VehicleImpl vehicle, double now);

    Id<Charger> getId();
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
