package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;

public class VehicleIDGenarator {
    public static void main(String[] args) throws IOException {
//        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
//        Scenario scenario = ScenarioUtils.loadScenario(config);
//        Network network = scenario.getNetwork();
        FleetImpl fleet = new FleetImpl();
        (new VehicleReader(NetworkUtils.createNetwork(),fleet)).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_600.xml"));
        BufferedWriter bw = IOUtils.getBufferedWriter("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/vehicleId_drtvehicles_600.txt");
        for (Vehicle vehicle: fleet.getVehicles().values()){
            bw.write(vehicle.getId().toString());
            bw.newLine();
        }
        bw.close();
    }
}
