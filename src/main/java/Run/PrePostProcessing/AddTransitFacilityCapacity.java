package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.TransitStopFacilityImpl;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class AddTransitFacilityCapacity {

    private static String FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/mp_c_tp/";

    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "mp_c_tp_bay_2018.xml");
        TransitSchedule transitSchedule = scenario.getTransitSchedule();
        for (TransitStopFacility stop : transitSchedule.getFacilities().values()){
            if (stop.getId().toString().startsWith("CC") || stop.getId().toString().startsWith("NS") || stop.getId().toString().startsWith("TE") || stop.getId().toString().startsWith("CE")) {
                continue;
            }
            stop.getAttributes().putAttribute("capacity",Double.MAX_VALUE);
        }
        new TransitScheduleWriterV2(transitSchedule).write(FOLDER + "mp_c_tp_bay_max.xml");
    }
}
