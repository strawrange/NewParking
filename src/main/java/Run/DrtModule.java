package Run;

import BayInfrastructure.BayManager;
import Dwelling.ClearNetworkChangeEvents;
import Dwelling.DrtAndTransitStopHandlerFactory;
import Dwelling.DrtStopHandler;
import ParkingStrategy.DefaultDrtOptimizer;
import ParkingStrategy.MixedParkingStrategy;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.ParkingInDepot.Depot.DepotManagerProvider;
import ParkingStrategy.ParkingInDepot.InsertionOptimizer.DrtScheduler;
import ParkingStrategy.ParkingOntheRoad.ParkingOntheRoad;
import Vehicle.DynVehicleType;
import Vehicle.FleetProvider;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.optimizer.rebalancing.NoRebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.drt.routing.DrtMainModeIdentifier;
import org.matsim.contrib.drt.routing.DrtRoutingModule;
import org.matsim.contrib.drt.routing.StopBasedDrtRoutingModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.pt.TransitStopHandlerFactory;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import Schedule.validator.DefaultDrtRequestValidator;
import Schedule.validator.DrtRequestValidator;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;


public final class
DrtModule extends AbstractModule {

	@Override
	public void install() {
		bind(VehicleType.class).annotatedWith(Names.named(VrpAgentSource.DVRP_VEHICLE_TYPE)).toInstance(new VehicleTypeImpl(Id.create(DynVehicleType.DYNTYPE,VehicleType.class)));
		DrtConfigGroup drtCfg = DrtConfigGroup.get(getConfig());
		bind(Fleet.class).toProvider(new FleetProvider(drtCfg.getVehiclesFileUrl(getConfig().getContext())))
				.asEagerSingleton();
		bind(DepotManager.class).toProvider(new DepotManagerProvider(ConfigGroup.getInputFileURL(getConfig().getContext(), "drt_depot.xml"))).asEagerSingleton();
		bind(DrtRequestValidator.class).to(DefaultDrtRequestValidator.class);
		bind(BayManager.class).asEagerSingleton();
		bind(RebalancingStrategy.class).to(NoRebalancingStrategy.class);
		bind(TravelDisutilityFactory.class).annotatedWith(Names.named(DefaultDrtOptimizer.DRT_OPTIMIZER))
				.toInstance(timeCalculator -> new TimeAsTravelDisutility(timeCalculator));
		bind(TransitStopHandlerFactory.class ).to( DrtAndTransitStopHandlerFactory.class );
		addControlerListenerBinding().to(ClearNetworkChangeEvents.class).asEagerSingleton();
		addControlerListenerBinding().to(BayManager.class).asEagerSingleton();


		switch (drtCfg.getOperationalScheme()) {
			case door2door:
				addRoutingModuleBinding(DrtConfigGroup.DRT_MODE).to(DrtRoutingModule.class).asEagerSingleton();
				break;

			case stopbased:
				final Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
				new TransitScheduleReader(scenario2)
						.readFile(drtCfg.getTransitStopsFileUrl(getConfig().getContext()).getFile());
				bind(TransitSchedule.class).annotatedWith(Names.named(DrtConfigGroup.DRT_MODE))
						.toInstance(scenario2.getTransitSchedule());
				bind(MainModeIdentifier.class).to(DrtMainModeIdentifier.class).asEagerSingleton();
				addRoutingModuleBinding(DrtConfigGroup.DRT_MODE).to(StopBasedDrtRoutingModule.class).asEagerSingleton();
				break;

			default:
				throw new IllegalStateException();
		}
	}
}