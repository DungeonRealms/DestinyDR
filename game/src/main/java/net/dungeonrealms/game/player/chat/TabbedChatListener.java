package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
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
import java.util.stream.Collectors;

public class TabbedChatListener implements Listener {

    @EventHandler
    public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent e) {
        final Player player = e.getPlayer();

        if (PunishAPI.isMuted(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage(PunishAPI.getMutedMessage(e.getPlayer().getUniqueId()));
            return;
        }

        if (!Chat.checkGlobalCooldown(player) || e.getChatMessage().length() > 128) {
            return;
        }

        int index = e.getChatMessage().indexOf("/");
        if (index > 0 && index < 3 && Rank.isTrialGM(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "Woah there! You sure you want to send that in global?");
            return;
        }

        player.closeInventory(); // Closes the chat after it grabs it!

        if(Chat.containsIllegal(e.getChatMessage())){
            player.sendMessage(ChatColor.RED + "Message contains illegal characters.");
            return;
        }
        String finalChat = Chat.getInstance().checkForBannedWords(e.getChatMessage());

        StringBuilder prefix = new StringBuilder();

        String messageType = GameChat.getGlobalType(finalChat);
        prefix.append(GameChat.getPreMessage(player, true, messageType));
        boolean tradeChat = messageType.equals("trade");
        if (tradeChat && !(Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_TRADE_CHAT, player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot talk in trade chat while its toggled off!");
            return;
        }
        List<Player> recipients = Chat.getRecipients(tradeChat);

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

            recipients.forEach(normal::sendToPlayer);
            return;
        }

        recipients.forEach(newPlayer -> newPlayer.sendMessage(prefix.toString() + finalChat));
    }

}
