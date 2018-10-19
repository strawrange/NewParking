package EAV;

import ParkingStrategy.ParkingInDepot.Depot.Depot;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.core.controler.listener.IterationStartsListener;

import java.util.Map;

public interface ChargerManager extends IterationStartsListener {
    void addCharger(Charger charger);

    Map<Id<Link>, Charger> getChargers();
    Map<Id<Link>, Charger> getChargers(Charger.ChargerMode chargerMode);


    Charger getChargersByLinkId(Id<Link> linkId);
}
