package com.att.scef.gson;

public class GHSSUserProfile {
	public String msisdn;
	public String IMSI;
	public String IMEI;
	public String userName;
	public String externalIdentity;
	
	public String MMEAdress;

	public GAESE_CommunicationPattern[] AESECommunicationPattern;
	public GMonitoringEventConfig[] monitoringConfig;
	
	
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getIMSI() {
		return IMSI;
	}
	public void setIMSI(String iMSI) {
		IMSI = iMSI;
	}
	public String getIMEI() {
		return IMEI;
	}
	public void setIMEI(String iMEI) {
		IMEI = iMEI;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getExternalIdentity() {
		return externalIdentity;
	}
	public void setExternalIdentity(String externalIdentity) {
		this.externalIdentity = externalIdentity;
	}
	public String getMMEAdress() {
		return MMEAdress;
	}
	public void setMMEAdress(String mMEAdress) {
		MMEAdress = mMEAdress;
	}
	public GAESE_CommunicationPattern[] getAESECommunicationPattern() {
		return AESECommunicationPattern;
	}
	public void setAESECommunicationPattern(GAESE_CommunicationPattern[] aESECommunicationPattern) {
		AESECommunicationPattern = aESECommunicationPattern;
	}
	public GMonitoringEventConfig[] getMonitoringConfig() {
		return monitoringConfig;
	}
	public void setMonitoringConfig(GMonitoringEventConfig[] monitoringConfig) {
		this.monitoringConfig = monitoringConfig;
	}

}
