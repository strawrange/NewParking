package ParkingStrategy.AlwaysRoaming;

import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.DrtZonalSystem;
import ParkingStrategy.AlwaysRoaming.ZoneBasedRoaming.ZonalDemandAggregator;
import ParkingStrategy.ParkingStrategy;
import com.google.inject.name.Named;
import org.apache.commons.lang3.mutable.MutableInt;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.network.NetworkUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class RoamingStrategy implements ParkingStrategy, MobsimBeforeSimStepListener {
    private DrtZonalSystem zonalSystem;
    private Network network;
    private ArrayList<Link> selectableLinks = new ArrayList();
    private ZonalDemandAggregator demandAggregator;
    ArrayList<Double> totalWeight;
    ArrayList<String> zoneidlist;

    @Inject
    public RoamingStrategy( ZonalDemandAggregator demandAggregator, DrtZonalSystem zonalSystem,
                           @Named(DvrpRoutingNetworkProvider.DVRP_ROUTING) Network network) {
        this.demandAggregator = demandAggregator;
        this.zonalSystem = zonalSystem;
        this.network = network;
        for (Link link : network.getLinks().values()){
            if (link.getAllowedModes().contains(TransportMode.car)){
                selectableLinks.add(link);
            }
        }
    }

    @Override
    public ParkingLocation Parking(Vehicle vehicle) {
        if (zoneidlist == null || zoneidlist.size() == 0) {
            Random random = new Random();
            Link randomLink = selectableLinks.get(random.nextInt(selectableLinks.size()));
                return new ParkingLocation(vehicle.getId(), randomLink);
        }
            // Now choose a random item
        double random = Math.random();
        int low = 0;
        int high = totalWeight.size() - 1;
        while(low  < high){
            int mid = (low + high) >>> 1;
            if (random < totalWeight.get(mid)) {
                high = mid - 1;
            }else if (random > totalWeight.get(mid)){
                low = mid  + 1;
            }else{
                high = mid;
            }
        }
        return new ParkingLocation(vehicle.getId(), NetworkUtils.getNearestLink(network, zonalSystem.getZoneCentroid(zoneidlist.get(low))));
    }

    private void calculateProbability(double time) {
        totalWeight = new ArrayList<>();
        zoneidlist = new ArrayList<>();
        Map<String, MutableInt> expectedDemand = demandAggregator.getExpectedDemandForTimeBin(time);
        if (expectedDemand == null) {
            return;
        }
        final MutableInt totalDemand = new MutableInt(0);
        expectedDemand.values().forEach(demand -> totalDemand.add(demand.intValue()));
        if (totalDemand.doubleValue() == 0){
            return;
        }
        // Logger.getLogger(getClass()).info("Rebalancing at "+Time.writeTime(time)+" vehicles: " +
        // rebalancableVehicles.size()+ " expected demand :"+totalDemand.toString());
        totalWeight.add(0.D);
        for (Map.Entry<String, MutableInt> entry : expectedDemand.entrySet()) {
            double demand = entry.getValue().doubleValue();
            if (demand != 0) {
                double probPerZone = demand / totalDemand.doubleValue();
                zoneidlist.add(entry.getKey());
                double cumulativeProb = probPerZone + totalWeight.get(totalWeight.size() - 1);
                totalWeight.add(cumulativeProb);
            }
        }
        totalWeight.remove(totalWeight.size() - 1);
    }

    @Override
    public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent event) {
        if (event.getSimulationTime() % ZonalDemandAggregator.binsize == 0){
            calculateProbability(event.getSimulationTime());
        }
    }

//    private Map<String, Double> calculateZonalVehicleRequirementsProbability(double time) {
//        Map<String, MutableInt> expectedDemand = demandAggregator.getExpectedDemandForTimeBin(time + 60);
//        if (expectedDemand == null) {
//            return new HashMap<>();
//        }
//        final MutableInt totalDemand = new MutableInt(0);
//        expectedDemand.values().forEach(demand -> totalDemand.add(demand.intValue()));
//        // Logger.getLogger(getClass()).info("Rebalancing at "+Time.writeTime(time)+" vehicles: " +
//        // rebalancableVehicles.size()+ " expected demand :"+totalDemand.toString());
//        Map<String, Double> requiredAdditionalVehiclesPerZone = new HashMap<>();
//        for (Map.Entry<String, MutableInt> entry : expectedDemand.entrySet()) {
//            double demand = entry.getValue().doubleValue();
//            double probPerZone = demand / totalDemand.doubleValue();
//            requiredAdditionalVehiclesPerZone.put(entry.getKey(), probPerZone);
//        }
//        return requiredAdditionalVehiclesPerZone;
//
//    }
}
