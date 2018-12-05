package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Map;

import java.io.IOException;
import java.util.HashMap;

public class ChargingAnalysis {
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/output/charging/drt_charging_fullBattery/";
    private static String ITER = "40";
    private static String EVENTSFILE =  FOLDER +  ITER + ".events.xml.gz";
    public static void main(String[] args) throws IOException {
        EventsManager manager = EventsUtils.createEventsManager();
        ChargingEventHandler chargingEventHandler = new ChargingEventHandler();
        manager.addHandler(chargingEventHandler);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        chargingEventHandler.output(FOLDER + ITER + "chargingAnalysis.csv");
    }

}


class ChargingEventHandler implements ActivityStartEventHandler,ActivityEndEventHandler{
    Map<Id<Person>, ArrayList<Event>> qEvents = new HashMap<>();

    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals("DrtQueue") || event.getActType().equals("DrtCharge")){
            qEvents.get(event.getPersonId()).add(event);
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals("DrtQueue") || event.getActType().equals("DrtCharge")){
            if (!qEvents.containsKey(event.getPersonId())){
                qEvents.put(event.getPersonId(), new ArrayList<>());
            }
            qEvents.get(event.getPersonId()).add(event);
        }
    }

    public void output(String filename) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(filename);
        bw.write("vid;charge_num;queue_start;queue_end;charge_start;charge_end");
        for (Id<Person> pid: qEvents.keySet()){
            int num = 1;
            for (int i = 0; i < qEvents.get(pid).size() - 1; i++){
                if (qEvents.get(pid).get(i) instanceof ActivityStartEvent && ((ActivityStartEvent)qEvents.get(pid).get(i)).getActType().equals("DrtCharge")){

                    bw.newLine();
                    if (i-2 >= 0 && qEvents.get(pid).get(i - 2) instanceof ActivityStartEvent && ((ActivityStartEvent)qEvents.get(pid).get(i - 2)).getActType().equals("DrtQueue") && qEvents.get(pid).get(i - 1).getTime() == qEvents.get(pid).get(i).getTime()){
                        bw.write(pid + ";" + num + ";" + qEvents.get(pid).get(i - 2).getTime() + ";" + qEvents.get(pid).get(i - 1).getTime() + ";" +
                                qEvents.get(pid).get(i).getTime() + ";" + qEvents.get(pid).get(i + 1).getTime());
                    }else{
                        bw.write(pid + ";" + num + ";" + qEvents.get(pid).get(i).getTime() +";"+ qEvents.get(pid).get(i).getTime() + ";" +
                                qEvents.get(pid).get(i).getTime() + ";" + qEvents.get(pid).get(i + 1).getTime());
                    }
                    num++;
                }
            }
        }
        bw.close();
    }
}
