package com.att.scef.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GHSSUserProfile;
import com.att.scef.gson.GMonitoringEventConfig;


public class MonitoringEventConfig extends GMonitoringEventConfig {
	
	public static GMonitoringEventConfig[] getNewHSSData(GHSSUserProfile hssData, List<GMonitoringEventConfig> me) {
		List<Integer> scefRefIdList = MonitoringEventConfig.getScefRefIdList(me);
		List<Integer> scefRefidForDelitionList = MonitoringEventConfig.getScefRefIdForDelitionList(me);

		// check and update monitoring event
		List<GMonitoringEventConfig> lm = new ArrayList<GMonitoringEventConfig>();

		for (GMonitoringEventConfig m : hssData.getMonitoringConfig()) {
			if (scefRefidForDelitionList.contains(m.scefRefId)) {
				continue; // it will be skipped and deleted
			}

			if (scefRefIdList.contains(m.scefRefId)) { 
				GMonitoringEventConfig mi = me.get(scefRefIdList.indexOf(m.scefRefId));
				lm.add(mi);
			} else { // add new to hss data and we need to maintain the old
				m.scefRefIdForDelition = null;
				// add the old tested since it was not deleted
				lm.add(m);
				// add the new tested one
				GMonitoringEventConfig mi = me.get(scefRefIdList.indexOf(m.scefRefId));
				lm.add(mi);
			}
		}
		return (GMonitoringEventConfig[])lm.toArray();
	}

	public static List<Integer> getScefRefIdList(List<GMonitoringEventConfig> list) {
		List<Integer> scefRefIdList = new ArrayList<Integer>();
		list.stream().forEach((GMonitoringEventConfig x) -> scefRefIdList.add(x.scefRefId));
		return scefRefIdList;
	}

	public static List<Integer> getScefRefIdForDelitionList(List<GMonitoringEventConfig> list) {
		List<Integer> scefRefidForDelitionList = new ArrayList<Integer>();
		list.stream().forEach((GMonitoringEventConfig x) -> {
	    	for (int i : x.getScefRefIdForDelition()) {
	    		scefRefidForDelitionList.add(i);
	    	}
	    });
		return scefRefidForDelitionList;
	}

	public static List<GMonitoringEventConfig> extractFromAvp(Avp avp) {
		List<GMonitoringEventConfig> s = new ArrayList<GMonitoringEventConfig>();
		List<Integer> scefRefidList = new ArrayList<Integer>();
		try {
			for (Avp a: avp.getGrouped()) {
				GMonitoringEventConfig m = extractFromAvpSingle(a);
				if (scefRefidList.contains(m.scefRefId)) {
					continue;
				}
				scefRefidList.add(m.scefRefId);
				s.add(extractFromAvpSingle(a));
			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public static GMonitoringEventConfig extractFromAvpSingle(Avp avp) {
		GMonitoringEventConfig monEventConfig = new GMonitoringEventConfig();
		AvpSet set;
		try {
			set = avp.getGrouped();
			Avp a = set.getAvp(Avp.SCEF_ID);
			if (a != null) {
				monEventConfig.scefId = a.getDiameterIdentity();
			} else {
				// TODO this is error if Monitoring-Event-Configuration exists
				// Avp.SCEF_ID is mandatory
				// if (logger.isErrorEnabled()) {
				// logger.error("if AVP of AESE-Comunication-Pattern exists
				// Avp.SCEF_ID "
				// + "is mandatory but not found need to return error later");
				// }
				monEventConfig.scefId = "";

			}
			a = set.getAvp(Avp.MONITORING_TYPE);
			if (a != null) {
				monEventConfig.monitoringType = a.getInteger32();
			}
			else {
				// TODO this is error if Monitoring-Event-Configuration exists
				// Avp.MONITORING_TYPE is mandatory
				// if (logger.isErrorEnabled()) {
				// logger.error("if AVP of AESE-Comunication-Pattern exists
				// Avp.MONITORING_TYPE "
				// + "is mandatory but not found need to return error later");
				// }
				monEventConfig.monitoringType = 0;
				
			}
			
			a = set.getAvp(Avp.SCEF_REFERENCE_ID);
			if (a != null) {
				monEventConfig.scefRefId = a.getInteger32();
			}
		
			a = set.getAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION);
			if (a != null) {
				monEventConfig.scefRefIdForDelition = ExtractTool.extractFromSCEF_REFERENCE_ID_FOR_DELETION(a);
			}
			
			a = set.getAvp(Avp.MAXIMUM_NUMBER_OF_REPORTS);
			if (a != null) {
				monEventConfig.maximumNumberOfReports = a.getInteger32();
			}

			a = set.getAvp(Avp.MONITORING_DURATION);
			if (a != null) {
				monEventConfig.monitoringDuration = a.getOctetString().toString();
			}

			a = set.getAvp(Avp.CHARGED_PARTY);
			if (a != null) {
				monEventConfig.chargedParty = a.getUTF8String();
			}
			
			a = set.getAvp(Avp.MAXIMUM_DETECTION_TIME);
			if (a != null) {
				monEventConfig.maximumDetectionTime = a.getInteger32();
			}

			a = set.getAvp(Avp.UE_REACHABILITY_CONFIGURATION);
			if (a != null) {
				monEventConfig.UEReachabilityConfiguration = UE_ReachabilityConfiguration.extractFromAvpSingle(a);
			}

			a = set.getAvp(Avp.LOCATION_INFORMATION_CONFIGURATION);
			if (a != null) {
				monEventConfig.locationInformationConfiguration = LocationInformationConfiguration.extractFromAvpSingle(a);
			}
			
			
			
			a = set.getAvp(Avp.ASSOCIATION_TYPE);
			if (a != null) {
				monEventConfig.associationType = a.getInteger32();
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return monEventConfig;
	}


}
