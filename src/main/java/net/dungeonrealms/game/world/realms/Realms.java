package net.dungeonrealms.game.world.realms;

import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.realms.instance.RealmInstance;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.lingala.zip4j.exception.ZipException;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public interface Realms extends GenericMechanic {

    static Realms getInstance() {
        return RealmInstance.getInstance();
    }


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
     * Loads the player's realm*
     *
     * @param player Owner of realm
     */
    void loadRealm(Player player);


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
    Future<Boolean> downloadRealm(UUID uuid);

    /**
     * This function uploads the player's realm to master ftp server for it to be downloaded
     * by the other shards
     *
     * @param runAsync Should execute on async pool?
     * @param uuid     Owner of realm
     */
    void uploadRealm(UUID uuid, boolean runAsync, Consumer<Boolean> doAfter);

    /**
     * Closes the realm portal
     *
     * @param uuid        Owner of realm
     * @param kickPlayers Kick all players?
     */
    void closeRealmPortal(UUID uuid, boolean kickPlayers);


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
     * @param uuid Owner of realm
     */
    void setRealmTitle(UUID uuid, String title);

    /**
     * @param uuid Owner of realm
     * @return Title of realm
     */
    String getRealmTitle(UUID uuid);


    /**
     * Updates the realm hologram to Chaotic or Peaceful
     *
     * @param realm Realm
     */
    void updateRealmHologram(RealmToken realm);


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
     * @return Realm size teir
     */
    int getRealmTier(UUID uuid);


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
     * Checks if the player's realm is loaded.
     *
     * @param uuid Owner of realm
     */
    boolean isRealmPortalOpen(UUID uuid);


    /**
     * Unzips default world for realms
     *
     * @param player Owner of realm
     */
    void loadTemplate(UUID player) throws ZipException;


    /**
     * @param uuid Owner of realm
     * @return Players realm.
     */
    RealmToken getRealm(UUID uuid);

    /**
     * @param portalLocation Location
     * @return Players realm.
     */
    RealmToken getRealm(Location portalLocation);


    /**
     * @param world Realm world
     * @return Players realm.
     */
    RealmToken getRealm(World world);
}
