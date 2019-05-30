package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

/*
public class CalculateDwellLength {
    private static String FOLDER = "/home/biyu/Dropbox (engaging_mobility)/Team-Ordner „engaging_mobility“/bulky/trb/depot/infinity/ITERS/";
    private static String ITER = "40";
    private static String EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
    public static void main(String[] args) throws IOException {
        Config config = ConfigUtils.loadConfig("/home/biyu/Dropbox (engaging_mobility)/tanjong_pagar/trb_drt_depot_infinity_V600.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        EventsManager manager = EventsUtils.createEventsManager();
        DwellingHandler dwellingCounter = new DwellingHandler(scenario);
        manager.addHandler(dwellingCounter);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        dwellingCounter.output(FOLDER + ITER + "dwellAnalysis.csv");
    }
}
*/

class DwellingHandler implements IndicatorModule,VehicleDepartsAtFacilityEventHandler, VehicleArrivesAtFacilityEventHandler{
    Map<Id<TransitStopFacility>, List<TimeDwellPair>> count = new HashMap<>();
    Scenario scenario;
    String filename = "dwellLength.csv";

    public DwellingHandler(Scenario scenario){
        this.scenario = scenario;
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
            if (event.getFacilityId().toString().endsWith("DRT")) {
                return;
            }
            if (!count.containsKey(event.getFacilityId())) {
                TimeDwellPair timeDwellPair = new TimeDwellPair(0.0, 0.0);
                List<TimeDwellPair> list = new ArrayList<>();
                list.add(timeDwellPair);
                count.put(event.getFacilityId(), list);
            }
            double length = 0.0;
            if (event.getVehicleId().toString().startsWith("drt_1s")) {
                length = 5.0;
            } else if (event.getVehicleId().toString().startsWith("drt_4s")) {
                length = 5.0;
            } else if (event.getVehicleId().toString().startsWith("drt_10s")) {
                length = 6.5;
            } else if (event.getVehicleId().toString().startsWith("drt_20s")) {
                length = 9.0;
            } else if (event.getVehicleId().toString().startsWith("CC")) {
            } else {
                length = scenario.getTransitVehicles().getVehicles().get(event.getVehicleId()).getType().getLength();
            }
            List<TimeDwellPair> list = count.get(event.getFacilityId());
            TimeDwellPair timeDwellPair;
            if (list.get(list.size() - 1).time == event.getTime()) {
                list.get(list.size() - 1).length = list.get(list.size() - 1).length + length;
            } else {
                timeDwellPair = new TimeDwellPair(event.getTime(), list.get(list.size() - 1).length + length);
                list.add(timeDwellPair);
            }
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
            if (event.getFacilityId().toString().endsWith("DRT")) {
                return;
            }
            double length = 0.0;
            if (event.getVehicleId().toString().startsWith("drt_1s")) {
                length = 5.0;
            } else if (event.getVehicleId().toString().startsWith("drt_4s")) {
                length = 5.0;
            } else if (event.getVehicleId().toString().startsWith("drt_10s")) {
                length = 6.5;
            } else if (event.getVehicleId().toString().startsWith("drt_20s")) {
                length = 9.0;
            } else if (event.getVehicleId().toString().startsWith("CC")) {
            } else {
                length = scenario.getTransitVehicles().getVehicles().get(event.getVehicleId()).getType().getLength();
            }
            List<TimeDwellPair> list = count.get(event.getFacilityId());
            TimeDwellPair timeDwellPair;
            if (list.get(list.size() - 1).time == event.getTime()) {
                list.get(list.size() - 1).length = list.get(list.size() - 1).length - length;
            } else {
                timeDwellPair = new TimeDwellPair(event.getTime(), list.get(list.size() - 1).length - length);
                list.add(timeDwellPair);
            }

    }

    public void output(String outputPath) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("time;stopID;dwellLength;capacity");
        for (Id<TransitStopFacility> sid: count.keySet()){
            int p = 0;
            for (int i = 0; i < IndicatorsRun.END_TIME; i++){
                if (p < count.get(sid).size() &&((int)count.get(sid).get(p).time) == i){
                    p++;
                }
                bw.newLine();
                bw.write(i + ";" + sid.toString() + ";" + count.get(sid).get(p - 1).length + ";" + scenario.getTransitSchedule().getFacilities().get(sid).getAttributes().getAttribute("capacity"));
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
