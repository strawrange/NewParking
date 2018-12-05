package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.*;

public class DropHalfStops {
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/tanjong_pagar/transit/";

    public static void main(String[] args) {
        Random random = new Random();
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "tp_PT_bay_walkshed_max.xml");
        TransitSchedule transitSchedule = scenario.getTransitSchedule();
        List<Id<TransitStopFacility>> stops = new ArrayList<>(transitSchedule.getFacilities().keySet());
        for (int i = 0; true; i++){
            if (i >= transitSchedule.getFacilities().size()){
                break;
            }
            if (stops.get(i).toString().startsWith("CC")){
                continue;
            }
            if (random.nextDouble() < 0.5){
                transitSchedule.removeStopFacility(transitSchedule.getFacilities().get(stops.get(i)));
                stops.remove(i);
                i--;
            }
        }
        new TransitScheduleWriterV2(transitSchedule).write(FOLDER + "tp_PT_bay_walkshed_max.xml");
    }
}
