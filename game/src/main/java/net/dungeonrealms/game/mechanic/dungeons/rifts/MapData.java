package net.dungeonrealms.game.mechanic.dungeons.rifts;

import lombok.Getter;
import org.bukkit.Location;

@Getter
public enum MapData {
        VARENGLADE("varenglade", new Location(null,-363,59,16),new Location(null,-363,59,-12)),
        INFERNAL("infernalAbyss", new Location(null,-55,157,670),new Location(null,-55,157,647));

        private String worldName;
        private Location spawnLocation;
        private Location bossLocation;
        MapData(String worldName, Location spawnLocation, Location bossLocation) {
            this.worldName = worldName;
            this.spawnLocation = spawnLocation;
            this.bossLocation = bossLocation;
        }
}