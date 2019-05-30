package Run.PrePostProcessing.Transit;

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
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/tp_20190211/transit/";

    public static void main(String[] args) throws IOException {
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "tp_TANVI_ACSP18.xml");
        TransitSchedule transitSchedule = scenario.getTransitSchedule();

        BufferedReader reader = new BufferedReader(new FileReader("/home/biyu/IdeaProjects/NewParking/scenarios/tp_20181016/tanjong-pagar/tanjong-pagar/transit/stops_TANVI.txt"));
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
        new TransitScheduleWriterV2(transitSchedule).write(FOLDER + "tp_TANVI_ACSP18.xml");
    }
}
