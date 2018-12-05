package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV1;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;

public class OccupancyAnalysis {
    private static String FOLDER;
    private static String ITER = "40";
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {

        String[] parking = new String[]{"depot","roam","road"};
        String[] bay = new String[]{"bay","curb","infinity","single"};
//        for (String p:parking) {
//            for (String b: bay) {
                FOLDER = "/home/biyu/IdeaProjects/NewParking/output/charging/drt_mix_V450_T250_bay_nocharger_debug/";
                //FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/trb/" + p + "/" + b + "/";
                EVENTSFILE = FOLDER + "it.40/40.events.xml.gz";
                EventsManager manager = EventsUtils.createEventsManager();
                Network network = NetworkUtils.createNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018_old.xml");
        //new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
                OccupancyHandler handler = new OccupancyHandler(network);
                manager.addHandler(handler);
                new MatsimEventsReader(manager).readFile(EVENTSFILE);
                handler.output(FOLDER + ITER + "occupancy.csv");
                handler.distanceSummarize(FOLDER + ITER + "drt_distance.csv");
//            }
//        }
    }

}

class OccupancyHandler implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler,  LinkLeaveEventHandler {
    HashMap<Id<Vehicle>, ArrayList<Occ>> occupancy = new HashMap<>();
    Network network;
    double drtD = 0;
    double occDrtD = 0;
    double emptyDrtD = 0;
    double totalTT = 0;

    public OccupancyHandler(Network network){
        this.network = network;
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {

        if (event.getPersonId().toString().equals(event.getVehicleId().toString())){
            return;
        }
        if (!occupancy.containsKey(event.getVehicleId())){

            Occ initialO = new Occ(0,0);
            ArrayList<Occ> os = new ArrayList<>();
            os.add(initialO);
            occupancy.put(event.getVehicleId(),os);
        }
        ArrayList<Occ> os = occupancy.get(event.getVehicleId());
        Occ lastO = os.get(os.size() - 1);
        lastO.endT = event.getTime();
        double newOcc = lastO.occ + 1;
        Occ newO = new Occ(event.getTime(),newOcc);
        os.add(newO);

    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (event.getPersonId().toString().equals(event.getVehicleId().toString())){
            return;
        }
        ArrayList<Occ> os = occupancy.get(event.getVehicleId());
        Occ lastO = os.get(os.size() - 1);
        lastO.endT = event.getTime();
        double newOcc = lastO.occ - 1;
        Occ newO = new Occ(event.getTime(),newOcc);
        os.add(newO);
    }

    public void output(String file) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(file);
        bw.write("vehicleId;startTime;endTime;occ;distance");
        for (Id<Vehicle> vid: occupancy.keySet()){
            for (Occ occ: occupancy.get(vid)) {
                bw.newLine();
                bw.write(vid + ";" + occ.startT + ";" + occ.endT + ";" + occ.occ + ";" + occ.distance);
            }
        }
        bw.close();
    }


    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (!occupancy.containsKey(event.getVehicleId())){
            Occ initialO = new Occ(0,0);
            ArrayList<Occ> os = new ArrayList<>();
            os.add(initialO);
            occupancy.put(event.getVehicleId(),os);
        }
        ArrayList<Occ> occs = occupancy.get(event.getVehicleId());
        Occ occ = occs.get(occs.size() - 1);
        occ.distance = occ.distance + network.getLinks().get(event.getLinkId()).getLength();
        if (event.getVehicleId().toString().startsWith("drt")){
            drtD = drtD + network.getLinks().get(event.getLinkId()).getLength();
            if (occ.occ > 0){
                occDrtD = occDrtD + network.getLinks().get(event.getLinkId()).getLength();
            }else{
                emptyDrtD = emptyDrtD + network.getLinks().get(event.getLinkId()).getLength();
            }
        }
    }

    public void distanceSummarize(String file) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(file);
        bw.write("totalD;emptyD;occupiedD;totalTT");
        bw.newLine();
        bw.write(drtD + ";" + emptyDrtD + ";" + occDrtD);
        bw.close();
    }
}

class Occ{
    final double startT;
    double endT = 30*3600;
    final double occ;
    double distance = 0;

    public Occ(double start,  double occ){
        this.startT = start;
        this.occ = occ;
    }
}