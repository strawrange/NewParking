package ParkingStrategy.NoParkingStrategy;

import ParkingStrategy.ParkingStrategy;
import org.matsim.contrib.dvrp.data.Vehicle;

public class NoParkingStrategy implements ParkingStrategy {
    @Override
    public Relocation calcRelocation(Vehicle vehicle) {
        return null;
    }

}
