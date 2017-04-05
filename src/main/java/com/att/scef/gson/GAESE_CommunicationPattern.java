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
	public int getScefRefId() {
		return scefRefId;
	}
	public void setScefRefId(int scefRefId) {
		this.scefRefId = scefRefId;
	}
	public String getScefId() {
		return scefId;
	}
	public void setScefId(String scefId) {
		this.scefId = scefId;
	}
	public int[] getScefRefIdForDelition() {
		return scefRefIdForDelition;
	}
	public void setScefRefIdForDelition(int[] scefRefIdForDelition) {
		this.scefRefIdForDelition = scefRefIdForDelition;
	}
	public GCommunicationPatternSet[] getCommunicationPatternSet() {
		return communicationPatternSet;
	}
	public void setCommunicationPatternSet(GCommunicationPatternSet[] communicationPatternSet) {
		this.communicationPatternSet = communicationPatternSet;
	}

	
}
