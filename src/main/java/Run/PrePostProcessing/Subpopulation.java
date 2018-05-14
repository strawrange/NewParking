package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.dvrp.data.FleetImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.file.VehicleReader;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.contrib.util.PopulationUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.run.gui.PopulationSampler;

import java.util.*;

public class Subpopulation {
    public static void main(String[] args) {
        double prct = 0.5;
        Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Population population = scenario.getPopulation();
        int bound = (int) (prct * population.getPersons().size());
        List<Id<Person>> keyset =  new ArrayList<>(population.getPersons().keySet());
        for (int i = 0; i < bound; i++){
            Random random = new Random();
            int idx = random.nextInt(population.getPersons().size());
            scenario.getPopulation().removePerson(keyset.get(idx));
        }
        new PopulationWriter(scenario.getPopulation()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/0.plans.xml");
    }
}
