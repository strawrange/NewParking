package BayInfrastructure;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Bay {
    private final TransitStopFacility transitStop;
    private final Id<Link> linkId;
    private final double capacity;


    private Queue<Id<Vehicle>> vehicles = new LinkedList<>();
    private Queue<Id<Vehicle>> dwellingVehicles = new LinkedList<>();

    public Bay(TransitStopFacility transitStop){
        this.transitStop = transitStop;
        this.linkId = transitStop.getLinkId();
        if (transitStop.getAttributes().getAttribute("capacity") == null){
            this.capacity = 1;
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

    public void addVehicle(Id<Vehicle> vid){
        if (dwellingVehicles.size() > capacity){
            throw new RuntimeException("too many dwelling vehicles!");
        }
        if (dwellingVehicles.contains(vid)){
            return;
        }
        if (dwellingVehicles.size() == capacity){
            if (!vehicles.contains(vid)) {
                vehicles.add(vid);
            }
        }else{
            dwellingVehicles.add(vid);

        }
    }


    public void removeVehicle(Id<Vehicle> vid){
        if(!dwellingVehicles.contains(vid) && !vehicles.contains(vid)){
            throw new RuntimeException();
        }
        vehicles.remove(vid);
        if (dwellingVehicles.remove(vid) && isFull()){
            Id<Vehicle> vehicleId = vehicles.peek();
            dwellingVehicles.add(vehicleId);
            vehicles.remove(vehicleId);
        }
    }

    public Queue<Id<Vehicle>> getVehicles() {
        return vehicles;
    }


    public boolean isFull() {
        return vehicles.size() > 0;
    }

}
