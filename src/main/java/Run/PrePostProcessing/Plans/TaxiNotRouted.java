package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class TaxiNotRouted {
        public static void main(String[] args) {
            Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            Scenario fakeScenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
            reader.addAlgorithm(new PersonAlgorithm() {
                @Override
                public void run(Person person) {
                   for (Plan plan:person.getPlans()){
                       for (PlanElement planElement: plan.getPlanElements()){
                           if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("taxi")){
                               ((Leg) planElement).setRoute(null);
                           }
                       }
                   }
                }
            });
            StreamingPopulationWriter writer = new StreamingPopulationWriter();
            writer.startStreaming("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_70prct_082018_farmrt_onlyav_40.xml.gz");
            reader.addAlgorithm(writer);
            reader.readFile("/home/biyu/IdeaProjects/NewParking/output/drt_mix_V450_T250_demand_bay_nocharger_pvttaxi2pt_convertr2rpvttaxi/output_plans.xml.gz");
            writer.closeStreaming();
        }
    }
