package com.att.scef.utils;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;

import com.att.scef.gson.GUserIdentifier;

public class UserIdentifier extends GUserIdentifier {

	public static GUserIdentifier extractFromAvpSingle(Avp avp) {
		GUserIdentifier uid = new GUserIdentifier();
		AvpSet set;
		try {
			set = avp.getGrouped();
			
			Avp a = set.getAvp(Avp.MSISDN);
			if (a != null) {
				uid.setMsisdn(BCDStringConverter.toStringNumber(a.getOctetString()));
			}

			a = set.getAvp(Avp.USER_NAME);
			if (a != null) {
				uid.setUserName(a.getUTF8String());
			}
			
			a = set.getAvp(Avp.EXTERNAL_IDENTIFIER);
			if (a != null) {
				uid.setExternalId(a.getUTF8String());
			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}

		return uid;
	}

}
