package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class ChangePartOfTaxiToPT {

    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (person.getAttributes().getAttribute("type").equals("place_place")){
                    for (Plan plan : person.getPlans()) {
                        for (PlanElement planElement : plan.getPlanElements()) {
                            if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("taxi")){
                                ((Leg) planElement).setMode("pt");
                            }
                        }
                    }
                }
            }
        });
        StreamingPopulationWriter writer = new StreamingPopulationWriter();
        writer.startStreaming("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_70prct_082018_taxi2pt.xml.gz");
        reader.addAlgorithm(writer);
        reader.readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_70prct_082018.xml.gz");
        writer.closeStreaming();
    }
}
