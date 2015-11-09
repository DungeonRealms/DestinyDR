package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.guild.Guild;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumGuildData;
import net.dungeonrealms.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Nick on 10/31/2015.
 */
public class CommandGlobalChat extends BasicCommand {

    public CommandGlobalChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) return false;

        if (args.length <= 0) {
            sender.sendMessage(ChatColor.RED + "/gl <message>");
            return true;
        }

        StringBuilder chatMessage = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            chatMessage.append(args[i] + " ");
        }

        Player player = (Player) sender;

        UUID uuid = player.getUniqueId();

        StringBuilder prefix = new StringBuilder();

        prefix.append(ChatColor.GREEN + "<" + ChatColor.BOLD + "G" + ChatColor.GREEN + ">" + ChatColor.RESET + "");

        Rank.RankBlob r = Rank.getInstance().getRank(uuid);
        if (r != null && !r.getPrefix().equals("null")) {
            if (r.getName().equalsIgnoreCase("default")) {
                prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + ""));
            } else {
                prefix.append(ChatColor.translateAlternateColorCodes('&', " " + r.getPrefix() + ChatColor.RESET));
            }

        }

        if (!Guild.getInstance().isGuildNull(uuid)) {
            String clanTag = (String) DatabaseAPI.getInstance().getData(EnumGuildData.CLAN_TAG, (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid));
            prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.WHITE + " [" + clanTag + ChatColor.RESET + "]"));
        }

        Bukkit.broadcastMessage(prefix.toString().trim() + " " + player.getName() + ChatColor.GRAY + ": " + chatMessage.toString());

        return true;
    }
}
