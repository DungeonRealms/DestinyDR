package net.dungeonrealms.game.world.realms.instance.obj;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.*;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */

public class RealmToken {

    @Getter
    private final UUID owner;

    @Getter
    private final String name;

    @Getter
    @Setter
    private RealmStatus status;

    @Getter
    @Setter
    private Location portalLocation;

    @Getter
    @Setter
    private boolean isLoaded = false;


    @Getter
    @Setter
    private Hologram hologram;

    @Getter
    private Set<UUID> playersInRealm = new HashSet<>();

    @Getter
    private Set<UUID> builders = new HashSet<>();

    @Getter
    private Map<String, RealmProperty> realmProperties = new HashMap<>();


    public RealmToken(UUID owner, String name) {
        this.owner = owner;
        this.name = name;

        // MUST BE ADDED IN THIS ORDER //
        addProperty(new RealmProperty<>("peaceful", false));
        addProperty(new RealmProperty<>("flying", false));
    }


    public void addProperty(RealmProperty<?> property) {
        realmProperties.put(property.getName(), property);
    }

    public boolean getPropertyBoolean(String name) {
        return (boolean) realmProperties.get(name).getValue() && !realmProperties.get(name).hasExpired();
    }

    public RealmProperty<?> getProperty(String name) {
        return realmProperties.get(name);
    }
}
