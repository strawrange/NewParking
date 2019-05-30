package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

/**
 *
 */
public class GenerateOnePersonPlan {
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_80prct_042019_farmrt.xml.gz");
        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        Id<Person> pid = Id.createPersonId("310128,bus,21600,6");
        Person p = scenario.getPopulation().getPersons().get(pid);
        population.addPerson(p);
        new PopulationWriter(population).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/test_plan.xml");
    }
}
