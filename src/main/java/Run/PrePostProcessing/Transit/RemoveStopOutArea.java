package Run.PrePostProcessing.Transit;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopArea;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class RemoveStopOutArea {
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/";

    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/stops_in_area.txt"));
        String line = reader.readLine();
        Set<Id<TransitStopFacility>> ids = new HashSet<>();
        while(line!=null) {
            if (line.hashCode() != 0) {
                ids.add(Id.create(line, TransitStopFacility.class));
            }
            line = reader.readLine();
        }
        reader.close();

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018_no_u_clean.xml");
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "mp_c_tp_PT_2018.xml");
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/stops_in_area.txt"));
        for(Id<TransitStopFacility> id : ids) {
            TransitStopFacility stop = scenario.getTransitSchedule().getFacilities().get(id);
            if (network.getLinks().containsKey(stop.getLinkId())){
                bw.write(id.toString());
                bw.newLine();
                bw.newLine();
            }
        }
        bw.close();
    }
}
