package net.dungeonrealms.game.mongo;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/2/2016
 */
public interface PlayerDataGrabber<T> {


    /**
     *
     * This method is used to retrieve values from
     * the player database
     *
     * @param uuid UUID of player
     * @param data Player data enum
     * @return Player data.
     */
    T getValue(UUID uuid, EnumData data);

}
