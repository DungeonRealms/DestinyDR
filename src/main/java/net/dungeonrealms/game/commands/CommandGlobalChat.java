package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

        for (String arg : args) {
            chatMessage.append(arg).append(" ");
        }

        String finalChat = Chat.getInstance().checkForBannedWords(chatMessage.toString());

        Player player = (Player) sender;

        UUID uuid = player.getUniqueId();

        StringBuilder prefix = new StringBuilder();

        prefix.append(ChatColor.AQUA + "<" + ChatColor.BOLD + "G" + ChatColor.AQUA + ">" + ChatColor.RESET + "");

        Rank.RankBlob r = Rank.getInstance().getRank(uuid);
        if (r != null && !r.getPrefix().equals("null")) {
            if (r.getName().equalsIgnoreCase("default")) {
                prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + ""));
            } else {
                prefix.append(ChatColor.translateAlternateColorCodes('&', " " + r.getPrefix() + ChatColor.RESET));
            }

        }

        if (!GuildDatabaseAPI.getInstance().isGuildNull(uuid)) {
            String clanTag = GuildDatabaseAPI.getInstance().getClanTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
            prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.WHITE + " [" + clanTag + ChatColor.RESET + "]"));
        }

       	if(finalChat.contains("@i@") && player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR){
            String aprefix = GameChat.getPreMessage(player);
            String[] split = finalChat.split("@i@");
            String after = "";
            String before = "";
            if (split.length > 0)
                before = split[0];
            if (split.length > 1)
                after = split[1];

            final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
            normal.addText(before + "");
            normal.addItem(player.getItemInHand(), ChatColor.GREEN + ChatColor.BOLD.toString() + "SHOW" + ChatColor.WHITE, ChatColor.UNDERLINE);
            normal.addText(after);
            Bukkit.getOnlinePlayers().stream().forEach(newPlayer ->{
                if((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, newPlayer.getUniqueId())){
                    normal.sendToPlayer(newPlayer);
                }
            });
            return true;
        }
        
        Bukkit.broadcastMessage(prefix.toString().trim() + " " + player.getName() + ChatColor.GRAY + ": " + finalChat);

        return true;
    }
}
