package Run.PrePostProcessing.Indicators;

import Run.PrePostProcessing.*;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;

import java.awt.peer.TrayIconPeer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndicatorsRun {
        public static String FOLDER;
        public static String ITER = "30";
        private static String EVENTSFILE;
        private static String PLANFILE;
        protected static final double END_TIME = 24 * 3600;
        protected static final double MP_START =  6*3600;
        protected static final double MP_END = 9*3600;
        protected static final double AP_START = 17*3600;
        protected static final double AP_END = 20*3600;
    public enum Indicator{
        Trip,
        Stage,
        Occupancy,
        VehicleDistance,
        DeniedBoarding,
        PassengerCarried,
        LinksStats,
        DepotsStats,
        PeopleFlow,
        DwellLength,
        QueueAnalaysis,
        NetworkPerformance
    }
        public static void main(String[] args) throws IOException {
            Config config = ConfigUtils.loadConfig("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/drtconfig_mix_V450_T250_charger_data_analysis.xml");
            Scenario scenario = ScenarioUtils.loadScenario(config);
            EventsManager manager = EventsUtils.createEventsManager();
            Network network = scenario.getNetwork();
            FOLDER = "/home/biyu/IdeaProjects/matsim-spatialDRT/output/mix_20190521/demand_bay_mix/ITERS/" ;


            Map<Indicator, IndicatorModule> modules = new HashMap<>();

            EVENTSFILE = FOLDER + "it." + ITER + "/" + ITER + ".events.xml.gz";
            PLANFILE = FOLDER + "it." + ITER + "/" + ITER + ".plans.xml.gz";
            new PopulationReader(scenario).readFile(PLANFILE);
            final org.matsim.core.trafficmonitoring.TravelTimeCalculator travelTimeCalculator = new TravelTimeCalculator(scenario.getNetwork(), scenario.getConfig().travelTimeCalculator());
            manager.addHandler(travelTimeCalculator);


            modules.put(Indicator.Trip, new NewTravelTimeHandler(network, scenario.getTransitSchedule(), scenario.getPopulation()));
            modules.put(Indicator.Stage, new StageTimeHandler(travelTimeCalculator, network));
            modules.put(Indicator.Occupancy, new OccupancyHandler(network));
            modules.put(Indicator.VehicleDistance, new VehicleDistanceHandler(network, FOLDER +  ITER));
            modules.put(Indicator.PassengerCarried, new PassengerCarriedHandler());
//         modules.put(Indicator.LinksStats, new LinkStatHandler(network));
            modules.put(Indicator.DepotsStats, new ParkingHandler());
            modules.put(Indicator.PeopleFlow, new PeopleFlowHandler(scenario.getTransitSchedule()));
            modules.put(Indicator.DwellLength, new DwellingHandler(scenario));
            modules.put(Indicator.QueueAnalaysis,  new QueueingAndDwellingCounter());
            modules.put(Indicator.DeniedBoarding, new DeniedBoardingHandler(network, scenario.getTransitSchedule()));
modules.put(Indicator.NetworkPerformance, new NetworkPerformanceAnalysis(network));

            for (IndicatorModule indicatorModule: modules.values()){
                manager.addHandler(indicatorModule);
            }
            new MatsimEventsReader(manager).readFile(EVENTSFILE);
            for (IndicatorModule indicatorModule: modules.values()){
                indicatorModule.output(FOLDER + ITER);
            }
        }
    }
