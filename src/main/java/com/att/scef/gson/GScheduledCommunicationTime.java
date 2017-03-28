package com.att.scef.gson;

public class GScheduledCommunicationTime {
	/**
	 *  Scheduled-communication-time ::= <AVP header: 3118 10415>
	 *  [ Day-Of-Week-Mask ]
	 *  [ Time-Of-Day-Start ]
	 *  [ Time-Of-Day-End ]
	 *  *[AVP] 
	 */
	
	/**
	 * The Day-Of-Week-Mask AVP (AVP Code 563) is of type Unsigned32. 
	 * Bit | Name
	 * ------+------------
	 * 0 | SUNDAY
	 * 1 | MONDAY
	 * 2 | TUESDAY
	 * 3 | WEDNESDAY
	 * 4 | THURSDAY
	 * 5 | FRIDAY
	 * 6 | SATURDAY
	 */
	public int dayOfWeekMask;
	
	/**
	 * The Time-Of-Day-Start AVP (AVP Code 561) is of type Unsigned32. The 
	 * value MUST be in the range from 0 to 86400. The value of this AVP
	 * specifies the start of an inclusive time window expressed as the
	 * offset in seconds from midnight. If this AVP is absent from the
	 * Time-Of-Day-Condition AVP, the time window starts at midnight.
	 */
	public int TimeOfDayStart;
	
	/**
	 * The Time-Of-Day-End AVP (AVP Code 562) is of type Unsigned32. The
	 * value MUST be in the range from 1 to 86400. The value of this AVP
	 * specifies the end of an inclusive time window expressed as the offset
	 * in seconds from midnight. If this AVP is absent from the Time-Of-
	 * Day-Condition AVP, the time window ends one second before midnight.
	 */
	public int TimeOfDayEnd;
}
