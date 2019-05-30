package Run.PrePostProcessing.ChargingAndParking;

import EAV.Charger;
import EAV.FastCharger;
import EAV.Level2Charger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.MatsimXmlWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GetChargersFromTable {

    public static void main(String[] args) throws IOException {
        Network network = NetworkUtils.createNetwork();
        Network cleanNetwork = NetworkUtils.createNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        (new TransportModeNetworkFilter(network)).filter(cleanNetwork, Collections.singleton("car"));
        new NetworkCleaner().run(cleanNetwork);
        BufferedReader reader = IOUtils.getBufferedReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/depot_nus_20181220.csv");
        reader.readLine();
        String line = reader.readLine();
        ChargerWriter dw = new ChargerWriter("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/charger_nus_20181220.xml");
        dw.writeStart();
        while(line!=null) {
            String[] atts = line.split(",");
            Coord coord_old = new Coord(Double.valueOf(atts[1]), Double.valueOf(atts[2]));
            CoordinateTransformation transformation = new GeotoolsTransformation(TransformationFactory.WGS84, TransformationFactory.WGS84_UTM48N);
            Coord coord = transformation.transform(coord_old);
            Link link = NetworkUtils.getNearestLinkExactly(cleanNetwork,coord);
            int capacityFast = Integer.valueOf(atts[3]);
            if (capacityFast != 0.0){
                if (String.valueOf(atts[5]).equals("street")) {
                    Id<Charger> id = Id.create("street_fast_" + atts[0], Charger.class);
                    Charger charger = new FastCharger(id, link, capacityFast, 7*3600,20*3600,true);
                    dw.writeCharger(charger);
                }else if (String.valueOf(atts[5]).equals("HDB")){
                    Id<Charger> id1 = Id.create("HDB_fast_" + atts[0], Charger.class);
                    Id<Charger> id2 = Id.create("HDB_fast_2_" + atts[0], Charger.class);
                    Charger charger1 = new FastCharger(id1, link, capacityFast, 0*3600, 7*3600, false);
                    Charger charger2 = new FastCharger(id2, link, capacityFast, 20*3600, 30*3600, false);
                    dw.writeCharger(charger1);
                    dw.writeCharger(charger2);
                }else if (String.valueOf(atts[5]).equals("depot")){
                    Id<Charger> id = Id.create("depot_fast_" + atts[0], Charger.class);
                    Charger charger = new FastCharger(id, link, capacityFast, 0*3600,30*3600,false);
                    dw.writeCharger(charger);
                }else{
                    throw new RuntimeException("Wrong depot type!");
                }
            }
            int capacityNormal = Integer.valueOf(atts[4]);
            if (capacityNormal != 0.0){
                if (String.valueOf(atts[5]).equals("street")) {
                    Id<Charger> id = Id.create("street_level2_" + atts[0], Charger.class);
                    Charger charger = new Level2Charger(id, link, capacityNormal, 7*3600,20*3600,true);
                    dw.writeCharger(charger);
                }else if (String.valueOf(atts[5]).equals("HDB")){
                    Id<Charger> id1 = Id.create("HDB_level2_" + atts[0], Charger.class);
                    Id<Charger> id2 = Id.create("HDB_level2_2_" + atts[0], Charger.class);
                    Charger charger1 = new Level2Charger(id1, link, capacityNormal, 0*3600, 7*3600, false);
                    Charger charger2 = new Level2Charger(id2, link, capacityNormal, 20*3600, 30*3600, false);
                    dw.writeCharger(charger1);
                    dw.writeCharger(charger2);
                }else if (String.valueOf(atts[5]).equals("depot")){
                    Id<Charger> id = Id.create("depot_level2_" + atts[0], Charger.class);
                    Charger charger = new Level2Charger(id, link, capacityNormal, 0*3600,30*3600,false);
                    dw.writeCharger(charger);
                }else{
                    throw new RuntimeException("Wrong depot type!");
                }
            }
            line = reader.readLine();
        }
        dw.writeEndTag();
    }

}

class ChargerWriter extends MatsimXmlWriter{
    private final String file;
    private List<Tuple<String, String>> atts = new ArrayList<Tuple<String, String>>();
    public ChargerWriter(String file) {
        this.file = file;
    }

    public void writeStart() {
        this.openFile(file);
        this.writeXmlHead();
        this.writeStartTag("chargers",atts);
    }


    public void writeCharger(Charger charger){
        atts.clear();
        atts.add(this.createTuple("id", charger.getId().toString()));
        atts.add(this.createTuple("link",charger.getLink().getId().toString()));
        atts.add(this.createTuple("capacity", charger.getCapacity()));
        atts.add(this.createTuple("mode", charger.getChargerMode().toString()));
        atts.add(this.createTuple("start_time", charger.getStartTime()));
        atts.add(this.createTuple("end_time", charger.getEndTime()));
        atts.add(this.createTuple("is_blocking", charger.isBlocking()));
        this.writeStartTag("charger", atts, true);
    }

    public void writeEndTag(){
        this.writeEndTag("chargers");
        this.close();
    }
}

