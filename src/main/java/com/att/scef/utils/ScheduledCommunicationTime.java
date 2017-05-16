package com.att.scef.utils;

import com.att.scef.gson.GScheduledCommunicationTime;
import com.att.scef.scef.SCEFRangeException;;

public class ScheduledCommunicationTime extends GScheduledCommunicationTime {
	
	public ScheduledCommunicationTime(int dayOfWeekMask, int timeOfDayStart, int timeOfDayEnd) throws SCEFRangeException {
		if (timeOfDayStart < 0 && timeOfDayStart > 86400) {
			throw new SCEFRangeException("Time-of-day-start out of range ");
		}
		if (timeOfDayEnd < 0 && timeOfDayEnd > 86400) {
			throw new SCEFRangeException("Time-of-day-end out of range ");
		}
		this.dayOfWeekMask = dayOfWeekMask;
		this.TimeOfDayStart = timeOfDayStart;
		this.TimeOfDayEnd = timeOfDayEnd;
	}

	public ScheduledCommunicationTime(int dayOfWeekMask, int timeOfDayStart)  throws SCEFRangeException {
		if (timeOfDayStart < 0 && timeOfDayStart > 86400) {
			throw new SCEFRangeException("Time-of-day-start out of range ");
		}
		this.dayOfWeekMask = dayOfWeekMask;
		this.TimeOfDayStart = timeOfDayStart;
		this.TimeOfDayEnd = 0;
	}

	public ScheduledCommunicationTime(int dayOfWeekMask) {
		this.dayOfWeekMask = dayOfWeekMask;
		this.TimeOfDayStart = 0;
		this.TimeOfDayEnd = 0;
	}

	public ScheduledCommunicationTime() {
		this.dayOfWeekMask = 0;
		this.TimeOfDayStart = 0;
		this.TimeOfDayEnd = 0;
	}

	public int getDayOfWeekMask() {
		return dayOfWeekMask;
	}

	public void setDayOfWeekMask(int dayOfWeekMask) {
		this.dayOfWeekMask = dayOfWeekMask;
	}

	public int getTimeOfDayStart() {
		return TimeOfDayStart;
	}

	public void setTimeOfDayStart(int timeOfDayStart) throws SCEFRangeException  {
		if (timeOfDayStart < 0 && timeOfDayStart > 86400) {
			throw new SCEFRangeException("Time-of-day-start out of range ");
		}
		this.TimeOfDayStart = timeOfDayStart;
	}

	public int getTimeOfDayEnd() {
		return TimeOfDayEnd;
	}

	public void setTimeOfDayEnd(int timeOfDayEnd) throws SCEFRangeException  {
		if (timeOfDayEnd < 0 && timeOfDayEnd > 86400) {
			throw new SCEFRangeException("Time-of-day-end out of range ");
		}
		this.TimeOfDayEnd = timeOfDayEnd;
	}
}
