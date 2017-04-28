package net.dungeonrealms.common.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

	public static String formatDifference(long time) {
		if (time == 0) {
			return "0s";
		}

		long day = TimeUnit.SECONDS.toDays(time);
		long hours = TimeUnit.SECONDS.toHours(time) - (day * 24);
		long minutes = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
		long seconds = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);

		StringBuilder sb = new StringBuilder();

		if (day > 0) {
			sb.append(day).append("").append(day == 1 ? "day" : "days").append(" ");
		}

		if (hours > 0) {
			sb.append(hours).append("").append(hours == 1 ? "h" : "h").append(" ");
		}

		if (minutes > 0) {
			sb.append(minutes).append("").append(minutes == 1 ? "m" : "m").append(" ");
		}

		if (seconds > 0) {
			sb.append(seconds).append("").append(seconds == 1 ? "s" : "s");
		}

		String diff = sb.toString();

		return diff.isEmpty() ? "Now" : diff;
	}
}
