package com.att.scef.utils;

import java.util.HashSet;
import java.util.Set;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;

import com.att.scef.gson.GCommunicationPatternSet;
import com.att.scef.gson.GScheduledCommunicationTime;

public class CommunicationPatternSet extends GCommunicationPatternSet {
	
	public static GCommunicationPatternSet[] extractFromAvp(Avp avp) {
		Set<GCommunicationPatternSet> s = new HashSet<GCommunicationPatternSet>();
		try {
			for (Avp a: avp.getGrouped()) {
				s.add(extractFromAvpSingle(a));
			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}
		return s.stream().toArray(GCommunicationPatternSet[]::new);
	}
	
	public static GCommunicationPatternSet extractFromAvpSingle(Avp avp) {
		GCommunicationPatternSet g = new GCommunicationPatternSet();
		try {
			for (Avp a: avp.getGrouped()) {
				switch (a.getCode()) {
				case Avp.PERIODIC_COMMUNICATION_INDICATOR:
					g.periodicCommunicationIndicator = a.getInteger32();
					break;
				case Avp.COMMUNICATION_DURATION_TIME:
					g.communicationDurationTime = a.getInteger32();
					break;
				case Avp.PERIODIC_TIME:
					g.periodictime = a.getInteger32();
					break;
				case Avp.SCHEDULED_COMMUNICATION_TIME:
					//TODO in the future
					break;
				case Avp.STATIONARY_INDICATION:
					//TODO in the future
					break;
				case Avp.REFERENCE_ID_VALIDITY_TIME:
					//TODO in the future
					break;
				}
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return g;
	}
}
