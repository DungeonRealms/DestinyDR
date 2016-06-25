package net.dungeonrealms.game.world.realms.instance;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.listeners.RealmListener;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmStatus;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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
    private Map<UUID, RealmToken> CACHED_REALMS = new ConcurrentHashMap<>();


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
        Bukkit.getPluginManager().registerEvents(new RealmListener(), DungeonRealms.getInstance());
    }


    @Override
    public void stopInvocation() {
        // REMOVES ALL CACHED REALMS //
        Utils.log.info("[REALM] [SYNC] Removing all player realms..");
        removeAllRealms(false);
    }

    @Override
    public void loadRealm(Player player, Consumer<?> doAfter) {
        if (((boolean) DatabaseAPI.getInstance().getData(EnumData.REALM_UPLOAD, player.getUniqueId()))) {
            player.sendMessage(ChatColor.RED + "Your realm is still being uploaded from another shard.");
            return;
        }

        if (isRealmCached(player.getUniqueId())) {
            doAfter.accept(null);
            return;
        }

        // CREATE REALM TOKEN //
        RealmToken realm = new RealmToken(player.getUniqueId());
        realm.setStatus(RealmStatus.DOWNLOADING);

        CACHED_REALMS.put(player.getUniqueId(), realm);

        player.sendMessage(ChatColor.YELLOW + "Please wait whilst your realm is being loaded...");

        Futures.addCallback(downloadRealm(player.getUniqueId()), new FutureCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean success) {
                // MAKE SURE WE ARE GOING BACK TO SYNC //
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
                    loadRealm(player, !success);

                    player.sendMessage(ChatColor.YELLOW + "Your realm has been loaded.");
                    // player.sendMessage(ChatColor.GRAY + "You may now right click your realm portal rune to open your portal.");

                    realm.setLoaded(true);
                    realm.setStatus(RealmStatus.CLOSED);

                    doAfter.accept(null);
                });
            }

            @ParametersAreNonnullByDefault
            public void onFailure(Throwable thrown) {
                handleRealmLoadFailure(player, thrown);
            }
        });
    }

    @Override
    public void openRealmPortal(Player player, Location location) {
        if (!isRealmCached(player.getUniqueId())) return;


        RealmToken realm = getRealm(player.getUniqueId());

        if (realm.getStatus() != RealmStatus.OPENED && realm.getStatus() != RealmStatus.CLOSED) {
            player.sendMessage(getRealmStatusMessage(realm.getStatus()));
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
            player.sendMessage(ChatColor.RED + "You cannot place a portal so close to another! (Min 6 Blocks)");
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

        if (isRealmPortalOpen(realm.getOwner()))
            closeRealmPortal(player.getUniqueId(), false);

        final Location portalLocation = location.clone().add(0, 1, 0);
        realm.setPortalLocation(portalLocation);

        location.add(0, 1, 0).getBlock().setType(Material.PORTAL);
        location.add(0, 1, 0).getBlock().setType(Material.PORTAL);

        Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), location.add(0.5, 1.5, 0.5));
        KarmaHandler.EnumPlayerAlignments playerAlignment = KarmaHandler.EnumPlayerAlignments.getByName(KarmaHandler.getInstance().getPlayerRawAlignment(player));
        assert playerAlignment != null;
        realmHologram.getVisibilityManager().setVisibleByDefault(true);
        realm.setHologram(realmHologram);
        updateRealmHologram(player.getUniqueId());

        realm.setStatus(RealmStatus.OPENED);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "                   " + "* Realm Portal OPENED *");

        player.getWorld().playEffect(portalLocation, Effect.ENDER_SIGNAL, 10);
        player.playSound(portalLocation, Sound.ENTITY_ENDERMEN_TELEPORT, 5F, 0.75F);

        if (getRealmTitle(player.getUniqueId()).equals(""))
            player.sendMessage(ChatColor.GRAY + "Type /realm <TITLE> to set the description of your realm, it will be displayed to all visitors.");
        else
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Description: " + ChatColor.GRAY + getRealmTitle(player.getUniqueId()));
    }


    @Override
    public void loadRealmWorld(UUID uuid) {
        Utils.log.info("[REALM] [SYNC] Loading world for " + uuid.toString());
        Bukkit.getServer().createWorld(new WorldCreator(uuid.toString())).setKeepSpawnInMemory(false);
    }


    @Override
    public void removeAllRealms(boolean runAsync) {
        // CLOSE REMOVES AND UPLOADS ALL REALMS
        CACHED_REALMS.values().stream().forEach(realm -> removeRealm(realm.getOwner(), runAsync));
    }

    @Override
    public void doLogout(Player player) {
        if (!isRealmCached(player.getUniqueId())) return;

        RealmToken realm = Realms.getInstance().getRealm(player.getLocation().getWorld());

        getRealm(player.getUniqueId()).getPlayersInRealm().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .forEach(uuid -> Bukkit.getPlayer(uuid).sendMessage(ChatColor.RED + "The owner of this realm has LOGGED OUT."));

        closeRealmPortal(player.getUniqueId(), true);
        realm.setStatus(RealmStatus.REMOVING);

        // MUST BE SYNC //
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> removeRealm(player.getUniqueId(), true), 60L);
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

    @Override
    public ListenableFuture<Boolean> downloadRealm(UUID uuid) {
        return MoreExecutors.listeningDecorator(AsyncUtils.pool).submit(() -> {
            FTPClient ftpClient = new FTPClient();
            FileOutputStream fos = null;
            String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";
            File TEMP_LOCAL_LOCATION = new File(DungeonRealms.getInstance().getDataFolder() + "/realms/downloaded/" + uuid.toString() + ".zip");

            try {
                ftpClient.connect(FTP_HOST_NAME, FTP_PORT);
                boolean login = ftpClient.login(FTP_USER_NAME, FTP_PASSWORD);
                if (login) {
                    Utils.log.warning("[REALM] [ASYNC] FTP Connection Established for " + uuid.toString());
                }
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                Utils.log.info("[REALM] [ASYNC] Downloading " + uuid.toString() + "'s Realm ... STARTING");

                fos = new FileOutputStream(TEMP_LOCAL_LOCATION);

                if (ftpClient.retrieveFile(REMOTE_FILE, fos)) {
                    Utils.log.info("[REALM] [ASYNC] Realm downloaded for " + uuid.toString());

                    ZipFile zipFile = new ZipFile(TEMP_LOCAL_LOCATION);
                    Utils.log.info("[REALM] [ASYNC] Extracting Realm for " + uuid.toString());
                    zipFile.extractAll(rootFolder.getAbsolutePath() + "/" + uuid.toString());
                    Utils.log.info("[REALM] [ASYNC] Realm Extracted for " + uuid.toString());

                    return true;
                }

                return false;
            } finally {
                if (fos != null) fos.close();

                FileUtils.forceDelete(TEMP_LOCAL_LOCATION);

                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            }
        });
    }

    @Override
    public void uploadRealm(UUID uuid, boolean runAsync, Consumer<Boolean> doAfter) {
        if (!isRealmCached(uuid)) return;

        RealmToken realm = getRealm(uuid);
        realm.setStatus(RealmStatus.UPLOADING);

        // PLAYER'S REALM IS STILL UPLOADING \\
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_UPLOAD, true, true);

        if (runAsync) {
            // SUBMIT THREAD INTO ASYNC POOL //
            AsyncUtils.pool.submit(() -> {
                uploadRealm(uuid, doAfter);
            });
        } else {
            // EXECUTE ON MAIN THREAD //
            uploadRealm(uuid, doAfter);
        }
    }

    private void uploadRealm(UUID uuid, Consumer<Boolean> doAfter) {
        Utils.log.info("[REALM] [ASYNC] Starting Compression for player realm " + uuid.toString());

        InputStream inputStream = null;

        try {
            zip(rootFolder.getAbsolutePath() + "/" + uuid.toString() + "/", pluginFolder.getAbsolutePath() + "/" + "realms/" + "uploading" + "/" + uuid.toString() + ".zip");
            FTPClient ftpClient = new FTPClient();

            ftpClient.connect(FTP_HOST_NAME);
            ftpClient.login(FTP_USER_NAME, FTP_PASSWORD);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String REMOTE_FILE = "/" + "realms" + "/" + uuid.toString() + ".zip";

            inputStream = new FileInputStream(pluginFolder.getAbsolutePath() + "/realms/uploading/" + uuid.toString() + ".zip");

            Utils.log.info("[REALM] [ASYNC] Started upload for player realm " + uuid.toString() + " ... STARTING");
            ftpClient.storeFile(REMOTE_FILE, inputStream);
            inputStream.close();
            Utils.log.info("[REALM] [ASYNC] Successfully uploaded player realm " + uuid.toString());
        } catch (IOException | ZipException e) {
            getRealm(uuid).setStatus(RealmStatus.CLOSED);
            e.printStackTrace();
            if (doAfter != null)
                doAfter.accept(false);
        } finally {
            Utils.log.info("[REALM] [ASYNC] Deleting local cache of realm " + uuid.toString());

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileUtils.forceDelete(new File(pluginFolder.getAbsolutePath() + "/realms/uploading/" + uuid.toString() + ".zip"));
                FileUtils.forceDelete(new File(rootFolder.getAbsolutePath() + "/" + uuid.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // PLAYER'S REALM IS STILL UPLOADING \\
            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_UPLOAD, false, true);

            // SEND PLAYER UPDATE PACKET IF THEY SWITCHED SHARDS //
            API.updatePlayerData(uuid);

            getRealm(uuid).setStatus(RealmStatus.CLOSED);

            if (doAfter != null)
                doAfter.accept(true);
        }
    }


    private boolean isPortalNearby(Location location, int radius) {
        double rad = Math.pow(radius, 2);
        for (RealmToken realm : CACHED_REALMS.values())
            if (realm.getPortalLocation() != null && realm.getPortalLocation().distanceSquared(location.clone()) <= rad)
                return true;
        return false;
    }


    public RealmToken getRealm(UUID uuid) {
        return CACHED_REALMS.get(uuid);
    }

    @Override
    public RealmStatus getRealmStatus(UUID uuid) {
        if (!isRealmCached(uuid)) return null;
        return getRealm(uuid).getStatus();
    }

    @Override
    public RealmToken getRealm(Location portalLocation) {
        for (RealmToken realm : CACHED_REALMS.values())
            if (realm.getPortalLocation() != null && realm.getPortalLocation().distance(portalLocation.clone()) <= 2)
                return realm;

        return null;
    }

    @Override
    public RealmToken getRealm(World world) {
        return world != null && world.getName().split("-").length == 5 ? getRealm(UUID.fromString(world.getName())) : null;
    }

    @Override
    public void closeRealmPortal(UUID uuid, boolean kickPlayers) {
        if (!isRealmLoaded(uuid)) return;

        RealmToken realm = getRealm(uuid);

        if (realm.getPortalLocation() == null) return;

        Location portalLocation = realm.getPortalLocation().clone();

        portalLocation.add(0, 1, 0).getBlock().setType(Material.AIR);
        portalLocation.add(0, 1, 0).getBlock().setType(Material.AIR);


        if (realm.getHologram() != null)
            realm.getHologram().delete();

        realm.setPortalLocation(null);
        realm.setStatus(RealmStatus.CLOSED);

        for (UUID u : realm.getPlayersInRealm()) {
            Player p = Bukkit.getPlayer(u);

            if (p != null)
                p.teleport(portalLocation);
        }
    }

    @Override
    public void resetRealm(UUID uuid) throws IOException, ZipException {
        // CLOSE REALM PORTAL AND KICK PLAYERS
        if (isRealmPortalOpen(uuid))
            closeRealmPortal(uuid, true);

        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_LAST_RESET, System.currentTimeMillis(), true);
        getRealm(uuid).setStatus(RealmStatus.RESETTING);

        // UNLOAD WORLD
        unloadRealmWorld(uuid);

        // DELETE REALM WORLD DIRECTORY
        FileUtils.forceDelete(new File(rootFolder.getAbsolutePath() + "/" + uuid.toString()));

        // LOAD NEW TEMPLATE
        loadTemplate(uuid);

        // CLOSE REALM //
        getRealm(uuid).setStatus(RealmStatus.CLOSED);
    }

    @Override
    public void unloadRealmWorld(UUID uuid) {
        if (!Realms.getInstance().isRealmCached(uuid)) return;

        // UNLOAD WORLD
        Utils.log.info("[REALM] [SYNC] Unloading realm world for " + uuid.toString());

        Bukkit.getWorlds().remove(getRealmWorld(uuid));
        Bukkit.getServer().unloadWorld(getRealmWorld(uuid), true);
    }

    @Override
    public void removeRealm(UUID uuid, boolean runAsync) {
        if (isRealmPortalOpen(uuid))
            closeRealmPortal(uuid, true);

        getRealm(uuid).setStatus(RealmStatus.REMOVING);

        // UNLOAD WORLD
        unloadRealmWorld(uuid);

        // SUBMITS ASYNC UPLOAD THREAD //
        uploadRealm(uuid, runAsync, success -> removeCachedRealm(uuid));
    }

    @Override
    public void removeCachedRealm(UUID uuid) {
        if (CACHED_REALMS.containsKey(uuid))
            CACHED_REALMS.remove(uuid);
    }

    @Override
    public void setRealmSpawn(UUID uuid, Location newLocation) {
        if (!isRealmLoaded(uuid)) return;

        newLocation.getBlock().setType(Material.PORTAL);
        newLocation.clone().subtract(0, 1, 0).getBlock().setType(Material.PORTAL);

        Location oldLocation = getRealmWorld(uuid).getSpawnLocation();
        oldLocation.getBlock().setType(Material.AIR);
        oldLocation.clone().subtract(0, 1, 0).getBlock().setType(Material.AIR);

        getRealmWorld(uuid).setSpawnLocation(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());
    }

    @Override
    public void setRealmTitle(UUID uuid, String title) {
        DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.REALM_TITLE, title, true);
    }

    @Override
    public String getRealmTitle(UUID uuid) {
        return (String) DatabaseAPI.getInstance().getData(EnumData.REALM_TITLE, uuid);
    }

    @Override
    public String getRealmStatusMessage(RealmStatus status) {
        String message = ChatColor.RED + "Please wait your realm is being ";

        switch (status) {
            case CREATING:
                message = message + "created";
                break;

            case REMOVING:
                message = message + "removed";
                break;

            case RESETTING:
                message = message + "reset";
                break;

            case DOWNLOADING:
                message = message + "downloaded";
                break;

            case UPLOADING:
                message = message + "uploaded";
                break;
        }

        return message + " ...";
    }

    public int getRealmTier(UUID uuid) {
        return 1;
    }

    @Override
    public void updateRealmHologram(UUID uuid) {
        if (!isRealmCached(uuid)) return;

        RealmToken realm = getRealm(uuid);

        Hologram realmHologram = realm.getHologram();

        String name = Bukkit.getPlayer(uuid).getName();

        if (realmHologram == null) return;
        realmHologram.clearLines();

        if (getRealmTitle(realm.getOwner()).equals("")) {
            realmHologram.insertTextLine(0, ChatColor.WHITE.toString() + ChatColor.BOLD + name);
            realmHologram.insertTextLine(1, realm.isPeaceful() ? ChatColor.AQUA + "Peaceful" : ChatColor.RED + "Chaotic");
        } else {
            realmHologram.insertTextLine(0, (ChatColor.WHITE.toString() + ChatColor.BOLD + name + ChatColor.WHITE + " - [" + ChatColor.DARK_PURPLE + getRealmTitle(realm.getOwner()) + ChatColor.WHITE + "]").trim());
            realmHologram.insertTextLine(1, realm.isPeaceful() ? ChatColor.AQUA + "Peaceful" : ChatColor.RED + "Chaotic");
        }
    }

    @Override
    public World getRealmWorld(UUID uuid) {
        return Bukkit.getWorld(uuid.toString());
    }

    @Override
    public int getRealmDimensions(int tier) {
        switch (tier) {
            case 1:
                return 17;
            case 2:
                return 22;
            case 3:
                return 32;
            case 4:
                return 45;
            case 5:
                return 64;
            case 6:
                return 82;
            case 7:
                return 128;
            default:
                return -1;
        }
    }

    public boolean isRealmCached(UUID uuid) {
        return CACHED_REALMS.containsKey(uuid);
    }

    public boolean isRealmLoaded(UUID uuid) {
        return isRealmCached(uuid) && getRealm(uuid).isLoaded() && getRealmWorld(uuid) != null;
    }

    @Override
    public boolean isRealmPortalOpen(UUID uuid) {
        return isRealmLoaded(uuid) && getRealm(uuid).getStatus() == RealmStatus.OPENED;
    }

    private void loadRealm(Player player, boolean create) {
        try {
            if (create) {
                player.sendMessage(ChatColor.GREEN + "Creating a new realm for you...");
                loadTemplate(player.getUniqueId());
            } else
                loadRealmWorld(player.getUniqueId());
        } catch (Exception e) {
            this.handleRealmLoadFailure(player, e);
        }
    }

    @Override
    public void handleRealmLoadFailure(Player player, Throwable thrown) {
        if (CACHED_REALMS.containsKey(player.getUniqueId()))
            CACHED_REALMS.remove(player.getUniqueId());

        player.sendMessage(ChatColor.RED + "There was an error whilst trying to loaded your realm!");
        thrown.printStackTrace();
    }

    private void zip(String targetFolderPath, String destinationFilePath) throws ZipException {
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        parameters.setIncludeRootFolder(false);


        ZipFile zipFile = new ZipFile(destinationFilePath);

        File targetFile = new File(targetFolderPath);
        if (targetFile.isFile()) {
            zipFile.addFile(targetFile, parameters);
        } else if (targetFile.isDirectory()) {
            zipFile.addFolder(targetFile, parameters);
        } else {
            System.out.println("ERROR ERROR, HOLY SHIT");
        }
    }

    public void generateRealmBase(UUID uuid) {
        WorldCreator worldCreator = new WorldCreator(uuid.toString());
        worldCreator.type(WorldType.FLAT);
        worldCreator.generateStructures(false);
        worldCreator.generator(new RealmGenerator());
        World world = Bukkit.createWorld(worldCreator);
        world.setKeepSpawnInMemory(false);
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