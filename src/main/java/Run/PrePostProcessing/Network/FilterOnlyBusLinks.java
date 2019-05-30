package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.api.core.v01.network.Link;

public class FilterOnlyBusLinks {
    public static String FOLDER;
    public static String ITER = "40";
    private static String EVENTSFILE;

    public static void main(String[] args) {
        FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/demand_bay_mix/ITERS/" ;
        EVENTSFILE = FOLDER + "it." + ITER + "/" + ITER + ".events.xml.gz";
        Network network = NetworkUtils.createNetwork();
        EventsManager manager = EventsUtils.createEventsManager();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190220_clean.xml");
        LinkFilterHandler linkFilterHandler = new LinkFilterHandler(network);
        manager.addHandler(linkFilterHandler);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        new NetworkWriter(network).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190318_no_spider.xml");
    }

}

class LinkFilterHandler implements LinkLeaveEventHandler, LinkEnterEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {
    Network network;

    public LinkFilterHandler(Network network) {
        this.network = network;
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (event.getVehicleId().toString().startsWith("drt") || event.getVehicleId().toString().contains("car") || event.getVehicleId().toString().contains("taxi") ||
                (network.getLinks().containsKey(event.getLinkId()) && network.getLinks().get(event.getLinkId()).getAllowedModes().contains("subway"))){
            network.removeLink(event.getLinkId());
            if (event.getLinkId().toString().endsWith("HW")){
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("_HW",""));
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }else{
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString() + "_HW");
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }
        }
        if (event.getLinkId().toString().startsWith("nl")){
            Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("nl",""));
            network.removeLink(newLinkId);
        }
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (event.getVehicleId().toString().startsWith("drt") || event.getVehicleId().toString().contains("car") || event.getVehicleId().toString().contains("taxi") ||
                (network.getLinks().containsKey(event.getLinkId()) && network.getLinks().get(event.getLinkId()).getAllowedModes().contains("subway"))){
            network.removeLink(event.getLinkId());
            if (event.getLinkId().toString().endsWith("HW")){
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("_HW",""));
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }else{
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString() + "_HW");
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }
        }
        if (event.getLinkId().toString().startsWith("nl")){
            Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("nl",""));
            network.removeLink(newLinkId);
        }
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        if (event.getVehicleId().toString().startsWith("drt") || event.getVehicleId().toString().contains("car") || event.getVehicleId().toString().contains("taxi") ||
                (network.getLinks().containsKey(event.getLinkId()) && network.getLinks().get(event.getLinkId()).getAllowedModes().contains("subway"))){
            network.removeLink(event.getLinkId());
            if (event.getLinkId().toString().endsWith("HW")){
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("_HW",""));
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }else{
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString() + "_HW");
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }
        }
        if (event.getLinkId().toString().startsWith("nl")){
            Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("nl",""));
            network.removeLink(newLinkId);
        }
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        if (event.getVehicleId().toString().startsWith("drt") || event.getVehicleId().toString().contains("car") || event.getVehicleId().toString().contains("taxi") ||
                (network.getLinks().containsKey(event.getLinkId()) && network.getLinks().get(event.getLinkId()).getAllowedModes().contains("subway"))){
            network.removeLink(event.getLinkId());
            if (event.getLinkId().toString().endsWith("HW")){
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("_HW",""));
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }else{
                Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString() + "_HW");
                if(network.getLinks().containsKey(newLinkId)){
                    network.removeLink(newLinkId);
                }
            }
        }
        if (event.getLinkId().toString().startsWith("nl")){
            Id<Link> newLinkId = Id.createLinkId(event.getLinkId().toString().replaceAll("nl",""));
            network.removeLink(newLinkId);
        }
    }
}
