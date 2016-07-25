package net.dungeonrealms.common.game.database.player;


import lombok.Getter;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 7/16/2016
 */

public class PlayerToken {

    /**
     * Player's name
     */
    @Getter
    private String name;


    /**
     * Player's UUID
     */
    private final UUID uuid;


    public PlayerToken(UUID uuid) {
        this.uuid = uuid;
        this.name = "";
    }


    public PlayerToken(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name.toLowerCase();
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public UUID getUUID() {
        return uuid;
    }

    public String toString() {
        return uuid.toString() + "," + name.toLowerCase();
    }

    public static PlayerToken fromString(String playerTokenString) {
        if (playerTokenString == null || playerTokenString.equals(""))
            return null;

        return new PlayerToken(UUID.fromString(playerTokenString.split(",")[0]), playerTokenString.split(",")[1]);
    }


    public static PlayerToken fromUUID(UUID uuid) {
        return new PlayerToken(uuid);
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