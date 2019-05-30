package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static Run.PrePostProcessing.Indicators.IndicatorsRun.FOLDER;


public class VehicleDistanceHandler implements IndicatorModule, LinkEnterEventHandler, PersonLeavesVehicleEventHandler, PersonEntersVehicleEventHandler {
    HashMap<Id<Vehicle>, ArrayList<VKT>> vkt = new HashMap<>();
    BufferedWriter pkt;
    HashMap<Id<Vehicle>, ArrayList<Id<Person>>> dictVP = new HashMap<>();
    HashMap<Id<Link>, Double> links = new HashMap<>();
    Network spiderNetwork = NetworkUtils.createNetwork();
    String filename = "vehicles.csv";

    public VehicleDistanceHandler(Network network, String outputPath) throws IOException {
        pkt = IOUtils.getBufferedWriter( outputPath + "pkt.csv");
        new MatsimNetworkReader(spiderNetwork).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190318_no_spider.xml");
        for (Id<Link> lid: network.getLinks().keySet()){
            if (spiderNetwork.getLinks().containsKey(lid)){
                links.put(lid, 0.D);
            }else{
                links.put(lid, network.getLinks().get(lid).getLength());
            }
        }
        pkt.write("pid;time;pkt;vid");
    }

    public void output(String outputPath) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("vid;time;vkt");
        for (Id<Vehicle> vid: vkt.keySet()){
            for (VKT v: vkt.get(vid)) {
                bw.newLine();
                bw.write(vid + ";" + v.time +";" + v.vkt);
            }
        }
        bw.close();
        pkt.close();
    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
            if (dictVP.containsKey(event.getVehicleId())) {
                for (Id<Person> pid : dictVP.get(event.getVehicleId())) {
                    try {
                        pkt.newLine();
                        pkt.write(pid + ";" + event.getTime() + ";" + links.get(event.getLinkId()) + ";" + event.getVehicleId());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!vkt.containsKey(event.getVehicleId())) {
                vkt.put(event.getVehicleId(), new ArrayList<>());
            }
            vkt.get(event.getVehicleId()).add(new VKT(event.getTime(), links.get(event.getLinkId())));
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
            if (!event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                dictVP.get(event.getVehicleId()).remove(event.getPersonId());
            }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
            if (!event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                if (!dictVP.containsKey(event.getVehicleId())) {
                    dictVP.put(event.getVehicleId(), new ArrayList<>());
                }
                dictVP.get(event.getVehicleId()).add(event.getPersonId());
            }
    }
}


class VKT{
    double time = 0.0;
    double vkt = 0.0;

    public VKT(double time, double vkt) {
        this.time = time;
        this.vkt = vkt;
    }
}