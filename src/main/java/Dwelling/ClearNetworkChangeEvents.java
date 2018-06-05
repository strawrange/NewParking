package Dwelling;

import ParkingStrategy.ParkingOntheRoad.ParkingOntheRoad;
import ParkingStrategy.ParkingStrategy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.network.NetworkUtils;

import java.util.ArrayList;

public class ClearNetworkChangeEvents implements IterationStartsListener {
    @Inject
    @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
    Network network;

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        NetworkUtils.setNetworkChangeEvents(network, new ArrayList<>());
    }
}
