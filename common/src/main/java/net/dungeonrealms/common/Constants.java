package net.dungeonrealms.common;

import java.util.logging.Logger;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/7/2016
 */

public class Constants {

    public static final Logger log = Logger.getLogger("DungeonRealms");

    public static boolean debug = false;

    public static final String DATABASE_URI = "mongodb://dungeonrealms:nPqMSTZrdyzLgGhW@131.153.27.42:27017/dungeonrealms";

    public static final String[] DEVELOPERS = new String[]{"Atlas__", "APOLLO_IO", "Bradez1571"};

    public static final String MOTD = "&6Dungeon Realms &8- &a• The #1 Minecraft MMORPG •     \n                &8- &f&nwww.dungeonrealms.net &8-";

    public static final String MAINTENANCE_MOTD = "&6Dungeon Realms &8- &cUndergoing Maintenance     \n                &8- &f&nwww.dungeonrealms.net &8-";

    public static final long MIN_GAME_TIME = 14100000L;

    public static final long MAX_GAME_TIME = 21300000L;

    public static final int PLAYER_SLOTS = 1300;

    // BACKEND SERVER SERVER PORT //
    public static final String MASTER_SERVER_IP = "158.69.122.139";

    // BACKEND SERVER SERVER PORT //
    public static final int MASTER_SERVER_PORT = 22964;

    public static final int NET_READ_BUFFER_SIZE = 16384;

    public static final int NET_WRITE_BUFFER_SIZE = 32768;

    // BUILD VERSION //
    public static final String BUILD_VERSION = "v5.0";

    // BUILD NUMBER //
    public static final String BUILD_NUMBER = "#186";

    // FTP INFO //
    public static final String FTP_HOST_NAME = "167.114.65.102";

    public static final String FTP_USER_NAME = "dungeonrealms.53";

    public static final String FTP_PASSWORD = "KNlZmiaNUp0B";

    public static final int FTP_PORT = 21;


}
