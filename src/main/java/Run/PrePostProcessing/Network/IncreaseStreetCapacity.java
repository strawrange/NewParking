package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class IncreaseStreetCapacity{
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/drt_example/drtconfig_stopbased.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        for (Link link : network.getLinks().values()){
            if (link.getNumberOfLanes() == 1){
                link.setNumberOfLanes(2);
                link.setCapacity(link.getCapacity() * 2);
            }
        }
        new NetworkWriter(network).write("/home/biyu/IdeaProjects/NewParking/scenarios/drt_example/network_2lanes.xml.gz");
    }
}
