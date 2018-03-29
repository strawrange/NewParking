# Implementation of different parking strategies for DRT mode

## Overview

The project implements four different parking strategies in DRT contrib, namely, always roaming strategy, parking in depot strategy, parking on the street strategy and mixed strategy. All parking strategies implement interface ParkingStrategy.class which is used in DefaultDrtOptimizer.class. DefaultDrtOptimizer decides when the drt vehicle executes next task. DrtStayTask is considered as parking in drt contrib, therefore, the strategies focus on the DrtStayTask. For all parking strategies, parking and departing are two main method to be implemented.

ParkingStrategy.class:

 
 public interface ParkingStrategy {
     public enum Strategies{
         AlwaysRoaming,
         NoParkingStrategy,
         ParkingInDepot,
         ParkingOntheRoad;
     }
 
     public class ParkingLocation {
         public final Id<Vehicle> vid;
         public final Link link;
 
         public ParkingLocation(Id<Vehicle> vid, Link link) {
             this.vid = vid;
             this.link = link;
         }
     }
 
     /**
      * This method is called at each parking step (interval defined in config).
      */
 
     ParkingLocation parking(Vehicle vehicle, double time);
 
     void departing(Vehicle vehicle, double time);
 


|                     |Parking                                                                                |Departing                                    |
|---------------------|---------------------------------------------------------------------------------------|---------------------------------------------|
|Always roaming       |Relocate to available links based on the demand of pre-defined zones                   |Nothing                                      |
|Parking in the depot |Relocate to the nearest available depot                                                    |Recalculate the empty space of the depot     |
|Parking on the street|Relocate to the nearest available link                                                     |Recalculate the available space of the link  |
|Mixed strategy       |Mixed parking in the depot and on the street strategies according to time and vehicle types                                              |

#### Always roaming
Always roaming strategy means whenever the vehicle is idle (parking somewhere with no passengeres and no comming requests), it starts roaming. In other words, the vehicle can never stop in this strategy.
#### Parking in the depot
In the parking in the depot strategy, whenever the vehicle is idle, it goes back to the nearest available depot. Each depot has a capacity, once the number of vehicles reaches the upper bound, the depot is not considered as available anymore. If the nearest depot is full, the vehicle has to go to the second nearest. One thing need to be noted is that once the vehicle starts to relocate to the depot, it is going to reserve a parking place in the depot, which means that later vehicles cannot occupy this parking place anymore even if they can reach the depot earlier. The vehicle is still allowed to accept new requests on the way to depot and once the request is accepted, the reservation of the parking place is going to be canceled.
#### Parking on the street
Similar to the parking in the depot, parking on the street strategy regard all two-or-more-than-two-lane links as depot, whose capacity is calculated based on the length of the link. Only one lane can be occupied for parking in this strategy, therefore one-lane link is excluded to avoid block the whole link. If the number of vehicles on the link reaches the capacity, the vehicle will drive to one randomly selected outlinks. If the next link is still full, it will go to next one and so on.
#### Mixed parking strategy
In Singapore, the local authorities hope that the parking strategy can be more flexible. Therefore a new mixed strategy is implemented, which is the combination of parking in the depot and parking on the street. Small vehicles (4- or 6-seater) should parking in HDB depot during daytime (7:00 - 20:00) as most HDB depot is relatively empty, therefore parking in the depot strategy is applied for day time. During night, as HDB becomes full and the traffic is not as heavy as during daytime, the AVtaxis should parking on the street. It is noted that at 7:00 or 20:00 all vehicles are force to drive to the depot or street even though they are idle. This is to guarantee that the extra vehicle will not block the street during daytime or occupy the space for private vehicle during night time.
