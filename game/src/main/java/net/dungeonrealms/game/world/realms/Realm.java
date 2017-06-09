package net.dungeonrealms.game.world.realms;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.item.items.functional.ItemPortalRune;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.rifts.RiftPortal;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenRealm;
import net.dungeonrealms.game.world.loot.LootManager;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;

/**
 * Realm - Realms restructure.
 *
 * @author Kneesnap
 */
public class Realm {

    private static File ROOT = new File(System.getProperty("user.dir"));

    @Getter //The UUID of the owner of this realm.
    private UUID owner;

    @Getter
    private int characterID;


    @Getter //The owner's username.
    private String name;

    @Getter
    @Setter //The realm state.
    private RealmState state;

    @Getter
    @Setter //The location of the portal. (This is the portal in the main world, not in a realm.)
    private Location portalLocation;

    @Setter //has this realm loaded?
    private boolean loaded = false;

    @Getter
    @Setter //Is the realm portal being set?
    private boolean settingSpawn = false;

    @Getter
    @Setter //The progress of this realm while upgrading.
    private double upgradeProgress;

    @Getter
    @Setter // The hologram that displays over the portal.
    private Hologram hologram;

    @Getter // A list of players allowed to build here.
    private Set<UUID> builders = new CopyOnWriteArraySet<>();

    @Getter //Properties.
    private Map<String, RealmProperty<?>> realmProperties = new HashMap<>();

    private RealmTier tier;

    public Realm(UUID owner, int characterID, String name) {
        this.owner = owner;
        this.characterID = characterID;
        this.name = name;

        // MUST BE ADDED IN THIS ORDER //
        addProperty(new RealmProperty<>("peaceful", false));
        addProperty(new RealmProperty<>("flight", false));

        setState(RealmState.CLOSED);
    }

