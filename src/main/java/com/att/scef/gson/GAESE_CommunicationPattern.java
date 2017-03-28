package com.att.scef.gson;

public class GAESE_CommunicationPattern {
	 //AESE-Communication-Pattern ::= <AVP header: 3113 10415>
	 //[ SCEF-Reference-ID ]
	 //{ SCEF-ID }
	 //*[ SCEF-Reference-ID-for-Deletion ]
	 //*[ Communication-Pattern-Set ]

	public int scefRefId = 0;
	public String scefId = null;
	public int[] scefRefIdForDelition = null;
	public GCommunicationPatternSet[] communicationPatternSet = null;

}
