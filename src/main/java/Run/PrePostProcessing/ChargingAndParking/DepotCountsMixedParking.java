package Run.PrePostProcessing.ChargingAndParking;

import ParkingStrategy.MixedParkingStrategy;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class DepotCountsMixedParking {
    private static String ITER = "40";
    private static String FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/drt_mix_V450_T250_bay_nocharger_pvttaxi2pt/ITERS/";
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {
//        String[] bay = new String[]{"bay", "infinity", "curb", "single"};
//        for (String b : bay) {
            //EVENTSFILE = FOLDER + b + "/" + ITER + ".events.xml.gz";
        EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
            EventsManager eventsManager = EventsUtils.createEventsManager();
            ParkingHandler2 p = new ParkingHandler2();
            eventsManager.addHandler(p);
            new MatsimEventsReader(eventsManager).readFile(EVENTSFILE);
            //p.output(FOLDER + b + "/depots_stat.csv");
        p.output(FOLDER + ITER +  "depots_stat.csv");
//        }
    }


}

class ParkingHandler2 implements ActivityStartEventHandler, ActivityEndEventHandler{
    Map<Id<Vehicle>,ActivityStartEvent> events = new HashMap<>();
    Map<Id<Link>, int[]> depots = new HashMap<>();
    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (!event.getPersonId().toString().startsWith("drt_20s")  && (event.getTime() <= MixedParkingStrategy.dayT0 || event.getTime() > MixedParkingStrategy.dayT1)){
            return;
        }
        if (event.getActType().equals("DrtStay")){
            Id<Vehicle> vid = Id.createVehicleId(event.getPersonId().toString());
            ActivityStartEvent startEvent = events.remove(vid);
            if (!depots.containsKey(event.getLinkId()) && startEvent.getTime() != event.getTime()){
                depots.put(event.getLinkId(), new int[3600*30]);
            }
            if (startEvent == null){
                System.out.println();
            }
            for (int i = (int) startEvent.getTime(); i < event.getTime(); i++){
                depots.get(event.getLinkId())[i]++;
            }
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {

        if (event.getActType().equals("DrtStay")){
            Id<Vehicle> vid = Id.createVehicleId(event.getPersonId().toString());
            events.put(vid, event);
        }
    }

    public void output(String filename) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("linkId;time;parking");
        for (Id<Link> linkId: depots.keySet()){
            for (int i = 0; i < 3600*30;i++) {
                bw.newLine();
                bw.write(linkId + ";" + i + ";" + depots.get(linkId)[i]);
            }
        }
        bw.close();
    }
}