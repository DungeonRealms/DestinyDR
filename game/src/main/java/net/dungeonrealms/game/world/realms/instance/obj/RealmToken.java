package net.dungeonrealms.game.world.realms.instance.obj;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import lombok.Data;
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

@Data
public class RealmToken {

    private final UUID owner;

    private final String name;

    private RealmStatus status;

    private Location portalLocation;

    private boolean isLoaded = false, settingSpawn = false;

    private double upgradeProgress;

    private Hologram hologram;

    private Set<UUID> builders = new CopyOnWriteArraySet<>();

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
