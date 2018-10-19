/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package Run;



import BayInfrastructure.VehicleLength;
import DynAgent.VrpAgentLogic;
import EAV.ChargingStrategy;
import EAV.EarlyReservedChargingStrategy;
import ParkingStrategy.AlwaysRoaming.RoamingStrategy;
import ParkingStrategy.NoParkingStrategy.NoParkingStrategy;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.ParkingInDepot.Depot.DepotManagerDifferentDepots;
import ParkingStrategy.ParkingInDepot.Depot.DepotManagerSameDepot;
import ParkingStrategy.InsertionOptimizer.*;
import Passenger.PassengerRequestCreator;
import firstLastAVPTRouter.MainModeIdentifierFirstLastAVPT;
import firstLastAVPTRouter.TransitRouterFirstLastAVPTFactory;
import firstLastAVPTRouter.TransitRouterNetworkFirstLastAVPT;
import firstLastAVPTRouter.linkLinkTimes.LinkLinkTimeCalculatorAV;
import firstLastAVPTRouter.stopStopTimes.StopStopTimeCalculatorAV;
import firstLastAVPTRouter.waitLinkTime.WaitLinkTimeCalculatorAV;
import firstLastAVPTRouter.waitTimes.WaitTimeCalculatorAV;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import Schedule.DrtActionCreator;
import ParkingAnalysis.DrtAnalysisModule;
import ParkingStrategy.DefaultDrtOptimizer;

import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.DrtZonalModule;
import ParkingStrategy.MixedParkingStrategy;
import ParkingStrategy.ParkingInDepot.ParkingInDepot;
import ParkingStrategy.ParkingOntheRoad.ParkingOntheRoad;
import ParkingStrategy.ParkingStrategy;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.drt.routing.DrtStageActivityType;
import org.matsim.contrib.drt.run.DrtConfigConsistencyChecker;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.contrib.eventsBasedPTRouter.stopStopTimes.StopStopTimeCalculator;
import org.matsim.contrib.eventsBasedPTRouter.waitTimes.WaitTimeStuckCalculator;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import Schedule.DrtRequestCreator;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.pt.transitSchedule.api.TransitStopArea;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jbischoff
 *
 */
public final class DrtControlerCreator {

	public static Controler createControler(Config config, boolean otfvis, String fileStops) throws IOException {
		adjustConfig(config);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		return createControlerImpl(otfvis, scenario, fileStops);
	}

	public static Controler createControler(Scenario scenario, boolean otfvis, String fileStops) throws IOException {
		// yy I know that this one breaks the sequential loading of the building blocks, but I would like to be able
		// to modify the scenario before I pass it to the controler. kai, oct'17
		adjustConfig(scenario.getConfig());
		return createControlerImpl(otfvis, scenario, fileStops);
	}

