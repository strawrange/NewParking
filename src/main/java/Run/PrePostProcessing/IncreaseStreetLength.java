package Run.PrePostProcessing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class IncreaseStreetLength {
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/";
    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(FOLDER + "mp_c_tp_2018.xml");
        for (Link link : network.getLinks().values()){
            if (link.getLength() < 20){
                link.setLength(20.0);
            }
        }
        new NetworkWriter(network).write(FOLDER + "mp_c_tp_min_length_20.xml");
    }
}
