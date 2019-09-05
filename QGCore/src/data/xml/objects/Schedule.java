package data.xml.objects;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import utils.Funcs;

public class Schedule {

	private static final long INTERVAL = 1800000;
	public static final Map<String,String> defaults;
	static
	{
		Map<String,String> temp = new HashMap<>();
		temp.put("HOURLY", "* 0-23 * * *");
		temp.put("DAILY", "0 8 * * *");
		temp.put("WEEKLY", "0 8 * * 1");
		temp.put("BIWEEKLY", "0 8 1,14,28 * 1");
		defaults = Collections.unmodifiableMap(temp);
	}
	
	private Date start;
	private Date end;
	private boolean force = false;
	private String scheduleFormatString;

	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public boolean isForce() {
		return force;
	}
	public void setForce(boolean force) {
		this.force = force;
	}
	public String getScheduleFormatString() {
		return scheduleFormatString;
	}
	public void setScheduleFormatString(String scheduleFormatString) {
		this.scheduleFormatString = scheduleFormatString;
	}
	
	/**
	 * Return if force is true, schedule not set, or now is between the start and end date.
	 * @param schedule
	 * @return
	 */
	public static boolean canRun(Schedule schedule){
		boolean result = false;
		
		if( schedule == null || schedule.isForce()){
			result = true;
		}
		else{
			Calendar now = Calendar.getInstance();
			
			//if between start and end dates inclusive
			if(now.getTime().compareTo(schedule.getStart()) >= 0 
				&& now.getTime().compareTo(schedule.getEnd()) <= 0){
				Funcs.shouldRun(schedule.getScheduleFormatString(), INTERVAL);
				result = true;
			}
		}
		
		return result;
	}
}
