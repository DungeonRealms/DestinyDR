package net.dungeonrealms.game.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A registry of non-instanced game worlds.
 * Created by Kneesnap on 11/14/2017.
 */
@Getter @AllArgsConstructor
public enum WorldType {
    ANDALUCIA("world"),
    ELORA("Elora");

    private final String worldName;

    /**
     * Get the bukkit world representing this DR-world.
     * @return bukkitWorld
     */
    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    /**
     * Get a DR world by its bukkit counterpart.
     * @param world
     * @return worldType
     */
    public static WorldType getWorld(World world) {
        return world != null ? Arrays.stream(values()).filter(wt -> wt.getWorldName().equalsIgnoreCase(world.getName())).findAny().orElse(null) : null;
    }


    /**
     * Get a DR world by its name.
     * @param name
     * @return worldType
     */
    public static WorldType getWorld(String name) {
        return Arrays.stream(values()).filter(wt -> wt.name().equalsIgnoreCase(name) || wt.getWorldName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    /**
     * Get a list of all game worlds.
     * @return worlds
     */
    public static List<World> getWorlds() {
        return Arrays.stream(values()).map(WorldType::getWorld).collect(Collectors.toList());
    }

    /**
     * Load all custom worlds.
     * Should be called on startup.
     */
    public static void setupWorlds() {
        for (WorldType type : values())
            if (type.getWorld() == null) // If the world has not been loaded yet.
                Bukkit.createWorld(new WorldCreator(type.getWorldName()).generateStructures(false));
    }
}
