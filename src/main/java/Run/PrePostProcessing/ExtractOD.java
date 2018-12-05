package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;

import org.matsim.api.core.v01.network.*;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;

import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;

public class ExtractOD {
    public static void main(String[] args) throws IOException {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/mp_c_tp/drt_mix_V1500_T1000_max_pricing/ITERS/it.40/40.plans.xml.gz");
        Network network = NetworkUtils.createNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        Population population = scenario.getPopulation();
        BufferedWriter bw = IOUtils.getBufferedWriter("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/mp_c_tp/drt_mix_V1500_T1000_max_pricing/drtOD.csv");
        bw.write("time;person;mode;xo;yo;xd;yd");
        for (Person person : population.getPersons().values()) {
            for (int i = 0; i < person.getSelectedPlan().getPlanElements().size();i++){
                PlanElement planElement = person.getSelectedPlan().getPlanElements().get(i);
                Activity act0 = (Activity) person.getSelectedPlan().getPlanElements().get(0);
                if (planElement instanceof Leg && ((Leg) planElement).getMode().startsWith("drt")){
                    bw.newLine();
                    Link startLink = network.getLinks().get(((Leg) planElement).getRoute().getStartLinkId());
                    Link endLink = network.getLinks().get(((Leg) planElement).getRoute().getEndLinkId());
                    bw.write(act0.getEndTime() + ";" + person.getId().toString() + ";" + ((Leg) planElement).getMode() + ";" + startLink.getCoord().getX() + ";" + startLink.getCoord().getY() +
                            ";" + endLink.getCoord().getX() + ";" + endLink.getCoord().getY());
                }
            }
        }
        bw.close();
    }
}
