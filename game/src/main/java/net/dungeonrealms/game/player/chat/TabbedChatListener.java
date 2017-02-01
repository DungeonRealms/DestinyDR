package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.game.player.json.JSONMessage;

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

public class TabbedChatListener implements Listener {

    @EventHandler
    public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent e) {
        final Player player = e.getPlayer();

        if (PunishAPI.isMuted(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage(PunishAPI.getMutedMessage(e.getPlayer().getUniqueId()));
            return;
        }

        if (!Chat.checkGlobalCooldown(player)) {
            return;
        }
        
        int index = e.getChatMessage().indexOf("/");
        if(index > 0 && index < 3 && Rank.isTrialGM(e.getPlayer())){
        	e.getPlayer().sendMessage(ChatColor.RED + "Woah there! You sure you want to send that in global?");
        	return;
        }

        player.closeInventory(); // Closes the chat after it grabs it!

        String finalChat = Chat.getInstance().checkForBannedWords(e.getChatMessage());

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

            ItemStack stack = player.getEquipment().getItemInMainHand();

            List<String> hoveredChat = new ArrayList<>();
            ItemMeta meta = stack.getItemMeta();
            hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
            if (meta.hasLore())
                hoveredChat.addAll(meta.getLore());
            final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
            normal.addText(before + "");
            normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
            normal.addText(after);

            Bukkit.getOnlinePlayers().forEach(normal::sendToPlayer);
            return;
        }

        Bukkit.getOnlinePlayers().forEach(newPlayer -> newPlayer.sendMessage(prefix.toString() + finalChat));
    }

}
