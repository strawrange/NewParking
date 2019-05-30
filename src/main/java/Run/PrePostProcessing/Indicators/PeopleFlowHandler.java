package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonStuckEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.AgentWaitingForPtEvent;
import org.matsim.core.api.experimental.events.handler.AgentWaitingForPtEventHandler;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PeopleFlowHandler implements IndicatorModule, BasicEventHandler, PersonEntersVehicleEventHandler, AgentWaitingForPtEventHandler, PersonStuckEventHandler{
    HashMap<Id<Person>, ArrayList<PassengerWait>> passengerWaits = new HashMap<>();
    HashMap<Id<Link>, ArrayList<Id<Person>>> peopleFLow = new HashMap<>();
    HashMap<Id<Link>, Integer> maxPeopleFlow = new HashMap<>();
    String filename = "people_flow.csv";
    TransitSchedule transitSchedule;

    public PeopleFlowHandler(TransitSchedule transitSchedule){
        this.transitSchedule = transitSchedule;
    }

//    public void output(String outputPath) throws IOException {
//        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
//        bw.write("pid;lid;start_time;end_time");
//        for (Id<Person> pid: passengerWaits.keySet()){
//            for (PassengerWait p:passengerWaits.get(pid)) {
//                if (p.linkid.toString().equals("1506-24598")) {
//                    bw.newLine();
//                    bw.write(pid + ";" + p.linkid + ";" + p.startT + ";" + p.endT);
//                }
//            }
//        }
//    }

    public void output(String outputPath) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("lid;max");
        for (Id<Link> lid: maxPeopleFlow.keySet()){
            bw.newLine();
            bw.write(lid + ";" + maxPeopleFlow.get(lid));
        }
        bw.close();
    }
    @Override
    public void handleEvent(Event event) {
        if (event.getTime() <= IndicatorsRun.END_TIME) {
            if (event.getEventType().equals("DrtRequest submitted")) {
                Id<Link> lid = Id.createLinkId(event.getAttributes().get("fromLink"));
                Id<Person> pid = Id.createPersonId(event.getAttributes().get("person"));
                if (!passengerWaits.containsKey(pid)) {
                    passengerWaits.put(pid, new ArrayList<>());
                }
                PassengerWait passengerWait = new PassengerWait();
                passengerWait.startT = event.getTime();
                passengerWait.linkid = lid;
                passengerWaits.get(pid).add(passengerWait);
                if (!peopleFLow.containsKey(lid)) {
                    peopleFLow.put(lid, new ArrayList<>());
                    maxPeopleFlow.put(lid,0);
                }
                peopleFLow.get(lid).add(pid);
                if (peopleFLow.get(lid).size() > maxPeopleFlow.get(lid)) {
                    if(peopleFLow.get(lid).size() == 1120){
                        System.out.println();
                    }
                    maxPeopleFlow.put(lid, peopleFLow.get(lid).size() );
                }
            }
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() <= IndicatorsRun.END_TIME) {
            if (passengerWaits.containsKey(event.getPersonId())) {
                passengerWaits.get(event.getPersonId()).get(passengerWaits.get(event.getPersonId()).size() - 1).endT = event.getTime();
                if(!peopleFLow.get(passengerWaits.get(event.getPersonId()).get(passengerWaits.get(event.getPersonId()).size() - 1).linkid).remove(event.getPersonId())){
                    System.out.println();
                }
            }
        }
    }


    @Override
    public void handleEvent(AgentWaitingForPtEvent event) {
        if (event.getTime() <= IndicatorsRun.END_TIME) {
            Id<Link> lid = transitSchedule.getFacilities().get(event.getWaitingAtStopId()).getLinkId();
            if (!passengerWaits.containsKey(event.getPersonId())) {
                passengerWaits.put(event.getPersonId(), new ArrayList<>());
            }
            PassengerWait passengerWait = new PassengerWait();
            passengerWait.startT = event.getTime();
            passengerWait.linkid = lid;
            passengerWaits.get(event.getPersonId()).add(passengerWait);
            if (!peopleFLow.containsKey(lid)) {
                peopleFLow.put(lid, new ArrayList<>());
                maxPeopleFlow.put(lid,0);
            }
            peopleFLow.get(lid).add(event.getPersonId());
            if (peopleFLow.get(lid).size() > maxPeopleFlow.get(lid)) {
                if(peopleFLow.get(lid).size() == 1120){
                    System.out.println();
                }
                maxPeopleFlow.put(lid, peopleFLow.get(lid).size() );
            }
        }
    }

    @Override
    public void handleEvent(PersonStuckEvent event) {
        if (event.getTime() <= IndicatorsRun.END_TIME) {
            if (peopleFLow.containsKey(event.getLinkId())) {
                if (!peopleFLow.get(event.getLinkId()).remove(event.getPersonId())) {
                    System.out.println();
                }
            }
        }
    }
}
class PassengerWait{
    Id<Link> linkid;
    double startT = 0.0;
    double endT = IndicatorsRun.END_TIME;
}
