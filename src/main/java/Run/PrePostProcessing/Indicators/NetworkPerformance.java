package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkPerformance {
    private static String FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/depot/demand_bay_only_av_remove_pvt_taxi/ITERS/";
    private static String ITER = "40";
    private static String EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
    public static void main(String[] args) throws IOException {
        EventsManager manager = EventsUtils.createEventsManager();
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        NetworkPerformanceAnalysis analyser = new NetworkPerformanceAnalysis(network);
        manager.addHandler(analyser);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        analyser.output(FOLDER + ITER + "networkPerformance.csv");
    }
}

class NetworkPerformanceAnalysis implements LinkEnterEventHandler, IndicatorModule {
    Map<Id<Link>, Integer>[] counter = new Map[30];
    String filename = "networkPerformance.csv";

    NetworkPerformanceAnalysis(Network network){
        for (int i = 0; i < counter.length; i++){
            counter[i] = new HashMap<>();
            for (Link link: network.getLinks().values()){
                counter[i].put(link.getId(),0);
            }
        }
    }

    public void output(String folder) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(folder + filename);
        bw.write("hour;linkid;numV");
        for (int i = 0; i < counter.length; i++){
            for (Id<Link> linkId: counter[i].keySet()){
                bw.newLine();
                bw.write(i + ";" + linkId + ";" + counter[i].get(linkId));
            }
        }
        bw.close();
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        int hour = (int) ((event.getTime() - 1.0)/3600);
        int value = counter[hour].get(event.getLinkId());
        value++;
        counter[hour].put(event.getLinkId(), value);
    }

    @Override
    public void reset(int iteration) {

    }
}
