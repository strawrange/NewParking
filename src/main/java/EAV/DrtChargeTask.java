package EAV;

import Schedule.VehicleImpl;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

public class DrtChargeTask extends StayTaskImpl implements DrtTask {

    Charger charger;

    public DrtChargeTask(double beginTime, double endTime, Charger charger) {
        super(beginTime, endTime, charger.getLink());
        this.charger = charger;
    }

    @Override
    public DrtTaskType getDrtTaskType() {
        return DrtTaskType.STAY;
    }

    public Charger getCharger() {
        return charger;
    }

}
