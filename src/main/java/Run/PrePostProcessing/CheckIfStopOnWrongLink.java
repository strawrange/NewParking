package Run.PrePostProcessing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleReaderV2;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class CheckIfStopOnWrongLink {
    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/scenarios/tanjong_pagar/";
    public static void main(String[] args) {
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        new NetworkReaderMatsimV2(network).readFile(FOLDER + "tp_phase4.xml");
        new TransitScheduleReaderV2(scenario).readFile(FOLDER + "tp_PT.xml");
        for (TransitStopFacility transitStopFacility : scenario.getTransitSchedule().getFacilities().values()){
            Id<Link> linkId = transitStopFacility.getLinkId();
            Link link = network.getLinks().get(linkId);
            if (link.getNumberOfLanes() == 1){
                System.out.println(linkId.toString());
            }
        }
    }

}
