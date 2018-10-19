package EAV;

import ParkingStrategy.ParkingInDepot.Depot.HDBDepot;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.data.file.ReaderUtils;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.Stack;

public class ChargerReader extends MatsimXmlParser {

    private static final String CHARGER = "charger";
    private static final int DEFAULT_CAPACITY = 0;
    private final ChargerManager chargerManager;
    private final Network network;


    public ChargerReader(ChargerManager chargerManager, Network network) {
        this.chargerManager = chargerManager;
        this.network = network;
    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if (CHARGER.equals(name)) {
            Charger charger = createCharger(atts);
            chargerManager.addCharger(charger);
        }
    }

    @Override
    public void endTag(String name, String content, Stack<String> context) {
    }

    private Charger createCharger(Attributes atts) {
        Id<Charger> id = Id.create(atts.getValue("id"), Charger.class);
        Link link = network.getLinks().get(Id.createLinkId(atts.getValue("link")));
        //TODO: Create specific links for depots
        int capacity = ReaderUtils.getInt(atts, "capacity", DEFAULT_CAPACITY);
        switch (atts.getValue("mode")){
            case ("level2"):
                return new Level2Charger(id, link, capacity);
            case ("fast"):
                return new FastCharger(id, link, capacity);
            default:
                throw new RuntimeException("Wrong input charger type!");
        }
    }

}
