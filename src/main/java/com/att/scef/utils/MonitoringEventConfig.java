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
		
		GMonitoringEventConfig[] la = new GMonitoringEventConfig[lm.size()];
		for (int i = 0; i < lm.size(); i++) {
		  la[i] = lm.get(i);
		}
		  
		return (la);
	}

	public static List<Integer> getScefRefIdList(List<GMonitoringEventConfig> list) {
		List<Integer> scefRefIdList = new ArrayList<Integer>();
		list.stream().forEach((GMonitoringEventConfig x) -> scefRefIdList.add(x.scefRefId));
		return scefRefIdList;
	}

	public static List<Integer> getScefRefIdForDelitionList(List<GMonitoringEventConfig> list) {
		List<Integer> scefRefidForDelitionList = new ArrayList<Integer>();
		if (list == null) {
		  return null;
		}
		for (GMonitoringEventConfig m : list) {
		  int[] d = m.getScefRefIdForDelition();
		  if (d == null) {
		    continue;
		  }
		  for (int i : m.getScefRefIdForDelition()) {
		    scefRefidForDelitionList.add(i);
		  }
		}
		return scefRefidForDelitionList;
	}

	public static List<GMonitoringEventConfig> extractFromAvp(AvpSet avp) {
		List<GMonitoringEventConfig> s = new ArrayList<GMonitoringEventConfig>();
		List<Integer> scefRefidList = new ArrayList<Integer>();
		for (Avp a: avp) {
		  GMonitoringEventConfig m = extractFromAvpSingle(a);
		  if (scefRefidList.contains(m.scefRefId)) {
		    continue;
		  }
		  scefRefidList.add(m.scefRefId);
		  s.add(extractFromAvpSingle(a));
		}
		return s;
	}
	
	public static GMonitoringEventConfig extractFromAvpSingle(Avp avp) {
		GMonitoringEventConfig monEventConfig = new GMonitoringEventConfig();
        Set<Integer> delition = new HashSet<Integer>();
        try {
		  monEventConfig.scefId = "";
          monEventConfig.monitoringType = 0;
          
		  for (Avp a: avp.getGrouped()) {
		    if(a.getCode() == Avp.SCEF_ID) {
              monEventConfig.scefId = a.getDiameterIdentity();
		    }
		    else if (a.getCode() == Avp.SCEF_REFERENCE_ID) {
              monEventConfig.scefRefId = a.getInteger32();
		    }
		    else if (a.getCode() == Avp.MONITORING_TYPE) {
              monEventConfig.monitoringType = a.getInteger32();
		    }
		    else if (a.getCode() == Avp.SCEF_REFERENCE_ID_FOR_DELETION) {
		      delition.add(a.getInteger32());
		    }
	        else if (a.getCode() == Avp.MAXIMUM_NUMBER_OF_REPORTS) {
	          monEventConfig.maximumNumberOfReports = a.getInteger32();
	        }
	        else if (a.getCode() == Avp.MONITORING_DURATION) {
	          monEventConfig.monitoringDuration = a.getOctetString().toString();
	        }
            else if (a.getCode() == Avp.CHARGED_PARTY) {
              monEventConfig.monitoringType = a.getInteger32();
            }
            else if (a.getCode() == Avp.MAXIMUM_DETECTION_TIME) {
              monEventConfig.maximumDetectionTime = a.getInteger32();
            }
            else if (a.getCode() == Avp.UE_REACHABILITY_CONFIGURATION) {
              monEventConfig.UEReachabilityConfiguration = UE_ReachabilityConfiguration.extractFromAvpSingle(a);
            }
            else if (a.getCode() == Avp.LOCATION_INFORMATION_CONFIGURATION) {
              monEventConfig.locationInformationConfiguration = LocationInformationConfiguration.extractFromAvpSingle(a);
            }
            else if (a.getCode() == Avp.ASSOCIATION_TYPE) {
              monEventConfig.associationType = a.getInteger32();
            }
		  }
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (delition.size() != 0) {
		  int[] d = new int[delition.size()];
		  int j = 0;
		  for (int i : delition) {
		    d[j++] = i;
		  }
          monEventConfig.scefRefIdForDelition = d;
		}

		return monEventConfig;
	}


}
