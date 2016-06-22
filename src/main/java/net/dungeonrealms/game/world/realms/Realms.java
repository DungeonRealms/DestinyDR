package net.dungeonrealms.game.world.realms;

import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.world.realms.instance.RealmInstance;
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
     * This function downloads the player's realm if it exists
     *
     * @param player Owner of realm
     */
    boolean downloadRealm(UUID player);


    /**
     * Unzips default world for realms
     *
     * @param player Owner of realm
     */
    void loadTemplate(UUID player) throws ZipException;


}
