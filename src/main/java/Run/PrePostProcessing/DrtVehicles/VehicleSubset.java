package Run.PrePostProcessing.DrtVehicles;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;

import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class VehicleSubset {
    public static void main(String[] args) {
        int num = 40;
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        FleetImpl fleet = new FleetImpl();
        FleetImpl newFleet = new FleetImpl();
        (new VehicleReader(network,fleet)).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drt_vehicles_mix.xml"));
        Random random = new Random();
        ArrayList<Id<Vehicle>> vehIdList = new ArrayList<>(fleet.getVehicles().keySet());
        while (newFleet.getVehicles().size() < num){
            Vehicle randomVeh = fleet.getVehicles().get(vehIdList.get(random.nextInt(vehIdList.size())));
            if (!newFleet.getVehicles().containsKey(randomVeh.getId())) {
                newFleet.addVehicle(randomVeh);
            }
        }
        new VehicleWriter(newFleet.getVehicles().values()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_" + num + ".xml");
    }
}
