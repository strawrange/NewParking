package Run.PrePostProcessing.Plans;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OutsideMRTPlans {
    static int totalA = 0;
    static int obeyA = 0;
    static int totalE = 0;
    static int obeyE = 0;
    private static final Set<Id<Link>> dictA = new HashSet<>();
    private static final Set<Id<Link>> dictE = new HashSet<>();
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        dictA.add(Id.createLinkId("CC20_CC19/DT9"));
        dictA.add(Id.createLinkId("TE12_DT10/TE11"));
        dictA.add(Id.createLinkId("NS21/DT11_NS20"));
        dictA.add(Id.createLinkId("NS16_CC15/NS17"));
        dictA.add(Id.createLinkId("CC15/NS17_CC16"));
        dictA.add(Id.createLinkId("TE7_TE8"));
        dictE.add(Id.createLinkId("CC18_CC19/DT9"));
        dictE.add(Id.createLinkId("TE10_DT10/TE11"));
        dictE.add(Id.createLinkId("NS19_NS20"));
        dictE.add(Id.createLinkId("NS18_CC15/NS17"));
        dictE.add(Id.createLinkId("CC17/TE9_CC16"));
        dictE.add(Id.createLinkId("CC17/TE9_TE8"));
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                int size = person.getSelectedPlan().getPlanElements().size();
                if (dictA.contains(((Activity)person.getSelectedPlan().getPlanElements().get(0)).getLinkId())){
                    if (person.getAttributes().getAttribute("type").toString().startsWith("stop_")){
                        totalA++;
                        if (((Leg)person.getSelectedPlan().getPlanElements().get(1)).getMode().equals(TransportMode.pt)){
                            obeyA++;
                        }
                    }
                }
                if (dictE.contains(((Activity)person.getSelectedPlan().getPlanElements().get(size-1)).getLinkId())){
                    if (person.getAttributes().getAttribute("type").toString().endsWith("_stop")){
                        totalE++;
                        if (((Leg)person.getSelectedPlan().getPlanElements().get(size - 2)).getMode().equals(TransportMode.pt)){
                            obeyE++;
                        }
                    }
                }
            }
        });
        reader.readFile("/home/biyu/IdeaProjects/NewParking/output/charging/drt_mix_V450_T250_bay_nocharger_debug/it.40//40.plans.xml.gz");
        System.out.println(totalA);
        System.out.println(obeyA);
        System.out.println(totalE);
        System.out.println(obeyE);
    }
}