	private static Controler createControlerImpl(boolean otfvis, Scenario scenario, String fileStops) throws IOException {
		Controler controler = new Controler(scenario);
		final WaitTimeStuckCalculator waitTimeCalculator = new WaitTimeStuckCalculator(scenario.getPopulation(), scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (scenario.getConfig().qsim().getEndTime()-scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitTimeCalculator);
		final WaitTimeCalculatorAV waitTimeCalculatorAV = new WaitTimeCalculatorAV(scenario.getPopulation(), scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (scenario.getConfig().qsim().getEndTime()-scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitTimeCalculatorAV);
		final WaitLinkTimeCalculatorAV waitLinkTimeCalculatorAV = new WaitLinkTimeCalculatorAV(scenario.getPopulation(), scenario.getNetwork(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (scenario.getConfig().qsim().getEndTime()-scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitLinkTimeCalculatorAV);
		final StopStopTimeCalculator stopStopTimeCalculator = new StopStopTimeCalculator(scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (scenario.getConfig().qsim().getEndTime()-scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(stopStopTimeCalculator);
		final StopStopTimeCalculatorAV stopStopTimeCalculatorAV = new StopStopTimeCalculatorAV(scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (scenario.getConfig().qsim().getEndTime()-scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(stopStopTimeCalculatorAV);
		final LinkLinkTimeCalculatorAV linkLinkTimeCalculatorAV = new LinkLinkTimeCalculatorAV(scenario.getNetwork(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int) (scenario.getConfig().qsim().getEndTime()-scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(linkLinkTimeCalculatorAV);
		final TravelTimeCalculator travelTimeCalculator = new TravelTimeCalculator(scenario.getNetwork(), scenario.getConfig().travelTimeCalculator());

//		//String EVENTSFILE = "/home/ubuntu/data/biyu/IdeaProjects/NewParking/out/artifacts/output/drt_mix_V450_T250_bay_optimal/ITERS/it.40/40.events.xml.gz";
//		String EVENTSFILE = "/home/biyu/IdeaProjects/NewParking/output/drt_mix_V450_T250_bay_optimal/ITERS/it.40/40.events.xml.gz";
//		EventsManager manager = EventsUtils.createEventsManager();
////		manager.addHandler(waitTimeCalculator);
////		manager.addHandler(waitLinkTimeCalculatorAV);
////		manager.addHandler(waitTimeCalculatorAV);
////		manager.addHandler(stopStopTimeCalculator);
////		manager.addHandler(stopStopTimeCalculatorAV);
////		manager.addHandler(linkLinkTimeCalculatorAV);
//		manager.addHandler(travelTimeCalculator);
//		new MatsimEventsReader(manager).readFile(EVENTSFILE);




		BufferedReader reader = new BufferedReader(new FileReader(fileStops));
		String line = reader.readLine();
		Set<Id<TransitStopFacility>> ids = new HashSet<>();
		while(line!=null) {
			ids.add(Id.create(line, TransitStopFacility.class));
			line = reader.readLine();

		}
		reader.close();
		for(TransitStopFacility stop:scenario.getTransitSchedule().getFacilities().values()) {
			IDS:
			for(Id<TransitStopFacility> id:ids)
				if(stop.getId().equals(id)) {
					stop.setStopAreaId(Id.create("mp", TransitStopArea.class));
					break IDS;
				}
		}
		controler.addOverridingModule(new DvrpModule(DrtControlerCreator::createModuleForQSimPlugin,
				DrtOptimizer.class,
				DefaultUnplannedRequestInserter.class, ParallelPathDataProvider.class));
		controler.addOverridingModule(new DrtZonalModule());
		controler.addOverridingModule(new DrtModule());
		controler.addOverridingModule(new DrtAnalysisModule());
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addTravelTimeBinding(DvrpTravelTimeModule.DVRP_INITIAL).toInstance(travelTimeCalculator.getLinkTravelTimes());
				bind(MainModeIdentifier.class).toInstance(new MainModeIdentifierFirstLastAVPT(new HashSet<>(Arrays.asList("pvt","taxi","walk"))));
				addRoutingModuleBinding("pt").toProvider(new TransitRouterFirstLastAVPTFactory(scenario, waitTimeCalculator.get(), waitTimeCalculatorAV.get(), waitLinkTimeCalculatorAV.get(), stopStopTimeCalculator.get(), stopStopTimeCalculatorAV.get(), linkLinkTimeCalculatorAV.get(), TransitRouterNetworkFirstLastAVPT.NetworkModes.PT_AV));
			}

		});
		//rebalancing strategy: demand based rebalancing strategy
		if (otfvis) {
			controler.addOverridingModule(new OTFVisLiveModule());
		}

		return controler;
	}

	private static void adjustConfig(Config config) {
		DrtConfigGroup drtCfg =(DrtConfigGroup)config.getModule(DrtConfigGroup.GROUP_NAME);
		if (drtCfg.getOperationalScheme().equals(DrtConfigGroup.OperationalScheme.stopbased)) {
			ActivityParams params = config.planCalcScore().getActivityParams(DrtStageActivityType.DRT_STAGE_ACTIVITY);
			if (params == null) {
				params = new ActivityParams(DrtStageActivityType.DRT_STAGE_ACTIVITY);
				params.setTypicalDuration(1);
				params.setScoringThisActivityAtAll(false);
				config.planCalcScore().addActivityParams(params);
				Logger.getLogger(DrtControlerCreator.class).info(
						"drt interaction scoring parameters not set. Adding default values (activity will not be scored).");
			}
			if (!config.planCalcScore().getModes().containsKey(DrtStageActivityType.DRT_WALK)) {
				ModeParams drtWalk = new ModeParams(DrtStageActivityType.DRT_WALK);
				ModeParams walk = config.planCalcScore().getModes().get(TransportMode.walk);
				drtWalk.setConstant(walk.getConstant());
				drtWalk.setMarginalUtilityOfDistance(walk.getMarginalUtilityOfDistance());
				drtWalk.setMarginalUtilityOfTraveling(walk.getMarginalUtilityOfTraveling());
				drtWalk.setMonetaryDistanceRate(walk.getMonetaryDistanceRate());
				config.planCalcScore().addModeParams(drtWalk);
				Logger.getLogger(DrtControlerCreator.class)
						.info("drt_walk scoring parameters not set. Adding default values (same as for walk mode).");
			}
		}

		config.addConfigConsistencyChecker(new DrtConfigConsistencyChecker());
		config.checkConsistency();
	}

	public static com.google.inject.Module createModuleForQSimPlugin(Config config) {
		return new com.google.inject.AbstractModule() {
			@Override
			protected void configure() {
				AtodConfigGroup drtCfg = AtodConfigGroup.get(config);
				//bind(ChargingStrategy.class).to(EarlyReservedChargingStrategy.class).asEagerSingleton();
				bind(DrtOptimizer.class).to(DefaultDrtOptimizer.class).asEagerSingleton();
				bind(VrpOptimizer.class).to(DrtOptimizer.class);
				bind(DefaultUnplannedRequestInserter.class).asEagerSingleton();
				bind(UnplannedRequestInserter.class).to(DefaultUnplannedRequestInserter.class);
				bind(EmptyVehicleRelocator.class).asEagerSingleton();
				bind(DrtScheduler.class).asEagerSingleton();
				bind(VrpAgentLogic.DynActionCreator.class).to(DrtActionCreator.class).asEagerSingleton();
				bind(PassengerRequestCreator.class).to(DrtRequestCreator.class).asEagerSingleton();
				bind(ParallelPathDataProvider.class).asEagerSingleton();
				bind(PrecalculatablePathDataProvider.class).to(ParallelPathDataProvider.class);
				bind(VehicleLength.class).asEagerSingleton();
				if (drtCfg.getParkingStrategy().equals(ParkingStrategy.Strategies.AlwaysRoaming)){
					bind(ParkingStrategy.class).to(RoamingStrategy.class).asEagerSingleton();
				}else if (drtCfg.getParkingStrategy().equals(ParkingStrategy.Strategies.ParkingOntheRoad)){
					bind(ParkingStrategy.class).to(ParkingOntheRoad.class).asEagerSingleton();
				}else if (drtCfg.getParkingStrategy().equals(ParkingStrategy.Strategies.ParkingInDepot)){
					bind(ParkingStrategy.class).to(ParkingInDepot.class).asEagerSingleton();
					bind(DepotManager.class).to(DepotManagerSameDepot.class).asEagerSingleton();
				}else if (drtCfg.getParkingStrategy().equals(ParkingStrategy.Strategies.MixedParking)){
					bind(ParkingOntheRoad.class).asEagerSingleton();
					bind(ParkingInDepot.class).asEagerSingleton();
					bind(ParkingStrategy.class).to(MixedParkingStrategy.class).asEagerSingleton();
					bind(DepotManager.class).to(DepotManagerDifferentDepots.class).asEagerSingleton();
				}else if (drtCfg.getParkingStrategy().equals(ParkingStrategy.Strategies.NoParkingStrategy)){
					bind(ParkingStrategy.class).to(NoParkingStrategy.class).asEagerSingleton();
				}else{
					throw new RuntimeException("Parking strategy: " + drtCfg.getParkingStrategy().toString() + " does not exist");
				}

			}



			@Provides
			@Singleton
			private MobsimTimer provideTimer(QSim qSim) {
				return qSim.getSimTimer();
			}

			@Provides
			@Named(DefaultDrtOptimizer.DRT_OPTIMIZER)
			private TravelDisutility provideTravelDisutility(
					@Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime,
					@Named(DefaultDrtOptimizer.DRT_OPTIMIZER) TravelDisutilityFactory travelDisutilityFactory) {
				return travelDisutilityFactory.createTravelDisutility(travelTime);
			}
		};
	}
}
