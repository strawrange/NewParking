//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Run;

import BayInfrastructure.VehicleLength;
import DynAgent.VrpAgentLogic.DynActionCreator;
import EAV.ChargingStrategy;
import EAV.EarlyReservedChargingStrategy;
import ParkingAnalysis.DrtAnalysisModule;
import ParkingStrategy.DefaultDrtOptimizer;
import ParkingStrategy.MixedParkingStrategy;
import ParkingStrategy.ParkingStrategy;
import ParkingStrategy.AlwaysRoaming.RoamingStrategy;
import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.DrtZonalModule;
import ParkingStrategy.InsertionOptimizer.DefaultUnplannedRequestInserter;
import ParkingStrategy.InsertionOptimizer.DrtScheduler;
import ParkingStrategy.InsertionOptimizer.EmptyVehicleRelocator;
import ParkingStrategy.InsertionOptimizer.ParallelPathDataProvider;
import ParkingStrategy.InsertionOptimizer.PrecalculatablePathDataProvider;
import ParkingStrategy.InsertionOptimizer.UnplannedRequestInserter;
import ParkingStrategy.NoParkingStrategy.NoParkingStrategy;
import ParkingStrategy.ParkingInDepot.ParkingInDepot;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.ParkingInDepot.Depot.DepotManagerDifferentDepots;
import ParkingStrategy.ParkingInDepot.Depot.DepotManagerSameDepot;
import ParkingStrategy.ParkingOntheRoad.ParkingOntheRoad;
import ParkingStrategy.ParkingStrategy.Strategies;
import Passenger.PassengerRequestCreator;
import Schedule.DrtActionCreator;
import Schedule.DrtRequestCreator;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import firstLastAVPTRouter.MainModeIdentifierFirstLastAVPT;
import firstLastAVPTRouter.TransitRouterFirstLastAVPTFactory;
import firstLastAVPTRouter.TransitRouterNetworkFirstLastAVPT.NetworkModes;
import firstLastAVPTRouter.linkLinkTimes.LinkLinkTimeCalculatorAV;
import firstLastAVPTRouter.stopStopTimes.StopStopTimeCalculatorAV;
import firstLastAVPTRouter.waitLinkTime.WaitLinkTimeCalculatorAV;
import firstLastAVPTRouter.waitTimes.WaitTimeCalculatorAV;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;
import mobsim.qnetsimenginelong.DefaultQNetworkFactory;
import mobsim.qnetsimenginelong.QNetworkFactory;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.optimizer.DrtOptimizer;
import org.matsim.contrib.drt.run.DrtConfigConsistencyChecker;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigGroup.OperationalScheme;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizer;
import org.matsim.contrib.eventsBasedPTRouter.stopStopTimes.StopStopTimeCalculator;
import org.matsim.contrib.eventsBasedPTRouter.waitTimes.WaitTimeStuckCalculator;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;

public final class DrtControlerCreator {
	public DrtControlerCreator() {
	}

	public static Controler createControler(Config config, boolean otfvis) throws IOException {
		adjustConfig(config);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		return createControlerImpl(otfvis, scenario);
	}

	public static Controler createControler(Scenario scenario, boolean otfvis) throws IOException {
		adjustConfig(scenario.getConfig());
		return createControlerImpl(otfvis, scenario);
	}

