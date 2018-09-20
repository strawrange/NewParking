/* *********************************************************************** *
 * project: org.matsim.*
 * WaitTimeCalculator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package firstLastAVPTRouter.waitLinkTime;

import java.io.Serializable;

/**
 * Structure for saving waiting times
 * 
 * @author sergioo
 */

public interface WaitLinkTimeData extends Serializable {

	//Methods
	void resetWaitLinkTimes();
	void addWaitLinkTime(final int timeSlot, final double waitLinkTime);
	double getWaitLinkTime(final int timeSlot);
	int getNumData(final int timeSlot);

}
