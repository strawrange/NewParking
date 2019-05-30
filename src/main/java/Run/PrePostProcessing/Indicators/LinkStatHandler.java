    package Run.PrePostProcessing.Indicators;

    import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
    import org.matsim.api.core.v01.Id;
    import org.matsim.api.core.v01.events.LinkEnterEvent;
    import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
    import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
    import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
    import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
    import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;
    import org.matsim.api.core.v01.network.Link;
    import org.matsim.api.core.v01.network.Network;
    import org.matsim.core.network.NetworkUtils;
    import org.matsim.core.network.io.MatsimNetworkReader;
    import org.matsim.core.utils.io.IOUtils;
    import org.matsim.vehicles.Vehicle;

    import java.io.BufferedWriter;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Map;

    import static Run.PrePostProcessing.Indicators.IndicatorsRun.*;

    public class LinkStatHandler implements IndicatorModule,LinkEnterEventHandler, VehicleLeavesTrafficEventHandler, VehicleEntersTrafficEventHandler{
        HashMap<Id<Vehicle>, ArrayList<LinkStat>> linkStat = new HashMap<>();
        Network network;
        String filename = "links_stats.csv";


        public LinkStatHandler(Network network){
            this.network = network;
        }

        @Override
        public void handleEvent(LinkEnterEvent event) {
                if (!linkStat.containsKey(event.getVehicleId())) {
                    linkStat.put(event.getVehicleId(), new ArrayList<>());
                } else {
                    linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).endTime = event.getTime();
                }
                LinkStat l = new LinkStat();
                l.lid = event.getLinkId();
                if (l.lid.toString().endsWith("_HW")) {
                    l.lid = Id.createLinkId(l.lid.toString().replaceAll("_HW", ""));
                }
                if (l.lid.toString().startsWith("nl")) {
                    l.lid = Id.createLinkId(l.lid.toString().replaceAll("nl", ""));
                }
                l.startTime = event.getTime();
                linkStat.get(event.getVehicleId()).add(l);
        }

        public void output(String outputPath) throws IOException {
            BufferedWriter bw = IOUtils.getBufferedWriter(outputPath + filename);
            bw.write("lid;time;avg_speed;linktype");
            Map<Id<Link>, double[]> links = new HashMap<>();
            Map<Id<Link>, double[]> counts = new HashMap<>();
            Map<Id<Link>, ? extends Link> ls = network.getLinks();
            int mb = 1024*1024;
            Runtime runtime = Runtime.getRuntime();
            System.out.println("Used Memory:"
                    + (runtime.totalMemory() - runtime.freeMemory()) / mb);
            System.out.println("Free Memory:"
                    + (runtime.freeMemory()) / mb);
            for (Id<Link> lid: ls.keySet()){
                if (!lid.toString().endsWith("_HW") && !lid.toString().startsWith("nl") && !ls.get(lid).getAllowedModes().contains("subway")) {
                    links.put(lid, new double[30 * 3600]);
                    counts.put(lid, new double[30 * 3600]);
                }
            }
            for (Id<Vehicle> vid: linkStat.keySet()) {
                for (LinkStat l : linkStat.get(vid)) {
                    if (l.entersTrafficT == 0.0 && l.leavesTrafficT != 0.0) {
                        l.endTime = l.leavesTrafficT;
                    }
                    double speed = ls.get(l.lid).getLength() / (l.endTime - l.startTime - l.activityT);
                    if (links.containsKey(l.lid)) {
                        for (int i = (int) l.startTime; i < l.endTime - l.activityT; i++) {
                            links.get(l.lid)[i] += speed;
                            counts.get(l.lid)[i]++;
                        }
                    }
                }
            }
            for (Id<Link> lid: links.keySet()) {
                double sum = 0.0;
                double count = 0.0;
                for (int i = (int) MP_START; i < MP_END; i++) {
                    double speed;
                    if (counts.get(lid)[i] == 0) {
                        speed = ls.get(lid).getFreespeed();
                    } else {
                        speed = links.get(lid)[i] / counts.get(lid)[i];
                    }
                    sum+=speed;
                    count++;
                }
                for (int i = (int) AP_START; i < AP_END; i++) {
                    double speed;
                    if (counts.get(lid)[i] == 0) {
                        speed = ls.get(lid).getFreespeed();
                    } else {
                        speed = links.get(lid)[i] / counts.get(lid)[i];
                    }
                    sum+=speed;
                    count++;
                }
                bw.newLine();
                bw.write(lid + ";peak;" + sum/count + ";");
                if (ls.get(lid).getNumberOfLanes() == 2) {
                    bw.write("Arterial");
                } else if (ls.get(lid).getNumberOfLanes() > 2) {
                    bw.write("Expressway");
                } else {
                    bw.write("Unknown");
                }
                sum = 0.0;
                count = 0.0;
                for (int i = 0; i < MP_START; i++) {
                    double speed;
                    if (counts.get(lid)[i] == 0) {
                        speed = ls.get(lid).getFreespeed();
                    } else {
                        speed = links.get(lid)[i] / counts.get(lid)[i];
                    }
                    sum+=speed;
                    count++;
                }
                for (int i = (int) MP_END; i < AP_START; i++) {
                    double speed;
                    if (counts.get(lid)[i] == 0) {
                        speed = ls.get(lid).getFreespeed();
                    } else {
                        speed = links.get(lid)[i] / counts.get(lid)[i];
                    }
                    sum+=speed;
                    count++;
                }
                for (int i = (int) AP_END; i < END_TIME; i++) {
                    double speed;
                    if (counts.get(lid)[i] == 0) {
                        speed = ls.get(lid).getFreespeed();
                    } else {
                        speed = links.get(lid)[i] / counts.get(lid)[i];
                    }
                    sum+=speed;
                    count++;
                }
                bw.newLine();
                bw.write(lid + ";offpeak;" + sum/count + ";");
                if (ls.get(lid).getNumberOfLanes() == 2) {
                    bw.write("Arterial");
                } else if (ls.get(lid).getNumberOfLanes() > 2) {
                    bw.write("Expressway");
                } else {
                    bw.write("Unknown");
                }
            }
            bw.close();
        }

        /*public void outputLink(String filename) throws IOException {
            BufferedWriter bw = IOUtils.getBufferedWriter(filename);
            bw.write("lid;time;avg_speed;linktype");
            Map<Id<Link>, ? extends Link> ls = network.getLinks();
            int mb = 1024*1024;
            Runtime runtime = Runtime.getRuntime();
            System.out.println("Used Memory:"
                    + (runtime.totalMemory() - runtime.freeMemory()) / mb);
            System.out.println("Free Memory:"
                    + (runtime.freeMemory()) / mb);
            for (Id<Link> lid: ls.keySet()){
                for (int i = 0; i < END_TIME; i++){
                    double meanSpeed = getMeanSpeed(lid, i);
                    bw.newLine();
                    bw.write(lid + ";" + i + ";" + meanSpeed + ";");
                    if (ls.get(lid).getNumberOfLanes() == 2) {
                        bw.write("Arterial");
                    } else if (ls.get(lid).getNumberOfLanes() > 2) {
                        bw.write("Expressways");
                    } else {
                        bw.write("Unknown");
                    }
                }
            }
            bw.close();
        }*/

