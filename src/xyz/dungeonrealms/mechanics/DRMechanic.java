package xyz.dungeonrealms.mechanics;

import xyz.dungeonrealms.DungeonRealms;

/**
 * Created by Nick on 12/10/2015.
 */
public interface DRMechanic {

    public default void onStart() {
        DungeonRealms.log.info("Starting Mechanic");
    }

    public void onDisable();

}
