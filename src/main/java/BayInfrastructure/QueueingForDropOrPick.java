package BayInfrastructure;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.vehicles.Vehicle;

import java.util.Map;

public class QueueingForDropOrPick implements VehicleArrivesAtFacilityEventHandler{

    Map<Id<Vehicle>, MobsimVehicle> qvehicles;
    BayManager bayManager;
    @Inject
    QSim qsim;

    @Inject
    public QueueingForDropOrPick(QSim qSim, BayManager bayManager, EventsManager eventsManager){
        this.qsim = qSim;
        //this.qvehicles = qSim.getVehicles();
        this.bayManager = bayManager;
        eventsManager.addHandler(this);

    }

    @Override
    public void handleEvent(VehicleArrivesAtFacilityEvent event) {
        qvehicles = qsim.getVehicles();
        System.out.println();
    }
}
