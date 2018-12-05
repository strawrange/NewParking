package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopArea;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AddStopsInArea {
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/";

    public static void main(String[] args) throws IOException {
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "mp_c_tp_PT_bay_optimal_2018.xml");
        TransitSchedule transitSchedule = scenario.getTransitSchedule();

        BufferedReader reader = new BufferedReader(new FileReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/stops_in_area.txt"));
        String line = reader.readLine();
        Set<Id<TransitStopFacility>> ids = new HashSet<>();
        while(line!=null) {
            ids.add(Id.create(line, TransitStopFacility.class));
            line = reader.readLine();

        }
        reader.close();
        for(TransitStopFacility stop:scenario.getTransitSchedule().getFacilities().values()) {
            IDS:
            for(Id<TransitStopFacility> id:ids)
                if(stop.getId().equals(id)) {
                    stop.setStopAreaId(Id.create("mp", TransitStopArea.class));
                    break IDS;
                }
        }
        new TransitScheduleWriterV2(transitSchedule).write(FOLDER + "mp_c_tp_PT_bay_optimal_2018.xml");
    }
}
