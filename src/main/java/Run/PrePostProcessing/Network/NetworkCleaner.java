package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NetworkCleaner {


    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork();
        Network drtNetwork = NetworkUtils.createNetwork();
        Network pvtNetwork = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190220.xml");
        new TransportModeNetworkFilter(network).filter(drtNetwork, Collections.singleton("car"));
        new TransportModeNetworkFilter(network).filter(pvtNetwork, Collections.singleton("pvt"));
        new org.matsim.core.network.algorithms.NetworkCleaner().run(drtNetwork);
        new org.matsim.core.network.algorithms.NetworkCleaner().run(pvtNetwork);
        for (Link link: network.getLinks().values()){
            if (link.getAllowedModes().contains("pvt") && !pvtNetwork.getLinks().containsKey(link.getId())){
                Set<String> newModes = new HashSet<>();
                for (String mode: link.getAllowedModes()){
                    if (!mode.equals("pvt") ){
                        newModes.add(mode);
                    }
                }
                link.setAllowedModes(newModes);
            }
            if (link.getAllowedModes().contains("taxi") && !pvtNetwork.getLinks().containsKey(link.getId())){
                Set<String> newModes = new HashSet<>();
                for (String mode: link.getAllowedModes()){
                    if (!mode.equals("taxi") ){
                        newModes.add(mode);
                    }
                }
                link.setAllowedModes(newModes);
            }
            if (link.getAllowedModes().contains("car") && !drtNetwork.getLinks().containsKey(link.getId())){
                Set<String> newModes = new HashSet<>();
                for (String mode: link.getAllowedModes()){
                    if (!mode.equals("car")){
                        newModes.add(mode);
                    }
                }
                link.setAllowedModes(newModes);
            }
        }
        new NetworkWriter(network).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190220_clean.xml");
    }

}
