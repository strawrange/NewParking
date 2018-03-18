package ParkingStrategy.NoParkingStrategy;

import ParkingStrategy.ParkingStrategy;
import org.matsim.contrib.dvrp.data.Vehicle;

public class NoParkingStrategy implements ParkingStrategy {
    @Override
    public ParkingLocation parking(Vehicle vehicle, double time) {
        return null;
    }

    @Override
    public void departing(Vehicle vehicle, double time) {
        
    }

}
