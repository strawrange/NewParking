package Run.PrePostProcessing;

import ParkingStrategy.MixedParkingStrategy;
import ParkingStrategy.ParkingInDepot.InsertionOptimizer.DrtScheduler;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;

public class AddParkingInVia {

    private static String FOLDER = "/home/biyu/IdeaProjects/NewParking/output/tanjong_pagar_mixed_parking/";
    private static String ITER = "0";
    private static String EVENTSFILE =  FOLDER +  "ITERS/it." + ITER + "/" + ITER + ".events.xml.gz";
    private static String NETWORKSFILE = "/home/biyu/IdeaProjects/NewParking/scenarios/tanjong_pagar/tp_phase4.xml";
    public static void main(String[] args) {
        EventsManager manager = EventsUtils.createEventsManager();
        Network network = NetworkUtils.createNetwork();
        ParkingEventHandler parking = new ParkingEventHandler(manager);
        EventWriterXML eventWriterXML = new EventWriterXML(FOLDER + "output_events_modified.xml");
        manager.addHandler(parking);
        manager.addHandler(eventWriterXML);
        new NetworkReaderMatsimV2(network).readFile(NETWORKSFILE);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
        eventWriterXML.closeFile();
    }
}

class ParkingEventHandler implements ActivityStartEventHandler, ActivityEndEventHandler{
    private EventsManager eventsManager;

    ParkingEventHandler(EventsManager manager){
        this.eventsManager = manager;
    }

    @Override
    public void handleEvent(ActivityStartEvent event) {
        if (event.getActType().equals("DrtStay") ){
            if (event.getTime() > MixedParkingStrategy.dayT0 && event.getTime() < MixedParkingStrategy.dayT1){
                return;
            }
            eventsManager.processEvent(new VehicleEntersTrafficEvent(event.getTime(), event.getPersonId(), event.getLinkId(),  Id.createVehicleId(event.getPersonId()), TransportMode.drt, 1));
            eventsManager.processEvent(new LinkEnterEvent(event.getTime(), Id.createVehicleId(event.getPersonId()), event.getLinkId()));
        }
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals("DrtStay") ){
            if (event.getTime() > MixedParkingStrategy.dayT0 && event.getTime() < MixedParkingStrategy.dayT1){
                return;
            }
            eventsManager.processEvent(new LinkLeaveEvent(event.getTime(), Id.createVehicleId(event.getPersonId()), event.getLinkId()));
            eventsManager.processEvent(new VehicleLeavesTrafficEvent(event.getTime(), event.getPersonId(), event.getLinkId(),  Id.createVehicleId(event.getPersonId()), TransportMode.drt, 1));
        }
    }
}