package EAV;

import ParkingStrategy.ParkingInDepot.Depot.Depot;
import ParkingStrategy.ParkingInDepot.Depot.DepotReader;
import Run.AtodConfigGroup;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import sun.awt.image.ImageWatched;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleChargerManager implements ChargerManager {

    Map<Id<Charger>, Charger> chargers = new HashMap<>();


    @Inject
    public SimpleChargerManager(Config config, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network){
        AtodConfigGroup atodCfg = AtodConfigGroup.get(config);
        new ChargerReader(this,network,config.qsim()).parse(atodCfg.getChargeFileURL(config.getContext()));
    }

    @Override
    public void addCharger(Charger charger) {
        chargers.put(charger.getId(), charger);
    }

    @Override
    public Map<Id<Charger>, Charger> getChargers() {
        return this.chargers;
    }

    @Override
    public Map<Id<Charger>, Charger> getChargers(Charger.ChargerMode chargerMode) {
        return chargers.entrySet().stream().filter(charger -> charger.getValue().getChargerMode() == chargerMode).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }



    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        for (Charger charger: chargers.values()){
            charger.clear();
        }
    }
}
