package Run.PrePostProcessing;

import Schedule.DrtActionCreator;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;
import org.matsim.api.core.v01.network.Link;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class QueueingAndDwellingTimeAnalysis {
    private static String FOLDER;
    private static String ITER = "0";
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {
        //double[] bay = new double[]{1,1.5,2,2.5};
        //for (double i:bay){
            //FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/HKSTS/Roam/tanjong_pagar_roam_max_v600_plans_"+ i +"/ITERS/";
            FOLDER = "/home/biyu/IdeaProjects/NewParking/output/drt_mix_V450_T250_bay_optimal_2/ITERS/";
            EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
            EventsManager manager = EventsUtils.createEventsManager();
            QueueingAndDwellingCounter queueingAndDwellingCounter = new QueueingAndDwellingCounter();
            manager.addHandler(queueingAndDwellingCounter);
            new MatsimEventsReader(manager).readFile(EVENTSFILE);
            queueingAndDwellingCounter.output(FOLDER + ITER + "queueingAndDwellingTimeAnalysis_28.csv");
            queueingAndDwellingCounter.outputSecond(FOLDER + ITER  + "queueingCounter.csv");
            queueingAndDwellingCounter.outputThird(FOLDER + ITER  + "DRTQ.csv");
        //}

    }
}

class QueueingAndDwellingCounter implements VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, ActivityStartEventHandler, ActivityEndEventHandler{
    private Map<Id<Vehicle>, ArrayList<QueueingAndDwellingRecorder>> counter = new HashMap<>();
    private Map<Id<Link>, ArrayList<Event>> numOfQueue = new HashMap<>();
    private Map<Id<TransitStopFacility>, ArrayList<Event>> events = new HashMap<>();

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() > 28 * 3600){
            return;
        }
        if (event.getTime() == 0){
            return;
        }
        if (event.getPersonId().toString().startsWith("pt")){
            return;
        }
        if (event.getPersonId().toString().equals(event.getVehicleId().toString())){
            return;
        }
        QueueingAndDwellingRecorder queueingAndDwellingRecorder = counter.get(event.getVehicleId()).get(counter.get(event.getVehicleId()).size() - 1);
        if (queueingAndDwellingRecorder.dwellingStartTime == queueingAndDwellingRecorder.arrivalTime){
            queueingAndDwellingRecorder.dwellingStartTime = event.getTime();
        }
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
        if (event.getTime() > 28 * 3600){
            return;
        }
        if (event.getTime() == 0){
            return;
        }
        if (!events.containsKey(event.getFacilityId())){
            ArrayList<Event> e = new ArrayList<>();
            e.add(event);
            events.put(event.getFacilityId(),e);
        }else {
            ArrayList<Event> e = events.get(event.getFacilityId());
            e.add(event);
            events.put(event.getFacilityId(), e);
        }
        QueueingAndDwellingRecorder queueingAndDwellingRecorder = new QueueingAndDwellingRecorder(event.getTime());
        queueingAndDwellingRecorder.transitStopFacilityId = event.getFacilityId();
        if (!counter.containsKey(event.getVehicleId())) {
            counter.put(event.getVehicleId(), new ArrayList<>());
        }
        counter.get(event.getVehicleId()).add(queueingAndDwellingRecorder);
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        if (event.getTime() > 28 * 3600){
            return;
        }
        if (event.getTime() == 0){
            return;
        }
        ArrayList<Event> e = events.get(event.getFacilityId());
        e.add(event);
        events.put(event.getFacilityId(), e);
        QueueingAndDwellingRecorder queueingAndDwellingRecorder = counter.get(event.getVehicleId()).get(counter.get(event.getVehicleId()).size() - 1);
        queueingAndDwellingRecorder.departureTime = event.getTime();
    }



    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (event.getTime() > 28 * 3600){
            return;
        }
        if ( event.getTime() == 0){
            return;
        }
        if (event.getPersonId().toString().startsWith("pt")){
            return;
        }
        if (event.getPersonId().toString().equals(event.getVehicleId().toString())){
            return;
        }
        QueueingAndDwellingRecorder queueingAndDwellingRecorder = counter.get(event.getVehicleId()).get(counter.get(event.getVehicleId()).size() - 1);
        if (queueingAndDwellingRecorder.dwellingStartTime == queueingAndDwellingRecorder.arrivalTime){
            queueingAndDwellingRecorder.dwellingStartTime = event.getTime();
        }
    }



    public void output(String filename) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("vehicleId;arrivalTime;dwellingStartTime;departureTime;transitStopFacilityId");
        for (Id<Vehicle> vid : counter.keySet()){
            for (QueueingAndDwellingRecorder q : counter.get(vid)){
                bw.newLine();
                bw.write(vid.toString() + ";" + q.arrivalTime  + ";" + q.dwellingStartTime + ";" + q.departureTime + ";" + q.transitStopFacilityId.toString());
            }
        }
        bw.close();
    }

    public void outputSecond(String filename) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("time;facility;count;duration");
        for (Id<TransitStopFacility> sid: events.keySet()){
            ArrayList<Event> q = events.get(sid);
            int i = 0;
            int count = 0;
            double last = 0;
            for (int time = 0; time <= 28 * 3600; ){
                bw.newLine();
                if (i < q.size() && time == q.get(i).getTime()) {
                    bw.write(time + ";" + sid + ";" + count + ";" + (time - last));
                    if (q.get(i) instanceof VehicleArrivesAtFacilityEvent){
                        count++;
                    }
                    if (q.get(i) instanceof VehicleDepartsAtFacilityEvent){
                        count--;
                    }
                    i++;
                    last = time;
                }else{
                    time++;
                }
            }
            bw.newLine();
            bw.write(30 * 3600 + ";" + sid + ";" + count + ";" + (30 * 3600 - last));
        }
    }

    public void outputThird(String filename) throws IOException{
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("time;linkId;queue");
        for (Id<Link> lid: numOfQueue.keySet()){
            int count = 0;
            for (Event event: numOfQueue.get(lid)){
                bw.newLine();
                if (event instanceof ActivityStartEvent){
                    count++;
                }else if (event instanceof  ActivityEndEvent){
                    count--;
                }
                bw.write(event.getTime() + ";" + lid + ";" + count);
            }
            bw.newLine();
            bw.write(3600*30 + ";" + lid + ";" + count);
        }
        bw.close();
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals("DrtQueue")){
            numOfQueue.get(event.getLinkId()).add(event);
        }

    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals("DrtQueue")){
            if (!numOfQueue.containsKey(event.getLinkId())){
                ArrayList<Event> events = new ArrayList<>();
                events.add(event);
                numOfQueue.put(event.getLinkId(),events);
            }
            numOfQueue.get(event.getLinkId()).add(event);
        }
    }
}

class QueueingAndDwellingRecorder{
    public final double arrivalTime;
    public double dwellingStartTime;
    public double departureTime = 0;
    public Id<TransitStopFacility> transitStopFacilityId;
    public QueueingAndDwellingRecorder(double arrivalTime){
        this.arrivalTime = arrivalTime;
        this.dwellingStartTime = arrivalTime;
    }
}

class QCounter{
    private final double time;

    private final int q;
    public QCounter(double time, int q){
        this.time = time;
        this.q = q;
    }
    public int getQ() {
        return q;
    }
    public double getTime() {
        return time;
    }


}


