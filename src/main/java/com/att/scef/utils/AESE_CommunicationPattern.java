package com.att.scef.utils;

import java.util.ArrayList;
import java.util.List;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GHSSUserProfile;
import com.att.scef.gson.GMonitoringEventConfig;

public class AESE_CommunicationPattern extends GAESE_CommunicationPattern {
	
	public static GAESE_CommunicationPattern[] getNewHSSData(GHSSUserProfile hssData, List<GAESE_CommunicationPattern> cp) {
		List<Integer> scefRefIdList = AESE_CommunicationPattern.getScefRefIdList(cp);
		List<Integer> scefRefidForDelitionList = AESE_CommunicationPattern.getScefRefIdForDelitionList(cp);
		// check and update monitoring event
		List<GAESE_CommunicationPattern> aese = new ArrayList<GAESE_CommunicationPattern>();

		scefRefIdList = AESE_CommunicationPattern.getScefRefIdList(cp);
		scefRefidForDelitionList = AESE_CommunicationPattern.getScefRefIdForDelitionList(cp);

		for (GAESE_CommunicationPattern m : hssData.getAESECommunicationPattern()) {
			if (scefRefidForDelitionList.contains(m.scefRefId)) {
				continue; // it will be skipped and deleted
			}

			if (scefRefIdList.contains(m.scefRefId)) {
				// already exists Scef-Refereance-Id so we need to overwrite the value
				GAESE_CommunicationPattern ae = cp.get(scefRefIdList.indexOf(m.scefRefId));
				aese.add(ae);
			} else {
				// add new to hss data and we need to maintain the old
				// add the old tested since it was not deleted
				aese.add(m);
				// add the new tested one
				GAESE_CommunicationPattern ae = cp.get(scefRefIdList.indexOf(m.scefRefId));
				ae.scefRefIdForDelition = null;
				aese.add(ae);
			}
		}
		
		GAESE_CommunicationPattern[] g = new GAESE_CommunicationPattern[aese.size()];
		for (int i = 0; i < aese.size(); i++) {
		  g[i] = aese.get(i);
		}
		
		
		return (g);
	}
	
	public static List<Integer> getScefRefIdList(List<GAESE_CommunicationPattern> list) {
		List<Integer> scefRefIdList = new ArrayList<Integer>();
		list.stream().forEach((GAESE_CommunicationPattern x) -> scefRefIdList.add(x.scefRefId));
		return scefRefIdList;
	}

	public static List<Integer> getScefRefIdForDelitionList(List<GAESE_CommunicationPattern> list) {
		List<Integer> scefRefidForDelitionList = new ArrayList<Integer>();
		list.stream().forEach((GAESE_CommunicationPattern x) -> {
	    	for (int i : x.getScefRefIdForDelition()) {
	    		scefRefidForDelitionList.add(i);
	    	}
	    });
		return scefRefidForDelitionList;
	}


	public static List<GAESE_CommunicationPattern> extractFromAvp(Avp avp) {
		List<GAESE_CommunicationPattern> s = new ArrayList<GAESE_CommunicationPattern>();
		List<Integer> scefRefidList = new ArrayList<Integer>();
		try {
			for (Avp a: avp.getGrouped()) {
				GAESE_CommunicationPattern ae = extractFromAvpSingle(a);
				if (scefRefidList.contains(ae.scefRefId)) {
					continue;
				}
				scefRefidList.add(ae.scefRefId);
				s.add(ae);
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

	public static GAESE_CommunicationPattern extractFromAvpSingle(Avp avp) {
		GAESE_CommunicationPattern aeseCommPattern = new GAESE_CommunicationPattern();
		AvpSet set;
		try {
			set = avp.getGrouped();
			Avp a = set.getAvp(Avp.SCEF_ID);
			if (a != null) {
				aeseCommPattern.scefId = a.getDiameterIdentity();
			} else {
				// TODO this is error if AESE_ComunicationPattern exists
				// Avp.SCEF_ID is mandatory
				// if (logger.isErrorEnabled()) {
				// logger.error("if AVP of AESE-Comunication-Pattern exists
				// Avp.SCEF_ID "
				// + "is mandatory but not found need to return error later");
				// }
				aeseCommPattern.scefId = "";

			}
			a = set.getAvp(Avp.SCEF_REFERENCE_ID);
			if (a != null) {
				aeseCommPattern.scefRefId = a.getInteger32();
			}
			a = set.getAvp(Avp.SCEF_REFERENCE_ID_FOR_DELETION);
			if (a != null) {
				aeseCommPattern.scefRefIdForDelition = ExtractTool.extractFromSCEF_REFERENCE_ID_FOR_DELETION(a);
			}
			a = set.getAvp(Avp.COMMUNICATION_PATTERN_SET);
			if (a != null) {
				aeseCommPattern.communicationPatternSet = CommunicationPatternSet.extractFromAvp(a);
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return aeseCommPattern;
	}

	public static GAESE_CommunicationPattern clone(GAESE_CommunicationPattern src) {
		GAESE_CommunicationPattern clo = new GAESE_CommunicationPattern();
		clo.communicationPatternSet = src.communicationPatternSet;
		clo.scefId = src.scefId;
		clo.scefRefId = src.scefRefId;
		clo.scefRefIdForDelition = src.scefRefIdForDelition;
		return clo;
	}
}
