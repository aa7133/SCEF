package com.att.scef.utils;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GLocationInformationConfiguration;

public class LocationInformationConfiguration extends GLocationInformationConfiguration {

	public static GLocationInformationConfiguration extractFromAvpSingle(Avp avp) {
		GLocationInformationConfiguration location = new GLocationInformationConfiguration();
		AvpSet set;
		try {
			set = avp.getGrouped();
			
			Avp a = set.getAvp(Avp.MONTE_LOCATION_TYPE);
			if (a != null) {
				location.MONTELocationType = a.getInteger32();
			}

			a = set.getAvp(Avp.ACCURACY);
			if (a != null) {
				location.accuracy = a.getInteger32();
			}
		} catch (AvpDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return location;
	}

}
