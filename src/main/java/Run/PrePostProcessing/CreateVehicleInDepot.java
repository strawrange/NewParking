package Run.PrePostProcessing;

import ParkingStrategy.ParkingInDepot.Depot.*;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.VehicleImpl;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CreateVehicleInDepot {
    public static void main(String[] args) {
        int seat4 = 20;
        int seat10 = 10;
        int seat20 = 10;
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        DepotManager depotManager = new DepotManager();
        new DepotReader(depotManager,network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drt_depot.xml");
        ArrayList<Vehicle> vehicles = new ArrayList<>();
        Map<Id<Depot>, Depot> HDBdepots = depotManager.getDepots(Depot.DepotType.HDB);
        Random random = new Random();
        Object[] hdbdepotIds = HDBdepots.keySet().toArray();
        HDBDepot HDBdepot;
        for (int i = 0; i < seat4; i++){
            do {
            HDBdepot = (HDBDepot) HDBdepots.get(hdbdepotIds[random.nextInt(hdbdepotIds.length)]);
            }while (HDBdepot.getCapacity() == HDBdepot.getNumOfVehicles());
            Vehicle vehicle = new VehicleImpl(Id.create("drt_4_" + i, Vehicle.class),HDBdepot.getLink(), 4, 1, 86399 );
            HDBdepot.addVehicle(vehicle.getId());
            vehicles.add(vehicle);
        }
        for (int i = 0; i < seat10; i++){
            do {
                HDBdepot = (HDBDepot) HDBdepots.get(hdbdepotIds[random.nextInt(hdbdepotIds.length)]);
            }while (HDBdepot.getCapacity() == HDBdepot.getNumOfVehicles());
            Vehicle vehicle = new VehicleImpl(Id.create("drt_10_" + i, Vehicle.class),HDBdepot.getLink(), 10, 1, 86399 );
            HDBdepot.addVehicle(vehicle.getId());
            vehicles.add(vehicle);
        }

        Map<Id<Depot>, Depot> depots = depotManager.getDepots(Depot.DepotType.DEPOT);
        Object[] depotIds = depots.keySet().toArray();
        DepotImpl depot;
        for (int i = 0; i < seat20; i++){
            do {
                depot = (DepotImpl) depots.get(depotIds[random.nextInt(depotIds.length)]);
            }while (depot.getCapacity() == depot.getNumOfVehicles());
            Vehicle vehicle = new VehicleImpl(Id.create("drt_20_" + i, Vehicle.class),depot.getLink(), 20, 1, 86399 );
            depot.addVehicle(vehicle.getId());
            vehicles.add(vehicle);
        }
        new VehicleWriter(vehicles).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_40.xml");
    }
}
