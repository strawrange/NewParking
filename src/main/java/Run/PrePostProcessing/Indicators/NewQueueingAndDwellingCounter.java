package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//public class NewQueueAnalysis {
//    private static String FOLDER = "/home/biyu/Dropbox (engaging_mobility)/Team-Ordner „engaging_mobility“/bulky/trb/depot/infinity/ITERS/";
//    private static String ITER = "40";
//    private static String EVENTSFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".events.xml.gz";
//    public static void main(String[] args) throws IOException {
//        EventsManager manager = EventsUtils.createEventsManager();
//        NewQueueingAndDwellingCounter queueingAndDwellingCounter = new NewQueueingAndDwellingCounter();
//        manager.addHandler(queueingAndDwellingCounter);
//        new MatsimEventsReader(manager).readFile(EVENTSFILE);
//        queueingAndDwellingCounter.output(FOLDER + ITER + "newQAnalysis.csv");
//    }
//
//
//}
class NewQueueingAndDwellingCounter implements VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {
    private Map<Id<Vehicle>, ArrayList<NewQueueingAndDwellingRecorder>> counter = new HashMap<>();
    //private Map<Id<TransitStopFacility>, ArrayList<QCounter>> numOfQueue = new HashMap<>();
    private Map<Id<TransitStopFacility>, ArrayList<Event>> events = new HashMap<>();

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() == 0){
            return;
        }
        if (event.getPersonId().toString().startsWith("pt")){
            return;
        }
        if (event.getPersonId().toString().equals(event.getVehicleId().toString())){
            return;
        }
        NewQueueingAndDwellingRecorder queueingAndDwellingRecorder = counter.get(event.getVehicleId()).get(counter.get(event.getVehicleId()).size() - 1);
        if (queueingAndDwellingRecorder.dwellingStartTime == queueingAndDwellingRecorder.arrivalTime){
            queueingAndDwellingRecorder.dwellingStartTime = event.getTime();
        }
    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
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
        NewQueueingAndDwellingRecorder queueingAndDwellingRecorder = new NewQueueingAndDwellingRecorder(event.getTime());
        queueingAndDwellingRecorder.transitStopFacilityId = event.getFacilityId();
        if (!counter.containsKey(event.getVehicleId())) {
            counter.put(event.getVehicleId(), new ArrayList<>());
        }
        counter.get(event.getVehicleId()).add(queueingAndDwellingRecorder);
    }

    @Override
    public void handleEvent(VehicleDepartsAtFacilityEvent event) {
        if (event.getTime() == 0){
            return;
        }
        ArrayList<Event> e = events.get(event.getFacilityId());
        e.add(event);
        events.put(event.getFacilityId(), e);
        NewQueueingAndDwellingRecorder queueingAndDwellingRecorder = counter.get(event.getVehicleId()).get(counter.get(event.getVehicleId()).size() - 1);
        queueingAndDwellingRecorder.departureTime = event.getTime();
    }



    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if ( event.getTime() == 0){
            return;
        }
        if (event.getPersonId().toString().startsWith("pt")){
            return;
        }
        if (event.getPersonId().toString().equals(event.getVehicleId().toString())){
            return;
        }
        NewQueueingAndDwellingRecorder queueingAndDwellingRecorder = counter.get(event.getVehicleId()).get(counter.get(event.getVehicleId()).size() - 1);
        if (queueingAndDwellingRecorder.dwellingStartTime == queueingAndDwellingRecorder.arrivalTime){
            queueingAndDwellingRecorder.dwellingStartTime = event.getTime();
        }
    }



    public void output(String filename) throws IOException {
        Map<Id<TransitStopFacility>, int[]> queue = new HashMap<>();
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("time;facility;queue");
        for (Id<Vehicle> vid : counter.keySet()){
            for (NewQueueingAndDwellingRecorder q : counter.get(vid)){
                if (!queue.containsKey(q.transitStopFacilityId)){
                    int[] temp = new int[3600*30];
                    queue.put(q.transitStopFacilityId, temp);
                }
                int[] count = queue.get(q.transitStopFacilityId);
                for (int i = ((int)q.arrivalTime); i < q.dwellingStartTime - 3; i++){
                    count[i]++;
                }
                queue.put(q.transitStopFacilityId,count);
            }
        }
        for (int i = 0; i < 30*3600; i++){
            for (Id<TransitStopFacility> tid: queue.keySet()){
                bw.newLine();
                bw.write(i + ";" + tid + ";" + queue.get(tid)[i]);
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
            for (int time = 0; time < 30 * 3600; ){
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
}

class NewQueueingAndDwellingRecorder{
    public final double arrivalTime;
    public double dwellingStartTime;
    public double departureTime = 0;
    public Id<TransitStopFacility> transitStopFacilityId;
    public NewQueueingAndDwellingRecorder(double arrivalTime){
        this.arrivalTime = arrivalTime;
        this.dwellingStartTime = arrivalTime;
    }
}

class NewQCounter{
    private final double time;

    private final int q;
    public NewQCounter(double time, int q){
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
