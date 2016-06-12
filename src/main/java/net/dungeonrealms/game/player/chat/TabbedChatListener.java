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

        prefix.append(GameChat.getPreMessage(player, true, GameChat.getGlobalType(finalChat)));

       	if (finalChat.contains("@i@") && player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType() != Material.AIR) {
            String aprefix = prefix.toString();
            String[] split = finalChat.split("@i@");
            String after = "";
            String before = "";
            if (split.length > 0)
                before = split[0];
            if (split.length > 1)
                after = split[1];

            final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
            normal.addText(before + "");
            normal.addItem(player.getEquipment().getItemInMainHand(), ChatColor.WHITE + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + "SHOW" + ChatColor.WHITE);
            normal.addText(after);
            Bukkit.getOnlinePlayers().stream().forEach(normal::sendToPlayer);
            return;
        }

        Bukkit.getOnlinePlayers().stream().forEach(newPlayer -> newPlayer.sendMessage(prefix.toString() + finalChat));
    }

}
