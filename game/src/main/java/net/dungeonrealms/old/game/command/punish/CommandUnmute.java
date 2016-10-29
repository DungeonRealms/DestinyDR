package net.dungeonrealms.old.game.command.punish;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class CommandUnmute extends BaseCommand {

    public CommandUnmute(String command, String usage, String description, String... aliases) {
        super(command, usage, description, Arrays.asList(aliases));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!Rank.isPMOD(player)) return false;
        }

        if (args.length == 0) {
            sender.sendMessage(usage);
            return true;
        }

        String p_name = args[0];
        Player p = Bukkit.getPlayer(p_name);

        if (p == null) {
            sender.sendMessage(ChatColor.RED + p_name + " is not online.");
            return true;
        }

        UUID p_uuid = p.getUniqueId();

        if (!PunishAPI.getInstance().isMuted(p_uuid)) {
            sender.sendMessage(ChatColor.RED + p_name + " is not muted.");
            return true;
        }

        PunishAPI.getInstance().unmute(p_uuid);
        sender.sendMessage(ChatColor.RED.toString() + "You have unmuted " + ChatColor.BOLD + p_name);
        return false;
    }
}
