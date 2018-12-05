package EAV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Run.AtodConfigGroup;
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
    private static final double DISCHARGE_RATE_DRT_4_PER_M = 32.0 / 180000.0;
    private static final double DISCHARGE_RATE_DRT_10_PER_M = 60.0 / 150000.0;
    private static final double DISCHARGE_RATE_DRT_20_PER_M = 90.0 / 130000.0;
    private static double MIN_BATTERY_DRT_4;
    private static double MIN_BATTERY_DRT_10;
    private static double MIN_BATTERY_DRT_20;
    private static double MIN_ACCEPTED_DRT_4;
    private static double MIN_ACCEPTED_DRT_10;
    private static double MIN_ACCEPTED_DRT_20;

    @Inject
    public DischargingRate(EventsManager eventsManager, AtodConfigGroup atodConfigGroup){
        eventsManager.addHandler(this);
        double minKm = atodConfigGroup.getMinBattery();
        double minAccept = atodConfigGroup.getMinRequestAccept();
        MIN_BATTERY_DRT_4 = minKm * DISCHARGE_RATE_DRT_4_PER_M;
        MIN_BATTERY_DRT_10 = minKm * DISCHARGE_RATE_DRT_10_PER_M;
        MIN_BATTERY_DRT_20 = minKm * DISCHARGE_RATE_DRT_20_PER_M;
        MIN_ACCEPTED_DRT_4 = minAccept * DISCHARGE_RATE_DRT_4_PER_M;
        MIN_ACCEPTED_DRT_10 = minAccept * DISCHARGE_RATE_DRT_10_PER_M;
        MIN_ACCEPTED_DRT_20 = minAccept * DISCHARGE_RATE_DRT_20_PER_M;
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
       veh.discharge(link.getLength() * calculateDischarge(veh));
    }

    public static double calculateDischargeByDistance(double distance, VehicleImpl vehicle){
        return distance * calculateDischarge(vehicle);
    }

    private static double calculateDischarge(VehicleImpl veh){
        switch ((int)veh.getCapacity()){
            case 1:
                return DISCHARGE_RATE_DRT_4_PER_M;
            case 4:
                return DISCHARGE_RATE_DRT_4_PER_M;
            case 10:
                return DISCHARGE_RATE_DRT_10_PER_M;
            case 20:
                return DISCHARGE_RATE_DRT_20_PER_M;
            default:
                throw  new RuntimeException("Wrong capacity!");
        }

    }

    public void clear(){
        events.clear();
        departure.clear();
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        clear();
    }

    public static double getMinBattery(double capacity) {
        switch ((int)capacity){
            case 1:
                return MIN_BATTERY_DRT_4;
            case 4:
                return MIN_BATTERY_DRT_4;
            case 10:
                return MIN_BATTERY_DRT_10;
            case 20:
                return MIN_BATTERY_DRT_20;
            default:
                throw  new RuntimeException("Wrong capacity!");
        }
    }

    public static double getMinAccepted(double capacity) {
        switch ((int)capacity){
            case 1:
                return MIN_ACCEPTED_DRT_4;
            case 4:
                return MIN_ACCEPTED_DRT_4;
            case 10:
                return MIN_ACCEPTED_DRT_10;
            case 20:
                return MIN_ACCEPTED_DRT_20;
            default:
                throw  new RuntimeException("Wrong capacity!");
        }
    }
}
