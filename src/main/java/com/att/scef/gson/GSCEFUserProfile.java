package com.att.scef.gson;

public class GSCEFUserProfile {
	public String externalId;
	public String msisdn;
	public String imsi;
	public String userName;
/**
 * operation|unique_device_ID|MSISDN=msisdn0,AppID=appid0sx,AppRef=appref0sx,MaxNIDD=maxNIDD0sx,
 * NIDDDur=dur0,NIDDDestAddr=dA2,AppRefID4Del=app4D0sx,dataEvent=0,mntrgEvent=7,dataQ=EventData,mntrgQ=EventMonitoring,ackQ=EventAck

•	operation today        is just a new/updated device. I do not distinguish between “new” and “update”
•	unique_device_ID       is the external ID
•	dataEvent              is Boolean for registration to data upstream
•	mntrgEvent             is bitmap for 8 possible monitoring events. The command brings the current full registration rather than any delta
•	dataQ
•	mntrgQ
•	ackQ
 */
	
	public String appId;
	public String appRefId;

	public int niddDuration;    // for how long the request is initiated. 0 - unlimited
	public int numberOfNiddMessages; // The max number of NIDD events are supported for the device. 0 - unlimited
	public int monitoringFlags; // bit 0 - 7 each bit represent monitoring event. 1 monitoring is requested, 0 monitoring is not requested
	
	// pub/sub pointers
	public String dataQueueAddress; // point to the pub/sub of NIDD queue if empty or not exists NIDD is not requested
	public String monitoroingQueue; // point to the topic of the monitoring (currently only one address for all monitoring events) if empty 
	                         // no monitoring is requested
	public String errorQueue;       // notify all errors on device. if empty no error messages will be sent

	public GMonitoringEventConfig[] mc;
	public GAESE_CommunicationPattern[] cp;
	
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	public String getImsi() {
		return imsi;
	}
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getAppRefId() {
		return appRefId;
	}
	public void setAppRefId(String appRefId) {
		this.appRefId = appRefId;
	}
	public int getNiddDuration() {
		return niddDuration;
	}
	public void setNiddDuration(int niddDuration) {
		this.niddDuration = niddDuration;
	}
	public int getNumberOfNiddMessages() {
		return numberOfNiddMessages;
	}
	public void setNumberOfNiddMessages(int numberOfNiddMessages) {
		this.numberOfNiddMessages = numberOfNiddMessages;
	}
	public int getMonitoringFlags() {
		return monitoringFlags;
	}
	public void setMonitoringFlags(int monitoringFlags) {
		this.monitoringFlags = monitoringFlags;
	}
	public String getDataQueueAddress() {
		return dataQueueAddress;
	}
	public void setDataQueueAddress(String dataQueueAddress) {
		this.dataQueueAddress = dataQueueAddress;
	}
	public String getMonitoroingQueue() {
		return monitoroingQueue;
	}
	public void setMonitoroingQueue(String monitoroingQueue) {
		this.monitoroingQueue = monitoroingQueue;
	}
	public String getErrorQueue() {
		return errorQueue;
	}
	public void setErrorQueue(String errorQueue) {
		this.errorQueue = errorQueue;
	}
	public GMonitoringEventConfig[] getMc() {
		return mc;
	}
	public void setMc(GMonitoringEventConfig[] mc) {
		this.mc = mc;
	}
	public GAESE_CommunicationPattern[] getCp() {
		return cp;
	}
	public void setCp(GAESE_CommunicationPattern[] cp) {
		this.cp = cp;
	}
	
	
	
}
