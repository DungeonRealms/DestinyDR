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
    public static int MASTER_SERVER_PORT = 22965;

//    public static int NET_READ_BUFFER_SIZE = 16384;

    public static int NET_READ_BUFFER_SIZE = 32768;
    public static int NET_WRITE_BUFFER_SIZE = 32768;

    // BUILD NUMBER //
    public static String BUILD_NUMBER = "#0";

    public static void build() {
        log = Logger.getLogger("DungeonRealms");
        debug = false;
        DEVELOPERS = new String[]{"Bradez1571", "Kneesnap", "iFamasssxD", "Ingot"};
        MOTD = "                   &6&lDUNGEON REALMS &r\n            &lThe #1 Minecraft MMORPG &f&l";
        MAINTENANCE_MOTD = "                   &6&lDUNGEON REALMS &r\n            &lThe #1 Minecraft MMORPG &f&l";
        MIN_GAME_TIME = 14100000L;
        MAX_GAME_TIME = 21300000L;
        PLAYER_SLOTS = 1300;
        MASTER_SERVER_IP = "158.69.121.40";
//        com.esotericsoftware.kryonet.KryoNetException: Unable to read object larger than read buffer: 318768141
        MASTER_SERVER_PORT = 22965;
        //NET_READ_BUFFER_SIZE = 16384;
        NET_READ_BUFFER_SIZE = 32768;
        NET_WRITE_BUFFER_SIZE = 32768;
        BUILD_NUMBER = "#0";
    }

}
