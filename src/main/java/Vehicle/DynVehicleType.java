package Vehicle;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;


public class DynVehicleType extends VehicleTypeImpl {
    public static String DYNTYPE = "dynType";
    private double accessTime;
    private double egressTime;


    public DynVehicleType(Id<VehicleType> typeId){
        super(typeId);
    }

}
