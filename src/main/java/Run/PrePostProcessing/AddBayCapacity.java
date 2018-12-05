package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddBayCapacity {

    public static void main(String[] args) throws IOException {
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Map<String,Double> sizes = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        reader.readLine();
        String line = reader.readLine();
        while (line!=null) {
            String[] parts = line.split(",");
            sizes.put(parts[0], 10.0/*Double.parseDouble(parts[1])*/);
            line = reader.readLine();
        }
        reader.close();
        String folder = args[1];
        new TransitScheduleReaderV2(scenario).readFile(folder + "/tp_PT_200m.xml");
        TransitSchedule transitSchedule = scenario.getTransitSchedule();
        for (TransitStopFacility stop : transitSchedule.getFacilities().values()){
            if (stop.getId().toString().startsWith("CC") || stop.getId().toString().startsWith("NS") || stop.getId().toString().startsWith("TE") || stop.getId().toString().startsWith("CE")) {
                continue;
            }
            Double size = sizes.get(stop.getId().toString());
            if(size!=null) {
                size*=0.8;
                if(size<10)
                    size = 10.0;
            }
            else
                size = 10.0;
            stop.getAttributes().putAttribute("capacity",size);
        }
        new TransitScheduleWriterV2(transitSchedule).write(folder + "/tp_PT_200m_bay_10.xml");
    }
}
