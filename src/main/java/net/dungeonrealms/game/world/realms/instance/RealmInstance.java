package net.dungeonrealms.game.world.realms.instance;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmStatus;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */

public class RealmInstance implements Realms {

    // INSTANCE //
    protected static RealmInstance instance = null;
    // FTP INFO //
    private final String FTP_HOST_NAME = "167.114.65.102";
    private final String FTP_USER_NAME = "dr.53";
    private final String FTP_PASSWORD = "devpass123";
    private final int FTP_PORT = 21;
    // CACHED REALM DATA
    private Map<UUID, RealmToken> LOADED_REALMS = new ConcurrentHashMap<>();
    // IMPORTANT FOLDERS //
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

        //TODO: CLOSE ALL REALMS
        //TODO: UPLOAD ALL REALMS TO MASTER FTP SERVER BEFORE SHUTTING DOWN THE SERVER

    }

    @Override
    public void openRealm(Player player, Location location) {
        if (!isRealmLoaded(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Error: Your realm is not loaded!");
            return;
        }

        RealmToken realm = LOADED_REALMS.get(player.getUniqueId());

        if (realm.getStatus() == RealmStatus.DOWNLOADING) {
            player.sendMessage(ChatColor.RED + "Your realm is being downloaded..");
            return;
        }

        if (realm.getStatus() == RealmStatus.CREATING) {
            player.sendMessage(ChatColor.RED + "Your realm is being creating..");
            return;
        }

        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "Cannot open Realm while in Combat!");
            return;
        }

        if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            player.sendMessage(ChatColor.RED + "You can only open a realm portal in the main world!");
            return;
        }


        if (LootManager.checkLocationForLootSpawner(location.clone())) {
            player.sendMessage(ChatColor.RED + "You cannot place a realm portal this close to a Loot Spawning location");
            return;
        }

        if (API.isMaterialNearby(location.clone().getBlock(), 3, Material.LADDER) || API.isMaterialNearby(location.clone().getBlock(), 10, Material.ENDER_CHEST)) {
            player.sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
            return;
        }

        if (isPortalNearby(location.clone().add(0, 1, 0), 6) || API.isMaterialNearby(location.clone().getBlock(), 6, Material.PORTAL)) {
            player.sendMessage(ChatColor.RED + "You cannot place a portal so close to another! (Min 3 Blocks)");
            return;
        }


        for (Player p : Bukkit.getWorlds().get(0).getPlayers()) {
            if (p.getName().equals(player.getName())) {
                continue;
            }
            if (!p.getWorld().equals(player.getWorld())) {
                continue;
            }
            if (p.getLocation().distanceSquared(player.getLocation()) <= 2) {
                player.sendMessage(ChatColor.RED + "You cannot place your realm portal near another player");
                return;
            }
        }

        if (realm.getStatus() == RealmStatus.OPENED) {
            // TODO: CLOSE ANY PORTAL THAT IS OPEN
            return;
        }

        final Location portalLocation = location.clone();
        realm.setPortalLocation(portalLocation);

        location.add(0, 1, 0).getBlock().setType(Material.PORTAL);
        location.add(0, 1, 0).getBlock().setType(Material.PORTAL);
        Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), location.add(0.5, 1.5, 0.5));
        KarmaHandler.EnumPlayerAlignments playerAlignment = KarmaHandler.EnumPlayerAlignments.getByName(KarmaHandler.getInstance().getPlayerRawAlignment(player));
        assert playerAlignment != null;
        realmHologram.appendTextLine(ChatColor.WHITE + player.getName() + ChatColor.GOLD + " [" + playerAlignment.getAlignmentColor() + playerAlignment.name().toUpperCase() + ChatColor.GOLD + "]");
        realmHologram.getVisibilityManager().setVisibleByDefault(true);

        realm.setHologram(realmHologram);
        realm.setStatus(RealmStatus.OPENED);
    }


    public void loadRealm(Player player) throws ZipException {
        if (isRealmLoaded(player.getUniqueId())) return;

        // CREATE REALM TOKEN //
        RealmToken realm = new RealmToken(player.getUniqueId());
        realm.setStatus(RealmStatus.DOWNLOADING);

        LOADED_REALMS.put(player.getUniqueId(), realm);

        if (!downloadRealm(player.getUniqueId())) {
            // CREATE NEW REALM //
            player.sendMessage(ChatColor.GREEN + "Your realm does not exist remotely! Creating a new realm...");

            realm.setStatus(RealmStatus.CREATING);
            loadTemplate(player.getUniqueId());

            player.sendMessage(ChatColor.GREEN + "Your realm has been created!");
        } else player.sendMessage(ChatColor.GREEN + "Your realm has been downloaded.");

        realm.setStatus(RealmStatus.LOADED);
    }


    public boolean downloadRealm(UUID uuid) {
        FTPClient ftpClient = new FTPClient();
        FileOutputStream fos;
        String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";

        try {
            ftpClient.connect(FTP_HOST_NAME, FTP_PORT);
            boolean login = ftpClient.login(FTP_USER_NAME, FTP_PASSWORD);
            if (login) {
                Utils.log.warning("[REALM] [ASYNC] FTP Connection Established for " + uuid.toString());
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            Utils.log.info("[REALM] [ASYNC] Downloading " + uuid.toString() + "'s Realm ... STARTING");
            File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloaded/" + uuid.toString() + ".zip");
            fos = new FileOutputStream(TEMP_LOCAL_LOCATION);

            if (ftpClient.retrieveFile(REMOTE_FILE, fos)) {
                Utils.log.info("[REALM] [ASYNC] Realm downloaded for " + uuid.toString());

                ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
                Utils.log.info("[REALM] [ASYNC] Extracting Realm for " + uuid.toString());
                zipFile.extractAll(rootFolder.getAbsolutePath() + "/" + uuid.toString());
                Utils.log.info("[REALM] [ASYNC] Realm Extracted for " + uuid.toString());
                fos.close();
                return true;
            }

            fos.close();
            return false;
        } catch (IOException | ZipException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.logout();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //   Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> loadInWorld(Bukkit.getPlayer(uuid)), 5);
            }
        }

        return false;
    }

    private boolean isPortalNearby(Location location, int radius) {
        double rad = Math.pow(radius, 2);
        for (RealmToken realm : LOADED_REALMS.values())
            if (realm.getPortalLocation().distanceSquared(location.clone()) <= rad) return true;
        return false;
    }


    public RealmToken getOrCreateRealm(UUID uuid) {
        if (LOADED_REALMS.containsKey(uuid)) {
            return LOADED_REALMS.get(uuid);
        }

        RealmToken realm = new RealmToken(uuid);
        realm.setStatus(RealmStatus.CLOSED);

        LOADED_REALMS.put(uuid, realm);
        return realm;
    }

    @Override
    public boolean canPlacePortal(Location location) {
        return false;
    }

    @Override
    public boolean isRealmLoaded(UUID uuid) {
        return LOADED_REALMS.containsKey(uuid) && Bukkit.getServer().getWorlds().contains(Bukkit.getWorld(uuid.toString()));
    }

    @Override
    public void loadTemplate(UUID uuid) throws ZipException {
        //Create the player realm folder
        new File(rootFolder.getAbsolutePath(), uuid.toString()).mkdir();
        //Unzip the local template.

        ZipFile realmTemplateFile = new ZipFile(pluginFolder.getAbsolutePath() + "/realms/" + "realm_template.zip");
        realmTemplateFile.extractAll(rootFolder.getAbsolutePath() + "/" + uuid.toString());

        generateRealmBase(uuid);
    }


    public void generateRealmBase(UUID uuid) {
        WorldCreator worldCreator = new WorldCreator(uuid.toString());
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        worldCreator.generator(new RealmGenerator());
        World world = Bukkit.createWorld(worldCreator);
        world.setSpawnLocation(24, 130, 24);
        world.getBlockAt(0, 64, 0).setType(Material.AIR);

        int x, y = 128, z;
        Vector vector = new Vector(16, 128, 16);

        for (x = vector.getBlockX(); x < 32; x++)
            for (z = vector.getBlockZ(); z < 32; z++)
                world.getBlockAt(new Location(world, x, y, z)).setType(Material.GRASS);
        for (x = vector.getBlockX(); x < 32; x++)
            for (y = 127; y >= 112; y--)
                for (z = vector.getBlockZ(); z < 32; z++)
                    world.getBlockAt(new Location(world, x, y, z)).setType(Material.DIRT);
        for (x = vector.getBlockX(); x < 32; x++)
            for (z = vector.getBlockZ(); z < 32; z++)
                world.getBlockAt(new Location(world, x, y, z)).setType(Material.BEDROCK);

        Location portalLocation = world.getSpawnLocation().clone();
        portalLocation.getBlock().setType(Material.PORTAL);
        portalLocation.subtract(0, 1, 0).getBlock().setType(Material.PORTAL);
        portalLocation.add(0, 1, 0);

        Utils.log.info("[REALMS] Base Realm generated for player " + uuid.toString());
    }


}