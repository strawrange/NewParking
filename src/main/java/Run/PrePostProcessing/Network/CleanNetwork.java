package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Collections;

public class CleanNetwork {
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network cleanNetwork = NetworkUtils.createNetwork();
        (new TransportModeNetworkFilter(scenario.getNetwork())).filter(cleanNetwork, Collections.singleton("car"));
        new NetworkCleaner().run(cleanNetwork);
        NetworkUtils.writeNetwork(cleanNetwork, "/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/cleanNetwork.xml");
    }

}
