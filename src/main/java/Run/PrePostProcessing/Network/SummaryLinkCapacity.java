package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;

public class SummaryLinkCapacity {
    public static void main(String[] args) throws IOException {
        Config config = ConfigUtils.createConfig();
        Network network = ScenarioUtils.loadScenario(config).getNetwork();
       new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018_lowcap.xml");
        BufferedWriter bw = IOUtils.getBufferedWriter("linkCapacitySummary.csv");
        bw.write("link;capacity;numLanes;capacity_lane");
        for (Link link : network.getLinks().values()){
            bw.newLine();
            bw.write(link.getId().toString() + ";" + link.getCapacity() + ";" + link.getNumberOfLanes() + ";" + link.getCapacity() / link.getNumberOfLanes());
        }
        bw.close();
    }
}
