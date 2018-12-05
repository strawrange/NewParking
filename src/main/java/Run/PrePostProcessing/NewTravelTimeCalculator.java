package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static Run.PrePostProcessing.NewTravelTimeCalculator.END_TIME;


public class NewTravelTimeCalculator {
    private static String FOLDER;
    private static String ITER = "40";
    private static String EVENTSFILE;
    protected static final double END_TIME = 30 * 3600;
    public static void main(String[] args) throws IOException {
        String[] parking = new String[]{"depot","roam","road"};
        String[] bay = new String[]{"bay","curb","infinity","single"};
//        for (String p:parking) {
//            for (String b: bay) {
        FOLDER = "/home/biyu/IdeaProjects/NewParking/output/charging/drt_mix_V450_T250_bay_nocharger_debug/";
        //FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/trb/" + p + "/" + b + "/";
        EVENTSFILE = FOLDER + "it.40/40.events.xml.gz";
        Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
        EventsManager manager = EventsUtils.createEventsManager();
        Network network = scenario.getNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        new TransitScheduleReaderV2(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_PT_2018.xml");
        NewTravelTimeHandler handler = new NewTravelTimeHandler(network, scenario.getTransitSchedule());
        manager.addHandler(handler);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        handler.output(FOLDER + ITER + "travel_time_mot.csv");
        handler.outputStage(FOLDER + ITER + "stage_travel_time_mot.csv");
//            }
//        }
    }
}

class NewTravelTimeHandler implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler, ActivityStartEventHandler, ActivityEndEventHandler{

