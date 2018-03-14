package ParkingStrategy.ParkingOntheRoad;

import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.DrtZonalSystem;
import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.ZonalDemandAggregator;
import ParkingStrategy.ParkingStrategy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;

public class ParkingOntheRoad implements ParkingStrategy{
    private Network network;

    @Inject
    public ParkingOntheRoad(@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network) {
        this.network = network;
    }
    @Override
    public ParkingStrategy.ParkingLocation Parking(Vehicle vehicle) {
        Link link = vehicle.getSchedule().getCurrentTask();
        return null;
    }
}

