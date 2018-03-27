package Run.PrePostProcessing;

import ParkingStrategy.ParkingInDepot.Depot.Depot;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.ParkingInDepot.Depot.DepotReader;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VehicleInDepot {
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        FleetImpl fleet = new FleetImpl();
        (new VehicleReader(network,fleet)).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drt_vehicles_mix.xml"));
        DepotManager depotManager = new DepotManager();
        new DepotReader(depotManager,network).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drt_depot.xml"));
        Random random = new Random();
        List depotIds = new ArrayList(depotManager.getDepots().keySet());
        for (Vehicle vehicle: fleet.getVehicles().values()){
            Depot depot = depotManager.getDepots().get(depotIds.get(random.nextInt(depotIds.size())));
            vehicle.setStartLink(depot.getLink());
        }
        new VehicleWriter(fleet.getVehicles().values()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_depot.xml");
    }
}
