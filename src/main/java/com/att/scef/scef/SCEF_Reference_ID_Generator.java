package com.att.scef.scef;

import java.util.Random;

public class SCEF_Reference_ID_Generator {
	private static Random rand = new Random();
	
	public static int getSCEFRefID() {
		return (int)rand.nextInt();
	}
}
