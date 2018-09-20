package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.vehicles.Vehicle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OccupancyChecker {
    private static String FOLDER;
    private static String ITER = "40";
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {
        //double[] bay = new double[]{1,1.5,2};
        //for (double i:bay) {
            //FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/HKSTS/Roam/tanjong_pagar_roam_max_v600_plans_" + i + "/ITERS/";
        FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/HKSTS/tanjong_pagar_roam_max_v600_plans_2/ITERS/";
            EVENTSFILE = FOLDER + "it." + ITER + "/" + ITER + ".events.xml.gz";
            EventsManager manager = EventsUtils.createEventsManager();
            Network network = NetworkUtils.createNetwork();
            new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/tanjong_pagar/tp_phase4.xml");
            Handler handler = new Handler();
            manager.addHandler(handler);
            new MatsimEventsReader(manager).readFile(EVENTSFILE);
            handler.out();
        //}
    }
}

class Handler implements PersonEntersVehicleEventHandler{
    HashMap<Id<Vehicle>, ArrayList<Id<Person>>> ids = new HashMap<>();
    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getVehicleId().toString().equals(event.getPersonId().toString())){
            return;
        }
        if (event.getVehicleId().toString().startsWith("drt_20s")){
            if (!ids.containsKey(event.getVehicleId())){
                ArrayList<Id<Person>> pids = new ArrayList<>();
                pids.add(event.getPersonId());
                ids.put(event.getVehicleId(),pids);
            }else{
                ArrayList<Id<Person>> pids = ids.get(event.getVehicleId());
                pids.add(event.getPersonId());
                ids.put(event.getVehicleId(),pids);
            }
        }
    }

    public void out(){
        System.out.println();
    }
}