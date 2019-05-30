package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.io.NetworkChangeEventsParser;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.matsim.api.core.v01.network.Link;

public class NetworkChangeEventAnalyser {
    private static String FOLDER;
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {
        FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/drt_mix_V450_T250_bay_nocharger_debug/";
        EVENTSFILE = FOLDER + "output_change_events.xml.gz";
        Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
        EventsManager manager = EventsUtils.createEventsManager();
        Network network = scenario.getNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        List<NetworkChangeEvent> events = new ArrayList<>();
        new NetworkChangeEventsParser(network, events).readFile(EVENTSFILE);
        BufferedWriter bw = IOUtils.getBufferedWriter(FOLDER + "network_change_analysis.csv");
        bw.write("link;startTime;flowCapacityChange;capacity;laneCapacityChange;numLane");
        for (NetworkChangeEvent event: events){
            for (Link link: event.getLinks()) {
                bw.newLine();
                bw.write(link.getId().toString() + ";" + event.getStartTime() + ";" + event.getFlowCapacityChange().getValue() + ";" + link.getCapacity() / 3600.0 + ";" + event.getLanesChange().getValue() + ";" + link.getNumberOfLanes() );
            }
        }
        bw.close();
    }
}
