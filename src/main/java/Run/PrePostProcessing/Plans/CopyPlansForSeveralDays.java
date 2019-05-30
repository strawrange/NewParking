package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.ArrayList;

public class CopyPlansForSeveralDays {
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_plans.xml.gz");
        Population population = scenario.getPopulation();
        int size = population.getPersons().size();
        for (int i = 0; i < size; i++){
            ArrayList<Id<Person>> pid = new ArrayList<>(population.getPersons().keySet());
            Person p = population.getPersons().get(pid.get(i));
            Person person = PopulationUtils.getFactory().createPerson(Id.createPersonId(p.getId().toString() + "_2d"));
            Activity act1 = PopulationUtils.createActivity((Activity) p.getSelectedPlan().getPlanElements().get(0));
            Leg leg = PopulationUtils.createLeg((Leg) p.getSelectedPlan().getPlanElements().get(1));
            Activity act2 = PopulationUtils.createActivity((Activity) p.getSelectedPlan().getPlanElements().get(2));
            act1.setEndTime(act1.getEndTime() + 24 * 3600);
            Plan plan = PopulationUtils.createPlan();
            plan.addActivity(act1);
            plan.addLeg(leg);
            plan.addActivity(act2);
            person.addPlan(plan);
            person.setSelectedPlan(plan);
            population.addPerson(person);
        }
        new PopulationWriter(population).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_plans_48h.xml.gz");
    }
}