    /**
     * Opens a realm portal at a given location. Grabs the realm from FTP if needed.
     */
    public void openPortal(Player player, Location location) {
        if (!canPlacePortal(player, location))
            return;

        loadRealm(player, () -> {

            //Their realm is upgrading, shouldn't be accessible.
            if (getState() == RealmState.UPGRADING) {
                DecimalFormat format = new DecimalFormat("#.##");
                player.sendMessage(ChatColor.RED + "This realm is currently UPGRADING. " + ChatColor.BOLD + format.format(getUpgradeProgress()) + "% Complete.");
                return;
            }

            //Their realm is currently in a state we don't allow opening from.
            if (getState() != RealmState.OPENED && getState() != RealmState.CLOSED) {
                player.sendMessage(getState().getStatusMessage());
                return;
            }

            if (!canPlacePortal(player, location))
                return; // Check again, it's possible the world could have changed since the download started.

            if (isLoaded())
                removePortal(null);

            setPortalLocation(location.clone().add(0, 1, 0));

            location.add(0, 1, 0).getBlock().setType(Material.PORTAL);
            location.add(0, 1, 0).getBlock().setType(Material.PORTAL);

            Hologram realmHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), location.add(0.5, 1.5, 0.5));
            realmHologram.getVisibilityManager().setVisibleByDefault(true);
            setHologram(realmHologram);

            updateWGFlags();
            updateHologram();

            setState(RealmState.OPENED);

            //Send player alert.
            Utils.sendCenteredMessage(player, ChatColor.LIGHT_PURPLE + "* Realm Portal OPENED *");
            player.getWorld().playEffect(portalLocation, Effect.ENDER_SIGNAL, 10);
            player.playSound(getPortalLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 5F, 1.25F);

            //Show title message.
            String title = getTitle();
            if (title == null)
                player.sendMessage(ChatColor.GRAY + "Type /realm <TITLE> to set the description of your realm, it will be displayed to all visitors.");
            else
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Description: " + title);

            //Trigger Quest Objective
            Quests.getInstance().triggerObjective(player, ObjectiveOpenRealm.class);

        });
    }

    /**
     * Is this realm loaded, and the portal open?
     */
    public boolean isOpen() {
        return isLoaded() && getWorld() != null && getState() == RealmState.OPENED && getPortalLocation() != null;
    }

    public boolean canPlacePortal(Player player, Location location) {

        if (GameAPI.getGamePlayer(player) != null && GameAPI.getGamePlayer(player).isJailed()) {
            player.sendMessage(ChatColor.RED + "You have been jailed.");
            return false;
        }

        if (CombatLog.isInCombat(player)) {
            player.sendMessage(ChatColor.RED + "Cannot open realm while in Combat!");
            return false;
        }

        if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            player.sendMessage(ChatColor.RED + "You can only open a realm portal in the main world!");
            return false;
        }

        if (LootManager.checkLocationForLootSpawner(location.clone())) {
            player.sendMessage(ChatColor.RED + "You cannot place a realm portal this close to a loot chest spawn.");
            return false;
        }

        if (location.clone().add(0, 1, 0).getBlock().getType() != Material.AIR || location.clone().add(0, 2, 0).getBlock().getType() != Material.AIR || location.clone().add(0, 3, 0).getBlock().getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
            return false;
        }

        if (GameAPI.isMaterialNearby(location.clone().getBlock(), 3, Material.LADDER) || GameAPI.isMaterialNearby(location.clone().getBlock(), 10, Material.ENDER_CHEST)) {
            player.sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
            return false;
        }

        RiftPortal nearby = RiftPortal.getNearbyRiftPortal(location, 16);
        if (nearby != null) {
            player.sendMessage(ChatColor.RED + "You cannot place a realm portal so close to a Rift Portal!");
            return false;
        }

        if (isPortalNearby(location.clone().add(0, 1, 0), 3) || GameAPI.isMaterialNearby(location.clone().getBlock(), 3, Material.PORTAL)) {
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " open a realm portal so close to another.");
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "REQ:" + ChatColor.GRAY + " >3 blocks away.");
            return false;
        }

        if (GameAPI.getRegionName(player.getLocation()).equalsIgnoreCase("tutorial_island")) {
            player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED
                    + " open a portal to your realm until you have left the tutorial.");
            return false;
        }

        if (DungeonRealms.getInstance().getRebootTime() - System.currentTimeMillis() < 5 * 60 * 1000) {
            player.sendMessage(ChatColor.RED + "This shard is rebooting in less than 5 minutes, so you cannot open your realm on this shard.");
            return false;
        }

        for (Player p : Bukkit.getWorlds().get(0).getPlayers()) {
            if (p.getName().equals(player.getName())) continue;
            if (!p.getWorld().equals(player.getWorld())) continue;
            if (p.getLocation().distance(location.clone()) <= 2) {
                player.sendMessage(ChatColor.RED + "You cannot place your realm portal near another player");
                return false;
            }
        }

        return true;
    }

    private boolean isPortalNearby(Location location, int radius) {
        double rad = Math.pow(radius, 2);
        for (Realm realm : Realms.getInstance().getRealms())
            if (realm.isOpen() && realm.getPortalLocation().distanceSquared(location) <= rad)
                return true;
        return false;
    }

    /**
     * Remove the portal block from the main world
     */
    public void removePortal(String kickMessage) {
        if (!isLoaded() || getPortalLocation() == null)
            return;

        Location portalLocation = getPortalLocation().clone();

        portalLocation.getBlock().setType(Material.AIR);
        portalLocation.add(0, 1, 0).getBlock().setType(Material.AIR);

        portalLocation.getWorld().playSound(portalLocation, Sound.ENTITY_ENDERMEN_TELEPORT, 1.5F, 0.75F);

        if (getHologram() != null)
            getHologram().delete();

        setPortalLocation(null);
        setState(RealmState.CLOSED);

        getWorld().getPlayers().stream().filter(p -> p != null).forEach(p -> {
            if (kickMessage != null && !isOwner(p))
                p.sendMessage(kickMessage);

            GameAPI.teleport(p, portalLocation);
        });
    }

    /**
     * Can a player build in this realm? (Checks if the player is a GM, or if they are on the builder list / owns the realm.
     */
    public boolean canBuild(Player player) {
        return Rank.isTrialGM(player) || isOwner(player) || getBuilders().contains(player.getUniqueId());
    }

    /**
     * Returns the realm tier.
     */
    public RealmTier getTier() {
        //Return this so players whose realms are upgrading while they are offline don't spam DB calls.
        if (Bukkit.getPlayer(getOwner()) == null && this.tier != null)
            return this.tier;

        RealmTier tier = Realms.getRealmTier(getOwner());
        this.tier = tier;
        return tier;
    }

    /**
     * Upgrades this realm to the next tier.
     */
    public void upgradeRealm(Player player) {
        RealmTier oldTier = getTier();
        RealmTier newTier = RealmTier.getByTier(oldTier.getTier() + 1);

        //Kick players from realm.
        removePortal(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + getName() + ChatColor.LIGHT_PURPLE + "'s realm is currently UPGRADING to "
                + ChatColor.BOLD + newTier.getDimensions() + "x" + newTier.getDimensions() + ChatColor.LIGHT_PURPLE
                + ", you have been kicked out of the realm while the upgrade takes place.");

        player.sendMessage(ChatColor.LIGHT_PURPLE + "Upgrading realm to " + ChatColor.BOLD + newTier.getDimensions() + "x" + newTier.getDimensions());
        player.sendMessage(ChatColor.GRAY + "We will notify you once it is ready.");

        //Give achievement (if any)
        if (newTier.getAchievement() != null)
            Achievements.giveAchievement(getOwner(), newTier.getAchievement());

        // Set data.
        setState(RealmState.UPGRADING);
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        wrapper.setRealmTier(newTier.getTier());
        wrapper.setUpgradingRealm(true);


        Realms.getInstance().upgradeRealmBlocks(this, newTier);

        updateRune();
    }

    /**
     * Updates the rune of the owner of this realm.
     */
    private void updateRune() {
        Player owner = Bukkit.getPlayer(getOwner());

        if (owner == null) {
            return;
        }

        int slot = GameAPI.getItemSlot(owner.getInventory(), "portalRune");
        if (slot != -1) {
            owner.getInventory().setItem(slot, new ItemPortalRune(owner).generateItem());
        }
        owner.updateInventory();
    }

    /**
     * Updates the realm hologram.
     */
    public void updateHologram() {
        if (hologram == null || hologram.isDeleted()) return;

        hologram.clearLines();
        hologram.insertTextLine(0, ChatColor.WHITE.toString() + ChatColor.BOLD + getName());
        hologram.insertTextLine(1, isChaotic() ? ChatColor.RED + "Chaotic" : ChatColor.AQUA + "Peaceful");
    }


    /**
     * Returns the realm title if it exists, otherwise null.
     */
    public String getTitle() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(getOwner());
        if (wrapper == null) return null;
        String title = wrapper.getRealmTitle();
        return (title == null || title.equals("")) ? null : ChatColor.GRAY + title;
    }

    /**
     * Reset this realm.
     */
    public void resetRealm(Player player) {
        // Close realm / remove players.
        removePortal(ChatColor.RED + player.getName() + " is resetting this realm...");

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        wrapper.setLastRealmReset(System.currentTimeMillis());
        setState(RealmState.RESETTING);

        // UNLOAD WORLD
        unloadRealmWorld();
        setLoaded(false);
        try {
            wipeRealm();
        } catch (IOException e) {
            e.printStackTrace();
        }

        wrapper.setRealmTitle("");
        setState(RealmState.CLOSED);
        wrapper.setRealmTier(1);

        if (isOwner(player)) {
            Utils.sendCenteredMessage(player, ChatColor.YELLOW.toString() + ChatColor.BOLD + "Your realm has successfully been reset!");
        } else {
            Utils.sendCenteredMessage(player, ChatColor.YELLOW.toString() + ChatColor.BOLD + getName() + "'s realm has been reset.");
        }

        this.updateRune();
    }

    public void wipeRealm() throws IOException {
        if (DungeonRealms.isMaster()) {
            File f = new File(getSaveFilePath());
            if (f.exists())
                f.delete();
            return;
        }

        FTPClient ftpClient = DungeonRealms.getInstance().getFTPClient();

        try {
            Utils.log.info("[REALM] Removing " + getName() + "'s realm: " + (ftpClient.deleteFile("/realms/" + getOwner().toString() + ".zip") ? "SUCCESS" : "FAILURE"));
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

    /**
     * Set the spawn location inside of a realm.
     */
    public void setRealmSpawn(Location newLocation) {
        if (!isLoaded()) return;

        // Used by a BlockPhysicsEvent to make sure the portal we place does not get removed.
        setSettingSpawn(true);

        // Remove old portal
        Location oldLocation = getWorld().getSpawnLocation().clone();
        oldLocation.getBlock().setType(Material.AIR);
        oldLocation.subtract(0, 1, 0).getBlock().setType(Material.AIR);


        // Create new portal
        newLocation.getBlock().setType(Material.PORTAL);
        newLocation.clone().subtract(0, 1, 0).getBlock().setType(Material.PORTAL);

        // Update spawn location.
        getWorld().setSpawnLocation(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());

        setSettingSpawn(false);
    }

    /**
     * Loads the realm from FTP.
     */
    private void loadRealm(Player player, Runnable doAfter) {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        // Don't load a realm that's already loaded.
        if (isLoaded()) {
            if (doAfter != null)
                doAfter.run();
            return;
        }

        // Their realm is uploading still..
        if (wrapper.isUploadingRealm()) {
            player.sendMessage(ChatColor.RED + "Your realm is being uploaded from another shard.");
            return;
        }

        // Their realm is upgrading.
        if (wrapper.isUpgradingRealm()) {
            player.sendMessage(ChatColor.RED + "Your realm is upgrading on another shard.");
            return;
        }

        // Realms are not allowed on this shard.
        if (DungeonRealms.isEvent()) {
            player.sendMessage(ChatColor.RED + "Realms are disabled on this shard.");
            return;
        }

        //Set the realm as downloading.
        setState(RealmState.DOWNLOADING);
        player.sendMessage(ChatColor.YELLOW + "Please wait whilst your realm is loaded...");

        GameAPI.submitAsyncCallback(this::downloadRealm, callback -> {
            try {
                final boolean result = callback.get();

                // RUN SYNC AGAIN //
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {

                    if (!result)
                        Utils.sendCenteredMessage(player, ChatColor.LIGHT_PURPLE + "* REALM CREATED *");


                    setupRealm(player, !result, () -> {
                        player.sendMessage(ChatColor.YELLOW + "Your realm has been loaded.");

                        setLoaded(true);
                        setState(RealmState.CLOSED);

                        if (doAfter != null)
                            doAfter.run();
                    });
                });

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "There was an error whilst trying to download your realm! Please contact a game master to solve this issue");
                GameAPI.sendError("Failed to load " + getName() + "'s realm on {SERVER}.");
                Realms.getInstance().getRealmMap().remove(getOwner());
                e.printStackTrace();
            }
        });
    }

    /**
     * Does this player own this realm?
     */
    public boolean isOwner(Player player) {
        return getOwner().equals(player.getUniqueId());
    }

    public boolean isLoaded() {
        return loaded && getWorld() != null;
    }

    /**
     * Downloads a realm from FTP
     */
    private boolean downloadRealm() throws IOException, ZipException {
        // Realms are stored locally on the master shard.
        if (DungeonRealms.isMaster()) {
            File realm = new File(getSaveFilePath());
            if (!realm.exists())
                return false;
            // Extract the realm.
            ZipFile zipFile = new ZipFile(realm);
            zipFile.extractAll(getWorldFolder());
            return true;
        }

        FTPClient ftpClient = DungeonRealms.getInstance().getFTPClient();
        FileOutputStream fos = null;
        File tempLocation = new File(getSaveFilePath());

        try {
            Utils.log.info("[REALM] Downloading " + getName() + "'s realm. " + characterID);

            fos = new FileOutputStream(tempLocation);

            if (ftpClient.retrieveFile("/realms/" + String.valueOf(characterID) + ".zip", fos)) {

                // Extract the realm we just downloaded.
                ZipFile zipFile = new ZipFile(tempLocation);
                zipFile.extractAll(getWorldFolder());
                Utils.log.info("[REALM] Downloaded and extracted " + getName() + "'s realm. " + characterID);

                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) fos.close();
            FileUtils.forceDelete(tempLocation);

            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
        return false;
    }

    /**
     * Called when a player logs out.
     * Removes portal -> Unloads world -> Uploads to FTP.
     */
    public void removeRealm(boolean runAsync) {
        if (!isLoaded())
            return;

        removePortal(null);
        setState(RealmState.REMOVING);

        // UNLOAD WORLD
        unloadRealmWorld();

        // SUBMITS ASYNC UPLOAD THREAD //
        if (runAsync) {
            GameAPI.submitAsyncCallback(() -> {
                uploadRealm(true);
                return true;
            }, null);
        } else {
            uploadRealm(true);
        }
    }

    /**
     * Unloads a realm world.
     */
    private void unloadRealmWorld() {
        if (!isLoaded() || CrashDetector.crashDetected) return;

        //Unload World
        Utils.log.info("[REALM] Unloading realm for " + getOwner().toString());

        //Delete loaded items.
        getWorld().getEntities().stream().filter(e -> e instanceof Item).forEach(Entity::remove);

        World w = getWorld();
        Bukkit.getWorlds().remove(w);
        Bukkit.getServer().unloadWorld(w, true);
    }

    /**
     * Zips up a realm and uploads it to FTP.
     */
    void uploadRealm(boolean deleteWorld) {
        Utils.log.info("[REALM] Compressing " + getName() + "'s realm.");

        try {
            GameAPI.createZipFile(getWorldFolder(), getSaveFilePath());
            uploadZippedRealm();
        } catch (ZipException e) {
            e.printStackTrace();
        } finally {
            if (deleteWorld)
                Utils.log.info("[REALM] Deleting " + getName() + "'s realm locally.");

            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(getOwner());
            if (wrapper != null)
                wrapper.setUploadingRealm(false);

            SQLDatabaseAPI.getInstance().executeUpdate(updates -> GameAPI.updatePlayerData(getOwner(), UpdateType.REALM),
                    QueryType.SET_REALM_UPLOADING.getQuery(0, characterID));

            setState(RealmState.CLOSED);
            Realms.getInstance().getRealmMap().remove(getOwner());

            try {
                if (!DungeonRealms.isMaster())
                    FileUtils.forceDelete(new File(getSaveFilePath()));

                if (deleteWorld)
                    FileUtils.forceDelete(new File(getWorldFolder()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Realms.getInstance().getRealmMap().remove(getOwner());
        }
    }

    public void sendInformation(Player player) {
        Player owner = Bukkit.getPlayer(getOwner());
        if (owner == null) {
            player.sendMessage(ChatColor.RED + "This realm owner is no longer online.");
            return;
        }
        PlayerWrapper realmOwner = PlayerWrapper.getPlayerWrapper(owner);
        if (realmOwner == null) return;
        player.sendMessage(ChatColor.LIGHT_PURPLE + "This portal teleports to " + ChatColor.BOLD + realmOwner.getDisplayName() + "'s" + ChatColor.LIGHT_PURPLE + " Realm");

        if (!isChaotic()) {

            boolean flight = getProperty("flight");

            int safeSeconds = getSecondsOfSafezoneLeft();
            int flySeconds = getSecondsOfFlyingLeft();

            if (flight) {
                player.sendMessage(ChatColor.GRAY.toString() + "Realm Type: " + ChatColor.GREEN + "SAFE" +
                        (safeSeconds > 1 ? ChatColor.GRAY + " for " + safeSeconds + "s" : "") + " │ " + ChatColor.GRAY + "Flying " + ChatColor.AQUA + "" + ChatColor.UNDERLINE
                        + "ENABLED" + (flySeconds > 1 ? ChatColor.GRAY + " for " + flySeconds + "s" : ""));
            } else {
                player.sendMessage(ChatColor.GRAY.toString() + "Realm Type: " + ChatColor.GREEN + "SAFE" + (safeSeconds > 1 ? ChatColor.GRAY + "for "
                        + safeSeconds + "s" : ""));
            }
        } else {
            player.sendMessage(ChatColor.GRAY.toString() + "Realm Type: " + ChatColor.RED + "CHAOTIC");
        }

        if (getWorld() != null) {
            player.sendMessage(ChatColor.GRAY + "Realm Population: " + getWorld().getPlayers().size() + " player(s)");
        }

        if (getTitle() != null && !getTitle().trim().equalsIgnoreCase("null")) {
            player.sendMessage(ChatColor.GRAY + getTitle());
        } else {
            player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "No description.");
        }
    }

    /**
     * Setup realm before opening.
     */
    private void setupRealm(Player player, boolean create, Runnable doAfter) {
        if (create) {
            //Create a new realm.
            GameAPI.submitAsyncCallback(() -> loadTemplate(), callback -> setupRealm(player, false, doAfter));
        } else {
            //Setup an existing realm.
            setupRealmWorld();
            if (doAfter != null)
                doAfter.run();
        }
    }

    /**
     * Setup world object for this realm.
     */
    private void setupRealmWorld() {
        Utils.log.info("[REALM] Setting up " + getName() + "'s realm.");

        WorldCreator wc = new WorldCreator(getOwner().toString());

        //Unsure this is supposed to use the WC object above instead of a default one.
        World world = Bukkit.getServer().createWorld(wc);
        world.setKeepSpawnInMemory(false);
        world.setStorm(false);

        // Remove items on the ground.
        world.getEntities().stream().filter(e -> e instanceof Item).forEach(Entity::remove);

        Utils.log.info("[REALM] Finished setting up " + getName() + "'s realm.");
    }

    public int getSecondsOfSafezoneLeft() {
        RealmProperty<Boolean> peaceful = (RealmProperty<Boolean>) getRealmProperties().get("peaceful");
        if (peaceful == null || peaceful.hasExpired()) return 0;
        long o_time = peaceful.getExpiry();
        long seconds_left = (o_time - System.currentTimeMillis()) / 1000;
        return (int) seconds_left;
    }

    public int getSecondsOfFlyingLeft() {
        RealmProperty<Boolean> flight = (RealmProperty<Boolean>) getRealmProperties().get("flight");
        if (flight == null || flight.hasExpired()) return 0;
        long o_time = flight.getExpiry();
        long seconds_left = (o_time - System.currentTimeMillis()) / 1000;
        return (int) seconds_left;
    }

    /**
     * Updates worldguard flags.
     * Should be called whenever chaotic status is changed.
     */
    public void updateWGFlags() {
        RegionManager regionManager = GameAPI.getWorldGuard().getRegionManager(getWorld());
        if (regionManager != null) {
            ProtectedRegion global = regionManager.getRegion("__global__");

            if (global == null) {
                global = new GlobalProtectedRegion("__global__");
                regionManager.addRegion(global);
            }

            global.setFlag(DefaultFlag.MOB_DAMAGE, isChaotic() ? StateFlag.State.ALLOW : StateFlag.State.DENY);
            global.setFlag(DefaultFlag.PVP, isChaotic() ? StateFlag.State.ALLOW : StateFlag.State.DENY);

            try {
                regionManager.save();
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Is this realm chaotic?
     */
    public boolean isChaotic() {
        return !(GameAPI.isInSafeRegion(getPortalLocation()) || getProperty("peaceful"));
    }

    /**
     * Create a blank realm.
     */
    private boolean loadTemplate() throws IOException, ZipException {
        Utils.log.info("[REALM] Creating blank realm for " + getName());

        // Create the player realm folder
        File file = new File(this.getWorldFolder());

        // Remove existing world. (If any)
        if (file.exists())
            FileUtils.forceDelete(file);

        if (!file.mkdir()) throw new IOException();

        //Unzip Template -> World folder.
        Utils.log.info("[REALM] Extracting Realm template.");
        ZipFile realmTemplateFile = new ZipFile(DungeonRealms.getInstance().getDataFolder() + "/realms/realm_template.zip");
        realmTemplateFile.extractAll(getWorldFolder());
        return true;
    }

    /**
     * Uploads a zipped realm to the remote FTP server.
     * A seperate method so it can be called by Realms#startInitialization to upload broken realms.
     */
    static void uploadZippedRealm(File zipFile, String uuidName) {
        if (DungeonRealms.isMaster())
            return;

        InputStream inputStream = null;
        try {
            FTPClient ftpClient = DungeonRealms.getInstance().getFTPClient();

            inputStream = new FileInputStream(zipFile);

            ftpClient.storeFile("/realms/" + uuidName + ".zip", inputStream);
            Utils.log.info("[REALM] Finished uploading realm. (" + uuidName + ".zip)");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (inputStream != null)
                inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Uploads the local zipped realm to the remote FTP server.
     */
    private void uploadZippedRealm() {
        if (DungeonRealms.isMaster())
            return;
        Utils.log.info("[REALM] Started uploading " + getName() + "'s realm.");
        uploadZippedRealm(new File(getSaveFilePath()), String.valueOf(characterID));
    }

    /**
     * Gets the world data folder location
     */
    public String getWorldFolder() {
        return ROOT.getAbsolutePath() + "/" + owner.toString() + "/";
    }

    /**
     * Get the file location to zip this realm to.
     */
    public String getSaveFilePath() {
        return DungeonRealms.getInstance().getDataFolder().getAbsolutePath() + "/realms/" + (DungeonRealms.isMaster() ? "downloaded" : "uploading") + "/" + this.characterID + ".zip";
    }

    /**
     * Get the world object for this realm.
     */
    public World getWorld() {
        return Bukkit.getWorld(owner.toString());
    }

    public void addProperty(RealmProperty<?> property) {
        realmProperties.put(property.getName(), property);
    }

    @SuppressWarnings("unchecked")
    public boolean getProperty(String name) {
        RealmProperty<Boolean> property = (RealmProperty<Boolean>) realmProperties.get(name);
        return (boolean) property.getValue() && !property.hasExpired();
    }
}