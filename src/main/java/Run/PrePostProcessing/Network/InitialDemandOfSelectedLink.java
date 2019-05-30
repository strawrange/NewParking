package Run.PrePostProcessing.Network;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class InitialDemandOfSelectedLink {

    public static void main(String[] args) throws IOException {
        String selectedLink = "l1273";
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/V550/tanjong_pagar_depot_10_v550/ITERS/it.40/40.plans.xml.gz");
        Population population = scenario.getPopulation();
        BufferedWriter bw = IOUtils.getBufferedWriter("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/V550/tanjong_pagar_depot_10_v550/ITERS/it.40/directPopulationFor" + selectedLink + ".csv");
        bw.write("personId;departureTime;initialLink;initialX;initialY");
        for (Person person: population.getPersons().values()){
            Plan plan = person.getSelectedPlan();
            Activity act1 = (Activity) plan.getPlanElements().get(0);
            for (PlanElement planElement : plan.getPlanElements()) {
                if (planElement instanceof Activity &&  ((Activity) planElement).getType().equals("pt interaction")){
                    if (((Activity) planElement).getLinkId().toString().equals(selectedLink)) {
                        bw.newLine();
                        bw.write(person.getId().toString() + ";" + act1.getEndTime() + ";" + act1.getLinkId().toString() + ";" + act1.getCoord().getX() + ";" + act1.getCoord().getY());
                    }else{
                        break;
                    }
                }
            }

        }

    }
}

