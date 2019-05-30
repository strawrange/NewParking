package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Iterator;
import java.util.Random;

public class RemoveHalfPVT {
    public static void main(String[] args) {
        double ratio = 0.5;
        Random random = new Random();
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/tanjong_pagar/plans/tp_plans_2_0.125_taxi.xml.gz");
        Population population = scenario.getPopulation();
        Iterator<? extends Person> pIter = population.getPersons().values().iterator();
        while (pIter.hasNext()) {
            Person person = pIter.next();
            for (Plan plan : person.getPlans()) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("pvt")){
                        if (random.nextDouble() <= ratio) {
                            pIter.remove();
                        }
                    }
                }
            }

        }
        new PopulationWriter(population).write("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/tanjong_pagar/plans/tp_plans_2_0.125_taxi.xml.gz");
    }
}
