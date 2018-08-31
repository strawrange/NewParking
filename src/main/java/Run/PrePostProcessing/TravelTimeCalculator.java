package Run.PrePostProcessing;

import org.matsim.api.core.v01.events.Event;
import org.matsim.contrib.dvrp.data.Request;
import com.sun.javafx.collections.MappingChange;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class TravelTimeCalculator {
    private static String FOLDER;
    private static String ITER = "40";
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {
        double[] bay = new double[]{1,1.5,2};
        //for (double i:bay){
            //FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/HKSTS/Mix/tanjong_pagar_mix_max_v600_plans_"+ i +"/ITERS/";
        FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/mp_c_tp/drt_mix_V1500_max/ITERS/";
            EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
            EventsManager manager = EventsUtils.createEventsManager();
            Network network = NetworkUtils.createNetwork();
            new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
            TravelTimeHandler handler = new TravelTimeHandler();
            manager.addHandler(handler);
            new MatsimEventsReader(manager).readFile(EVENTSFILE);
            handler.output(FOLDER + ITER + "travel_time_28.csv");
       // }
    }
}

class TravelTimeHandler implements BasicEventHandler,PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler{
    HashMap<Id<Person>, TravelTime> tt = new HashMap<>();
    HashMap<Id<Request>, Id<Person>> dict = new HashMap<>();

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() > 28*3600){
            return;
        }
        if (event.getVehicleId().toString().startsWith("drt") && !event.getVehicleId().toString().equals(event.getPersonId().toString())) {
            TravelTime t = tt.get(event.getPersonId());
            t.enterVehicleT = event.getTime();
        }

    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTime() > 28*3600){
            return;
        }
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
            }else{
                System.out.println();
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (event.getTime() > 28*3600){
            return;
        }
        if (event.getVehicleId().toString().startsWith("drt") && !event.getVehicleId().toString().equals(event.getPersonId().toString())) {
            TravelTime t = tt.get(event.getPersonId());
            t.leaveVehicleT = event.getTime();
        }
    }

    public void output(String file) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(file);
        bw.write("personId;submissionT;scheduledT;enterVehicleT;leaveVehicleT");
        for (Id<Person> pid : tt.keySet()){
            bw.newLine();
            TravelTime t = tt.get(pid);
            bw.write(pid + ";" + t.submissionT + ";" + t.scheduledT + ";" + t.enterVehicleT + ";" + t.leaveVehicleT);
        }
    }
}

class TravelTime{
    double submissionT;
    double scheduledT;
    double enterVehicleT;
    double leaveVehicleT;

    public TravelTime(double submissionT){
        this.submissionT = submissionT;
    }
}
