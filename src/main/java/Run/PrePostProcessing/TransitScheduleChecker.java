package Run.PrePostProcessing;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.utils.TransitScheduleValidator;

public class TransitScheduleChecker {
    public static void main(String[] args) {
        Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
        new TransitScheduleReader(scenario).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_PT_2018.xml");
        new MatsimNetworkReader(scenario.getNetwork()).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        TransitScheduleValidator.printResult(TransitScheduleValidator.validateAll(scenario.getTransitSchedule(),scenario.getNetwork()));
    }
}
