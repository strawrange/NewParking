package Run.PrePostProcessing.Transit;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

/**
 *
 */
public class CheckAccessT2MRT {
    public static int num = 0;
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (person.getId().toString().equals("2000142,pt,32400,7")){
                    System.out.println();
                }
                if (person.getAttributes().getAttribute("type").toString().startsWith("place_")){
                    int numLeg = 0;
                    for (PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
                        if (planElement instanceof Leg){
                            if (((Leg) planElement).getMode().equals("drt") || ((Leg) planElement).getMode().equals("pt")){
                                numLeg++;
                                if (numLeg == 1) {
                                    if (((Leg) planElement).getRoute().getStartLinkId().toString().startsWith("CC") || ((Leg) planElement).getRoute().getStartLinkId().toString().startsWith("NS") ||
                                            ((Leg) planElement).getRoute().getStartLinkId().toString().startsWith("TE") || ((Leg) planElement).getRoute().getStartLinkId().toString().startsWith("CE")) {
                                        num++;
                                    }
                                    break;
                                }
                            }

                        }
                    }
                }
            }
        });

        reader.readFile("/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix_new/demand_bay_mix/ITERS/it.0/0.plans.xml.gz");
        System.out.println(num);
    }
}
