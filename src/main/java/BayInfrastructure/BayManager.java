package BayInfrastructure;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;

import java.util.HashMap;
import java.util.Map;

public class BayManager implements VehicleDepartsAtFacilityEventHandler{
    Map<Id<TransitStopFacility>, Bay> bays = new HashMap<>();
    Map<Id<Link>, Id<TransitStopFacility>> baysByStops = new HashMap<>();
    Network network;


    @Inject
    public BayManager(Scenario scenario, EventsManager eventsManager){
        for (TransitStopFacility stop: scenario.getTransitSchedule().getFacilities().values()){
            Bay bay = new Bay(stop);
            bays.put(stop.getId(), bay);
            baysByStops.put(stop.getLinkId(),stop.getId());
        }
        this.network = scenario.getNetwork();
        eventsManager.addHandler(this);
    }

    public Bay getBayByLinkId(Id<Link> linkId){
        if (!baysByStops.containsKey(linkId)){
            TransitStopFacility transitStopFacility = (new TransitScheduleFactoryImpl()).createTransitStopFacility(Id.create(linkId.toString() + "_DRT", TransitStopFacility.class),
                    network.getLinks().get(linkId).getCoord(),false);
            bays.put(transitStopFacility.getId(), new Bay(transitStopFacility));
            baysByStops.put(linkId,transitStopFacility.getId());
        }
        return bays.get(baysByStops.get(linkId));
    }

    public Bay getBayByFacilityId(Id<TransitStopFacility> stopFacilityId){
        return bays.get(stopFacilityId);
    }

    public Id<TransitStopFacility> getStopIdByLinkId(Id<Link> linkId){
        return baysByStops.get(linkId);
    }

//    @Override
//    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
//        Bay bay = bays.get(event.getFacilityId());
//        bay.addVehicle(event.getVehicleId());
//    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        Bay bay = bays.get(event.getFacilityId());
        bay.removeVehicle(event.getVehicleId());
    }


}
