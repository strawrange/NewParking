    package Run.PrePostProcessing.Plans;

    import org.matsim.api.core.v01.Id;
    import org.matsim.api.core.v01.Scenario;
    import org.matsim.api.core.v01.population.Leg;
    import org.matsim.api.core.v01.population.Person;
    import org.matsim.api.core.v01.population.Plan;
    import org.matsim.api.core.v01.population.PlanElement;
    import org.matsim.core.config.ConfigUtils;
    import org.matsim.core.population.algorithms.PersonAlgorithm;
    import org.matsim.core.population.io.StreamingPopulationReader;
    import org.matsim.core.scenario.ScenarioUtils;
    import org.matsim.core.utils.io.IOUtils;

    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.HashMap;

    public class CountDuplicatePlans {
            static ArrayList<Id<Person>> pids = new ArrayList<>();
            static int count = 0;
            public static void main(String[] args) throws IOException {
                    Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
                    StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
                BufferedReader br = IOUtils.getBufferedReader("/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/30.drt_rejections.csv");
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
                                int i = 1;
                                if (person.getPlans().size() > 1){
                                    if (person.getPlans().get(i).getPlanElements().size() ==  person.getPlans().get(i - 1).getPlanElements().size()){
                                        for (int j = 0; j < person.getPlans().get(i).getPlanElements().size(); j++) {
                                            if (person.getPlans().get(i).getPlanElements().get(j) instanceof Leg) {
                                                Leg leg1 = (Leg) person.getPlans().get(i).getPlanElements().get(j);
                                                if (person.getPlans().get(i - 1).getPlanElements().get(j) instanceof Leg){
                                                    Leg leg0 = (Leg) person.getPlans().get(i - 1).getPlanElements().get(j);
                                                    if (!leg0.getMode().equals(leg1.getMode())) {
                                                        count++;
                                                        break;
                                                    }
                                                }else{
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                    else{
                                        count++;
                                    }
                                }
                                if (person.getPlans().size() > 2) {
                                    if (person.getPlans().get(i).getPlanElements().size() == person.getPlans().get(i + 1).getPlanElements().size() &&
                                            person.getPlans().get(i - 1).getPlanElements().size() == person.getPlans().get(i + 1).getPlanElements().size()) {
                                        for (int j = 0; j < person.getPlans().get(i).getPlanElements().size(); j++) {
                                            if (person.getPlans().get(i).getPlanElements().get(j) instanceof Leg) {
                                                Leg leg1 = (Leg) person.getPlans().get(i).getPlanElements().get(j);
                                                if (person.getPlans().get(i - 1).getPlanElements().get(j) instanceof Leg && person.getPlans().get(i + 1).getPlanElements().get(j) instanceof Leg) {
                                                    Leg leg0 = (Leg) person.getPlans().get(i - 1).getPlanElements().get(j);
                                                    Leg leg2 = (Leg) person.getPlans().get(i + 1).getPlanElements().get(j);
                                                    if (!leg0.getMode().equals(leg1.getMode()) || !leg0.getMode().equals(leg2.getMode())) {
                                                        count++;
                                                        break;
                                                    }
                                                } else {
                                                    count++;
                                                    break;
                                                }
                                            }

                                        }
                                    } else {
                                        count++;
                                    }
                                }

                            }
                        }
                    });
                    reader.readFile("/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/30.plans.xml");
                    System.out.println(count);
                    System.out.println(pids.size());
            }
    }
