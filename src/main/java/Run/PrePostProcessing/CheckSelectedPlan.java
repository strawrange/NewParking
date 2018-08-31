package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckSelectedPlan {
    private static String FOLDER;
    private static String ITER = "40";
    private static String EVENTSFILE;

    public static void main(String[] args) throws IOException {
        int[] bay = new int[]{10,15,16,17,18,19,20,25,30};
        for (int i:bay){
            FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/TRB/Road/tanjong_pagar_road_"+ i +"_v300_0.125/ITERS/";
            EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
            EventsManager manager = EventsUtils.createEventsManager();
            transitHandler handler = new transitHandler();
            manager.addHandler(handler);
            new MatsimEventsReader(manager).readFile(EVENTSFILE);
            handler.output(FOLDER + ITER + "walkAndMRTAnalyser.csv");
        }
    }
}

class transitHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler{
    HashMap<Id<Person>, Trip> trips = new HashMap<>();

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (event.getLegMode().equals(TransportMode.transit_walk)){
            Trip trip = trips.get(event.getPersonId());
            trip.endT.add(event.getTime());
            int idx = trip.endT.size() - 1;
            trip.duration = trip.duration + trip.endT.get(idx) - trip.startT.get(idx);
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(TransportMode.transit_walk)){
            if (!trips.containsKey(event.getPersonId())){
                trips.put(event.getPersonId(), new Trip());
            }
            Trip trip = trips.get(event.getPersonId());
            trip.startT.add(event.getTime());
        }
    }

    public void output(String file) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(file);
        bw.write("personId;walkTime;count");
        for (Id<Person> pid: trips.keySet()){
            bw.newLine();
            bw.write(pid + ";" + trips.get(pid).duration + ";" + trips.get(pid).startT.size());
        }
        bw.close();
    }
}

class Trip{
    ArrayList<Double> startT = new ArrayList<>();
    ArrayList<Double> endT = new ArrayList<>();
    double duration = 0;
}
