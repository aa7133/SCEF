package com.att.scef.scef;

import java.util.Random;

public class SCEF_Reference_ID_Generator {
	// SCEF-Reference-ID unsignedint32
	private static Random rand = new Random();
	private static int maxInt = 0xffffffff;
	
	public static int getSCEFRefID() {
		return rand.nextInt(maxInt);
	}
}
