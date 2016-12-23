package net.dungeonrealms.common;

import java.util.logging.Logger;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/7/2016
 */

public class Constants {

    public static Logger log = Logger.getLogger("DungeonRealms");

    public static boolean debug = false;

    public static String DATABASE_URI = "mongodb://dungeonrealms:nPqMSTZrdyzLgGhW@131.153.27.42:27017/dungeonrealms";

    public static String[] DEVELOPERS = new String[]{"Atlas__", "VawkeNetty", "Evoltr"};

    public static String[] OLD_DEVELOPERS = new String[]{"APOLLO_IO", "Proxying", "xWaffle", "xFinityPro", "Necrone_", "EtherealTemplar", "Bradez1571"};

    public static String MOTD = "                    &a• &c&lDUNGEON 7f&lREALMS&r &a•    \n     &8- &f&lTHIS CHRISTMAS &8-";

    public static String MAINTENANCE_MOTD = "                    &a• &c&lDUNGEON 7f&lREALMS&r &a•    \n     &8- &f&l24 DEC. 4:30CST &8-";

    public static long MIN_GAME_TIME = 14100000L;

    public static long MAX_GAME_TIME = 21300000L;

    public static int PLAYER_SLOTS = 1300;

    // BACKEND SERVER SERVER PORT //
    public static String MASTER_SERVER_IP = "131.153.27.42";

    // BACKEND SERVER SERVER PORT //
    public static int MASTER_SERVER_PORT = 22964;

    public static int NET_READ_BUFFER_SIZE = 16384;

    public static int NET_WRITE_BUFFER_SIZE = 32768;

    // BUILD VERSION //
    public static String BUILD_VERSION = "UNSTABLE BUILD";

    // BUILD NUMBER //
    public static String BUILD_NUMBER = "#0";

    // FTP INFO //
    public static String FTP_HOST_NAME = "167.114.65.102";

    public static String FTP_USER_NAME = "dungeonrealms.53";

    public static String FTP_PASSWORD = "CXxbpBbWdyekJtyv";

    public static int FTP_PORT = 21;

    /**
     * Fix null variables
     */
    public static void build() {
        log = Logger.getLogger("DungeonRealms");
        debug = false;
        DATABASE_URI = "mongodb://dungeonrealms:nPqMSTZrdyzLgGhW@131.153.27.42:27017/dungeonrealms";
        DEVELOPERS = new String[]{"Atlas__", "VawkeNetty", "Evoltr"};
        OLD_DEVELOPERS = new String[]{"APOLLO_IO", "Proxying", "xWaffle", "xFinityPro", "Necrone_", "EtherealTemplar", "Bradez1571"};
        MOTD = "                 &f&l❆ &c&lDUNGEON &f&lREALMS &f&l❆\n     &cThis christmas&e&l★ &f&l- &awww.dungeonrealms.net";
        MAINTENANCE_MOTD = "                 &f&l❆ &c&lDUNGEON &f&lREALMS &f&l❆\n     &f&l24 DEC. 4:30CST★ &f&l- &awww.dungeonrealms.net";
        MIN_GAME_TIME = 14100000L;
        MAX_GAME_TIME = 21300000L;
        PLAYER_SLOTS = 1300;
        MASTER_SERVER_IP = "131.153.27.42";
        MASTER_SERVER_PORT = 22964;
        NET_READ_BUFFER_SIZE = 16384;
        NET_WRITE_BUFFER_SIZE = 32768;
        BUILD_VERSION = "UNSTABLE BUILD";
        BUILD_NUMBER = "#0";
        FTP_HOST_NAME = "167.114.65.102";
        FTP_USER_NAME = "dungeonrealms.53";
        FTP_PASSWORD = "CXxbpBbWdyekJtyv";
        FTP_PORT = 21;
    }

    //Removed use sql.yml
//    public static  String SQL_HOSTNAME = "";
//    public static  int SQL_PORT = 3306;
//    public static  String SQL_DATABASE = "dungeonrealms";
//    public static  String SQL_PASSWORD = "";
//    public static  String SQL_USERNAME = "";
}