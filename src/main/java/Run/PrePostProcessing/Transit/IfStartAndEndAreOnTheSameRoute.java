package Run.PrePostProcessing.Transit;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.TransitScheduleImpl;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.api.core.v01.network.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IfStartAndEndAreOnTheSameRoute {
    static HashMap<Id<Link>, Set<Id<Link>>> direct = new HashMap<>();
    static int count = 0;
    static int stop2stop=0;
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new TransitScheduleReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_PT_bay_demand_2018_farmrt.xml");
        for (TransitLine line: scenario.getTransitSchedule().getTransitLines().values()){
            for (TransitRoute route: line.getRoutes().values()){
                addStops(route.getStops());
            }
        }
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (person.getAttributes().getAttribute("type").equals("bus_bus")||person.getAttributes().getAttribute("type").equals("mrt_mrt")||
                        person.getAttributes().getAttribute("type").equals("bus_mrt")||person.getAttributes().getAttribute("type").equals("mrt_bus")||
                        person.getAttributes().getAttribute("type").equals("stop_stop")){
                //if (person.getAttributes().getAttribute("type").equals("stop_place")||person.getAttributes().getAttribute("type").equals("mrt_place")||person.getAttributes().getAttribute("type").equals("bus_place")){
                    stop2stop++;
                    Id<Link> fromLink = ((Activity)person.getSelectedPlan().getPlanElements().get(0)).getLinkId();
                    Id<Link> toLink = ((Activity)person.getSelectedPlan().getPlanElements().get(2)).getLinkId();
                    if(direct.get(fromLink).contains(toLink)){
                        count++;
                    }
                }
            }
        });
        reader.readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_042019.xml.gz");
        System.out.println(stop2stop);
        System.out.println(count);
    }

    private static void addStops(List<TransitRouteStop> stops) {
        for (int i = 0; i < stops.size() - 1; i++){
            if (direct.containsKey(stops.get(i).getStopFacility().getLinkId())){
                direct.get(stops.get(i).getStopFacility().getLinkId()).addAll(stops.subList(i+1,stops.size()).stream().map(stop -> stop.getStopFacility().getLinkId()).collect(Collectors.toSet()));
            }else{
                direct.put(stops.get(i).getStopFacility().getLinkId(),stops.subList(i+1,stops.size()).stream().map(stop -> stop.getStopFacility().getLinkId()).collect(Collectors.toSet()));
            }
        }
    }
}
