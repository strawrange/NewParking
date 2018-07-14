package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.StageActivityTypesImpl;
import org.matsim.core.scenario.ScenarioUtils;

public class CheckSelectedPlan {

    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/tanjong_pagar_depot_10/ITERS/it.40/40.plans.xml.gz");
        Population population = scenario.getPopulation();
        for (Person person: population.getPersons().values()){
            boolean selected = false;
            int sIdx = 0;
            for (Plan plan: person.getPlans()) {
                int index = 0;
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity && ((Activity) planElement).getType().equals("pt interaction") && ((Activity) planElement).getLinkId().toString().equals("l1273")) {
                        if (selected == false){
                            selected =true;
                            sIdx = index;
                        }
                    }else if (planElement instanceof Activity && ((Activity) planElement).getType().equals("pt interaction") & selected && index == sIdx && !((Activity) planElement).getLinkId().toString().equals("l1272") && !((Activity) planElement).getLinkId().toString().equals("l1273")){
                        System.out.println();
                    }
                    index++;
                }
            }
        }
    }
}
