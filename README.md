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

## Demand-based roaming
![alt text](http://url/to/img.png)
Demand-based roaming is similar to the roaming of taxis, which always roam and tend to drive to anticipated high demand areas when idle. The strategy works by recording demand from previous iterations at a zonal level, where the study area is divided into several zones according to a predefined cell size. In each iteration of the MATSim mobility simulation, the number of departures in each zone will be counted every 30 minutes and saved as a demand estimate for the next iteration. During the simulation, once a vehicle is idle, it will choose an adjacent destination zone through weighted sampling of anticipated demand. Within the destination zone, the vehicle performs a random walk and repeats the above process until it is assigned a passenger. To simplify and characterize each strategy, vehicles in this strategy will never stop, even if the expected demand of all zones is zero, in which case vehicles will randomly choose a zone to roam.

This (non-)parking strategy is the most prevalent for taxis and ride-sharing services in reality. 
Although in reality, no car really roams for 24 hours because drivers need a break, it is still an important strategy to consider for AVs, as it represents a corner solution to possible deployments. 
The strategy increases the probability for drivers to be matched with passengers as well as dynamically relocating vehicles depending on demand; while at the same time, it increases empty mileage. 
In the project, the simulation aims at finding out whether the other centralized parking strategies can replace the conventional roaming strategy with comparable service and less empty traveling. 
## Parking in the depot
Parking in depots means that the vehicle always drives back to a depot once it is empty but can still accept a new request either on the way or in the depot. There are several depots in the system, once the nearest depot is full, the vehicle has to drive to the second nearest depot and so on. After choosing the nearest available depot, the parking place in the depot will be reserved for the vehicle, and once the vehicle accepts a new request, the reserved parking place will be released.

This strategy is similar to existing public transit parking strategies, which relocates vehicles away from the dense downtown area with high parking cost and limited supply. 
Depots can be located in peripheral areas and the strategy can totally free up dense urban space from parking. 
Its disadvantage is extra empty travel time and distance from depots to the origin of the traveler and from the destination of the traveler to the depots, as well as the cost of purpose-built parking space. 
## Parking on the street
Parking on the street is similar to the existing parking strategy of private vehicles in many cities. Vehicles park in the nearest available road space to the trip destination. In the strategy, the parking lot is the street and AV buses will wait on the street for the next request once empty. Parking on the street will block one lane and the capacity of the street will be reduced accordingly once it is occupied for parking by at least one vehicle. Later, if all parking vehicles leave the link, the number of lanes and the capacity of the link will recover. Some streets are not allowed for parking, such as one-lane streets, and streets with transit stops. The number of parking spaces along the street depends on the length of the street and the size of the vehicle, thus offering more spaces for smaller vehicles. If the current link is not allowed for parking, the vehicle will randomly choose one link among all next links to cruise for parking till it reaches an available space. 

Parking on the street saves space of parking depots, but may cause congestion, which is why it is discouraged in many urban areas. However, as the shared autonomous vehicle fleet is significantly smaller than that of a private vehicle fleet, and we assume that only these transit vehicles are allowed to use parking on the street, we expect that it will not produce a significant reduction in capacity relative to the fleet size.
## Mixed parking strategy
The mixed parking strategy can switch between both parking on the street and in depots depending on the time of day. 
It can be adjusted to minimize the impact on traffic flow by street parking and the space required for depots. 
AVs will stay on the street for overnight parking (20:00 - 07:00) but will switch to the depot strategy during the daytime (07:00 - 20:00). At 7:00 in the morning, all idle vehicles will move to the nearest available residential parking lot; while at 20:00 in the evening, all idle vehicles will move to the nearest available street. 
From 20:00 to 07:00, the traffic is not as heavy as in the daytime, so AVs can park on the street to save parking space; while from 07:00-20:00, when traffic on the street is heavy, but most private vehicles in the parking lot in residential areas are gone for work, AVs may employ the parking lot in the residential area as depots. 

It should be noted that parking on the street and in depots strategy in the mixed strategy is slightly different from the two strategies in previous sections. Only small-size AV buses are allowed to park on the street in the mixed strategy while in parking on the street strategy, all vehicles are allowed to park. Furthermore, small-sized AV buses can park in small depots in residential areas, while with the parking in depots strategy, all vehicles have to go to the peripheral depot. Big AV buses can only park in the peripheral depot throughout the day in the mixed parking strategy. 

The mixed parking strategy tries to take advantage of both road and depot strategies with dynamic traffic management. It is expected that the strategy has less operational cost than the depot strategy but will produce better user experience than the road strategy, as less congestion may result.

# Implementation of bay size restriction for DRT mode
The design of the stop bay is not considered in the current default MATSim transit implementation, but it will be very important for AV buses. In the default MATSim there is only one attribute, \text{isBlockLane}, which denotes whether a bus stop blocks a lane of traffic or has a bay of infinite capacity. In reality, bus bays indeed have a limited capacity, leading to bus bunching when that capacity is exceeded. In Singapore, bus bays usually can accommodate 1-3 buses.

Bay size will definitely influence traffic flow. Consider the example shown in \cref{fig:baysize}: the dwelling time of the first vehicle is 10s and the second vehicle is 15s. If there is no bay, one lane of the road will be blocked for 10s + 15s = 25s; while if there is a bay big enough for the first vehicle, the lane will be blocked for only 10s; while if there is a bay enough for both vehicles, the lane will not be blocked. Considering that pick-up or drop-off points are widely distributed in the urban network and AV bus services are quite frequent, the influence of bay size design on traffic flow of the whole network cannot be negligible. Once the bay length cannot satisfy all dwelling vehicles, queuing vehicles will block one lane of the road. 
Different bay sizes enables different combination of dwelling vehicles.

In the simulation, a bay manager is implemented to manage both AV bus and public transit. A bay in the simulation consists of a transit stop, bay length and information of the queuing vehicles. Whenever an AV bus arrives at the facility, it will register at the corresponding transit stop and check whether there is still space to dwell. If the bay is full, the vehicle will queue and a network change event will be processed to change the capacity and the number of lanes of the link. As long as there is no vehicle queuing, the capacity and the number of lanes of the link will recover immediately. Vehicles will follow a "first come, first served" rule, and jumping the queue is not allowed. 
