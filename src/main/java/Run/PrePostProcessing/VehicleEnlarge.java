package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.VehicleImpl;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class VehicleEnlarge {
    static FleetImpl fleet = new FleetImpl();
    static FleetImpl newFleet = new FleetImpl();
    public static void main(String[] args) {
        int numS = 1000;
        int numM = 700;
        int numL = 300;
        int num = numL + numM + numS;
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        (new VehicleReader(network,fleet)).parse(IOUtils.getUrlFromFileOrResource("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drt_vehicles_mix.xml"));
        ArrayList<Vehicle> smallV = new ArrayList<>(filter(4).values());
        ArrayList<Vehicle> mediumV = new ArrayList<>(filter(10).values());
        ArrayList<Vehicle> largeV = new ArrayList<>(filter(20).values());
        copy(smallV, numS);
        copy(mediumV, numM);
        copy(largeV, numL);
        new VehicleWriter(newFleet.getVehicles().values()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtvehicles_" + num + ".xml");
    }

    public static Map<Id<Vehicle>,Vehicle> filter(int capacity){
        return fleet.getVehicles().entrySet().stream().filter(v -> v.getValue().getCapacity() == capacity).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    public static void copy(ArrayList<Vehicle> vehicles, int num){
        int size = vehicles.size();
        Random random = new Random();
        for (int i = 0; i < num; i++){
            Vehicle v = vehicles.get(random.nextInt(size));
            Vehicle veh;
            if (i >= 1000) {
                veh = new VehicleImpl(Id.create("drt_" + (int)v.getCapacity() + "s_" + i, Vehicle.class), v.getStartLink(),v.getCapacity(),  v.getServiceBeginTime(), v.getServiceEndTime());
            }else if (i >= 100) {
                veh = new VehicleImpl(Id.create("drt_" + (int)v.getCapacity() + "s_0" + i, Vehicle.class), v.getStartLink(),v.getCapacity(),  v.getServiceBeginTime(), v.getServiceEndTime());
            }else if (i >= 10){
                veh = new VehicleImpl(Id.create("drt_" + (int)v.getCapacity() + "s_00" + i, Vehicle.class), v.getStartLink(),v.getCapacity(),  v.getServiceBeginTime(), v.getServiceEndTime());
            }else{
                veh = new VehicleImpl(Id.create("drt_" + (int)v.getCapacity() + "s_000" + i, Vehicle.class), v.getStartLink(), v.getCapacity(),  v.getServiceBeginTime(), v.getServiceEndTime());
            }

            newFleet.addVehicle(veh);
        }
    }
}
