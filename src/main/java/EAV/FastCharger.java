package EAV;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public class FastCharger extends Charger {
    public static final double CHARGING_RATE_PER_SECOND =  0.8/60.0;

    public FastCharger(Id<Charger> id, Link link, int capacity, double startTime, double endTime, boolean isBlocking) {
        super(id, link, capacity,startTime,endTime, isBlocking);
    }

    @Override
    public double getChargingRate() {
        return CHARGING_RATE_PER_SECOND;
    }

    @Override
    public double getChargingTime(VehicleImpl vehicle) {
        return (((Vehicle.DynVehicleType)vehicle.getVehicleType()).getBatteryCapacity() - vehicle.getBattery()) / CHARGING_RATE_PER_SECOND;
    }

    @Override
    public double getEstimatedChargeTime(VehicleImpl vehicle, double estimatedBattery) {
        return ((((Vehicle.DynVehicleType)vehicle.getVehicleType()).getBatteryCapacity()) - estimatedBattery) / CHARGING_RATE_PER_SECOND;
    }


    @Override
    public ChargerMode getChargerMode() {
        return ChargerMode.fast;
    }
}