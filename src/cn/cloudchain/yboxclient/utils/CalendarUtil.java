package cn.cloudchain.yboxclient.utils;

import java.util.Calendar;

public class CalendarUtil {
	public static final int FORMAT_FROM_YEAR_TO_MINUTE = 0;
	public static final int FORMAT_FROM_YEAR_TO_SECOND = 2;
	public static final int FORMAT_FROM_YEAR_TO_DAY = 1;

	/**
	 * 
	 * @param date
	 *            the format of date is like "2012-11-12 13:31:12"
	 * @param format
	 * @return
	 */
	public static Calendar transString2Calendar(String date, int format) {
		Calendar calendarTime = null;
		if (date == null || date.length() < 16) {
			return calendarTime;
		}

		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7)) - 1;
		int day = Integer.parseInt(date.substring(8, 10));
		int hours = Integer.parseInt(date.substring(11, 13));
		int minutes = Integer.parseInt(date.substring(14, 16));
		int seconds = 0;
		if (date.length() >= 19) {
			seconds = Integer.parseInt(date.substring(17, 19));
		}
		calendarTime = Calendar.getInstance();

		switch (format) {
		case FORMAT_FROM_YEAR_TO_SECOND:
			calendarTime.set(year, month, day, hours, minutes, seconds);
			break;
		case FORMAT_FROM_YEAR_TO_MINUTE:
			calendarTime.set(year, month, day, hours, minutes);
			break;
		case FORMAT_FROM_YEAR_TO_DAY:
			calendarTime.set(year, month, day);
			break;
		}
		return calendarTime;
	}

	public static Calendar transString2Calendar(String date) {
		return transString2Calendar(date, FORMAT_FROM_YEAR_TO_SECOND);
	}

	public static String getOnlyHourMinute(String date) {
		StringBuilder sb = new StringBuilder(5);
		if (date != null && date.length() >= 16) {
			String hours = date.substring(11, 13);
			String minutes = date.substring(14, 16);
			sb.append(hours);
			sb.append(":");
			sb.append(minutes);
		}
		return sb.toString();
	}

	/**
	 * yyyy-mm-dd hh:mm:ss
	 * 
	 * @return
	 */
	public static Calendar getCurrentTime() {
		return getCurrentTime(FORMAT_FROM_YEAR_TO_SECOND);
	}

	public static Calendar getCurrentTime(int format) {
		Calendar currentTime = Calendar.getInstance();
		int year = currentTime.get(Calendar.YEAR);
		int month = currentTime.get(Calendar.MONTH);
		int day = currentTime.get(Calendar.DAY_OF_MONTH);
		int hours = currentTime.get(Calendar.HOUR_OF_DAY);
		int minutes = currentTime.get(Calendar.MINUTE);
		int seconds = currentTime.get(Calendar.SECOND);

		switch (format) {
		case FORMAT_FROM_YEAR_TO_DAY:
			currentTime.set(year, month, day);
			break;
		case FORMAT_FROM_YEAR_TO_MINUTE:
			currentTime.set(year, month, day, hours, minutes);
			break;
		case FORMAT_FROM_YEAR_TO_SECOND:
			currentTime.set(year, month, day, hours, minutes, seconds);
			break;

		}
		return currentTime;
	}

	/**
	 * 
	 * @param format
	 * @return format is like "2012-11-12 13:31:12"
	 */
	public static String getCurrentTimeInString(int format) {
		Calendar currentTime = Calendar.getInstance();
		int year = currentTime.get(Calendar.YEAR);
		int month = currentTime.get(Calendar.MONTH);
		int day = currentTime.get(Calendar.DAY_OF_MONTH);
		int hours = currentTime.get(Calendar.HOUR_OF_DAY);
		int minutes = currentTime.get(Calendar.MINUTE);
		int seconds = currentTime.get(Calendar.SECOND);

		StringBuilder timeBuilder = new StringBuilder();
		timeBuilder.append(year);
		timeBuilder.append('-');
		timeBuilder.append(formatCalendarItem(Calendar.MONTH, month));
		timeBuilder.append('-');
		timeBuilder.append(formatCalendarItem(Calendar.DAY_OF_MONTH, day));

		switch (format) {
		case FORMAT_FROM_YEAR_TO_DAY:
			break;
		case FORMAT_FROM_YEAR_TO_MINUTE:
			timeBuilder.append(' ');
			timeBuilder.append(formatCalendarItem(Calendar.HOUR_OF_DAY, hours));
			timeBuilder.append(':');
			timeBuilder.append(formatCalendarItem(Calendar.MINUTE, minutes));
			break;
		case FORMAT_FROM_YEAR_TO_SECOND:
			timeBuilder.append(' ');
			timeBuilder.append(formatCalendarItem(Calendar.HOUR_OF_DAY, hours));
			timeBuilder.append(':');
			timeBuilder.append(formatCalendarItem(Calendar.MINUTE, minutes));
			timeBuilder.append(':');
			timeBuilder.append(formatCalendarItem(Calendar.SECOND, seconds));
			break;

		}
		return timeBuilder.toString();
	}

	private static String formatCalendarItem(int itemType, int itemValue) {
		StringBuilder sb = new StringBuilder();
		switch (itemType) {
		case Calendar.MONTH:
			itemValue++;
		case Calendar.DAY_OF_MONTH:
		case Calendar.HOUR_OF_DAY:
		case Calendar.MINUTE:
		case Calendar.SECOND:
			if (itemValue < 10) {
				sb.append('0');
			}
			sb.append(itemValue);
			break;
		}
		return sb.toString();
	}

	// /**
	// * define the date is today/tomorrow/day after tomorrow/others
	// *
	// * @param currentDate
	// * 需要对比的时间
	// * @param compareDate
	// * 用于对比的时间，值为null时与今天做对比
	// * @return 0 means it's today; 1 means it's tomorrow; 2 means it's the day
	// * after tomorrow; 3 means other day
	// */
	// public static int getDateStatus(String currentDate, String compareDate) {
	// Calendar calendar = transString2Calendar(currentDate,
	// FORMAT_FROM_YEAR_TO_DAY);
	// Calendar today = null;
	//
	// if (compareDate == null) {
	// today = getCurrentTime(FORMAT_FROM_YEAR_TO_DAY);
	// } else {
	// today = transString2Calendar(compareDate, FORMAT_FROM_YEAR_TO_DAY);
	// }
	//
	// int rollDir = calendar.compareTo(today);
	// if (rollDir == 0) {
	// return 0;
	// }
	//
	// // if calendar is before today, then today need to minus to equal to
	// // calendar
	// boolean increment = (rollDir < 0 ? false : true);
	// // get the total distance between today and calendar
	// int i = 0;
	//
	// do {
	// today.roll(Calendar.DAY_OF_MONTH, increment);
	// i++;
	// } while (Math.abs(calendar.compareTo(today) - rollDir) < 1);
	//
	// return (increment ? i : -i);
	// }
	//
	// public static Calendar getDayAfter(Calendar currentDay, int distance) {
	// Calendar today = currentDay;
	// today.add(Calendar.DAY_OF_MONTH, distance);
	// return today;
	// }
}
