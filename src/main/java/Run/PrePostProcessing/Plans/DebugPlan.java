package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class DebugPlan {
    public static void main(String[] args) {
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        for (Person person: scenario.getPopulation().getPersons().values()){
            for (PlanElement planElement: person.getSelectedPlan().getPlanElements()){
                if (planElement instanceof Leg){
                    Leg leg = (Leg) planElement;
                    if (leg.getRoute().getStartLinkId().toString().equals("22245-31303") && !leg.getMode().equals(TransportMode.pt) && !leg.getMode().equals(TransportMode.transit_walk)){
                        System.out.println();
                    }
                }
            }
        }
    }
}
