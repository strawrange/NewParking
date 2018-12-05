package Run.PrePostProcessing;

import ParkingStrategy.ParkingInDepot.Depot.*;
import Run.AtodConfigGroup;
import Schedule.VehicleImpl;
import Vehicle.FleetImpl;
import Vehicle.VehicleReader;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.util.distance.DistanceUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.*;

public class VehicleInDepot {
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig_mix_V900_T500_charger.xml", new AtodConfigGroup());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        FleetImpl fleet = new FleetImpl();
        (new VehicleReader(network,fleet)).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_1400.xml"));
        DepotManager depotManager = new DepotManagerSameDepot(config,network);
        List depotIds = new ArrayList(depotManager.getDepots().keySet());
        for (Vehicle vehicle: fleet.getVehicles().values()){
            Depot depot = findDepots(vehicle, depotManager);
            vehicle.setStartLink(depot.getLink());
        }
        new VehicleWriter(fleet.getVehicles().values()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_1400_depot.xml");
    }

    private static Depot findDepots(Vehicle vehicle, DepotManager depotManager) {
        Link currentLink = vehicle.getStartLink();
        if (depotManager.isVehicleInDepot(vehicle)) {
            return null;// stay where it is
        }

        Depot bestDepot = null;
        double bestDistance = Double.MAX_VALUE;
        for (Depot d : depotManager.getDepots(vehicle.getCapacity()).values()) {
            if (d.getCapacity() > d.getNumOfVehicles()){
                double currentDistance = DistanceUtils.calculateSquaredDistance(currentLink.getCoord(), d.getLink().getCoord());
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestDepot = d;
                }
            }
        }
        if (bestDepot == null){
            throw new RuntimeException("All depots are full!!");
        }
        bestDepot.addVehicle(vehicle.getId());
        depotManager.registerVehicle(vehicle.getId(),bestDepot.getId());
        return bestDepot;
    }
}
