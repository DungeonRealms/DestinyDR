package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.title.TitleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Created by Alan on 7/31/2016.
 */
public class TutorialMechanics implements GenericMechanic, Listener {

    private static TutorialMechanics instance = null;

    public static TutorialMechanics getInstance() {
        if (instance == null) {
            instance = new TutorialMechanics();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
    }

    public void doLogin(Player p) {
        TitleAPI.sendTitle(p, 10, 5 * 20, 1, ChatColor.GRAY.toString() + ChatColor.BOLD + "Welcome to " + ChatColor
                .GOLD.toString() + ChatColor.BOLD + "Dungeon Realms", ChatColor.GRAY + "A world of myth and adventure.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD + "                    Welcome to " + ChatColor.UNDERLINE + "Dungeon Realms!");


            p.sendMessage(ChatColor.GRAY + "Dive into the mystical world of Andalucia and discover all of its wonders.");
            p.sendMessage("");
            p.sendMessage(ChatColor.GRAY + "You'll get a crash course on game mechanics and " + ChatColor.UNDERLINE + "get free loot" + ChatColor.GRAY + " just for completing it!");
            p.sendMessage("");
        }, 80L);
    }

    @Override
    public void stopInvocation() {

    }
}
