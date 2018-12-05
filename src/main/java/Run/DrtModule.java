//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Run;

import BayInfrastructure.BayManager;
import Dwelling.ClearNetworkChangeEvents;
import Dwelling.DrtAndTransitStopHandlerFactory;
import RoutingModule.DrtRoutingModule;
import Vehicle.FleetProvider;
import com.google.inject.name.Names;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.drt.data.validator.DefaultDrtRequestValidator;
import org.matsim.contrib.drt.data.validator.DrtRequestValidator;
import org.matsim.contrib.drt.optimizer.rebalancing.NoRebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.drt.routing.DrtMainModeIdentifier;
import org.matsim.contrib.drt.routing.StopBasedDrtRoutingModule;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.mobsim.qsim.pt.TransitStopHandlerFactory;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;

public final class DrtModule extends AbstractModule {
	public DrtModule() {
	}

	public void install() {
		DrtConfigGroup drtCfg = DrtConfigGroup.get(this.getConfig());
		this.bind(Fleet.class).toProvider(new FleetProvider(drtCfg.getVehiclesFileUrl(this.getConfig().getContext()))).asEagerSingleton();
		this.bind(DrtRequestValidator.class).to(DefaultDrtRequestValidator.class);
		this.bind(BayManager.class).asEagerSingleton();
		this.bind(RebalancingStrategy.class).to(NoRebalancingStrategy.class);
		this.bind(TravelDisutilityFactory.class).annotatedWith(Names.named("drt_optimizer")).toInstance((timeCalculator) -> {
			return new TimeAsTravelDisutility(timeCalculator);
		});
		this.bind(TransitStopHandlerFactory.class).to(DrtAndTransitStopHandlerFactory.class);
		this.addControlerListenerBinding().to(ClearNetworkChangeEvents.class).asEagerSingleton();
		this.addControlerListenerBinding().to(BayManager.class).asEagerSingleton();
		switch(drtCfg.getOperationalScheme()) {
			case door2door:
				this.addRoutingModuleBinding("drt").to(DrtRoutingModule.class).asEagerSingleton();
				break;
			case stopbased:
				Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
				(new TransitScheduleReader(scenario2)).readFile(drtCfg.getTransitStopsFileUrl(this.getConfig().getContext()).getFile());
				this.bind(TransitSchedule.class).annotatedWith(Names.named("drt")).toInstance(scenario2.getTransitSchedule());
				this.bind(MainModeIdentifier.class).to(DrtMainModeIdentifier.class).asEagerSingleton();
				this.addRoutingModuleBinding("drt").to(StopBasedDrtRoutingModule.class).asEagerSingleton();
				break;
			default:
				throw new IllegalStateException();
		}

	}
}
