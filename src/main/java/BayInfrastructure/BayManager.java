package BayInfrastructure;

import Run.DrtConfigGroup;
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
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BayManager implements VehicleDepartsAtFacilityEventHandler, IterationStartsListener {
    Map<Id<TransitStopFacility>, Bay> bays = new HashMap<>();
    Map<Id<Link>, Id<TransitStopFacility>> baysByStops = new HashMap<>();
    Collection<TransitStopFacility> transitStopFacilities;
    Network network;
    DrtConfigGroup drtConfigGroup;


    @Inject
    public BayManager(Scenario scenario, EventsManager eventsManager){
        transitStopFacilities = scenario.getTransitSchedule().getFacilities().values();
        this.network = scenario.getNetwork();
        this.drtConfigGroup = DrtConfigGroup.get(scenario.getConfig());
        initate();
        eventsManager.addHandler(this);
    }

    private void initate(){
        for (TransitStopFacility stop: transitStopFacilities){
            Bay bay = new Bay(stop, network.getLinks().get(stop.getLinkId()).getLength());
            bays.put(stop.getId(), bay);
            baysByStops.put(stop.getLinkId(),stop.getId());
        }
    }

    public Bay getBayByLinkId(Id<Link> linkId){
        if (!baysByStops.containsKey(linkId)){
            TransitStopFacility transitStopFacility = (new TransitScheduleFactoryImpl()).createTransitStopFacility(Id.create(linkId.toString() + "_DRT", TransitStopFacility.class),
                    network.getLinks().get(linkId).getCoord(),false);
            transitStopFacility.setLinkId(linkId);
            bays.put(transitStopFacility.getId(), new Bay(transitStopFacility, network.getLinks().get(linkId).getLength(),drtConfigGroup.getDoor2DoorStop()));
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


    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        bays.clear();
        baysByStops.clear();
        initate();
    }
}
