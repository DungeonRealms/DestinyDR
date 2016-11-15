package net.dungeonrealms.common.old.game.database.player;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/16/2016
 */

@NoArgsConstructor
public class PlayerToken implements Serializable {

    /**
     * Player's name
     */
    @Getter
    private String name;


    /**
     * Player's UUID
     */
    private String uuid;


    public PlayerToken(String uuid) {
        this.uuid = uuid;
        this.name = "";
    }


    public PlayerToken(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUUID() {
        return uuid;
    }

    public String toString() {
        return uuid + "," + name;
    }

    public static PlayerToken fromString(String playerTokenString) {
        if (playerTokenString == null || playerTokenString.equals(""))
            return null;

        return new PlayerToken(playerTokenString.split(",")[0], playerTokenString.split(",")[1]);
    }


    public static PlayerToken fromUUID(UUID uuid) {
        return new PlayerToken(uuid.toString());
    }

    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof PlayerToken) && ((PlayerToken) obj).uuid.equals(uuid)) || (obj instanceof UUID && obj.equals(uuid));
    }


    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}