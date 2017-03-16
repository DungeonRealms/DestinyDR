package net.dungeonrealms.game.miscellaneous;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor
public class Graveyard {
    @Getter
    private String name;
    @Getter
    private Location location;
}
