package net.dungeonrealms.game.world.realms;

import lombok.Data;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/28/2016
 */
@Data
public class RealmProperty<T> {

    private final String name;

    private T value;

    private long expiry;

    private boolean acknowledgeExpiration = true;

    public RealmProperty(String name) {
        this.name = name;
    }

    public RealmProperty(String name, T def) {
        this.name = name;
        this.value = def;
    }

    public boolean hasExpired() {
        return expiry != 0 && System.currentTimeMillis() >= expiry;
    }
}
