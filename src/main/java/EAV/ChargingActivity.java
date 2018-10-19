package EAV;

import Schedule.VehicleImpl;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.AbstractDynActivity;

public class ChargingActivity extends AbstractDynActivity {

    double endTime;
    VehicleImpl vehicle;

    public ChargingActivity(String activityType, DrtChargeTask task, Charger charger, Vehicle vehicle) {
        super(activityType);
        double chargingTime = charger.getChargingTime((VehicleImpl) vehicle);
        this.endTime = task.getBeginTime() + chargingTime;
        task.setEndTime(endTime);
        this.vehicle = (VehicleImpl) vehicle;
        this.vehicle.changeStatus(false);
        //charger.addVehicle(vehicle, task.getEndTime());
    }

    @Override
    public void doSimStep(double now) {
        vehicle.charge(Level2Charger.CHARGING_RATE_PER_SECOND);
    }

    @Override
    public double getEndTime() {
        return endTime;
    }
}
