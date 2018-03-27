package ParkingStrategy.ParkingInDepot.Depot;

import com.sun.xml.internal.ws.util.QNameMap;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.Vehicle;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DepotManager {

    Map<Id<Depot>, Depot> depots = new HashMap<>();
    Map<Id<Vehicle>,Id<Depot>> vehicleLists = new HashMap<>(); //  vehicle id, depot id

    public void addDepot(Depot depot) {
        depots.put(depot.getId(), depot);
    }

    public Map<Id<Depot>, Depot> getDepots() {
        return depots;
    }
    public Map<Id<Depot>, Depot> getDepots(Depot.DepotType depotType) {
        return depots.entrySet().stream().filter(depot -> depot.getValue().getDepotType() == depotType).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    public boolean isVehicleInDepot(Vehicle vehicle) {
        return vehicleLists.containsKey(vehicle.getId());
    }

    public void registerVehicle(Id<Vehicle> vid, Id<Depot> did) {
        vehicleLists.put(vid,did);
    }

    private Depot getDepotOfVehicle(Vehicle vehicle) {
        if (vehicleLists.containsKey(vehicle.getId())){
            return depots.get(vehicleLists.get(vehicle.getId()));
        }
        return null;
    }

    public void vehicleLeavingDepot(Vehicle vehicle) {
        Depot currentDepot = getDepotOfVehicle(vehicle);
        if (currentDepot == null){
            return;
        }
        currentDepot.removeVehicle(vehicle.getId());
        vehicleLists.remove(vehicle.getId());
    }

    public Map<Id<Depot>, Depot> getDepots(double capacity) {
        if (capacity <= 10){
            return getDepots(Depot.DepotType.HDB);
        }else{
            return getDepots(Depot.DepotType.DEPOT);
        }
    }
}
