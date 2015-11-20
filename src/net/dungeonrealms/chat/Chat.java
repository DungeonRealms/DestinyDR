package net.dungeonrealms.chat;

import net.dungeonrealms.API;
import net.dungeonrealms.events.PlayerMessagePlayerEvent;
import net.dungeonrealms.json.JSONMessage;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Created by Nick on 9/26/2015.
 */
public class Chat {

    static Chat instance = null;

    public static Chat getInstance() {
        if (instance == null) {
            instance = new Chat();
        }
        return instance;
    }


    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        if (event.getMessage().startsWith("@") && !event.getMessage().contains("@i@")) {
            String playerName = event.getMessage().replace("@", "").split(" ")[0];
            Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(playerName)).forEach(player1 -> {
                event.getPlayer().sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO " + GameChat.getPreMessage(Bukkit.getPlayer(playerName)) + event.getMessage().toLowerCase().replace("@" + playerName, ""));
                player1.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "FROM " + GameChat.getPreMessage(event.getPlayer()) + event.getMessage().toLowerCase().replace("@" + event.getPlayer(), ""));
                Bukkit.getPluginManager().callEvent(new PlayerMessagePlayerEvent(event.getPlayer(), Bukkit.getPlayer(playerName), event.getMessage()));
            });
            event.setCancelled(true);
            return;
        }

        boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, uuid);

        if (gChat) {
            if (event.getMessage().contains("@i@") && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR) {
                String message = event.getMessage();
                final Player p = event.getPlayer();
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
                Bukkit.getOnlinePlayers().stream().forEach(player -> {
                    if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId())) {
                        normal.sendToPlayer(player);
                    }
                });
                event.setCancelled(true);
                return;
            }


            if ((Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, event.getPlayer().getUniqueId())) {
                event.setFormat(GameChat.getPreMessage(event.getPlayer()) + event.getMessage());
            }
        } else {
            if (API.getNearbyPlayers(event.getPlayer().getLocation(), 75).size() >= 2) {
                if (event.getMessage().contains("@i@") && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR) {
                    String message = event.getMessage();
                    final Player p = event.getPlayer();
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
                    normal.addItem(p.getItemInHand(), ChatColor.AQUA + ChatColor.BOLD.toString() + "SHOW" + ChatColor.WHITE, ChatColor.UNDERLINE);
                    normal.addText(after);
                    API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player -> {
                        if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, player.getUniqueId())) {
                            normal.sendToPlayer(player);
                        }
                    });
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player -> player.sendMessage(GameChat.getPreMessage(player) + event.getMessage()));
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "No one heard you...");
            }

        }
    }

}
