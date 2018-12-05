/**
 * 
 */
package mobsim.qnetsimenginelong;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLaneI;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

/**
 * @author kainagel
 *
 */
public interface TurnAcceptanceLogic {
	
	enum AcceptTurn { GO, WAIT, ABORT }

	AcceptTurn isAcceptingTurn(Link currentLink, QLaneI currentLane, Id<Link> nextLinkId, QVehicle veh, QNetwork qNetwork);

}
