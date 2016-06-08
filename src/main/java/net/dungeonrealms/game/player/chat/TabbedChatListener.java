package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

import java.util.UUID;

public class TabbedChatListener implements Listener {
	
	@EventHandler
    public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent e) {
        final Player player = e.getPlayer();

        player.closeInventory(); // Closes the chat after it grabs it!

        String finalChat = Chat.getInstance().checkForBannedWords(e.getChatMessage());

        UUID uuid = player.getUniqueId();

        StringBuilder prefix = new StringBuilder();

        prefix.append(GameChat.GLOBAL);

        Rank.RankBlob r = Rank.getInstance().getRank(uuid);
        if (r != null && !GameChat.getRankPrefix(r.getName()).equals("null")) {
            if (r.getName().equalsIgnoreCase("default")) {
                prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.GRAY + ""));
            } else {
                prefix.append(ChatColor.translateAlternateColorCodes('&', " " + GameChat.getRankPrefix(r.getName()) + ChatColor.RESET));
            }

        }

        if (!GuildDatabase.getAPI().isGuildNull(uuid)) {
            String clanTag = GuildDatabase.getAPI().getTagOf(DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId()).toString());
            prefix.append(ChatColor.translateAlternateColorCodes('&', ChatColor.WHITE + " [" + clanTag + ChatColor.RESET + "] "));
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

        prefix.append(GameChat.getName(player, r.getName()));
        
        Bukkit.broadcastMessage(prefix.toString() + finalChat);
    }

}
