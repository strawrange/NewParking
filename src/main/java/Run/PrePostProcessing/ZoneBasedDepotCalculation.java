package Run.PrePostProcessing;

import ParkingStrategy.ParkingInDepot.Depot.Depot;
import ParkingStrategy.ParkingInDepot.Depot.DepotImpl;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.contrib.util.distance.DistanceUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.contrib.dvrp.data.file.VehicleReader;


public class ZoneBasedDepotCalculation {
    public static void main(String[] args) {
        int gridRow = 2;
        int gridCol = 2;
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/drt_example/drtconfig_stopbased.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        FleetImpl fleet = new FleetImpl();
        (new VehicleReader(network,fleet)).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/drt_example/drtvehicles.xml"));

        double[] boundingbox = NetworkUtils.getBoundingBox(network.getNodes().values());
        double minX = boundingbox[0];
        double maxX = boundingbox[2];
        double minY = boundingbox[1];
        double maxY = boundingbox[3];
        Depot[] depots = new Depot[gridRow*gridCol];
        int i = 0;
        for (int m = 0; m < gridRow; m++){
            for (int n = 0; n < gridCol; n++){
                double minDepotX = (maxX - minX) / gridRow * m + minX;
                double maxDepotX = (maxX - minX) / gridRow * (m + 1) + minX;
                double minDepotY = (maxY - minY) / gridRow * n + minY;
                double maxDepotY = (maxY - minY) / gridRow * (n + 1) + minY;
                depots[i] = new DepotImpl(Id.create("depot" + i,Depot.class), NetworkUtils.getNearestLink(network, new Coord((minDepotX + maxDepotX) / 2, (minDepotY + maxDepotY) / 2)), 50);
                i++;
            }
        }
        for (Vehicle vehicle: fleet.getVehicles().values()){
            Link nearestDepot = findDepot(vehicle, depots).getLink();
            vehicle.setStartLink(nearestDepot);
        }
        new VehicleWriter(fleet.getVehicles().values()).write("/home/biyu/IdeaProjects/NewParking/scenarios/drt_example/drtvehicles_depot.xml");
    }

    public static Depot findDepot(Vehicle vehicle, Depot[] depots) {
        Link startLink = vehicle.getStartLink();
        Depot bestDepot = null;
        double bestDistance = Double.MAX_VALUE;
        for (Depot l : depots) {
            if (l.getCapacity() > l.getNumOfVehicles()) {
                double currentDistance = DistanceUtils.calculateSquaredDistance(startLink.getCoord(), l.getLink().getCoord());
                if (currentDistance < bestDistance) {
                    bestDistance = currentDistance;
                    bestDepot = l;
                }
            }
        }
        bestDepot.addVehicle(vehicle.getId());
        return bestDepot;
    }

}
