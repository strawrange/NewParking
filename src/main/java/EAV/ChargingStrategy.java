package EAV;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.api.core.v01.network.Link;

public interface ChargingStrategy {
    public enum Strategies{
        EarlyReserved;
    }
    ChargerPathPair charging(Vehicle vehicle, double time);

    void leaving(Vehicle vehicle, double time);

    ChargingStrategy.Strategies getCurrentStrategy(Id<Vehicle> vehicleId);

}
