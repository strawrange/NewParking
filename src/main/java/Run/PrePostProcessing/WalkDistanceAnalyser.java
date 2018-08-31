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
    private static String ITER = "0";
    private static String PLANFILE;

    public static void main(String[] args) throws IOException {
        double[] bay = new double[]{1,1.5,2};
        BufferedWriter bw2 = org.matsim.core.utils.io.IOUtils.getBufferedWriter("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/mp_c_tp/drt_mix_V1500_T1000_max/legstats.csv");
        bw2.write("bay;drt;pt;drtaxi");
        double i=1;
        //for (double i:bay){
            //FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/HKSTS/Roam/tanjong_pagar_roam_max_v600_plans_"+ i +"/ITERS/";
        FOLDER = "/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/mp_c_tp/drt_mix_V1500_T1000_max/ITERS/";
            PLANFILE =  FOLDER +  "it." + ITER + "/" + ITER + ".plans.xml.gz";
            Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            new PopulationReader(scenario).readFile(PLANFILE);
            BufferedWriter bw = org.matsim.core.utils.io.IOUtils.getBufferedWriter(FOLDER + ITER + "walk.csv");
            bw.write("personId;accessT;accessD;egressT;egressD");
            int drt = 0;
            int pt = 0;
            int drtaxi = 0;
            for (Person person:scenario.getPopulation().getPersons().values()){
                Plan plan = person.getSelectedPlan();
                for (int idx =0; idx < plan.getPlanElements().size();idx++){
                    PlanElement planElement = plan.getPlanElements().get(idx);
                    if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("drt")){
                        if (((Leg) planElement).getDepartureTime() > 28*3600){
                            return;
                        }
                        drt++;
                        double accessT = 0;
                        double egressT = 0;
                        double accessD = 0;
                        double egressD = 0;
                        Leg access = (Leg) plan.getPlanElements().get(idx - 2);
                        if (access.getMode().equals(TransportMode.transit_walk)){
                            accessT = access.getTravelTime();
                            accessD = access.getRoute().getDistance();
                        }
                        if (plan.getPlanElements().size() > idx + 2){
                            Leg egress = (Leg) plan.getPlanElements().get(idx + 2);
                            if (egress.getMode().equals(TransportMode.transit_walk)){
                                egressT = egress.getTravelTime();
                                egressD = egress.getRoute().getDistance();
                            }
                        }
                        bw.newLine();
                        bw.write(person.getId().toString() + ";" + accessT + ";" + accessD + ";" + egressT + ";" + egressD);
                    }
                    if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("pt")){
                        if (((Leg) planElement).getDepartureTime() > 28*3600){
                            return;
                        }
                        pt++;
                    }
                    if (planElement instanceof Leg && ((Leg) planElement).getMode().equals("drtaxi")){
                        if (((Leg) planElement).getDepartureTime() > 28*3600){
                            return;
                        }
                        drtaxi++;
                    }
                }
            }
            bw2.newLine();
            bw2.write(i + ";" + drt + ";" + pt + ";" + drtaxi);
            bw.close();
        //}
        bw2.close();
    }
}
