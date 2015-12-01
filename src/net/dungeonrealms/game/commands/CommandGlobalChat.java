package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.guild.Guild;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumGuildData;
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
            chatMessage.append(arg + " ");
        }

        Player player = (Player) sender;

        UUID uuid = player.getUniqueId();

        StringBuilder prefix = new StringBuilder();

        prefix.append(ChatColor.AQUA + "<" + ChatColor.BOLD + "G" + ChatColor.GREEN + ">" + ChatColor.RESET + "");

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

       	if(chatMessage.toString().contains("@i@") && player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR){
            String message = chatMessage.toString();
           final Player p = player;
           String aprefix = GameChat.getPreMessage(p);
           String[] split = message.split("@i@");
           String after = "";
           String before = "";
           if (split.length > 0)
               before = split[0];
           if (split.length > 1)
               after = split[1];
           
           final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
           normal.addText(before + "");
           normal.addItem(p.getItemInHand(), ChatColor.GREEN + ChatColor.BOLD.toString() + "SHOW" + ChatColor.WHITE, ChatColor.UNDERLINE);
           normal.addText(after);
           Bukkit.getOnlinePlayers().stream().forEach(newPlayer ->{
         	  if((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, newPlayer.getUniqueId())){
         		  normal.sendToPlayer(newPlayer);
         	  }
           });
           return true;
     	}
        
        Bukkit.broadcastMessage(prefix.toString().trim() + " " + player.getName() + ChatColor.GRAY + ": " + chatMessage.toString());

        return true;
    }
}
