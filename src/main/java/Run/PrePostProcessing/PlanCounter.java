package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class PlanCounter {
    static int count = 0;

    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (person.getSelectedPlan().getPlanElements().size() == 3){
                    PlanElement planElement = person.getSelectedPlan().getPlanElements().get(1);
                    if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("transit_walk")){
                        count++;

                    }
                }
            }
        });
        reader.readFile("/home/biyu/IdeaProjects/NewParking/output/charging/drt_mix_V450_T250_bay_nocharger/it.40/40.plans.xml.gz");
        System.out.println(count);
    }
}
