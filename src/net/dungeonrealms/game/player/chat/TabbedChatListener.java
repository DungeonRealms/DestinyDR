package net.dungeonrealms.game.player.chat;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import net.dungeonrealms.game.guild.Guild;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.rank.Rank;

public class TabbedChatListener implements Listener {
	
	@EventHandler
    public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent e) {
        final Player player = e.getPlayer();
        StringBuilder chatMessage = new StringBuilder();

        String finalChat = Chat.getInstance().checkForBannedWords(chatMessage.toString());

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

        if (!Guild.getInstance().isGuildNull(uuid)) {
            String clanTag = Guild.getInstance().getClanTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
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
            return;
        }
        
        Bukkit.broadcastMessage(prefix.toString().trim() + " " + player.getName() + ChatColor.GRAY + ": " + finalChat);
    }

}
