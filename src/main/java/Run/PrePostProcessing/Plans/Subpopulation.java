package Run.PrePostProcessing.Plans;

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
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.run.gui.PopulationSampler;

import java.util.*;

public class Subpopulation {
    public static void main(String[] args) {
        double prct = 0.8;
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        StreamingPopulationWriter writer = new StreamingPopulationWriter(prct);
        writer.startStreaming("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_80prctall_052019_farmrt.xml.gz");
        reader.addAlgorithm(writer);
        reader.readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_052019_farmrt.xml.gz");
        writer.closeStreaming();

//        new PopulationReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_plans_082018.xml.gz");
//        Population population = scenario.getPopulation();
//        int bound = (int) (prct * population.getPersons().size());
//        List<Id<Person>> keyset =  new ArrayList<>(population.getPersons().keySet());
//        for (int i = 0; i < bound; i++){
//            Random random = new Random();
//            boolean inc = false;
//            do {
//                int idx = random.nextInt(population.getPersons().size());
//                if (!scenario.getPopulation().getPersons().containsKey(keyset.get(idx))){
//                    inc = true;
//                }
//                scenario.getPopulation().removePerson(keyset.get(idx));
//                keyset.remove(idx);
//            }while (inc);
//        }
//        new PopulationWriter(scenario.getPopulation()).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/plans_70prct_082018.xml.gz");
    }
}
