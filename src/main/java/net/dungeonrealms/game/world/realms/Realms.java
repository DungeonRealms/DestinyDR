package net.dungeonrealms.game.world.realms;

import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.realms.instance.RealmInstance;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.lingala.zip4j.exception.ZipException;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public interface Realms extends GenericMechanic {


    static Realms getInstance() {
        return RealmInstance.getInstance();
    }


    EnumPriority startPriority();


    void startInitialization();


    void stopInvocation();


    /**
     * Opens the player's realm portal
     *
     * @param player   Owner of realm
     * @param location Desired location for portal
     */
    void openRealm(Player player, Location location);


    /**
     * Loads the player's realm*
     *
     * @param player Owner of realm
     */
    void loadRealm(Player player) throws ZipException;


    /**
     * This function downloads the player's realm from the realm FTP database if it exists
     *
     * @param uuid Owner of realm
     */
    boolean downloadRealm(UUID uuid);


    /**
     * Checks if the player's realm is loaded.
     *
     * @param location Location of portal
     */
    boolean canPlacePortal(Location location);


    /**
     * Checks if the player's realm is loaded.
     *
     * @param uuid Owner of realm
     */
    boolean isRealmLoaded(UUID uuid);


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
    RealmToken getOrCreateRealm(UUID uuid);
}