//        private double getMeanSpeed(Id<Link> lid, int i) {
//            double sumSpeed = 0;
//            double sum=0;
//            for (Id<Vehicle> vid: linkStat.keySet()){
//                for (LinkStat l: linkStat.get(vid)){
//                    if (l.lid.equals(lid)) {
//                        if (l.entersTrafficT == 0.0 && l.leavesTrafficT != 0.0) {
//                            l.endTime = l.leavesTrafficT;
//                        }
//                        if (i >= l.startTime && i < l.endTime - l.activityT) {
//                            sumSpeed += network.getLinks().get(l.lid).getLength() / (l.endTime - l.activityT - l.startTime);
//                            sum++;
//                        }
//                    }
//                }
//            }
//            return sum==0?network.getLinks().get(lid).getFreespeed(): (sumSpeed/sum);
//        }


        @Override
        public void handleEvent(VehicleEntersTrafficEvent event) {
            if (linkStat.containsKey(event.getVehicleId())) {
                linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).entersTrafficT = event.getTime();
                linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).activityT =
                        linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).activityT +
                                linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).entersTrafficT -
                                linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).leavesTrafficT;
            }
        }

        @Override
        public void handleEvent(VehicleLeavesTrafficEvent event) {
            if (linkStat.containsKey(event.getVehicleId())) {
                linkStat.get(event.getVehicleId()).get(linkStat.get(event.getVehicleId()).size() - 1).leavesTrafficT = event.getTime();
            }
        }
    }
    class LinkStat{
        Id<Link> lid;
        double startTime = 0.0;
        double endTime = 30*3600;
        double leavesTrafficT = 0.0;
        double entersTrafficT = 0.0;
        double activityT = 0.0;
    }
