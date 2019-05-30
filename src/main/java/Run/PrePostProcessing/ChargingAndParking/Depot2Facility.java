package Run.PrePostProcessing.ChargingAndParking;

import ParkingStrategy.MixedParkingStrategy;
import ParkingStrategy.ParkingInDepot.Depot.Depot;
import ParkingStrategy.ParkingInDepot.Depot.DepotManager;
import ParkingStrategy.ParkingInDepot.Depot.DepotManagerSameDepot;
import ParkingStrategy.ParkingInDepot.Depot.DepotReader;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.facilities.*;

import java.util.Random;

public class Depot2Facility {

    public static void main(String[] args) {
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_20190215.xml");
        DepotManager depotManager = new DepotManagerSameDepot();
        new DepotReader(depotManager, network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/depot_nus_20181220.xml");
        ActivityFacilitiesFactory activityFacilitiesFactory = new ActivityFacilitiesFactoryImpl();
        ActivityFacilities facilities = FacilitiesUtils.createActivityFacilities();
        facilities.setName("parking");
        for (Depot depot: depotManager.getDepots().values()) {
            ActivityFacility activityFacility = activityFacilitiesFactory.createActivityFacility(Id.create(depot.getId(), ActivityFacility.class), depot.getLink().getCoord(), depot.getLink().getId());
            switch (depot.getDepotType()) {
                case DEPOT:
                    ActivityOption activityOption = new ActivityOptionImpl("DEPOT");
                    activityOption.setCapacity(depot.getCapacity());
                    activityOption.addOpeningTime(new OpeningTimeImpl(0, 30 * 3600));
                    activityFacility.getActivityOptions().put(activityOption.getType(), activityOption);
                    break;
                case HDB:
                    ActivityOption activityOption2 = new ActivityOptionImpl("HDB");
                    activityOption2.setCapacity(depot.getCapacity());
                    activityOption2.addOpeningTime(new OpeningTimeImpl(MixedParkingStrategy.dayT0, MixedParkingStrategy.dayT1));
                    activityFacility.getActivityOptions().put(activityOption2.getType(), activityOption2);
                    break;
                default:
                    throw new RuntimeException("Wrong depot type!");
            }
            facilities.addActivityFacility(activityFacility);
        }
        new FacilitiesWriter(facilities).write("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/depot_nus_20181220_facility.xml");
    }
}
