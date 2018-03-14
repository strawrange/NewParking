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
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.mobsim.framework.MobsimTimer;

import java.util.HashMap;
import java.util.Map;

public class ParkingOntheRoad implements ParkingStrategy, IterationStartsListener{
    private Map<Id<Link>, Integer> linkRecord = new HashMap<>(); // Interger counts the number of vehicles parks on the link

    @Override
    public ParkingStrategy.ParkingLocation Parking(Vehicle vehicle) {
        Link currentLink = ((DrtStayTask)vehicle.getSchedule().getCurrentTask()).getLink();
        if (!linkRecord.containsKey(currentLink.getId())){
            linkRecord.put(currentLink.getId(),0);
            blockOneLane(currentLink);
        }
        int num = linkRecord.get(currentLink.getId()) + 1;
        linkRecord.put(currentLink.getId(),num);
        return null;
    }

    @Override
    public void Departing(Vehicle vehicle) {
        int previousIdx = vehicle.getSchedule().getCurrentTask().getTaskIdx() - 1;
        Link link = ((DrtStayTask)vehicle.getSchedule().getTasks().get(previousIdx)).getLink();
        if (!linkRecord.containsKey(link.getId())){
            throw new RuntimeException("The departing vehicle has not registered in link records");
        }
        int num = linkRecord.get(link.getId()) - 1;
        linkRecord.put(link.getId(),num);
        if (num == 0){
            releaseOneLane(link);
        }
    }

    public void blockOneLane(Link currentLink){
        double numOfLanes = currentLink.getNumberOfLanes();
        double newCapacity = ((numOfLanes - 1) / numOfLanes) * currentLink.getCapacity();
        currentLink.setCapacity(newCapacity);
    }

    public void releaseOneLane(Link currentLink){
        linkRecord.remove(currentLink.getId());
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        linkRecord.clear();
    }
}

