package com.att.scef.utils;

import java.util.HashSet;
import java.util.Set;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GAESE_CommunicationPattern;
import com.att.scef.gson.GCommunicationPatternSet;

public class AESE_CommunicationPattern extends GAESE_CommunicationPattern {

	public static GAESE_CommunicationPattern[] extractFromAvp(Avp avp) {
		Set<GAESE_CommunicationPattern> s = new HashSet<GAESE_CommunicationPattern>();
		try {
			for (Avp a: avp.getGrouped()) {
				s.add(extractFromAvpSingle(a));
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s.stream().toArray(GAESE_CommunicationPattern[]::new);
	}

	public static GAESE_CommunicationPattern extractFromAvpSingle(Avp avp) {
		GAESE_CommunicationPattern aeseCommPattern = new GAESE_CommunicationPattern();
		AvpSet set;
		try {
			set = avp.getGrouped();
			Avp a = set.getAvp(Avp.SCEF_ID);
			if (a != null) {
				aeseCommPattern.scefId = a.getUTF8String();
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
