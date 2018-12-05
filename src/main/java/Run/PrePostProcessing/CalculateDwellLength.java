package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;

public class CalculateDwellLength {
    private static String FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/trb/mixed/infinity/";
    private static String ITER = "40";
    private static String EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
    public static void main(String[] args) throws IOException {
        EventsManager manager = EventsUtils.createEventsManager();
        DwellingHandler dwellingCounter = new DwellingHandler();
        manager.addHandler(dwellingCounter);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        dwellingCounter.output(FOLDER + ITER + "dwellAnalysis.csv");
    }
}

class DwellingHandler implements VehicleDepartsAtFacilityEventHandler, VehicleArrivesAtFacilityEventHandler{
    Map<Id<TransitStopFacility>, List<TimeDwellPair>> count = new HashMap<>();

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
        if (event.getFacilityId().toString().endsWith("DRT")){
            return;
        }
        if (!count.containsKey(event.getFacilityId())){
            TimeDwellPair timeDwellPair = new TimeDwellPair(0.0,0.0);
            List<TimeDwellPair> list = new ArrayList<>();
            list.add(timeDwellPair);
            count.put(event.getFacilityId(), list);
        }
        double length = 0.0;
        if (event.getVehicleId().toString().startsWith("drt_1s")){
            length = 3.0;
        }else if (event.getVehicleId().toString().startsWith("drt_4s")){
            length = 5.0;
        }else if (event.getVehicleId().toString().startsWith("drt_10s")){
            length = 6.5;
        }else if (event.getVehicleId().toString().startsWith("drt_20s")){
            length = 9.0;
        }else if (event.getVehicleId().toString().startsWith("CC")) {
        }else{
            throw new RuntimeException("Wrong Vehicle ID");
        }
        List<TimeDwellPair> list = count.get(event.getFacilityId());
        TimeDwellPair timeDwellPair = new TimeDwellPair(event.getTime(),list.get(list.size() - 1).length + length);
        list.add(timeDwellPair);
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        if (event.getFacilityId().toString().endsWith("DRT")){
            return;
        }
        double length = 0.0;
        if (event.getVehicleId().toString().startsWith("drt_1s")){
            length = 3.0;
        }else if (event.getVehicleId().toString().startsWith("drt_4s")){
            length = 5.0;
        }else if (event.getVehicleId().toString().startsWith("drt_10s")){
            length = 6.5;
        }else if (event.getVehicleId().toString().startsWith("drt_20s")){
            length = 9.0;
        }else if (event.getVehicleId().toString().startsWith("CC")) {
        }else{
            throw new RuntimeException("Wrong Vehicle ID");
        }
        List<TimeDwellPair> list = count.get(event.getFacilityId());
        TimeDwellPair timeDwellPair = new TimeDwellPair(event.getTime(),list.get(list.size() - 1).length - length);
        list.add(timeDwellPair);
    }

    public void output(String fileName) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(fileName);
        bw.write("time;stopID;dwellLength");
        for (Id<TransitStopFacility> sid: count.keySet()){
            int p = 0;
            for (int i = 0; i < 28*3600; i++){
                if (p < count.get(sid).size() &&((int)count.get(sid).get(p).time) == i){
                    p++;
                }
                bw.newLine();
                bw.write(i + ";" + sid.toString() + ";" + count.get(sid).get(p - 1).length);
            }
        }
        bw.close();
    }
}

class TimeDwellPair{
    double time = 0.0;
    double length= 0.0;

    TimeDwellPair(double time, double length){
        this.time = time;
        this.length = length;
    }
}
