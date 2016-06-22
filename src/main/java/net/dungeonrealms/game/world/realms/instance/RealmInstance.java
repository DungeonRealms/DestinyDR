package net.dungeonrealms.game.world.realms.instance;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick on 11/12/2015.
 */
public class RealmInstance implements Realms {

    protected static RealmInstance instance = null;
    private final String FTP_HOST_NAME = "167.114.65.102";
    private final String FTP_USER_NAME = "dr.53";
    private final String FTP_PASSWORD = "devpass123";
    private final int FTP_PORT = 21;
    private Map<UUID, RealmToken> CURRENT_REALMS = new HashMap<>();
    private File pluginFolder = null;
    private File rootFolder = null;

    public static RealmInstance getInstance() {
        if (instance == null) {
            instance = new RealmInstance();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    @Override
    public void startInitialization() {
        Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");

        pluginFolder = DungeonRealms.getInstance().getDataFolder();
        rootFolder = new File(System.getProperty("user.dir"));

        try {
            FileUtils.forceMkdir(new File(pluginFolder, "/realms/downloaded"));
            FileUtils.forceMkdir(new File(pluginFolder, "/realms/uploading"));
        } catch (IOException e) {
            e.printStackTrace();
            Utils.log.info("Failed to create realm directories!");

        }

        Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");
    }

    @Override
    public void stopInvocation() {

    }


    public void openRealm(Player player, Location location) {

    }


    public boolean downloadRealm(UUID player) {
        return false;
    }

    @Override
    public void loadTemplate(UUID player) throws ZipException {

    }
}