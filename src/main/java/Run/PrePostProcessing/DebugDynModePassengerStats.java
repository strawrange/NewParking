package Run.PrePostProcessing;

import com.google.inject.Inject;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;

import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.vehicles.Vehicle;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class DebugDynModePassengerStats {
    private static String FOLDER;
    private static String ITER = "0";
    private static String EVENTSFILE;
    public static void main(String[] args) throws IOException {
        FOLDER ="/home/biyu/IdeaProjects/NewParking/output/drt_mix_V1500_max_debug_badguys/ITERS/";
        EVENTSFILE = FOLDER + "it." + ITER + "/" + ITER + ".events.xml.gz";
        EventsManager manager = EventsUtils.createEventsManager();
        Network network = NetworkUtils.createNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        DynModePassengerStats handler = new DynModePassengerStats(network, DrtConfigGroup.DRT_MODE);
        manager.addHandler(handler);
        new MatsimEventsReader(manager).readFile(EVENTSFILE);
    }
}

class DynModePassengerStats implements PersonEntersVehicleEventHandler, PersonDepartureEventHandler,
        PersonArrivalEventHandler, LinkEnterEventHandler, ActivityEndEventHandler, BasicEventHandler {

    final private Map<Id<Person>, Double> departureTimes = new HashMap<>();
    final private Map<Id<Person>, Id<Link>> departureLinks = new HashMap<>();
    final private List<DynModeTrip> drtTrips = new ArrayList<>();
    final private Map<Id<Vehicle>, Map<Id<Person>, MutableDouble>> inVehicleDistance = new HashMap<>();
    final private Map<Id<Vehicle>, double[]> vehicleDistances = new HashMap<>();
    final private Map<Id<Person>, DynModeTrip> currentTrips = new HashMap<>();
    final private Map<Id<Person>, Double> unsharedDistances = new HashMap<>();
    final private Map<Id<Person>, Double> unsharedTimes = new HashMap<>();
    final String mode;

    final private Network network;
	/*
	 * (non-Javadoc)
	 *
	 * @see org.matsim.core.events.handler.EventHandler#reset(int)
	 */

    /**
     *
     */
    @Inject
    public DynModePassengerStats(Network network, EventsManager events, Config config) {
        this.mode = ((DvrpConfigGroup)config.getModules().get(DvrpConfigGroup.GROUP_NAME)).getMode();
        this.network = network;
        events.addHandler(this);
    }

    public DynModePassengerStats(Network network, String mode) {
        this.mode = mode;
        this.network = network;
    }

    @Override
    public void reset(int iteration) {
        drtTrips.clear();
        departureTimes.clear();
        departureLinks.clear();
        inVehicleDistance.clear();
        currentTrips.clear();
        vehicleDistances.clear();
        unsharedDistances.clear();
        unsharedTimes.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.matsim.api.core.v01.events.handler.ActivityEndEventHandler#handleEvent(org.matsim.api.core.v01.events.
     * ActivityEndEvent)
     */
    @Override
    public void handleEvent(ActivityEndEvent event) {
        if (event.getActType().equals(VrpAgentLogic.BEFORE_SCHEDULE_ACTIVITY_TYPE)) {
            Id<Vehicle> vid = Id.createVehicleId(event.getPersonId().toString());
            this.inVehicleDistance.put(vid, new HashMap<Id<Person>, MutableDouble>());
            this.vehicleDistances.put(vid, new double[3]);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.matsim.api.core.v01.events.handler.LinkEnterEventHandler#handleEvent(org.matsim.api.core.v01.events.
     * LinkEnterEvent)
     */
    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (inVehicleDistance.containsKey(event.getVehicleId())) {
            double distance = network.getLinks().get(event.getLinkId()).getLength();
            for (MutableDouble d : inVehicleDistance.get(event.getVehicleId()).values()) {
                d.add(distance);
            }
            this.vehicleDistances.get(event.getVehicleId())[0] += distance; // overall distance drive
            this.vehicleDistances.get(event.getVehicleId())[1] += distance
                    * inVehicleDistance.get(event.getVehicleId()).size(); // overall revenue distance
            if (inVehicleDistance.get(event.getVehicleId()).size() > 0) {
                this.vehicleDistances.get(event.getVehicleId())[2] += distance; // overall occupied distance
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler#handleEvent(org.matsim.api.core.v01.events.
     * PersonArrivalEvent)
     */
    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (event.getLegMode().equals(mode)) {
            DynModeTrip trip = currentTrips.remove(event.getPersonId());
            if (trip != null) {
                double distance = inVehicleDistance.get(trip.getVehicle()).remove(event.getPersonId()).doubleValue();
                trip.setTravelDistance(distance);
                trip.setArrivalTime(event.getTime());
                trip.setToLink(event.getLinkId());
                Coord toCoord = this.network.getLinks().get(event.getLinkId()).getCoord();
                trip.setToCoord(toCoord);
                trip.setInVehicleTravelTime(event.getTime() - trip.getDepartureTime() - trip.getWaitTime());
            } else {
                throw new NullPointerException("Arrival without departure?");
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler#handleEvent(org.matsim.api.core.v01.events.
     * PersonDepartureEvent)
     */
    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLegMode().equals(mode)) {
            this.departureTimes.put(event.getPersonId(), event.getTime());
            this.departureLinks.put(event.getPersonId(), event.getLinkId());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler#handleEvent(org.matsim.api.core.v01.events
     * .PersonEntersVehicleEvent)
     */
    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (this.departureTimes.containsKey(event.getPersonId())) {
            double departureTime = this.departureTimes.remove(event.getPersonId());
            double waitTime = event.getTime() - departureTime;
            Id<Link> departureLink = this.departureLinks.remove(event.getPersonId());
            double unsharedDistance = this.unsharedDistances.remove(event.getPersonId());
            double unsharedTime = this.unsharedTimes.remove(event.getPersonId());
            Coord departureCoord = this.network.getLinks().get(departureLink).getCoord();
            DynModeTrip trip = new DynModeTrip(departureTime, event.getPersonId(), event.getVehicleId(), departureLink,
                    departureCoord, waitTime);
            trip.setUnsharedDistanceEstimate_m(unsharedDistance);
            trip.setUnsharedTimeEstimate_m(unsharedTime);
            this.drtTrips.add(trip);
            this.currentTrips.put(event.getPersonId(), trip);
            this.inVehicleDistance.get(event.getVehicleId()).put(event.getPersonId(), new MutableDouble());
        }
    }

    /**
     * @return the drtTrips
     */
    public List<DynModeTrip> getDrtTrips() {
        return drtTrips;
    }

    /**
     * @return the vehicleDistances
     */
    public Map<Id<Vehicle>, double[]> getVehicleDistances() {
        return vehicleDistances;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler#handleEvent(org.matsim.contrib.drt.
     * passenger.events.DrtRequestScheduledEvent)
     */
    @Override
    public void handleEvent(Event event) {
        if (event.getEventType().equals("DrtRequest submitted")) {
            this.unsharedDistances.put(Id.createPersonId(event.getAttributes().get("person")),Double.valueOf(event.getAttributes().get("unsharedRideDistance")));
            this.unsharedTimes.put(Id.createPersonId(event.getAttributes().get("person")), Double.valueOf(event.getAttributes().get("unsharedRideTime")));
        }
    }
}

class DynModeTrip implements Comparable<org.matsim.contrib.drt.analysis.DynModeTrip> {
    private final double departureTime;
    private final Id<Person> person;
    private final Id<Vehicle> vehicle;
    private final Id<Link> fromLinkId;
    private final double waitTime;
    private double travelTime = Double.NaN;
    private double travelDistance_m = Double.NaN;
    private double unsharedDistanceEstimate_m = Double.NaN;
    private double unsharedTimeEstimate_m = Double.NaN;
    private Id<Link> toLink = null;
    private double arrivalTime = Double.NaN;
    private final Coord fromCoord;
    private Coord toCoord = null;
    private final DecimalFormat format;


    static final String demitter = ";";
    public static final String HEADER = "departureTime" + demitter + "personId" + demitter + "vehicleId" + demitter
            + "fromLinkId" + demitter + "fromX" + demitter + "fromY" + demitter + "toLinkId" + demitter + "toX"
            + demitter + "toY" + demitter + "waitTime" + demitter + "arrivalTime" + demitter + "travelTime" + demitter
            + "travelDistance_m"+demitter+"direcTravelDistance_m";

    DynModeTrip(double departureTime, Id<Person> person, Id<Vehicle> vehicle, Id<Link> fromLinkId, Coord fromCoord,
                double waitTime) {
        this.departureTime = departureTime;
        this.person = person;
        this.vehicle = vehicle;
        this.fromLinkId = fromLinkId;
        this.fromCoord = fromCoord;
        this.waitTime = waitTime;

        this.format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(false);
    }

    public Double getDepartureTime() {
        return departureTime;
    }

    public Id<Person> getPerson() {
        return person;
    }


    public void setUnsharedDistanceEstimate_m(double unsharedDistanceEstimate_m) {
        this.unsharedDistanceEstimate_m = unsharedDistanceEstimate_m;
    }

    public void setUnsharedTimeEstimate_m(double unsharedTimeEstimate_m) {
        this.unsharedTimeEstimate_m = unsharedTimeEstimate_m;
    }

    public Id<Vehicle> getVehicle() {
        return vehicle;
    }

    public Id<Link> getFromLinkId() {
        return fromLinkId;
    }

    public double getWaitTime() {
        return waitTime;
    }

    public double getInVehicleTravelTime() {
        return travelTime;
    }

    public void setInVehicleTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }

    public double getTravelDistance() {
        return travelDistance_m;
    }


    public double getUnsharedDistanceEstimate_m() {
        return unsharedDistanceEstimate_m;
    }

    public double getUnsharedTimeEstimate_m() {
        return unsharedTimeEstimate_m;
    }

    public void setTravelDistance(double travelDistance_m) {
        this.travelDistance_m = travelDistance_m;
    }

    public Id<Link> getToLinkId() {
        return toLink;
    }

    public void setToLink(Id<Link> toLink) {
        this.toLink = toLink;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Coord getToCoord() {
        return toCoord;
    }

    public void setToCoord(Coord toCoord) {
        this.toCoord = toCoord;
    }

    public Coord getFromCoord() {
        return fromCoord;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(org.matsim.contrib.drt.analysis.DynModeTrip o) {
        return getDepartureTime().compareTo(o.getDepartureTime());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        double fromCoordX = Double.NaN;
        double fromCoordY = Double.NaN;

        double toCoordX = Double.NaN;
        double toCoordY = Double.NaN;
        if (toCoord != null) {
            toCoordX = toCoord.getX();
            toCoordY = toCoord.getY();
        }
        if (fromCoord != null) {
            fromCoordX = fromCoord.getX();
            fromCoordY = fromCoord.getY();
        }
        return getDepartureTime() + demitter + getPerson() + demitter + getVehicle() + demitter + getFromLinkId()
                + demitter + format.format(fromCoordX) + demitter + format.format(fromCoordY) + demitter + getToLinkId() + demitter + format.format(toCoordX)
                + demitter + format.format(toCoordY) + demitter + getWaitTime() + demitter + getArrivalTime() + demitter
                + getInVehicleTravelTime() + demitter + format.format(getTravelDistance())+ demitter+ format.format(unsharedDistanceEstimate_m);
    }

}