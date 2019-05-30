package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.TransitScheduleWriterV2;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RemoveRoad2RoadTaxiPVT {

    public static int num = 0;
    public static double prct = 0.225;

    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_042019_farmrt.xml.gz");
        Population population = scenario.getPopulation();
        ArrayList<Id<Person>> keyset = new ArrayList<>(population.getPersons().keySet());
        Random random = new Random();
        for (int i = 0; i < keyset.size(); i++){
            Person person = population.getPersons().get(keyset.get(i));
            if (person.getAttributes().getAttribute("type").equals("road_road")){
                for (Plan plan : person.getPlans()) {
                    for (PlanElement planElement : plan.getPlanElements()) {
                        if (planElement instanceof Leg && (((Leg) planElement).getMode().equals("taxi") || ((Leg) planElement).getMode().equals("pvt"))){
                            if (random.nextDouble() <prct){
                                population.getPersons().remove(person.getId());
                                num++;
                            }

                        }
                    }
                }

            }

        }
        new PopulationWriter(scenario.getPopulation()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_85prct_042019_farmrt.xml.gz");
        System.out.println(num);
    }
}
