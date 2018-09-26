package EAV;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

public class DrtChargeTask extends StayTaskImpl implements DrtTask {

    public DrtChargeTask(double beginTime, double endTime, Link link) {
        super(beginTime, endTime, link);
    }

    @Override
    public DrtTaskType getDrtTaskType() {
        return null;
    }
}
