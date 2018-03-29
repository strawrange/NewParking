package BayInfrastructure;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.HashMap;
import java.util.Map;

public class BayManager {
    Map<Id<TransitStopFacility>, Bay> bays = new HashMap<>();


    @Inject
    public BayManager(Scenario scenario){
        for (TransitStopFacility stop: scenario.getTransitSchedule().getFacilities().values()){
            Bay bay = new Bay(stop);
            bays.put(stop.getId(), bay);
        }
    }
}
