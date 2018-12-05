package Run.PrePostProcessing;

import ParkingStrategy.ParkingInDepot.Depot.Depot;
import ParkingStrategy.ParkingInDepot.Depot.DepotImpl;
import ParkingStrategy.ParkingInDepot.Depot.DepotReader;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
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
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.api.core.v01.network.Link;
import org.matsim.vehicles.VehicleSchemaV1Names;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class GetDepotsFromTable {

    public static void main(String[] args) throws IOException {
        Network network = NetworkUtils.createNetwork();
        Network cleanNetwork = NetworkUtils.createNetwork();
        new NetworkReaderMatsimV2(network).readFile("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/mp_c_tp_2018.xml");
        (new TransportModeNetworkFilter(network)).filter(cleanNetwork, Collections.singleton("car"));
        new NetworkCleaner().run(cleanNetwork);
        BufferedReader reader = IOUtils.getBufferedReader("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/depot.csv");
        reader.readLine();
        String line = reader.readLine();
        DepotWriter dw = new DepotWriter("/home/biyu/IdeaProjects/NewParking/scenarios/mp_c_tp/depot_nus.xml");
        dw.writeStart();
        while(line!=null) {
            String[] atts = line.split(",");
            Id<Depot> id = Id.create(atts[0], Depot.class);
            Coord coord_old = new Coord(Double.valueOf(atts[1]), Double.valueOf(atts[2]));
            CoordinateTransformation transformation = new GeotoolsTransformation(TransformationFactory.WGS84, TransformationFactory.WGS84_UTM48N);
            Coord coord = transformation.transform(coord_old);
            Link link = NetworkUtils.getNearestLinkExactly(cleanNetwork,coord);
            Depot depot = new DepotImpl(id,link,Double.valueOf(atts[3]));
            dw.writeDepot(depot);
            line = reader.readLine();
        }
        dw.writeEndTag();
    }

}

class DepotWriter extends MatsimXmlWriter{
    private final String file;
    private List<Tuple<String, String>> atts = new ArrayList<Tuple<String, String>>();
    public DepotWriter(String file) {
        this.file = file;
    }

    public void writeStart() {
        this.openFile(file);
        this.writeXmlHead();
        this.writeStartTag("depots",atts);
    }


    public void writeDepot(Depot depot){
        atts.clear();
        atts.add(this.createTuple("id", depot.getId().toString()));
        atts.add(this.createTuple("link",depot.getLink().getId().toString()));
        atts.add(this.createTuple("capacity", depot.getCapacity()));
        atts.add(this.createTuple("type", depot.getDepotType().toString()));
        this.writeStartTag("depot", atts, true);
    }

    public void writeEndTag(){
        this.writeEndTag("depots");
        this.close();
    }
}

