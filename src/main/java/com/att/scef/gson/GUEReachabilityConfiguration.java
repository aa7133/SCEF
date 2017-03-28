package com.att.scef.gson;

public class GUEReachabilityConfiguration {
	/**
	 * UE-Reachability-Configuration::= <AVP header: 3129 10415> 
	 * [ Reachability-Type ]
	 * [ Maximum-Latency ] 
	 * [ Maximum-Response-Time ]
	 */

	/**
	 * bit 0 is for sms  1 - configure the sms 0 - disable
	 * bit 1 is for data 1 - enable 0 - disable
	 * so values can be 0-3 only
	 */
	public int reachabilityType = 0;
	
	public int maximumLatency = 0;
	
	public int maximumResponseTime = 0;
}
