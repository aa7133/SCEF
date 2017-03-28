package com.att.scef.utils;

import java.util.HashSet;
import java.util.Set;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GMonitoringEventConfig;


public class MonitoringEventConfig extends GMonitoringEventConfig {
	
	public static GMonitoringEventConfig[] extractFromAvp(Avp avp) {
		Set<GMonitoringEventConfig> s = new HashSet<GMonitoringEventConfig>();
		try {
			for (Avp a: avp.getGrouped()) {
				s.add(extractFromAvpSingle(a));
			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}
		return s.stream().toArray(GMonitoringEventConfig[]::new);
	}
	
	public static GMonitoringEventConfig extractFromAvpSingle(Avp avp) {
		GMonitoringEventConfig monEventConfig = new GMonitoringEventConfig();
		AvpSet set;
		try {
			set = avp.getGrouped();
			Avp a = set.getAvp(Avp.SCEF_ID);
			if (a != null) {
				monEventConfig.scefId = a.getUTF8String();
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
				monEventConfig.monitoringDuration = a.getUTF8String();
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
