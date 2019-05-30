package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.*;

import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class DrtRejectionAnalyzer {

    static ArrayList<Id<Person>> pids = new ArrayList<>();
    static int count = 0;
    static int countDrtTaxi = 0;
    static int countDrt = 0;
    static int outsideIn[] = new int[2];
    static int insideOut[] = new int[2];
    static int outsideOut[] = new int[2];
    static int insideIn[] = new int[2];
    public static void main(String[] args) throws IOException {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190220_clean.xml");
        BufferedReader br = IOUtils.getBufferedReader("/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/demand_bay_mix/ITERS/it.20/20.drt_rejections.csv");
        br.readLine();
        String line = br.readLine();
        while (line != null){
            Id<Person> pid = Id.createPersonId(line.split(";")[1]);
            pids.add(pid);
            line = br.readLine();
        }
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                if (pids.contains(person.getId())){
                    Link fromLink = network.getLinks().get(((Activity)person.getSelectedPlan().getPlanElements().get(0)).getLinkId());
                    Link toLink = network.getLinks().get(((Activity)person.getSelectedPlan().getPlanElements().get(person.getSelectedPlan().getPlanElements().size() - 1)).getLinkId());
                    String mode = new String();
                    if (((Activity)person.getSelectedPlan().getPlanElements().get(0)).getEndTime() >= 6.5*3600 && ((Activity)person.getSelectedPlan().getPlanElements().get(0)).getEndTime() < 8.5 * 3600) {
                        for (PlanElement planElement: person.getSelectedPlan().getPlanElements()){
                            if (planElement instanceof Leg){
                                if (((Leg) planElement).getMode().equals("drt")){
                                    if (mode.equals("drtaxi")){
                                        count++;
                                    }
                                    mode = "drt";
                                    if (person.getSelectedPlan().getPlanElements().size() > 3){
                                        countDrt++;
                                    }
                                }
                                if (((Leg) planElement).getMode().equals("drtaxi")){
                                    if (mode.equals("drt")){
                                        count++;
                                    }
                                    mode = "drtaxi";
                                    if (person.getSelectedPlan().getPlanElements().size() > 3){
                                        countDrtTaxi++;
                                    }
                                }
                            }
                        }
                        if (fromLink.getAllowedModes().contains(TransportMode.car)) {
                            if (toLink.getAllowedModes().contains(TransportMode.car)) {
                                if (mode.equals("drtaxi")) {
                                    insideIn[1]++;
                                }
                                if (mode.equals("drt")) {
                                    insideIn[0]++;
                                }
                            } else {
                                if (mode.equals("drtaxi")) {
                                    insideOut[1]++;
                                }
                                if (mode.equals("drt")) {
                                    insideOut[0]++;
                                }
                            }
                        } else {
                            if (toLink.getAllowedModes().contains(TransportMode.car)) {
                                if (mode.equals("drtaxi")) {
                                    outsideIn[1]++;
                                }
                                if (mode.equals("drt")) {
                                    outsideIn[0]++;
                                }
                            } else {
                                if (mode.equals("drtaxi")) {
                                    outsideOut[1]++;
                                }
                                if (mode.equals("drt")) {
                                    outsideOut[0]++;
                                }
                            }
                        }
                    }
                }
            }
        });
        reader.readFile("/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/demand_bay_mix/ITERS/it.20/20.plans.xml.gz");
        System.out.println("In-in: drt: " + insideIn[0] + ", drtaxi: " + insideIn[1] + ", in-out: drt: " + insideOut[0] + ", drtaxi: " + insideOut[1] +
                ", out-in: drt: " + outsideIn[0] + ", drtaxi: " + outsideIn[1] + ", out-out: drt: " + outsideOut[0] + ",drtaxi: " + outsideOut[1]);
        System.out.println(count);
    }
}
