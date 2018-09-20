package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import Schedule.VehicleImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;

import org.matsim.core.utils.io.MatsimXmlWriter;

import java.util.*;
;

public class VehicleGenerator {
    static FleetImpl newFleet = new FleetImpl();
    public static void main(String[] args) {
        int numT = 250;
        int numS = 200;
        int numM = 150;
        int numL = 100;
        int num = numL + numM + numS + numT;
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        Network drtNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(drtNetwork, Collections.singleton("car"));
        new NetworkCleaner().run(drtNetwork);
        Random random = new Random();
        ArrayList<Id<Link>> links = new ArrayList<>(drtNetwork.getLinks().keySet());
        for (int i =0;i<numT;i++){
            Id<Link> lid = links.get(random.nextInt(links.size()));
            Vehicle veh = new VehicleImpl(Id.create("drt_1s_" + i, Vehicle.class),drtNetwork.getLinks().get(lid), 1.0,0,30*3600,"drtaxi");
            newFleet.addVehicle(veh);
        }
        for (int i =0;i<numS;i++){
            Id<Link> lid = links.get(random.nextInt(links.size()));
            Vehicle veh = new VehicleImpl(Id.create("drt_4s_" + i, Vehicle.class),drtNetwork.getLinks().get(lid), 4.0,0,30*3600,"drt");
            newFleet.addVehicle(veh);
        }
        for (int i =0;i<numM;i++){
            Id<Link> lid = links.get(random.nextInt(links.size()));
            Vehicle veh = new VehicleImpl(Id.create("drt_10s_" + i, Vehicle.class),drtNetwork.getLinks().get(lid), 10.0,0,30*3600,"drt");
            newFleet.addVehicle(veh);
        }
        for (int i =0;i<numL;i++){
            Id<Link> lid = links.get(random.nextInt(links.size()));
            Vehicle veh = new VehicleImpl(Id.create("drt_20s_" + i, Vehicle.class),drtNetwork.getLinks().get(lid), 20.0,0,30*3600,"drt");
            newFleet.addVehicle(veh);
        }
        new VehicleWriter(newFleet.getVehicles().values()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_" + num + ".xml");
    }

}

class VehicleWriter extends MatsimXmlWriter {
    private Iterable<? extends Vehicle> vehicles;

    public VehicleWriter(Iterable<? extends Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void write(String file) {
        openFile(file);
        writeDoctype("vehicles", "http://matsim.org/files/dtd/dvrp_vehicles_v1.dtd");
        writeStartTag("vehicles", Collections.<Tuple<String, String>> emptyList());
        writeVehicles();
        writeEndTag("vehicles");
        close();
    }

    private void writeVehicles() {
        for (Vehicle veh : vehicles) {
            List<Tuple<String, String>> atts = Arrays.asList(
                    new Tuple<>("id", veh.getId().toString()),
                    new Tuple<>("start_link", veh.getStartLink().getId().toString()),
                    new Tuple<>("t_0", veh.getServiceBeginTime() + ""),
                    new Tuple<>("t_1", veh.getServiceEndTime() + ""),
                    new Tuple<>("capacity", veh.getCapacity() + ""),
                    new Tuple<>("mode",((VehicleImpl)veh).getMode() + ""));
            writeStartTag("vehicle", atts, true);
        }
    }
}
