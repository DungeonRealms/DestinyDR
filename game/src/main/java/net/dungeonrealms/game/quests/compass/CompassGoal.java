package net.dungeonrealms.game.quests.compass;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;

/**
 * Created by Rar349 on 8/8/2017.
 */
@Getter
public class CompassGoal {

    private Location toDirect;
    private ChatColor color;
    public CompassGoal(Location toDirect, ChatColor color) {
        this.toDirect = toDirect;
        this.color = color;
    }
}
