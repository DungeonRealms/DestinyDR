package net.dungeonrealms.game.handlers;

import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/20/2015.
 */
public class BountyHandler {

    static BountyHandler instance = null;

    public static BountyHandler getInstance() {
        if (instance == null) {
            instance = new BountyHandler();
        }
        return instance;
    }

    public void startInitialization() {
    }


    public void setBounty(Player player, int amount) {

    }

    public void clearBounty(Player player) {

    }

}
