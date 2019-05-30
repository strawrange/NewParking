package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.api.core.v01.network.Link;

import java.util.Collections;

public class CompareNetwork {
    public static void main(String[] args) {
        Network oldNetwork = NetworkUtils.createNetwork();
        Network network = NetworkUtils.createNetwork();
        new NetworkReaderMatsimV2(oldNetwork).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/output_network.xml.gz");
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190220_clean.xml");
        /*Network oldNetworkPVT = NetworkUtils.createNetwork();
        Network networkPVT = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(oldNetwork).filter(oldNetworkPVT, Collections.singleton("pvt"));
        new TransportModeNetworkFilter(network).filter(networkPVT, Collections.singleton("pvt"));
        new NetworkCleaner().run(oldNetworkPVT);
        new NetworkCleaner().run(networkPVT);*/
        for (Link link: network.getLinks().values()){
            for(String mode:link.getAllowedModes())
                if (!oldNetwork.getLinks().get(link.getId()).getAllowedModes().contains(mode)){
                    System.out.println(link.getId()+" "+mode);
                }
        }
    }
}
