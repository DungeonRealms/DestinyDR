package net.dungeonrealms.old.game.world.realms;

import net.dungeonrealms.old.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.old.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.old.game.world.realms.instance.RealmInstance;
import net.dungeonrealms.old.game.world.realms.instance.obj.RealmState;
import net.dungeonrealms.old.game.world.realms.instance.obj.RealmToken;
import net.lingala.zip4j.exception.ZipException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public interface Realms extends GenericMechanic {

    static Realms getInstance() {
        return RealmInstance.getInstance();
    }

    // BUFFER SIZE OF BLOCK PROCESSOR //
    int BLOCK_PROCESSOR_BUFFER_SIZE = 1024;


    /**
     * @return EnumPriority.BISHOP
     */
    EnumPriority startPriority();


    /**
     * Instantiate realm cache folders
     */
    void startInitialization();


    /**
     * All realms must be uploaded and removed from cache before shutdown
     */
    void stopInvocation();


    /**
     * Opens the player's realm portal
     * Realm should be already cached before executing this command
     *
     * @param player   Owner of realm
     * @param location Desired location for portal
     */
    void openRealmPortal(Player player, Location location);


    /**
     * Opens the material store for purchasing
     * blocks used for building in realms
     *
     * @param player Owner of realm
     */
    void openRealmMaterialStore(Player player);


    /**
     * Loads the player's realm*
     *
     * @param player  Owner of realm
     * @param doAfter What should be executed after?
     */
    void loadRealm(Player player, Runnable doAfter);

    /**
     * Checks player can place realm portal
     *
     * @param player   Owner of realm
     * @param location Desired location for portal
     */
    boolean canPlacePortal(Player player, Location location);


    /**
     * Saves all realms
     */
    void saveAllRealms();


    /**
     * Sets up realm world guard region
     *
     * @param world     Realm world
     * @param isChaotic Is chaotic?
     */
    void setRealmRegion(World world, boolean isChaotic);


    /**
     * Loads the realm world
     *
     * @param uuid Owner of realm
     */
    void loadRealmWorld(UUID uuid);


    /**
     * Uploads all open realms to Master FTP Serve
     *
     * @param runAsync Should execute on async pool?
     */
    void removeAllRealms(boolean runAsync);


    /**
     * Executed when player logs of DR
     *
     * @param player Player who is logging out
     */
    void doLogout(Player player);


    /**
     * This function downloads the player's realm from the realm FTP database if it exists
     *
     * @param uuid Owner of realm
     */
    boolean downloadRealm(UUID uuid) throws IOException, ZipException;

    /**
     * Unzips generic world for realms
     *
     * @param player Owner of realm
     */
    boolean loadTemplate(UUID player) throws IOException, ZipException;


    /**
     * This function uploads the player's realm to master ftp server for it to be downloaded
     * by the other shards
     *
     * @param runAsync          Should execute on async pool?
     * @param removeCacheFolder Removed cached folder
     * @param uuid              Owner of realm
     */
    void uploadRealm(UUID uuid, boolean removeCacheFolder, boolean runAsync, Consumer<Boolean> doAfter);

    /**
     * Closes the realm portal
     *
     * @param uuid        Owner of realm
     * @param kickPlayers Kick all players?
     * @param kickMessage Kick message
     */
    void closeRealmPortal(UUID uuid, boolean kickPlayers, String kickMessage);

    /**
     * Reset realm for player
     *
     * @param player Owner of realm
     */
    void resetRealm(Player player) throws IOException;

    /**
     * Completely wipe realm
     *
     * @param uuid Owner of realm
     */
    void wipeRealm(UUID uuid) throws IOException;


    /**
     * Upgrades realm dimensions
     *
     * @param player Owner of realm
     */
    void upgradeRealm(Player player);


    /**
     * Unloads realm world
     *
     * @param uuid Owner of realm
     */
    void unloadRealmWorld(UUID uuid);


    /**
     * Removes entire realm for server and uploads it to FTP
     *
     * @param runAsync Should execute on async pool?
     * @param uuid     Owner of realm
     */
    void removeRealm(UUID uuid, boolean runAsync);

    /**
     * Removes the cached realm token.
     *
     * @param uuid Owner of realm
     */
    void removeCachedRealm(UUID uuid);

    /**
     * Set realm spawn and realm portal back to main world
     *
     * @param uuid        Owner of realm
     * @param newLocation Location must be above air to place portal
     */
    void setRealmSpawn(UUID uuid, Location newLocation);

    /**
     * Sets the title of realm.
     *
     * @param uuid Owner of realm
     */
    void setRealmTitle(UUID uuid, String title);

    /**
     * @param uuid Owner of realm
     * @return Title of realm
     */
    String getRealmTitle(UUID uuid);


    /**
     * @param status Status of realm
     * @return Status message
     */
    String getRealmStatusMessage(RealmState status);


    /**
     * Updates the realm hologram to Chaotic or Peaceful
     *
     * @param uuid Owner of realm
     */
    void updateRealmHologram(UUID uuid);


    /**
     * @param uuid Owner of realm
     * @return World object of realm
     */
    World getRealmWorld(UUID uuid);


    /**
     * Realm Dimensions
     *
     * @param tier Tier level of realm
     * @return Realm dimensions number = NxNxN
     */
    int getRealmDimensions(int tier);


    /**
     * Realm tier
     *
     * @param uuid Owner of realm
     * @return Realm size tier
     */
    int getRealmTier(UUID uuid);

    /**
     * Realm upgrade cost
     *
     * @param tier Realm tier
     */
    int getRealmUpgradeCost(int tier);


    /**
     * Checks player's realm is cached
     *
     * @param uuid Owner of realm
     */
    boolean isRealmCached(UUID uuid);

    /**
     * Checks if the player's realm is loaded.
     *
     * @param uuid Owner of realm
     */
    boolean isRealmLoaded(UUID uuid);

    /**
     * Checks if any realm is being upgraded
     */
    boolean realmsAreUpgrading();

    /**
     * Checks if the player's realm is loaded.
     *
     * @param uuid Owner of realm
     */
    boolean isRealmPortalOpen(UUID uuid);


    /**
     * @return Players realms.
     */
    Map<UUID, RealmToken> getCachedRealms();


    /**
     * @return Processing blocks
     */
    Map<UUID, List<Location>> getProcessingBlocks();

    /**
     * @param uuid Owner of realm
     * @return Players realm.
     */
    RealmToken getToken(UUID uuid);

    /**
     * @param uuid Owner of realm
     * @return Players realm state.
     */
    RealmState getRealmStatus(UUID uuid);

    /**
     * @param portalLocation Location
     * @return Players realm.
     */
    RealmToken getToken(Location portalLocation);


    /**
     * @param world Realm world
     * @return Players realm.
     */
    RealmToken getToken(World world);
}
