package Run.PrePostProcessing;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.contrib.dvrp.data.Request;
import com.sun.javafx.collections.MappingChange;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.passenger.events.DrtRequestScheduledEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestScheduledEventHandler;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static Run.PrePostProcessing.TravelTimeCalculator.END_TIME;
public class TravelTimeCalculator {
    private static String FOLDER;
    private static String ITER = "40";
    private static String EVENTSFILE;
    protected static final double END_TIME = 30 * 3600;
    public static void main(String[] args) throws IOException {
        String[] parking = new String[]{"tp_base","tp_cheap_drtaxi","tp_expensive_drt"};
        String[] bay = new String[]{"bay","curb","infinity","single"};
//        for (String p:parking) {
//            for (String b: bay) {
                //FOLDER = "/home/biyu/IdeaProjects/NewParking/output/charging/drt_mix_V450_T250_bay_nocharger_debug/";
                FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/depot/demand_bay_mix/ITERS/";
                EVENTSFILE = FOLDER + "it." + ITER + "/" + ITER + ".events.xml.gz";
                EventsManager manager = EventsUtils.createEventsManager();
                Network network = NetworkUtils.createNetwork();
            new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/tanjong_pagar/network/tp_phase4.xml");
                TravelTimeHandler handler = new TravelTimeHandler();
                manager.addHandler(handler);
                new MatsimEventsReader(manager).readFile(EVENTSFILE);
                handler.output(FOLDER + ITER + "travel_time.csv");
//            }
        }
    }
//}

class TravelTimeHandler implements BasicEventHandler,PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler, ActivityStartEventHandler, ActivityEndEventHandler{
    HashMap<Id<Person>, TravelTime> tt = new HashMap<>();
    HashMap<Id<Request>, Id<Person>> dict = new HashMap<>();
    HashMap<Id<Person>, ArrayList<AccessEgressTime>> accessEgress = new HashMap<>();
    HashMap<Id<Person>, TotalTime> totalT = new HashMap<>();

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if ( !event.getVehicleId().toString().equals(event.getPersonId().toString())) {
            TravelTime t = tt.get(event.getPersonId());
            t.enterVehicleT = event.getTime();
            t.leaveVehicleT = event.getTime();
            t.vid = event.getVehicleId();
        }

    }

    @Override
    public void handleEvent(Event event) {
        if (event.getEventType().equals("DrtRequest scheduled")) {
            Id<Request> rid = Id.create(event.getAttributes().get("request"), Request.class);
            Id<Person> pid = dict.get(rid);
            TravelTime t = tt.get(pid);
            t.scheduledT = event.getTime();
        }
        if (event.getEventType().equals("DrtRequest submitted")){
            Id<Person> pid = Id.createPersonId(event.getAttributes().get("person"));
            if (!tt.containsKey(pid)){
                tt.put(pid,new TravelTime(event.getTime()));
                dict.put(Id.create(event.getAttributes().get("request"), Request.class),pid);
            }
        }
//        if (event.getEventType().equals("DrtRequest rejected")){
//            Id<Request> rid = Id.create(event.getAttributes().get("request"), Request.class);
//            Id<Person> pid = dict.get(rid);
//            TravelTime t  = tt.get(pid);
//            t.scheduledT = event.getTime();
//            t.enterVehicleT = Double.min(3 * 3600 + event.getTime(), END_TIME);
//            t.leaveVehicleT = Double.min(3 * 3600 + event.getTime(), END_TIME);
//        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (event.getVehicleId().toString().startsWith("drt") && !event.getVehicleId().toString().equals(event.getPersonId().toString())) {
            TravelTime t = tt.get(event.getPersonId());
            t.leaveVehicleT = event.getTime();
        }
    }

    public void output(String file) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(file);
        bw.write("personId;submissionT;scheduledT;enterVehicleT;leaveVehicleT;vid;departureT;arrivalT;accessEgressT;nAccessEgress");
        for (Id<Person> pid : tt.keySet()){
            bw.newLine();
            TravelTime t = tt.get(pid);
            TotalTime total = totalT.get(pid);
            double accessEgressTime = 0;
            int count = 0;
            if (accessEgress.get(pid) != null) {
                for (AccessEgressTime a : accessEgress.get(pid)) {
                    accessEgressTime = a.arrivalT - a.departureT + accessEgressTime;
                    count++;
                }
            }
            bw.write(pid + ";" + t.submissionT + ";" + t.scheduledT + ";" + t.enterVehicleT + ";" + t.leaveVehicleT + ";" + t.vid + ";" + total.departureT + ";" + total.arrivalT +";" + accessEgressTime + ";" + count);
        }
        bw.close();
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (totalT.containsKey(event.getPersonId())){
            return;
        }
        totalT.put(event.getPersonId(), new TotalTime(event.getTime()));

    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (!totalT.containsKey(event.getPersonId())){
            return;
        }
        totalT.get(event.getPersonId()).arrivalT = event.getTime();
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (event.getLegMode().equals(TransportMode.transit_walk)){
            ArrayList<AccessEgressTime> accessEgressList = accessEgress.get(event.getPersonId());
            accessEgressList.get(accessEgressList.size() - 1).arrivalT = event.getTime();
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(TransportMode.transit_walk)){
            if (!accessEgress.containsKey(event.getPersonId())){
                accessEgress.put(event.getPersonId(),new ArrayList<>());
            }
            accessEgress.get(event.getPersonId()).add(new AccessEgressTime(event.getTime()));
        }

    }
}

class TravelTime{

    Id<Vehicle> vid;
    double submissionT;
    double scheduledT;
    double enterVehicleT = END_TIME;
    double leaveVehicleT = END_TIME;


    public TravelTime(double submissionT){
        this.submissionT = submissionT;
    }
}


class AccessEgressTime{
    double departureT;
    double arrivalT = Double.POSITIVE_INFINITY;
    public AccessEgressTime(double departureT){
        this.departureT = departureT;
    }
}

class TotalTime{
    double departureT;
    double arrivalT = Double.POSITIVE_INFINITY;

    public TotalTime(double departureT){
        this.departureT = departureT;
    }
}
