package Run.PrePostProcessing.Plans;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.*;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.algorithms.PersonAlgorithm;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ConvertPopulationToNewNetwork {

    public static void main(String[] args) throws IOException {
        Map<Coord, Id<Link>> bldgs = new HashMap<>();
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/tp_20190211/network/after.xml");
        BufferedReader br = new BufferedReader(new FileReader("/home/biyu/IdeaProjects/NewParking/scenarios/tp_20190211/network/20190131_Buildings.csv"));
        br.readLine();
        String line = br.readLine();
        while(line!=null) {
            String[] parts = line.split(",");
            Coord c = new Coord(Double.valueOf(parts[0]), Double.valueOf(parts[1]));
            String l = String.valueOf(parts[16]);
            l = l.substring(1,l.length()-1);
            if (l.equals("100053")){
                System.out.println();
            }
            bldgs.put(c, Id.create(l, Link.class));
            line = br.readLine();
        }
        br.close();
        reader.addAlgorithm(new PersonAlgorithm() {
            @Override
            public void run(Person person) {
                    for (Plan plan : person.getPlans()) {
                        Activity plan1 = (Activity) plan.getPlanElements().get(0);
                        Leg plan2 = (Leg) plan.getPlanElements().get(1);
                        Activity plan3 = (Activity) plan.getPlanElements().get(2);
                        if (person.getAttributes().getAttribute("type").toString().startsWith("place_")){
                                Coord oldCoord = plan1.getCoord();
                                Coord newCoord = findNearestCoord(oldCoord);
                                Id<Link> linkid = bldgs.get(newCoord);
                                if (linkid.toString().equals("100053")){
                                    System.out.println();
                                }
                                plan1.setCoord(newCoord);
                                plan1.setLinkId(linkid);
                        }
                        if (person.getAttributes().getAttribute("type").toString().endsWith("_place")){
                            Coord oldCoord = plan3.getCoord();
                            Coord newCoord = findNearestCoord(oldCoord);
                            Id<Link> linkid = bldgs.get(newCoord);
                            if (linkid.toString().equals("100053")){
                                System.out.println();
                            }
                            plan3.setCoord(newCoord);
                            plan3.setLinkId(linkid);
                        }
                    }
                    }
                                private Coord findNearestCoord(Coord coord){
                                    Coord nearst = null;
                                    double dist = Double.MAX_VALUE;
                                    for (Coord c:bldgs.keySet()){
                                        double d = NetworkUtils.getEuclideanDistance(c,coord);
                                        if (d < dist){
                                            nearst = c;
                                            dist = d;
                                        }
                                    }
                                    return nearst;
                                }
        }
        );
        StreamingPopulationWriter writer = new StreamingPopulationWriter();
        writer.startStreaming("/home/biyu/IdeaProjects/NewParking/scenarios/tp_20190211/plans/plans.xml.gz");
        reader.addAlgorithm(writer);
        reader.readFile("/home/biyu/IdeaProjects/NewParking/scenarios/tp_20181016/tanjong-pagar/tanjong-pagar/plans/tp-s1.3.plans.xml.gz");
        writer.closeStreaming();
    }


}

