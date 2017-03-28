package com.att.scef.utils;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GUEReachabilityConfiguration;

public class UE_ReachabilityConfiguration extends GUEReachabilityConfiguration {
	
	public static GUEReachabilityConfiguration extractFromAvpSingle(Avp avp) {
		UE_ReachabilityConfiguration ueRe = new UE_ReachabilityConfiguration();
		AvpSet set;
		try {
			set = avp.getGrouped();
			
			Avp a = set.getAvp(Avp.REACHABILITY_TYPE);
			if (a != null) {
				ueRe.reachabilityType = a.getInteger32();
			}

			a = set.getAvp(Avp.MAXIMUM_LATENCY);
			if (a != null) {
				ueRe.maximumLatency = a.getInteger32();
			}
			
			a = set.getAvp(Avp.MAXIMUM_RESPONSE_TIME);
			if (a != null) {
				ueRe.maximumResponseTime = a.getInteger32();
			}
			
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ueRe;
	}

}
