package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.BoardingDeniedEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.handler.BoardingDeniedEventHandler;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.util.ArrayList;

class DeniedBoardingHandler implements IndicatorModule, BoardingDeniedEventHandler, PersonDepartureEventHandler{
//    private static String FOLDER;
//    private static String ITER = "40";
//    private static String EVENTSFILE;
//    protected static final double END_TIME = 30 * 3600;
//    public static void main(String[] args) throws IOException {
//        String[] parking = new String[]{"depot","roam","road"};
//        String[] bay = new String[]{"bay","curb","infinity","single"};
////        for (String p:parking) {
////            for (String b: bay) {
//        FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/demand_bay_onlyav/ITERS/";
//        //FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/trb/" + p + "/" + b + "/";
//        EVENTSFILE = FOLDER + "it."+ ITER +"/" + ITER + ".events.xml.gz";
//        Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
//        EventsManager manager = EventsUtils.createEventsManager();
//        Network network = scenario.getNetwork();
//        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
//        new TransitScheduleReaderV2(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_PT_2018.xml");
//        DeniedBoardingHandler handler = new DeniedBoardingHandler(network, scenario.getTransitSchedule());
//        manager.addHandler(handler);
//        new MatsimEventsReader(manager).readFile(EVENTSFILE);
//        handler.output(FOLDER + ITER + "denied_boarding.csv");
////            }
////        }
//    }
//}
    String filename = "denied_boarding.csv";
    Network network;
    Map<Id<Person>, ArrayList<Event>> deniedEvents = new HashMap<>();
    HashMap<Id<Link>, Id<TransitStopFacility>> dict = new HashMap<>();

    public DeniedBoardingHandler(Network network, TransitSchedule transitSchedule) throws IOException {
        this.network = network;
        ArrayList<Id<TransitStopFacility>> outAreaStops = new ArrayList<>();
        BufferedReader br = IOUtils.getBufferedReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/out_area_stops.txt");
        String line = br.readLine();
        while (line != null){
            Id<TransitStopFacility> sid = Id.create(line, TransitStopFacility.class);
            outAreaStops.add(sid);
            line = br.readLine();
        }
        for (TransitStopFacility stop : transitSchedule.getFacilities().values()){
            if (!outAreaStops.contains(stop.getId())) {
                dict.put(stop.getLinkId(), stop.getId());
            }
        }
    }

    @Override
    public void handleEvent(BoardingDeniedEvent e) {
            if (deniedEvents.containsKey(e.getPersonId())) {
                deniedEvents.get(e.getPersonId()).add(e);
            }
    }


    @Override
    public void handleEvent(PersonDepartureEvent event) {
            if (event.getLegMode().equals(TransportMode.pt) && dict.containsKey(event.getLinkId())) {
                if (!deniedEvents.containsKey(event.getPersonId())) {
                    deniedEvents.put(event.getPersonId(), new ArrayList<>());
                }
                deniedEvents.get(event.getPersonId()).add(event);
            }
    }


    @Override
    public void output(String outputPath) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("pid;stop;time;departures");
        int count = 0;
        for (Id<Person> pid: deniedEvents.keySet()){
            PersonDepartureEvent departure = null;
            for (Event event: deniedEvents.get(pid)){
                if (event instanceof  PersonDepartureEvent){
                    count = 0;
                    departure = (PersonDepartureEvent) event;
                    bw.newLine();
                    bw.write(pid + ";" + dict.get(departure.getLinkId()) + ";" + departure.getTime() + ";" + count);
                }
                if (event instanceof BoardingDeniedEvent){
                    bw.newLine();
                    count++;
                    bw.write(pid + ";" + dict.get(departure.getLinkId()) + ";" + departure.getTime() + ";" + count);
                }
            }
        }
        bw.close();
    }
}

