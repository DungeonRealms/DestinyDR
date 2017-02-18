package net.dungeonrealms.common;

import java.util.logging.Logger;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/7/2016
 */

public class Constants {

    public static Logger log = Logger.getLogger("DungeonRealms");

    public static boolean debug = false;

    public static String[] DEVELOPERS = new String[]{""};

    public static String MOTD = "&6Dungeon Realms™ &8- &a• The #1 Minecraft MMORPG •    \n                      &e&o&lOut of Beta!";

    public static String MAINTENANCE_MOTD = "&6Dungeon Realms™ &8- &cUndergoing Maintenance     \n                &8- &f&nwww.dungeonrealms.net &8-";

    public static long MIN_GAME_TIME = 14100000L;

    public static long MAX_GAME_TIME = 21300000L;

    public static int PLAYER_SLOTS = 1300;

    // BACKEND SERVER SERVER PORT //
    public static String MASTER_SERVER_IP;

    // BACKEND SERVER SERVER PORT //
    public static int MASTER_SERVER_PORT = 22964;

    public static int NET_READ_BUFFER_SIZE = 16384;

    public static int NET_WRITE_BUFFER_SIZE = 32768;

    // BUILD VERSION //
    public static String BUILD_VERSION = "v0";

    // BUILD NUMBER //
    public static String BUILD_NUMBER = "#0";

    // FTP INFO //
    public static String FTP_HOST_NAME = "";

    public static String FTP_USER_NAME = "";

    public static String FTP_PASSWORD = "";

    public static int FTP_PORT = 21;

    public static void build() {
        log = Logger.getLogger("DungeonRealms");
        debug = false;
        DEVELOPERS = new String[]{"Atlas__", "Bradez1571", "Kneesnap", "Nentify", "iFamasssxD"};
        MOTD = "                   &6&lDUNGEON REALMS &r\n            &lThe #1 Minecraft MMORPG &f&l";
        MAINTENANCE_MOTD = "                   &6&lDUNGEON REALMS &r\n            &lThe #1 Minecraft MMORPG &f&l";
        MIN_GAME_TIME = 14100000L;
        MAX_GAME_TIME = 21300000L;
        PLAYER_SLOTS = 1300;
        MASTER_SERVER_IP = "131.153.29.90";
        MASTER_SERVER_PORT = 22964;
        NET_READ_BUFFER_SIZE = 16384;
        NET_WRITE_BUFFER_SIZE = 32768;
        BUILD_VERSION = "UNSTABLE BUILD";
        BUILD_NUMBER = "#6";
        FTP_HOST_NAME = "167.114.65.102";
        FTP_USER_NAME = "dungeonrealms.53";
        FTP_PASSWORD = "BSmZWafwEk3QX7JTnF";
        FTP_PORT = 21;
    }

}
