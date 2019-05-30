package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.core.api.experimental.events.AgentWaitingForPtEvent;
import org.matsim.core.api.experimental.events.handler.AgentWaitingForPtEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;
import org.matsim.core.router.util.TravelTime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static Run.PrePostProcessing.Indicators.IndicatorsRun.END_TIME;


class NewTravelTimeHandler implements IndicatorModule, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, PersonDepartureEventHandler, PersonArrivalEventHandler, ActivityStartEventHandler,
        ActivityEndEventHandler{

    HashMap<Id<Person>, WaitTime> wt = new HashMap<>();
    HashMap<Id<Person>, InVehicleTime> ivvt = new HashMap<>();
    HashMap<Id<Person>, TransitWalkTime> transitWalk = new HashMap<>();
    HashMap<Id<Person>, NewTotalTime> totalT = new HashMap<>();
    HashMap<Id<Person>, ArrayList<Event>> accessEgress = new HashMap<>();
    HashMap<Id<Person>, ArrayList<Event>> MRTaccessEgress = new HashMap<>();
    Population population;
    Network network;
    TransitSchedule transitSchedule;
    HashMap<Id<Link>, TransitStopFacility> dict = new HashMap<>();
    String filename = "travel_time_mot.csv";


    public NewTravelTimeHandler(Network network, TransitSchedule transitSchedule, Population population) throws IOException {
        this.network = network;
        this.transitSchedule = transitSchedule;
        this.population = population;
        for (TransitStopFacility stop : transitSchedule.getFacilities().values()){
            dict.put(stop.getLinkId(),stop);
        }
        for (Link l: network.getLinks().values()){
            if (l.getId().toString().endsWith("_HW")){
                double nLanes = l.getNumberOfLanes();
                Id<Link> newLinkId = Id.createLinkId(l.getId().toString().replaceAll("_HW",""));
                network.getLinks().get(newLinkId).setNumberOfLanes(network.getLinks().get(newLinkId).getNumberOfLanes() + nLanes);
            }
            if (l.getId().toString().startsWith("nl")){
                double nLanes = l.getNumberOfLanes();
                Id<Link> newLinkId = Id.createLinkId(l.getId().toString().replaceAll("nl",""));
                network.getLinks().get(newLinkId).setNumberOfLanes(network.getLinks().get(newLinkId).getNumberOfLanes() + nLanes);
            }
        }
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
            if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt") && !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                WaitTime t = wt.get(event.getPersonId());
                t.enterVehicleT = event.getTime();
                t.waitT = t.waitT + t.enterVehicleT - t.arrivalT;
                t.vid = event.getVehicleId();
                if (!ivvt.containsKey(event.getPersonId())) {
                    ivvt.put(event.getPersonId(), new InVehicleTime(event.getTime()));
                } else {
                    ivvt.get(event.getPersonId()).enterVehicleT = event.getTime();
                }
                ivvt.get(event.getPersonId()).vid = event.getVehicleId();
            }
        }


    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
            if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt") && !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                InVehicleTime t = ivvt.get(event.getPersonId());
                t.leaveVehicleT = event.getTime();
                t.inVehicleT = t.inVehicleT + t.leaveVehicleT - t.enterVehicleT;
            }
    }

    public void output(String outputPath) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("personId;fromLink;toLink;departureT;firstMode;arrivalT;lastMode;transitWalkT;waitT;inVehicleT;vid;mode;plantype;access;stopA;egress;stopE;accessMRT;egressMRT");
        for (Id<Person> pid : totalT.keySet()){
            bw.newLine();
            bw.write(pid.toString() + ";" + totalT.get(pid).departureL + ";" + totalT.get(pid).arrivalL + ";" + totalT.get(pid).departureT + ";" + totalT.get(pid).firstMode +";" +
                    totalT.get(pid).arrivalT + ";" + totalT.get(pid).lastMode + ";");
            if (transitWalk.containsKey(pid)){
                bw.write(transitWalk.get(pid).transitWalkT + ";");
            }else{
                bw.write(";");
            }
            if (wt.containsKey(pid)){
                bw.write(wt.get(pid).waitT + ";");
            }else{
                bw.write(";");
            }
            if (ivvt.containsKey(pid)){
                bw.write(ivvt.get(pid).inVehicleT + ";" + ivvt.get(pid).vid + ";");
            }else{
                bw.write(";;");
            }
            bw.write( totalT.get(pid).mode + ";" + population.getPersons().get(pid).getAttributes().getAttribute("type") + ";");
            if (accessEgress.containsKey(pid)){
                int size = accessEgress.get(pid).size();
                PersonDepartureEvent departureEvent = (PersonDepartureEvent) accessEgress.get(pid).get(0);
                TransitStopFacility accessStop = dict.get(departureEvent.getLinkId());
                if (isMRT(accessStop)){
                    bw.write(departureEvent.getTime() + ";MRT;");
                }else{
                    bw.write(departureEvent.getTime() + ";bus;");
                }

                if (accessEgress.get(pid).get(size - 1) instanceof PersonArrivalEvent) {
                    PersonArrivalEvent arrivalEvent = (PersonArrivalEvent) accessEgress.get(pid).get(size - 1);
                    TransitStopFacility egressStop = dict.get(arrivalEvent.getLinkId());
                    if (isMRT(egressStop)) {
                        bw.write(arrivalEvent.getTime() + ";MRT");
                    } else {
                        bw.write( arrivalEvent.getTime() + ";bus" );
                    }
                }
            }
            if (MRTaccessEgress.containsKey(pid)){
                int size = MRTaccessEgress.get(pid).size();
                PersonDepartureEvent departureEvent = (PersonDepartureEvent) MRTaccessEgress.get(pid).get(0);
                bw.write(";" + departureEvent.getTime());
                if (accessEgress.get(pid).get(size - 1) instanceof PersonArrivalEvent) {
                    PersonArrivalEvent arrivalEvent = (PersonArrivalEvent) accessEgress.get(pid).get(size - 1);
                    bw.write(";" + arrivalEvent.getTime());
                }
            }
        }
        bw.close();
    }

    private boolean isMRT(TransitStopFacility stop) {
        return stop.getId().toString().startsWith("CC") || stop.getId().toString().startsWith("NS") || stop.getId().toString().startsWith("TE") || stop.getId().toString().startsWith("CE");
    }


    @Override
    public void handleEvent(PersonArrivalEvent event) {
            if (event.getLegMode().equals(TransportMode.transit_walk)) {
                TransitWalkTime acc = transitWalk.get(event.getPersonId());
                acc.arrivalT = event.getTime();
                acc.transitWalkT = acc.transitWalkT + acc.arrivalT - acc.departureT;
                if (!wt.containsKey(event.getPersonId())) {
                    wt.put(event.getPersonId(), new WaitTime(event.getTime()));
                } else {
                    wt.get(event.getPersonId()).arrivalT = event.getTime();
                }
            }
            if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt")) {
                accessEgress.get(event.getPersonId()).add(event);
                if (isMRT(dict.get(event.getLinkId()))){
                    MRTaccessEgress.get(event.getPersonId()).add(event);
                }
            }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
            if (event.getLegMode().equals(TransportMode.transit_walk)) {
                if (!transitWalk.containsKey(event.getPersonId())) {
                    transitWalk.put(event.getPersonId(), new TransitWalkTime(event.getTime()));
                } else {
                    transitWalk.get(event.getPersonId()).departureT = event.getTime();
                    transitWalk.get(event.getPersonId()).arrivalT = END_TIME;
                }
            }
            if (totalT.containsKey(event.getPersonId())) {
                totalT.get(event.getPersonId()).lastMode = event.getLegMode();
                if (totalT.get(event.getPersonId()).firstMode == null) {
                    totalT.get(event.getPersonId()).firstMode = event.getLegMode();
                }
                if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt") || event.getLegMode().equals("drtaxi")) {
                    totalT.get(event.getPersonId()).mode = TransportMode.pt;
                    if (!wt.containsKey(event.getPersonId())) {
                        wt.put(event.getPersonId(), new WaitTime(event.getTime()));
                    }

                    if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt")) {
                        if (!accessEgress.containsKey(event.getPersonId())) {
                            accessEgress.put(event.getPersonId(), new ArrayList<>());
                        }
                        accessEgress.get(event.getPersonId()).add(event);
                        if (isMRT(dict.get(event.getLinkId()))){
                            if (!MRTaccessEgress.containsKey(event.getPersonId())) {
                                MRTaccessEgress.put(event.getPersonId(), new ArrayList<>());
                            }
                            MRTaccessEgress.get(event.getPersonId()).add(event);
                        }
                    }
                } else if (event.getLegMode().equals("pvt")) {
                    totalT.get(event.getPersonId()).mode = TransportMode.car;
                } else if (event.getLegMode().equals(TransportMode.walk)) {
                    totalT.get(event.getPersonId()).mode = TransportMode.walk;
                } else if (event.getLegMode().equals(TransportMode.taxi)) {
                    totalT.get(event.getPersonId()).mode = TransportMode.taxi;
                } else {
                    if (!event.getLegMode().equals(TransportMode.transit_walk)) {
                        System.out.println();
                    }
                }
            }

    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
            if (event.getActType().equals("dummy")) {
                if (!totalT.containsKey(event.getPersonId())) {
                    totalT.put(event.getPersonId(), new NewTotalTime(event.getTime(), event.getLinkId()));
                }
            }
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
            if (event.getActType().equals("dummy")) {
                if (totalT.containsKey(event.getPersonId())) {
                    if (event.getTime() != totalT.get(event.getPersonId()).departureT) {
                        totalT.get(event.getPersonId()).arrivalT = event.getTime();
                        totalT.get(event.getPersonId()).arrivalL = event.getLinkId();
                    } else {
                        totalT.remove(event.getPersonId());
                    }
                }
            }
    }


    }


class WaitTime{
    Id<Vehicle> vid;
    double arrivalT;
    double enterVehicleT = END_TIME;
    double waitT;

    public WaitTime(double arrivalT){
        this.arrivalT = arrivalT;
    }
}

class InVehicleTime{
    Id<Vehicle> vid;
    double inVehicleT;
    double enterVehicleT;
    double leaveVehicleT = END_TIME;

    public InVehicleTime(double enterVehicleT){
        this.enterVehicleT = enterVehicleT;
    }
}

class TransitWalkTime{
    double departureT;
    double arrivalT = END_TIME;
    double transitWalkT;
    public TransitWalkTime(double departureT){
        this.departureT = departureT;
    }
}

class NewTotalTime{
    double departureT;
    double arrivalT = Double.POSITIVE_INFINITY;
    Id<Link> departureL;
    Id<Link> arrivalL;
    String mode = "";
    String firstMode = null;
    String lastMode=null;
    public NewTotalTime(double departureT, Id<Link> departureL){
        this.departureT = departureT;
        this.departureL =departureL;
    }
}