	private static Controler createControlerImpl(boolean otfvis, final Scenario scenario) throws IOException {
		Controler controler = new Controler(scenario);
		final WaitTimeStuckCalculator waitTimeCalculator = new WaitTimeStuckCalculator(scenario.getPopulation(), scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int)(scenario.getConfig().qsim().getEndTime() - scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitTimeCalculator);
		final WaitTimeCalculatorAV waitTimeCalculatorAV = new WaitTimeCalculatorAV(scenario.getPopulation(), scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int)(scenario.getConfig().qsim().getEndTime() - scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitTimeCalculatorAV);
		final WaitLinkTimeCalculatorAV waitLinkTimeCalculatorAV = new WaitLinkTimeCalculatorAV(scenario.getPopulation(), scenario.getNetwork(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int)(scenario.getConfig().qsim().getEndTime() - scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(waitLinkTimeCalculatorAV);
		final StopStopTimeCalculator stopStopTimeCalculator = new StopStopTimeCalculator(scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int)(scenario.getConfig().qsim().getEndTime() - scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(stopStopTimeCalculator);
		final StopStopTimeCalculatorAV stopStopTimeCalculatorAV = new StopStopTimeCalculatorAV(scenario.getTransitSchedule(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int)(scenario.getConfig().qsim().getEndTime() - scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(stopStopTimeCalculatorAV);
		final LinkLinkTimeCalculatorAV linkLinkTimeCalculatorAV = new LinkLinkTimeCalculatorAV(scenario.getNetwork(), scenario.getConfig().travelTimeCalculator().getTraveltimeBinSize(), (int)(scenario.getConfig().qsim().getEndTime() - scenario.getConfig().qsim().getStartTime()));
		controler.getEvents().addHandler(linkLinkTimeCalculatorAV);
		final TravelTimeCalculator travelTimeCalculator = new TravelTimeCalculator(scenario.getNetwork(), scenario.getConfig().travelTimeCalculator());
		controler.addOverridingModule(new DvrpModule(DrtControlerCreator::createModuleForQSimPlugin, new Class[]{DrtOptimizer.class, DefaultUnplannedRequestInserter.class, ParallelPathDataProvider.class}));
		controler.addOverridingModule(new DrtZonalModule());
		controler.addOverridingModule(new DrtModule());
		controler.addOverridingModule(new DrtAnalysisModule());
		controler.addOverridingModule(new AbstractModule() {
			public void install() {
				this.addTravelTimeBinding("dvrp_initial").toInstance(travelTimeCalculator.getLinkTravelTimes());
				this.bind(MainModeIdentifier.class).toInstance(new MainModeIdentifierFirstLastAVPT(new HashSet(Arrays.asList("pvt", "taxi", "walk"))));
				this.addRoutingModuleBinding("pt").toProvider(new TransitRouterFirstLastAVPTFactory(scenario, waitTimeCalculator.get(), waitTimeCalculatorAV.get(), waitLinkTimeCalculatorAV.get(), stopStopTimeCalculator.get(), stopStopTimeCalculatorAV.get(), linkLinkTimeCalculatorAV.get(), NetworkModes.PT_AV));
				this.bind(QNetworkFactory.class).to(DefaultQNetworkFactory.class);
			}
		});
		if (otfvis) {
			controler.addOverridingModule(new OTFVisLiveModule());
		}

		return controler;
	}

	private static void adjustConfig(Config config) {
		DrtConfigGroup drtCfg = (DrtConfigGroup)config.getModule("drt");
		if (drtCfg.getOperationalScheme().equals(OperationalScheme.stopbased)) {
			ActivityParams params = config.planCalcScore().getActivityParams("drt interaction");
			if (params == null) {
				params = new ActivityParams("drt interaction");
				params.setTypicalDuration(1.0D);
				params.setScoringThisActivityAtAll(false);
				config.planCalcScore().addActivityParams(params);
				Logger.getLogger(DrtControlerCreator.class).info("drt interaction scoring parameters not set. Adding default values (activity will not be scored).");
			}

			if (!config.planCalcScore().getModes().containsKey("drt_walk")) {
				ModeParams drtWalk = new ModeParams("drt_walk");
				ModeParams walk = (ModeParams)config.planCalcScore().getModes().get("walk");
				drtWalk.setConstant(walk.getConstant());
				drtWalk.setMarginalUtilityOfDistance(walk.getMarginalUtilityOfDistance());
				drtWalk.setMarginalUtilityOfTraveling(walk.getMarginalUtilityOfTraveling());
				drtWalk.setMonetaryDistanceRate(walk.getMonetaryDistanceRate());
				config.planCalcScore().addModeParams(drtWalk);
				Logger.getLogger(DrtControlerCreator.class).info("drt_walk scoring parameters not set. Adding default values (same as for walk mode).");
			}
		}

		config.addConfigConsistencyChecker(new DrtConfigConsistencyChecker());
		config.checkConsistency();
	}

	public static Module createModuleForQSimPlugin(final Config config) {
		return new com.google.inject.AbstractModule() {
			protected void configure() {
				AtodConfigGroup drtCfg = AtodConfigGroup.get(config);
				if (drtCfg.isEAV()) {
					this.bind(ChargingStrategy.class).to(EarlyReservedChargingStrategy.class).asEagerSingleton();
				}
				this.bind(DrtOptimizer.class).to(DefaultDrtOptimizer.class).asEagerSingleton();
				this.bind(VrpOptimizer.class).to(DrtOptimizer.class);
				this.bind(DefaultUnplannedRequestInserter.class).asEagerSingleton();
				this.bind(UnplannedRequestInserter.class).to(DefaultUnplannedRequestInserter.class);
				this.bind(EmptyVehicleRelocator.class).asEagerSingleton();
				this.bind(DrtScheduler.class).asEagerSingleton();
				this.bind(DynActionCreator.class).to(DrtActionCreator.class).asEagerSingleton();
				this.bind(PassengerRequestCreator.class).to(DrtRequestCreator.class).asEagerSingleton();
				this.bind(ParallelPathDataProvider.class).asEagerSingleton();
				this.bind(PrecalculatablePathDataProvider.class).to(ParallelPathDataProvider.class);
				this.bind(VehicleLength.class).asEagerSingleton();
				if (drtCfg.getParkingStrategy().equals(Strategies.AlwaysRoaming)) {
					this.bind(ParkingStrategy.class).to(RoamingStrategy.class).asEagerSingleton();
				} else if (drtCfg.getParkingStrategy().equals(Strategies.ParkingOntheRoad)) {
					this.bind(ParkingStrategy.class).to(ParkingOntheRoad.class).asEagerSingleton();
				} else if (drtCfg.getParkingStrategy().equals(Strategies.ParkingInDepot)) {
					this.bind(ParkingStrategy.class).to(ParkingInDepot.class).asEagerSingleton();
					this.bind(DepotManager.class).to(DepotManagerSameDepot.class).asEagerSingleton();
				} else if (drtCfg.getParkingStrategy().equals(Strategies.MixedParking)) {
					this.bind(ParkingOntheRoad.class).asEagerSingleton();
					this.bind(ParkingInDepot.class).asEagerSingleton();
					this.bind(ParkingStrategy.class).to(MixedParkingStrategy.class).asEagerSingleton();
					this.bind(DepotManager.class).to(DepotManagerDifferentDepots.class).asEagerSingleton();
				} else {
					if (!drtCfg.getParkingStrategy().equals(Strategies.NoParkingStrategy)) {
						throw new RuntimeException("Parking strategy: " + drtCfg.getParkingStrategy().toString() + " does not exist");
					}

					this.bind(ParkingStrategy.class).to(NoParkingStrategy.class).asEagerSingleton();
				}


			}

			@Provides
			@Singleton
			private MobsimTimer provideTimer(QSim qSim) {
				return qSim.getSimTimer();
			}

			@Provides
			@Named("drt_optimizer")
			private TravelDisutility provideTravelDisutility(@Named("dvrp_estimated") TravelTime travelTime, @Named("drt_optimizer") TravelDisutilityFactory travelDisutilityFactory) {
				return travelDisutilityFactory.createTravelDisutility(travelTime);
			}
		};
	}
}
