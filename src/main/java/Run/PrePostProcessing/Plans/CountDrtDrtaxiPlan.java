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
    import java.io.IOException;
    import java.util.ArrayList;

    public class CountDrtDrtaxiPlan {
            static ArrayList<Id<Person>> pids = new ArrayList<>();
            static int count = 0;
            static int countDrt = 0;
            static int countDrtaxi = 0;
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
                            boolean drt = false;
                            boolean drtaxi = false;
                            String mode = null;
                            if (pids.contains(person.getId())){
                                for (Plan plan : person.getPlans()){
                                    for (PlanElement planElement: plan.getPlanElements()){
                                        if (planElement instanceof Leg ){
                                            if (((Leg) planElement).getMode().equals("drt")){
                                                drt = true;
                                                if (person.getSelectedPlan().getPlanElements().size() > 3){
                                                    countDrt++;
                                                }
                                                break;
                                            }
                                            if (((Leg) planElement).getMode().equals("drtaxi")){
                                                drtaxi = true;
                                                if (person.getSelectedPlan().getPlanElements().size() > 3){
                                                    countDrtaxi++;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    if (drt ){
                                        if (mode == "drtaxi"){
                                            count++;
                                            break;
                                        }
                                        mode = "drt";
                                    }
                                    if (drtaxi){
                                        if (mode == "drt"){
                                            count++;
                                            break;
                                        }
                                        mode = "drtaxi";
                                    }
                                }
                            }
                        }
                    });
                    reader.readFile("/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix/30.plans.xml");
                    System.out.println(count);
                    System.out.println(pids.size());
                System.out.println(countDrt);
                System.out.println(countDrtaxi);
            }
    }
