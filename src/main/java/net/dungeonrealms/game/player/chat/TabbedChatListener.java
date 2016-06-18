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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
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

            ItemStack stack = player.getItemInHand();

            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta = stack.getItemMeta();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());
            final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
            normal.addText(before + "");
            normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
            normal.addText(after);

            Bukkit.getOnlinePlayers().stream().forEach(normal::sendToPlayer);
            return;
        }

        Bukkit.getOnlinePlayers().stream().forEach(newPlayer -> newPlayer.sendMessage(prefix.toString() + finalChat));
    }

}
