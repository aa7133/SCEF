package com.att.scef.gson;

public class GMonitoringEventConfig {
	/**
	 *	Monitoring-Event-Configuration ::=	<AVP header: 3122 10415>
	 *	[ SCEF-Reference-ID ]
	 *	{ SCEF-ID }
	 *	{ Monitoring-Type }
	 *	*[ SCEF-Reference-ID-for-Deletion ]
	 *	[ Maximum-Number-of-Reports ]
	 *	[ Monitoring-Duration ]
	 *	[ Charged-Party ]
	 *	[ Maximum-Detection-Time ]
	 *	[ UE-Reachability-Configuration ]
	 *	[ Location-Information-Configuration ]
	 *	[ Association-Type ]
	 *	*[AVP]
	 */
	public int scefRefId = 0;
	
	public String scefId = null;
	
	/**
	 * LOSS_OF_CONNECTIVITY (0)
	 * UE_REACHABILITY (1)
	 * LOCATION_REPORTING (2)
	 * CHANGE_OF_IMSI_IMEI(SV)_ASSOCIATION (3)
	 * ROAMING_STATUS (4)
	 * COMMUNICATION_FAILURE (5)
	 * AVAILABILITY_AFTER_DDN_FAILURE (6)
	 * NUMBER_OF_UES_PRESENT_IN_A_GEOGRAPHICAL_AREA (7)
	 */
	public int monitoringType = 0;

	public int[] scefRefIdForDelition = null;
	
	public int maximumNumberOfReports = 0;
	
	public String monitoringDuration = null;
	
	public String chargedParty = null;
	
	public int maximumDetectionTime = 0;
	
	
	public GUEReachabilityConfiguration UEReachabilityConfiguration = null;
	
	public GLocationInformationConfiguration locationInformationConfiguration = null;
	
	/**
	 * IMEI-CHANGE (0)
	 * IMEISV-CHANGE (1)
	 */
	public int associationType = 0;
	

}
