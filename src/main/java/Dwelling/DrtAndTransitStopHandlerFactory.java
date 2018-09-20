package Dwelling;



import BayInfrastructure.BayManager;
import com.google.inject.Inject;
import org.matsim.core.mobsim.qsim.pt.TransitStopHandlerFactory;
import org.matsim.vehicles.Vehicle;


public class DrtAndTransitStopHandlerFactory implements TransitStopHandlerFactory {
    @Inject
    private BayManager bayManager;
    @Override
    public org.matsim.core.mobsim.qsim.pt.TransitStopHandler createTransitStopHandler(Vehicle vehicle) {
        return new FixedTransitStopHandler(vehicle, bayManager);
    }

}