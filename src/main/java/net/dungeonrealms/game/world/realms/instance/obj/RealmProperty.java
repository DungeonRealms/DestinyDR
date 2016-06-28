package net.dungeonrealms.game.world.realms.instance.obj;

import lombok.Getter;
import lombok.Setter;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/28/2016
 */
public class RealmProperty<T> {

    @Getter
    private final String name;

    @Getter
    @Setter
    private T value;

    @Setter
    @Getter
    private long expiry;


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
