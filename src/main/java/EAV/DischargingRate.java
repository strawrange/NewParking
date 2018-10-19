package EAV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Schedule.VehicleImpl;
import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.vehicles.Vehicle;
import sun.plugin2.message.EventMessage;

public class DischargingRate implements LinkEnterEventHandler, LinkLeaveEventHandler, IterationEndsListener{
    Map<Id<Vehicle>, Event> events = new HashMap<>();
    ArrayList<Id<Vehicle>> departure = new ArrayList<>();
    @Inject
    Network network;
    @Inject
    Fleet fleet;
    private static final double DISCHARGE_RATE_PER_M = 0.0005;

    @Inject
    public DischargingRate(EventsManager eventsManager){
        eventsManager.addHandler(this);
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (event.getVehicleId().toString().startsWith("drt")) {
            if (!events.containsKey(event.getVehicleId())) {
                events.put(event.getVehicleId(), event);
            } else {
                throw new RuntimeException("Cannot enter another link before leave!");
            }
        }
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (event.getVehicleId().toString().startsWith("drt")) {
            if (events.containsKey(event.getVehicleId()) || !departure.contains(event.getVehicleId())) {
                discharge(event.getVehicleId(), event.getLinkId());
                events.remove(event.getVehicleId());
                departure.add(event.getVehicleId());
            } else {
                throw new RuntimeException("Cannot leave without enter!");
            }
        }
    }

    private void discharge(Id<Vehicle> vehicleId, Id<Link> linkId) {
       VehicleImpl veh = (VehicleImpl) fleet.getVehicles().get(vehicleId);
       Link link = network.getLinks().get(linkId);
       veh.discharge(link.getLength() * DISCHARGE_RATE_PER_M);
    }

    public static double calculateDischargeByDistance(double distance){
        return distance * DISCHARGE_RATE_PER_M;
    }

    public void clear(){
        events.clear();
        departure.clear();
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        clear();
    }
}
