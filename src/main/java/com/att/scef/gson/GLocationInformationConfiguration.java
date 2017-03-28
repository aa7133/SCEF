package com.att.scef.gson;

public class GLocationInformationConfiguration {
	/**
	 * Location-Information-Configuration::=	<AVP header: 3135 10415>
	 * [ MONTE-Location-Type ]
	 * [ Accuracy ]
	 * *[AVP]
	 */

	/**
	 * CURRENT_LOCATION (0)
	 * LAST_KNOWN_LOCATION (1)
	 */
	public int MONTELocationType = 0;

	/**
	 * CGI-ECGI (0)
	 * eNB (1)
	 * LA-TA-RA (2)
	 * PRA(3)
	 */
	public int accuracy = 0;
}
