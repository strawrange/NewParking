package Schedule;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.StayTaskImpl;

public class DrtQuequeTask extends StayTaskImpl implements DrtTask {

    public DrtQuequeTask(double beginTime, double endTime, Link link) {
        super(beginTime, endTime, link);
    }

    @Override
    public DrtTask.DrtTaskType getDrtTaskType() {
        return DrtTaskType.QUEUE;
    }
}