    HashMap<Id<Person>, WaitTime> wt = new HashMap<>();
    HashMap<Id<Person>, InVehicleTime> ivvt = new HashMap<>();
    HashMap<Id<Person>, TransitWalkTime> transitWalk = new HashMap<>();
    HashMap<Id<Person>, NewTotalTime> totalT = new HashMap<>();
    HashMap<Id<Person>, ArrayList<Event>> accessEgress = new HashMap<>();
    Network network;
    TransitSchedule transitSchedule;
    HashMap<Id<Link>, TransitStopFacility> dict = new HashMap<>();
    HashMap<Id<Person>,ArrayList<Event>> waitTime = new HashMap<>();
    public NewTravelTimeHandler(Network network, TransitSchedule transitSchedule) {
        this.network = network;
        this.transitSchedule = transitSchedule;
        for (TransitStopFacility stop : transitSchedule.getFacilities().values()){
            dict.put(stop.getLinkId(),stop);
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt") && !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
            WaitTime t = wt.get(event.getPersonId());
            t.enterVehicleT = event.getTime();
            t.waitT = t.waitT + t.enterVehicleT - t.arrivalT;
            t.vid = event.getVehicleId();
            if (!ivvt.containsKey(event.getPersonId())){
                ivvt.put(event.getPersonId(),new InVehicleTime(event.getTime()));
            }else{
                ivvt.get(event.getPersonId()).enterVehicleT = event.getTime();
            }
            ivvt.get(event.getPersonId()).vid = event.getVehicleId();
            waitTime.get(event.getPersonId()).add(event);
        }

    }


    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt")&& !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
            InVehicleTime t = ivvt.get(event.getPersonId());
            t.leaveVehicleT = event.getTime();
            t.inVehicleT = t.inVehicleT + t.leaveVehicleT - t.enterVehicleT;
        }
    }

    public void output(String file) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(file);
        bw.write("personId;departureT;arrivalT;transitWalkT;waitT;inVehicleT;vid;mode;access;stopA;egress;stopE");
        for (Id<Person> pid : totalT.keySet()){
            bw.newLine();
            bw.write(pid.toString() + ";" + totalT.get(pid).departureT + ";" + totalT.get(pid).arrivalT + ";");
            if (transitWalk.containsKey(pid)){
                bw.write(transitWalk.get(pid).transitWalkT + ";");
            }else{
                bw.write(";");
            }
            if (wt.containsKey(pid)){
                bw.write(wt.get(pid).waitT + ";");
            }else{
                bw.write(";");
            }
            if (ivvt.containsKey(pid)){
                bw.write(ivvt.get(pid).inVehicleT + ";" + ivvt.get(pid).vid + ";");
            }else{
                bw.write(";;");
            }
            bw.write(totalT.get(pid).mode + ";");
            if (accessEgress.containsKey(pid)){
                int size = accessEgress.get(pid).size();
                PersonDepartureEvent departureEvent = (PersonDepartureEvent) accessEgress.get(pid).get(0);
                TransitStopFacility accessStop = dict.get(departureEvent.getLinkId());
                if (isMRT(accessStop)){
                    bw.write(departureEvent.getTime() + ";MRT;");
                }else{
                    bw.write(departureEvent.getTime() + ";bus;");
                }

                if (accessEgress.get(pid).get(size - 1) instanceof PersonArrivalEvent) {
                    PersonArrivalEvent arrivalEvent = (PersonArrivalEvent) accessEgress.get(pid).get(size - 1);
                    TransitStopFacility egressStop = dict.get(arrivalEvent.getLinkId());
                    if (isMRT(egressStop)) {
                        bw.write(arrivalEvent.getTime() + ";MRT");
                    } else {
                        bw.write( arrivalEvent.getTime() + ";bus" );
                    }
                }
            }
        }
        bw.close();
    }

    private boolean isMRT(TransitStopFacility stop) {
        return stop.getId().toString().startsWith("CC") || stop.getId().toString().startsWith("NS") || stop.getId().toString().startsWith("TE") || stop.getId().toString().startsWith("CE");
    }


    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (event.getLegMode().equals(TransportMode.transit_walk)){
            TransitWalkTime acc = transitWalk.get(event.getPersonId());
            acc.arrivalT = event.getTime();
            acc.transitWalkT = acc.transitWalkT + acc.arrivalT - acc.departureT;
            if (!wt.containsKey(event.getPersonId())){
                wt.put(event.getPersonId(), new WaitTime(event.getTime()));
            }else{
                wt.get(event.getPersonId()).arrivalT = event.getTime();
            }
        }
        if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt")) {
            accessEgress.get(event.getPersonId()).add(event);
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(TransportMode.transit_walk)){
            if (!transitWalk.containsKey(event.getPersonId())){
                transitWalk.put(event.getPersonId(),new TransitWalkTime(event.getTime()));
            }else {
                transitWalk.get(event.getPersonId()).departureT = event.getTime();
                transitWalk.get(event.getPersonId()).arrivalT = END_TIME;
            }
        }
        if (totalT.containsKey(event.getPersonId())) {
            if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt") || event.getLegMode().equals("drtaxi")) {
                totalT.get(event.getPersonId()).mode = TransportMode.pt;
                if (!wt.containsKey(event.getPersonId())){
                    wt.put(event.getPersonId(), new WaitTime(event.getTime()));
                }
                if (!waitTime.containsKey(event.getPersonId())){
                    waitTime.put(event.getPersonId(), new ArrayList<>());
                }
                waitTime.get(event.getPersonId()).add(event);
                if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt")) {
                    if (!accessEgress.containsKey(event.getPersonId())) {
                        accessEgress.put(event.getPersonId(), new ArrayList<>());
                    }
                    accessEgress.get(event.getPersonId()).add(event);
                }
            } else if (event.getLegMode().equals("pvt")){
                totalT.get(event.getPersonId()).mode = TransportMode.car;
            }else if (event.getLegMode().equals(TransportMode.walk)){
                totalT.get(event.getPersonId()).mode = TransportMode.walk;
            }else if (event.getLegMode().equals(TransportMode.taxi)){
                totalT.get(event.getPersonId()).mode = TransportMode.taxi;
            }else{
                if (!event.getLegMode().equals(TransportMode.transit_walk)){
                    System.out.println();
                }
            }
        }

    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals("dummy")){
            if (!totalT.containsKey(event.getPersonId())) {
                totalT.put(event.getPersonId(), new NewTotalTime(event.getTime()));
            }
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals("dummy")) {
            if (totalT.containsKey(event.getPersonId())){
                if (event.getTime() != totalT.get(event.getPersonId()).departureT) {
                    totalT.get(event.getPersonId()).arrivalT = event.getTime();
                }else{
                    totalT.remove(event.getPersonId());
                }
            }
        }
    }

    public void outputStage(String filename) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("pid;numT;departureT;enterVT;mode");
        for (Id<Person> pid: waitTime.keySet()){
            int numT = 1;
            for (int i = 0; i + 1 < waitTime.get(pid).size(); i = i + 2){
                bw.newLine();
                bw.write(pid + ";" + numT + ";" + waitTime.get(pid).get(i).getTime() + ";" + waitTime.get(pid).get(i+1).getTime() + ";" + ((PersonDepartureEvent)waitTime.get(pid).get(i)).getLegMode());
                numT++;
            }
        }
        bw.close();
    }
}

class WaitTime{
    Id<Vehicle> vid;
    double arrivalT;
    double enterVehicleT = END_TIME;
    double waitT;

    public WaitTime(double arrivalT){
        this.arrivalT = arrivalT;
    }
}

class InVehicleTime{
    Id<Vehicle> vid;
    double inVehicleT;
    double enterVehicleT;
    double leaveVehicleT = END_TIME;

    public InVehicleTime(double enterVehicleT){
        this.enterVehicleT = enterVehicleT;
    }
}

class TransitWalkTime{
    double departureT;
    double arrivalT = END_TIME;
    double transitWalkT;
    public TransitWalkTime(double departureT){
        this.departureT = departureT;
    }
}

class NewTotalTime{
    double departureT;
    double arrivalT = Double.POSITIVE_INFINITY;
    String mode = "";
    public NewTotalTime(double departureT){
        this.departureT = departureT;
    }
}