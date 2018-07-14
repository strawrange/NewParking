package ParkingStrategy.ParkingInDepot.Depot;

import Run.DrtConfigGroup;
import com.google.inject.Inject;
import com.sun.xml.internal.ws.util.QNameMap;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.core.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface DepotManager {



     void addDepot(Depot depot);

     Map<Id<Depot>, Depot> getDepots();
     Map<Id<Depot>, Depot> getDepots(Depot.DepotType depotType);

     boolean isVehicleInDepot(Vehicle vehicle);

     void registerVehicle(Id<Vehicle> vid, Id<Depot> did);

     Depot getDepotOfVehicle(Vehicle vehicle) ;

     void vehicleLeavingDepot(Vehicle vehicle) ;

     Map<Id<Depot>, Depot> getDepots(double capacity);
}
