package net.dungeonrealms.control.utils;

/**
 * Created by Evoltr on 11/15/2016.
 */
public class UtilLogger {

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

    public static void warn(String message) {
        System.out.println("[WARN] " + message);
    }

    public static void critical(String message) {
        System.out.println("[CRITICAL] " + message);
    }
}
