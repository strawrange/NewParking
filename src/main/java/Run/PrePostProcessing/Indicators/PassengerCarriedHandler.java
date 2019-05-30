package Run.PrePostProcessing.Indicators;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.vehicles.Vehicle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PassengerCarriedHandler implements IndicatorModule,PersonEntersVehicleEventHandler {
    HashMap<Id<Vehicle>, ArrayList<PC>> passengersCarried = new HashMap<>();
    String filename = "passenger_stats.csv";

    public void output(String outputPath) throws IOException {
        BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
        bw.write("vid;time;num_passengers");
        for (Id<Vehicle> vid: passengersCarried.keySet()){
            for (PC pc: passengersCarried.get(vid)) {
                bw.newLine();
                bw.write(vid + ";" + pc.time +";" + pc.passengerCarried);
            }
        }
        bw.close();
    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent event) {
        if (event.getTime() <= IndicatorsRun.END_TIME) {
            if (!event.getPersonId().toString().startsWith("pt") && !event.getPersonId().toString().startsWith("drt") && !event.getPersonId().toString().equals(event.getVehicleId().toString())) {
                if (!passengersCarried.containsKey(event.getVehicleId())) {
                    passengersCarried.put(event.getVehicleId(), new ArrayList<>());
                }
                passengersCarried.get(event.getVehicleId()).add(new PC(event.getTime(), 1.0));
            }
        }
    }
}
class PC{
    double time = 0.0;
    double passengerCarried = 0.0;

    public PC(double time, double passengerCarried) {
        this.time = time;
        this.passengerCarried = passengerCarried;
    }
}