package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

public class CheckPlacePlacePVT {
    public static void main(String[] args) {
        int count = 0;
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/tanjong_pagar/plans/tp_plans.xml.gz");
        for (Person person: scenario.getPopulation().getPersons().values()){
            Plan plan = person.getSelectedPlan();
            if (person.getAttributes().getAttribute("type").equals("place_place")) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Leg) {
                        if (((Leg) planElement).getMode().equals("pvt")) {
                            count++;
                        }
                    }
                }
            }
        }
        System.out.println(count);
    }
}
