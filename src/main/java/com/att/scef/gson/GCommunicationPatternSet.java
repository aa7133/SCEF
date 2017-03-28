package com.att.scef.gson;

public class GCommunicationPatternSet {
	/**
	 *  Communication-Pattern-Set ::= <AVP header: 3114 10415>
	 *  	[ Periodic-Communication-Indicator ]
	 *  	[ Communication-Duration-Time ]
	 *  	[ Periodic-Time ]
	 *  	*[ Scheduled-Communication-Time ]
	 *  	[ Stationary-Indication ]
	 *  	[ Reference-ID-Validity-Time ]
	 */

	/**
	 * PeriodicCommunicationIndicator type int 0 - PERIODICALLY, 1 ON_DEMAND
	 */
	public int periodicCommunicationIndicator; // accept only 0 - PERIODICALLY, 1 ON_DEMAND
	
	/**
	 * CommunicationDurationTime int provide the time in seconds of the duration of the periodic communication
	 */
	public int communicationDurationTime;
	
	/**
	 * Periodictime type Unsigned32 and shall provide the time in seconds of the interval for periodic communication.
	 */
	public int periodictime;


	public  GScheduledCommunicationTime[] scheduledCommunicationTime;
	
	/**
	 * stationaryIndication 0- STATIONARY_UE, 1 - MOBILE_UE
	 */
	public int stationaryIndication;
	
	/**
	 * this is time from 1/1/1900
	 */
	public String referenceIDValidityTime;
}
