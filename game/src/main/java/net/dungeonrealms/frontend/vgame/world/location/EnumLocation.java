package net.dungeonrealms.frontend.vgame.world.location;

import lombok.Getter;

import net.dungeonrealms.frontend.Game;
import org.bukkit.Location;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumLocation {

    CYRENNICA(0, new Location(Game.getGame().getGameWorld().getBukkitWorld(), -378, 85, 357), "Cyrennica"),
    HARRISONS_FIELD(1, new Location(Game.getGame().getGameWorld().getBukkitWorld(), -594, 59, 687, 92.0F, 1F), "Harrissons Field"),
    DARK_OAK_TAVERN(2, new Location(Game.getGame().getGameWorld().getBukkitWorld(), 280, 59, 1132, 2.0F, 1F), "Dark Oak"),
    DEAPEAKS_MOUNTAIN_CAMP(3, new Location(Game.getGame().getGameWorld().getBukkitWorld(), -1173, 106, 1030, -88.0F, 1F), "Deadpeaks"),
    TROLLSBANE_TAVERN(4, new Location(Game.getGame().getGameWorld().getBukkitWorld(), 962, 95, 1069, -153.0F, 1F), "Trollsbane"),
    TRIPOLI(5, new Location(Game.getGame().getGameWorld().getBukkitWorld(), -1320, 91, 370, 153F, 1F), "Tripoli"),
    GLOOMY_HALLOWS(6, new Location(Game.getGame().getGameWorld().getBukkitWorld(), -590, 44, 0, 144F, 1F), "Gloomy Hallows"),
    CRESTGAURD_KEEP(7, new Location(Game.getGame().getGameWorld().getBukkitWorld(), -1428, 116, -489, 95F, 1F), "Crestgaurd");

    @Getter
    private int id;

    @Getter
    private Location location;

    @Getter
    private String name;

    EnumLocation(int id, Location location, String name) {
        this.id = id;
        this.location = location;
        this.name = name;
    }

    public static EnumLocation getById(int id) {
        for (EnumLocation location : values()) {
            if (location.getId() == id) {
                return location;
            }
        }
        return null;
    }
}
