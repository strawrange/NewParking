package ParkingStrategy.ParkingInDepot.Depot;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.core.controler.AbstractModule;

import java.net.URL;

public class DepotManagerProvider implements Provider<DepotManager> {

    @Inject
    @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
    Network network;

    URL url;

    public DepotManagerProvider(URL url){
        this.url = url;
    }

    @Override
    public DepotManager get() {
        DepotManager depotManager = new DepotManager();
        new DepotReader(depotManager,network).parse(url);
        return depotManager;
    }

    public static AbstractModule createModule(URL url) {
        return new AbstractModule() {
            @Override
            public void install() {
                bind(DepotManager.class).toProvider(new DepotManagerProvider(url)).asEagerSingleton();
            }
        };
    }
}
