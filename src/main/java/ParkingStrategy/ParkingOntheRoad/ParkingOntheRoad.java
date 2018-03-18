package ParkingStrategy.ParkingOntheRoad;

import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.DrtZonalSystem;
import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.ZonalDemandAggregator;
import ParkingStrategy.ParkingStrategy;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimInitializedListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.network.NetworkChangeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ParkingOntheRoad implements ParkingStrategy, IterationStartsListener{
    private Map<Id<Link>, Integer> linkRecord = new HashMap<>(); // Interger counts the number of vehicles parks on the link
    @Inject
    private QSim qsim;
    private final Map<Id<Link>, Integer> supply = new HashMap<>();
    private final double vehicleLength = 8.0; //TODO: Later Move to the DRTConfig or Vehicle File


    @Inject
    public ParkingOntheRoad(QSim qSim, @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network) {
        this.qsim = qSim;
        for (Link link : network.getLinks().values()){
            supply.put(link.getId(), (int) Math.floor(link.getLength() / vehicleLength));
        }
    }

    @Override
    public ParkingStrategy.ParkingLocation parking(Vehicle vehicle, double time) {
        Link currentLink = ((DrtStayTask)vehicle.getSchedule().getCurrentTask()).getLink();
        if (supply.get(currentLink.getId()) > 0){
            if (!linkRecord.containsKey(currentLink.getId())){
                linkRecord.put(currentLink.getId(),0);
                modifyLanes(currentLink, time, -1.D);
            }
            if (supply.get(currentLink.getId()) > linkRecord.get(currentLink.getId())) {
                int num = linkRecord.get(currentLink.getId()) + 1;
                linkRecord.put(currentLink.getId(), num);
                return null;
            }
        }
        return new ParkingLocation(vehicle.getId(),nextLink(currentLink));
    }

    private Link nextLink(Link currentLink){
        Random random = new Random();
        Map<Id<Link>, ? extends Link> nextLinks = currentLink.getToNode().getOutLinks();
        ArrayList<Id<Link>> linksKey = new ArrayList<>(nextLinks.keySet());
        return nextLinks.get(linksKey.get(random.nextInt(nextLinks.size())));
    }

    @Override
    public void departing(Vehicle vehicle, double time) {
        int previousIdx = vehicle.getSchedule().getCurrentTask().getTaskIdx() - 1;
        Link link = ((DrtStayTask)vehicle.getSchedule().getTasks().get(previousIdx)).getLink();
        if (!linkRecord.containsKey(link.getId())){
            throw new RuntimeException("The departing vehicle has not registered in link records");
        }
        int num = linkRecord.get(link.getId()) - 1;
        linkRecord.put(link.getId(),num);
        if (num == 0){
            modifyLanes(link, time, 0.D);
        }
    }

    public void modifyLanes(Link currentLink, double time, double change){
        double numOfLanes = currentLink.getNumberOfLanes();
        NetworkChangeEvent event = new NetworkChangeEvent(time);
        event.addLink(currentLink);
        NetworkChangeEvent.ChangeValue capacityChange = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, (numOfLanes + change)/numOfLanes * currentLink.getCapacity());
        NetworkChangeEvent.ChangeValue lanesChange = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, numOfLanes + change);
        event.setLanesChange(lanesChange);
        event.setFlowCapacityChange(capacityChange);
        qsim.addNetworkChangeEvent(event);
    }


    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        linkRecord.clear();
    }

}

