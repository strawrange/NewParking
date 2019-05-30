package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import sun.misc.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;

public class WalkDistanceAnalyser {
    private static String FOLDER;
    private static String ITER = "40";
    private static String PLANFILE;

    public static void main(String[] args) throws IOException {
        String[] parking = new String[]{"depot","roam","road"};
        String[] bay = new String[]{"bay","curb","infinity","single"};
//        for (String p:parking) {
//            for (String b: bay) {
                FOLDER = "/home/biyu/Dropbox (engaging_mobility)/Team-Ordner „engaging_mobility“/bulky/20181211_TANVI_result/tp-s1-3-MRT-AVBUS-AVTAXI_depot_V450/ITERS/it." + ITER + "/";
                //FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/trb/" + p + "/" + b + "/";
                PLANFILE = FOLDER + ITER + ".plans.xml.gz";
                Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
                new PopulationReader(scenario).readFile(PLANFILE);
                BufferedWriter bw = org.matsim.core.utils.io.IOUtils.getBufferedWriter(FOLDER + ITER + "walk.csv");
                bw.write("personId;accessT;accessD;egressT;egressD;mode");
                int drt = 0;
                int pt = 0;
                int drtaxi = 0;
                for (Person person : scenario.getPopulation().getPersons().values()) {
                    Plan plan = person.getSelectedPlan();
                    for (int idx = 0; idx < plan.getPlanElements().size(); idx++) {
                        PlanElement planElement = plan.getPlanElements().get(idx);
                        if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("drt")) {
                            drt++;
                            double accessT = 0;
                            double egressT = 0;
                            double accessD = 0;
                            double egressD = 0;
                            Leg access = (Leg) plan.getPlanElements().get(idx - 2);
                            if (access.getMode().equals(TransportMode.transit_walk)) {
                                accessT = access.getTravelTime();
                                accessD = access.getRoute().getDistance();
                            }
                            if (plan.getPlanElements().size() > idx + 2) {
                                Leg egress = (Leg) plan.getPlanElements().get(idx + 2);
                                if (egress.getMode().equals(TransportMode.transit_walk)) {
                                    egressT = egress.getTravelTime();
                                    egressD = egress.getRoute().getDistance();
                                }
                            }
                            bw.newLine();
                            bw.write(person.getId().toString() + ";" + accessT + ";" + accessD + ";" + egressT + ";" + egressD + ";drt");
                        }
                        if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("pt")) {
                            pt++;
                        }
                        if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("drtaxi")) {
                            drtaxi++;

                        }
                    }
                }
            }
            //bw2.newLine();
            //bw2.write(i + ";" + drt + ";" + pt + ";" + drtaxi);
            //bw.close();
        }
        //bw2.close();
//    }
//}
