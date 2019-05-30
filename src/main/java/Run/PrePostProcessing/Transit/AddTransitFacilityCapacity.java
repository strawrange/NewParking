package Run.PrePostProcessing.Transit;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.IOException;

public class AddTransitFacilityCapacity {

    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/";

    public static void main(String[] args) throws IOException {
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "mp_c_tp_PT_bay_optimal_2018_farmrt.xml");
        TransitSchedule transitSchedule = scenario.getTransitSchedule();

        for (TransitStopFacility stop : transitSchedule.getFacilities().values()){
            if (stop.getId().toString().startsWith("CC") || stop.getId().toString().startsWith("NS") || stop.getId().toString().startsWith("TE") || stop.getId().toString().startsWith("CE")) {
                continue;
            }
            stop.getAttributes().clear();
        }
        new TransitScheduleWriterV2(transitSchedule).write(FOLDER + "mp_c_tp_PT_bay_inf_2018_farmrt.xml");
    }
}
