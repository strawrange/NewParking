package Run.PrePostProcessing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PrepareNetworkForDRT {
    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        Config config2 = ConfigUtils.createConfig();
        Network network = ScenarioUtils.loadScenario(config).getNetwork();
        Network cleanNetwork = ScenarioUtils.loadScenario(config2).getNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        new NetworkReaderMatsimV2(cleanNetwork).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018_no_u_clean.xml");
        for (Link link: network.getLinks().values()){
            if (!cleanNetwork.getLinks().containsKey(link.getId())){
                ArrayList<String> collection = new ArrayList<>(link.getAllowedModes());
                collection.remove("car");
                link.setAllowedModes(new HashSet<>(collection));
            }
            if (link.getAllowedModes().size() == 1 && link.getAllowedModes().toArray()[0].equals("bus")){
                ArrayList<String> collection = new ArrayList<>(link.getAllowedModes());
                collection.add("car");
                link.setAllowedModes(new HashSet<>(collection));
            }
        }
        new NetworkWriter(network).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
    }
}
