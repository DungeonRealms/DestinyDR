package net.dungeonrealms.game.command.guild;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.command.friend.CooldownCommand;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */


public class CommandGInfo extends BaseCommand implements CooldownCommand {

    public CommandGInfo(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        UUID target = player.getUniqueId();


        if (checkCooldown(player)) return false;
//        if (cooldown.contains(player.getUniqueId())) {
//            player.sendMessage(ChatColor.RED + "You must wait 30 seconds to use " + ChatColor.BOLD + "GUILD INFO.");
//            return true;
//        }

        Player checking = player;

        if (args.length > 0) {
            checking = Bukkit.getPlayer(args[0]);
        }

        if (checking == null) {
            player.sendMessage(ChatColor.RED + "This player must be online on the same shard as you!");
            return false;
        }

        GuildWrapper guildWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(checking.getUniqueId());
        if (guildWrapper == null) {
            if (!player.equals(checking))
                player.sendMessage(ChatColor.RED + "This player is not in a guild!");
            else
                player.sendMessage(ChatColor.RED + "You must be in a " + ChatColor.BOLD + "GUILD" + ChatColor.RED + " to use " + ChatColor.BOLD + "GUILD INFO.");
            return false;
        }

//        cooldown.add(player.getUniqueId());

//        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> cooldown.remove(player.getUniqueId()), 30 * 20L);

        GuildMechanics.getInstance().showGuildInfo(player, guildWrapper, true);
        return false;
    }

    @Override
    public long getCooldown() {
        return TimeUnit.SECONDS.toMillis(30);
    }

    @Override
    public String getName() {
        return "ginfo";
    }
}
