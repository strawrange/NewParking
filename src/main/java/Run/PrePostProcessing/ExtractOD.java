package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;

public class ExtractOD {
    public static void main(String[] args) throws IOException {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/scenarios/tanjong_pagar/plans/tp_plans_with_taxi_part_to_PT.xml.gz");
        Population population = scenario.getPopulation();
        BufferedWriter bw = IOUtils.getBufferedWriter("/home/biyu/Dropbox (engaging_mobility)/TanjongPagar/out/output/TRB/drtOD.csv");
        bw.write("x;y");
        for (Person person : population.getPersons().values()) {
            bw.newLine();
            bw.write(((Activity)person.getSelectedPlan().getPlanElements().get(0)).getCoord().getX() + ";" + ((Activity)person.getSelectedPlan().getPlanElements().get(0)).getCoord().getY());
        }
        bw.close();
    }
}
