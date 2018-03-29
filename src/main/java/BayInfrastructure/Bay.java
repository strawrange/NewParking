package BayInfrastructure;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.util.PriorityQueue;

public class Bay {
    private final TransitStopFacility transitStop;
    private final Id<Link> linkId;
    private final double capacity;
    private PriorityQueue<Id<Vehicle>> vehicles;

    public Bay(TransitStopFacility transitStop){
        this.transitStop = transitStop;
        this.linkId = transitStop.getLinkId();
        if (transitStop.getAttributes().getAttribute("capacity") == null){
            this.capacity = Double.MAX_VALUE;
        }else{
            this.capacity = (double) transitStop.getAttributes().getAttribute("capacity");
        }

    }

    public TransitStopFacility getTransitStop() {
        return transitStop;
    }

    public Id<Link> getLinkId() {
        return linkId;
    }

    public double getCapacity() {
        return capacity;
    }
}
