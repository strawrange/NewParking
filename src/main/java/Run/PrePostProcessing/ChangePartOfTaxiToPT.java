package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

public class ChangePartOfTaxiToPT {

    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/tanjong_pagar/plans/tp_plans_2.0.xml.gz");
        Population population = scenario.getPopulation();
        for (Person person : population.getPersons().values()) {
            if (person.getAttributes().getAttribute("type").equals("place_place")){
                if(person.getId().toString().split(",")[1].equals("taxi"))
                    System.out.println();
                for (Plan plan : person.getPlans()) {
                    for (PlanElement planElement : plan.getPlanElements()) {
                        if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("taxi")){
                            ((Leg) planElement).setMode("pt");
                        }
                    }
                }
            }

        }
        new PopulationWriter(population).write("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/tanjong_pagar/plans/tp_plans_2.xml.gz");
    }
}
