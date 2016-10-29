package net.dungeonrealms.old.game.command.guild;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.old.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.old.game.guild.GuildMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */


public class CommandGInfo extends BaseCommand {

    public CommandGInfo(String command, String usage, String description) {
        super(command, usage, description);
    }

    private List<UUID> cooldown = new CopyOnWriteArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        UUID target = player.getUniqueId();


        if (cooldown.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You must wait 30 seconds to use " + ChatColor.BOLD + "GUILD INFO.");
            return true;
        }


        if (args.length > 0) {
            if (DatabaseAPI.getInstance().getUUIDFromName(args[0]).equals("")) {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + args[0] + ChatColor.RED + " does not exist in our database.");
                return true;
            }

            UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(args[0]));

            if (GuildDatabaseAPI.get().isGuildNull(uuid)) {
                player.sendMessage(ChatColor.RED + args[0] + " is not in a guild.");
                return true;
            }

            target = uuid;
        } else {
            if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "GUILD INFO.");
                return true;
            }
        }

        cooldown.add(player.getUniqueId());

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> cooldown.remove(player.getUniqueId()), 30 * 20L);

        String guildName = GuildDatabaseAPI.get().getGuildOf(target);
        GuildMechanics.getInstance().showGuildInfo(player, guildName, target.equals(player.getUniqueId()));
        return false;
    }
}
