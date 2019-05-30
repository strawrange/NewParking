package Run.PrePostProcessing.Transit;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import sun.awt.image.ImageWatched;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AddMissingTransitSchedule {
    public static void main(String[] args) throws IOException {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_plans_2018.xml.gz");
        new TransitScheduleReaderV2(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_bay.xml");
        new TransitScheduleReaderV2(scenario2).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/TransitScheduleCEPASAvgDepartures_nonblocking.xml");
        BufferedReader reader = new BufferedReader(new FileReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/stops_in_area.txt"));
        String line = reader.readLine();
        Set<Id<TransitStopFacility>> ids = new HashSet<>();
        Map<Id<TransitStopFacility>, TransitStopFacility> stops = scenario.getTransitSchedule().getFacilities();
        Map<Id<Link>, Id<TransitStopFacility>> dict = new HashMap<>();
        Map<Id<TransitStopFacility>, ArrayList<Id<TransitStopFacility>>> outFrom = new HashMap<>();
        Map<Id<TransitStopFacility>, ArrayList<Id<TransitStopFacility>>> outTo = new HashMap<>();
        for (TransitStopFacility stop:stops.values()){
            dict.put(stop.getLinkId(),stop.getId());
        }
        while(line!=null) {
            Id<TransitStopFacility> tid = Id.create(line, TransitStopFacility.class);
            ids.add(tid);
            line = reader.readLine();
        }
        reader.close();
        int count1 = 0;
        int count2 = 0;
        for (Person person: scenario.getPopulation().getPersons().values()){
            if (person.getAttributes().getAttribute("type").equals("stop_stop")){
                Plan plan = person.getSelectedPlan();
                Activity act1 = (Activity) plan.getPlanElements().get(0);
                Id<TransitStopFacility> stop1 = dict.get(act1.getLinkId());
                Leg leg = (Leg) plan.getPlanElements().get(1);
                Activity act2 = (Activity) plan.getPlanElements().get(2);
                Id<TransitStopFacility> stop2 = dict.get(act2.getLinkId());
                if (leg.getMode().equals(TransportMode.pt)){
                    if (stop1.toString().charAt(0) >= '0' && stop1.toString().charAt(0) <= '9' && stop2.toString().charAt(0) >= '0' && stop2.toString().charAt(0) <= '9') {
                        if (!ids.contains(stop1)) {
                            if (outFrom.containsKey(stop1)) {
                                ArrayList<Id<TransitStopFacility>> stopIds = outFrom.get(stop1);
                                stopIds.add(stop2);
                            } else {
                                ArrayList<Id<TransitStopFacility>> stopIds = new ArrayList<>();
                                stopIds.add(stop2);
                                outFrom.put(stop1, stopIds);
                            }
                        }
                        if (!ids.contains(stop2)) {
                            if (outTo.containsKey(stop2)) {
                                ArrayList<Id<TransitStopFacility>> stopIds = outTo.get(stop2);
                                stopIds.add(stop1);
                            } else {
                                ArrayList<Id<TransitStopFacility>> stopIds = new ArrayList<>();
                                stopIds.add(stop1);
                                outTo.put(stop2, stopIds);
                            }
                        }
                    }
                    if (stop1.toString().equals("52079")){
                        count1++;
                    }
                    if (stop2.toString().equals("52079")){
                        count2++;
                    }
                }
            }
        }
    }
}
