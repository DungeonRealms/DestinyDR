package net.dungeonrealms.old.game.world.entity.type.monster.boss;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.world.entity.type.monster.type.EnumDungeonBoss;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Created by Chase on Oct 18, 2015
 */

public interface DungeonBoss extends Boss {

    EnumDungeonBoss getEnumBoss();

    default void say(Entity ent, String msg) {
        for (Player p : GameAPI.getNearbyPlayers(ent.getLocation(), 50)) {
            p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getEnumBoss().name() + ChatColor.WHITE + "] "
                    + ChatColor.GREEN + msg);
        }
    }

    default void say(Entity ent, Location location, String msg) {
        for (Player p : GameAPI.getNearbyPlayers(location, 50)) {
            p.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD + getEnumBoss().name() + ChatColor.WHITE + "] "
                    + ChatColor.GREEN + msg);
        }
    }
}
