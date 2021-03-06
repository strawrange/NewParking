/* *********************************************************************** *
 * project: org.matsim.*
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

package Vehicle;


import EAV.DischargingRate;
import Run.AtodConfigGroup;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Fleet;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentSource;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.vehicles.VehicleType;

import java.net.URL;

/**
 * @author michalm
 */
public class FleetProvider implements Provider<Fleet> {
	@Inject
	@Named(DvrpRoutingNetworkProvider.DVRP_ROUTING)
	Network network;
	@Inject
	Config config;

	private final URL url;

	public FleetProvider(URL url) {
		this.url = url;
	}

	@Override
	public Fleet get() {
		FleetImpl fleet = new FleetImpl();
		new VehicleReader(network, fleet, AtodConfigGroup.get(config).getDrtVehicleTypeFileURL(config.getContext())).parse(url);
		return fleet;
	}

	public static AbstractModule createModule(URL url) {
		return new AbstractModule() {
			@Override
			public void install() {
				bind(Fleet.class).toProvider(new FleetProvider(url)).asEagerSingleton();
			}
		};
	}
}