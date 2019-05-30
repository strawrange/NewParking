package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;

public class IncreaseStreetCapacityForLowCapLinks {
    public static void main(String[] args) {
        double lb = 750.0;
        Config config = ConfigUtils.createConfig();
        Network network = ScenarioUtils.loadScenario(config).getNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018_lowcap.xml");
        for (Link link : network.getLinks().values()){
            if (link.getCapacity() / link.getNumberOfLanes() < lb){
                link.setCapacity(lb * link.getNumberOfLanes());
            }
        }
        new NetworkWriter(network).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
    }
}
