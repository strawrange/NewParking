package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class StageTimeHandler implements IndicatorModule, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, PersonArrivalEventHandler, PersonDepartureEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {
    Network network;
    HashMap<Id<Person>,ArrayList<Event>> stageTime = new HashMap<>();
    HashMap<Id<Vehicle>, ArrayList<Id<Person>>> dict = new HashMap<>();
    HashMap<Id<Vehicle>, VehicleLeavesTrafficEvent> leaves = new HashMap<>();
    TravelTimeCalculator ttcalculator;
    String filename = "stage_travel_time_mot.csv";

    public StageTimeHandler(TravelTimeCalculator ttcalculator, Network network){
        this.ttcalculator = ttcalculator;
        this.network = network;
    }

    public void output(String outputPath) throws IOException {
        Network dvrpNetwork = NetworkUtils.createNetwork();
        LeastCostPathCalculator leastCostPathCalculator = new DijkstraFactory(false).createPathCalculator(dvrpNetwork, new TimeAsTravelDisutility(ttcalculator.getLinkTravelTimes()), ttcalculator.getLinkTravelTimes());
        Set<String> allowModes = new HashSet<>();
        allowModes.add("car");
        allowModes.add("pvt");
        new TransportModeNetworkFilter(network).filter(dvrpNetwork, allowModes);
        new NetworkCleaner().run(dvrpNetwork);
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("pid;vid;numT;departureT;enterVT;enterTraffic;mode;leaveTraffic;leaveVT;equivalentT");
        for (Id<Person> pid : stageTime.keySet()) {
            int numT = 1;
            for (int i = 0; i + 2 < stageTime.get(pid).size();) {
                if (((PersonDepartureEvent) stageTime.get(pid).get(i)).getLegMode().startsWith("drt")) {
                    bw.newLine();
                    bw.write(pid + ";" + ((PersonEntersVehicleEvent) stageTime.get(pid).get(i + 1)).getVehicleId() + ";" + numT + ";" + stageTime.get(pid).get(i).getTime() + ";" + stageTime.get(pid).get(i + 1).getTime() + ";" +
                            stageTime.get(pid).get(i + 2).getTime() + ";" + ((PersonDepartureEvent) stageTime.get(pid).get(i)).getLegMode());
                    if (i + 5 < stageTime.get(pid).size()) {
                        Id<Link> fromLink = ((PersonDepartureEvent) stageTime.get(pid).get(i)).getLinkId();
                        Id<Link> toLink = ((PersonArrivalEvent) stageTime.get(pid).get(i + 5)).getLinkId();
                        if (dvrpNetwork.getLinks().get(fromLink) == null || dvrpNetwork.getLinks().get(toLink) == null) {
                            bw.write(";" + stageTime.get(pid).get(i + 2).getTime() + ";" + 0.0);
                        } else {
                            double ett = leastCostPathCalculator.calcLeastCostPath(
                                    dvrpNetwork.getLinks().get(fromLink).getToNode(),
                                    dvrpNetwork.getLinks().get(toLink).getFromNode(),
                                    stageTime.get(pid).get(i).getTime(), null, null).travelTime +
                                    ttcalculator.getLinkTravelTimes().getLinkTravelTime(dvrpNetwork.getLinks().get(toLink), stageTime.get(pid).get(i + 2).getTime(), null, null);
                            bw.write(";" + stageTime.get(pid).get(i + 3).getTime() + ";" + stageTime.get(pid).get(i + 4).getTime() + ";" + ett);
                        }
                    }
                    i = i + 6;
                }else{
                    bw.newLine();
                    bw.write(pid + ";" + ((PersonEntersVehicleEvent) stageTime.get(pid).get(i + 1)).getVehicleId() + ";" + numT + ";" + stageTime.get(pid).get(i).getTime() + ";" + stageTime.get(pid).get(i + 1).getTime() + ";" +
                            "" + ";" + ((PersonDepartureEvent) stageTime.get(pid).get(i)).getLegMode());
                    if (i + 3 < stageTime.get(pid).size()) {
                        Id<Link> fromLink = ((PersonDepartureEvent) stageTime.get(pid).get(i)).getLinkId();
                        Id<Link> toLink = ((PersonArrivalEvent) stageTime.get(pid).get(i + 3)).getLinkId();
                        if (dvrpNetwork.getLinks().get(fromLink) == null || dvrpNetwork.getLinks().get(toLink) == null) {
                            bw.write(";" + stageTime.get(pid).get(i + 2).getTime() + ";" + 0.0);
                        } else {
                            double ett = leastCostPathCalculator.calcLeastCostPath(
                                    dvrpNetwork.getLinks().get(fromLink).getToNode(),
                                    dvrpNetwork.getLinks().get(toLink).getFromNode(),
                                    stageTime.get(pid).get(i).getTime(), null, null).travelTime +
                                    ttcalculator.getLinkTravelTimes().getLinkTravelTime(dvrpNetwork.getLinks().get(toLink), stageTime.get(pid).get(i + 1).getTime(), null, null);
                            bw.write(";" + ""+ ";" + stageTime.get(pid).get(i + 3).getTime() + ";" + ett);
                        }
                    }
                    i = i + 4;
                }
                numT++;
            }

        }
        bw.close();

    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (stageTime.containsKey(event.getPersonId())) {
            if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt") && !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                stageTime.get(event.getPersonId()).add(event);
                if (!dict.containsKey(event.getVehicleId())){
                    dict.put(event.getVehicleId(),new ArrayList<>());
                }
                dict.get(event.getVehicleId()).add(event.getPersonId());
            }
        }
    }

    @Override
    public void handleEvent(PersonLeavesVehicleEvent event) {
        if (stageTime.containsKey(event.getPersonId())) {
            if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt") && !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                if (event.getVehicleId().toString().startsWith("drt")) {
                    stageTime.get(event.getPersonId()).add(leaves.get(event.getVehicleId()));
                }
                stageTime.get(event.getPersonId()).add(event);
                dict.get(event.getVehicleId()).remove(event.getPersonId());
            }

        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (stageTime.containsKey(event.getPersonId())) {
            if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt") || event.getLegMode().equals("drtaxi")) {
                stageTime.get(event.getPersonId()).add(event);
            }
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(TransportMode.pt) || event.getLegMode().equals("drt") || event.getLegMode().equals("drtaxi")) {
            if (!stageTime.containsKey(event.getPersonId())) {
                stageTime.put(event.getPersonId(), new ArrayList<>());
            }
            stageTime.get(event.getPersonId()).add(event);
        }
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        if (dict.containsKey(event.getVehicleId())){
            for (Id<Person> pid: dict.get(event.getVehicleId())){
                if (stageTime.get(pid).get(stageTime.get(pid).size() - 1) instanceof PersonEntersVehicleEvent) {
                    stageTime.get(pid).add(event);
                }
            }
        }
    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        leaves.put(event.getVehicleId(), event);
    }
}