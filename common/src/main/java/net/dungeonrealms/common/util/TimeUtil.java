package net.dungeonrealms.common.util;

import org.bukkit.ChatColor;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
    public static String formatDifference(long time) {
        return formatDifference(time, false);
    }

    public static String formatDifference(long time, boolean suffix) {
        if (time == 0) {
            return "0s";
        }

        long day = TimeUnit.SECONDS.toDays(time);
        long hours = TimeUnit.SECONDS.toHours(time) - (day * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);

        StringBuilder sb = new StringBuilder();

        if (day > 0) {
            sb.append(day).append(suffix ? " " : "").append(day == 1 ? "day" : "days").append(" ");
        }

        if (hours > 0) {
            sb.append(hours).append(suffix ? " " : "").append(hours == 1 ? "h" + (suffix ? "our" : "") : "h" + (suffix ? "ours" : "")).append(" ");
        }

        if (minutes > 0) {
            sb.append(minutes).append(suffix ? " " : "").append(minutes == 1 ? "m" + (suffix ? "inute" : "") : "m" + (suffix ? "inutes" : "")).append(" ");
        }

        if (seconds > 0) {
            sb.append(seconds).append(suffix ? " " : "").append(seconds == 1 ? "s" : "s");
        }

        String diff = sb.toString();

        return diff.isEmpty() ? "Now" : diff;
    }

    public static String formatDifference(long time, ChatColor numColor, ChatColor letterColor) {
        if (time == 0) {
            return "0s";
        }

        long day = TimeUnit.SECONDS.toDays(time);
        long hours = TimeUnit.SECONDS.toHours(time) - (day * 24);
        long minutes = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);

        StringBuilder sb = new StringBuilder();

        if (day > 0) {
            sb.append(numColor).append(day).append(letterColor).append("d").append(" ");
        }

        if (hours > 0) {
            sb.append(numColor).append(hours).append(letterColor).append(hours == 1 ? "h" : "h").append(" ");
        }

        if (minutes > 0) {
            sb.append(numColor).append(minutes).append(letterColor).append(minutes == 1 ? "m" : "m").append(" ");
        }

        if (seconds > 0) {
            sb.append(numColor).append(seconds).append(letterColor).append(seconds == 1 ? "s" : "s");
        }

        String diff = sb.toString();

        return diff.isEmpty() ? "N/A" : diff;
    }
}
