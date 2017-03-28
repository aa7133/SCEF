package com.att.scef.utils;

import java.util.HashSet;
import java.util.Set;

import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;

import com.att.scef.gson.GCommunicationPatternSet;

public class ExtractTool {

	
	
	public static int[] extractFromSCEF_REFERENCE_ID_FOR_DELETION(Avp a) {
		Set<Integer> s = new HashSet<Integer>();
		try {
			for (Avp av: a.getGrouped()) {
				s.add(av.getInteger32());
			}
		} catch (AvpDataException e) {
			e.printStackTrace();
		}
		return s.stream().mapToInt(Number::intValue).toArray();
		
	}

}
