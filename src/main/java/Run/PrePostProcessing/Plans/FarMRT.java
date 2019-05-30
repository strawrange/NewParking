package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FarMRT {

    public static void main(String[] args) throws IOException {
        BufferedReader br = IOUtils.getBufferedReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/farmrt.txt");
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new TransitScheduleReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_PT_bay_demand_2018_farmrt.xml");
        String line = br.readLine();
        HashMap<Id<Link>, TransitStopFacility> stops = new HashMap<>();

        while (line != null){
            TransitStopFacility stop = scenario.getTransitSchedule().getFacilities().get(Id.create(line, TransitStopFacility.class));
            stops.put(stop.getLinkId(), stop);
            line = br.readLine();
        }
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (person.getAttributes().getAttribute("type").toString().startsWith("stop_") || person.getAttributes().getAttribute("type").toString().startsWith("bus_")||person.getAttributes().getAttribute("type").toString().startsWith("mrt_")){
                    Activity activity = ((Activity)person.getSelectedPlan().getPlanElements().get(0));
                    if (stops.containsKey(activity.getLinkId())){
                        activity.setCoord(stops.get(activity.getLinkId()).getCoord());
                    }
                }
                if (person.getAttributes().getAttribute("type").toString().endsWith("_stop") || person.getAttributes().getAttribute("type").toString().endsWith("_bus") || person.getAttributes().getAttribute("type").toString().endsWith("_mrt")){
                    Activity activity = ((Activity)person.getSelectedPlan().getPlanElements().get(2));
                    if (stops.containsKey(activity.getLinkId())){
                        activity.setCoord(stops.get(activity.getLinkId()).getCoord());
                    }
                }
            }
        });
        StreamingPopulationWriter writer = new StreamingPopulationWriter();
        writer.startStreaming("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_052019_farmrt.xml.gz");
        reader.addAlgorithm(writer);
        reader.readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_052019.xml.gz");
        writer.closeStreaming();

    }
}
