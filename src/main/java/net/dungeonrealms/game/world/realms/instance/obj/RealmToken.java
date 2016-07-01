package net.dungeonrealms.game.world.realms.instance.obj;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

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

    @Setter
    @Getter
    private boolean settingSpawn = false;

    @Getter
    @Setter
    private double upgradeProgress;

    @Getter
    @Setter
    private Hologram hologram;


    @Getter
    private Set<UUID> builders = new CopyOnWriteArraySet<>();

    @Getter
    private Map<String, RealmProperty> realmProperties = new HashMap<>();


    public RealmToken(UUID owner, String name) {
        this.owner = owner;
        this.name = name;

        // MUST BE ADDED IN THIS ORDER //
        addProperty(new RealmProperty<>("peaceful", false));
        addProperty(new RealmProperty<>("flight", false));
    }


    public World getWorld() {
        return Bukkit.getWorld(owner.toString());
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
