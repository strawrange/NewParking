package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class MovePlansToFarMRT {
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Scenario fakeScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        new PopulationReader(fakeScenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_70prct_082018.xml.gz");
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (person.getAttributes().getAttribute("type").equals("place_place")){
                    Person fakeP = fakeScenario.getPopulation().getPersons().get(person.getId());
                    if (fakeP == null){
                        System.out.println();
                    }
                    for (Plan plan : fakeP.getPlans()) {
                        for (PlanElement planElement : plan.getPlanElements()) {
                            if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("taxi") ){
                                ((Leg)person.getSelectedPlan().getPlanElements().get(1)).setMode("taxi");
                            }
                        }
                    }
                }
            }
        });
        StreamingPopulationWriter writer = new StreamingPopulationWriter();
        writer.startStreaming("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_70prct_052019_farmrt.xml.gz");
        reader.addAlgorithm(writer);
        reader.readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_052019.xml.gz");
        writer.closeStreaming();
    }
}
