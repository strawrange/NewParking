package ParkingStrategy.NoParkingStrategy;

import ParkingStrategy.ParkingStrategy;
import org.matsim.contrib.dvrp.data.Vehicle;

public class NoParkingStrategy implements ParkingStrategy {
    @Override
    public ParkingLocation Parking(Vehicle vehicle) {
        return null;
    }

    @Override
    public void Departing(Vehicle vehicle) {
        
    }

}
