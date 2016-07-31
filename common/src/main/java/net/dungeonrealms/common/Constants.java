package net.dungeonrealms.common;

import java.util.logging.Logger;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/7/2016
 */

public class Constants {

    public static final Logger log = Logger.getLogger("DungeonRealms");

    public static boolean debug = false;

    public static final String DATABASE_URI = "mongodb://dungeonrealms:ZwGQGVpbPGhSqSXM@131.153.25.138:27017/dungeonrealms";

    public static final String[] DEVELOPERS = new String[]{"Proxying", "Atlas__", "APOLLO_IO", "Bradez1571", "EtherealTemplar", "Xwaffle"};

    public static final String MOTD = "&6Dungeon Realms &8- &a• The Best Minecraft MMORPG •\n   &ePatch v5.0          &8- &f&nwww.dungeonrealms.net &8-";

    public static final String MAINTENANCE_MOTD = "&6Dungeon Realms &8- &cUndergoing Maintenance     \n                &8- &f&nwww.dungeonrealms.net &8-";

    public static final long MIN_GAME_TIME = 14100000L;

    public static final long MAX_GAME_TIME = 21300000L;

    public static final int PLAYER_SLOTS = 1300;

    // BACKEND SERVER SERVER PORT //
    public static final String MASTER_SERVER_IP = "158.69.122.139";

    // BACKEND SERVER SERVER PORT //
    public static final int MASTER_SERVER_PORT = 22964;

    // BUILD VERSION //
    public static final String BUILD_VERSION = "v5.0";

    // BUILD NUMBER //
    public static final String BUILD_NUMBER = "#152";

}
