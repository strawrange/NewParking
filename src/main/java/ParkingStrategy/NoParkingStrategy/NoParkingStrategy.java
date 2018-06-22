package ParkingStrategy.NoParkingStrategy;

import ParkingStrategy.ParkingStrategy;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

public class NoParkingStrategy implements ParkingStrategy {
    @Override
    public ParkingLocation parking(Vehicle vehicle, double time) {
        return null;
    }

    @Override
    public void departing(Vehicle vehicle, double time) {
        
    }

    @Override
    public ParkingStrategy.Strategies getCurrentStrategy(Id<Vehicle> vehicleId) {
        return Strategies.NoParkingStrategy;
    }

}
