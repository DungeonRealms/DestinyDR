package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListInstance extends BaseCommand {

    public CommandListInstance() {
        super("listinstance", "/<command>", "List all instances.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Rank.isTrialGM((Player) sender)) return true;

        sender.sendMessage(ChatColor.GREEN + "Listing all " + DungeonManager.getInstance().getDungeons().size() + " Active Instances.");
        for (DungeonManager.DungeonObject object : DungeonManager.getInstance().getDungeons()) {
            sender.sendMessage(ChatColor.RED + "Time elapsed: " + object.getTime() + "s Mobs Alive: " + object.aliveMonsters.size() + " / " + object.maxAlive + " Type: " + object.getType().name());

            StringBuilder build = new StringBuilder();
            for (Player pl : Bukkit.getWorld(object.getWorldName()).getPlayers()) {
                build.append(pl.getName()).append(", ");
            }

            String players = build.toString();

            if (players.endsWith(", ")) players = players.substring(0, players.length() - 2);
            sender.sendMessage(ChatColor.GRAY + "Players in world: " + players);
        }
        return false;
    }
}
