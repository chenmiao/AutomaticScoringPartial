package common;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class getTime {
	
	public static String getCurrentDate () {
		SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		return dfDate.format(calendar.getTime());
	}

	public static String getNextDate (String date, int interval) {
		SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(new Integer(date.substring(0, 4)), new Integer(date.substring(5, 7))-1, new Integer(date.substring(8, 10)));
		calendar.add(Calendar.DATE, interval);
		return dfDate.format(calendar.getTime());
	}
	
	public static String getCurrentTime () {
		SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		return dfTime.format(calendar.getTime());
	}
	
	public static int getCurrentHour () {
		String time = getCurrentTime();
		return new Integer(time.substring(0,2)).intValue();
	}
}
