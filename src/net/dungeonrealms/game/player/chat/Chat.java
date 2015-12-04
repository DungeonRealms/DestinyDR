package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.API;
import net.dungeonrealms.game.events.PlayerMessagePlayerEvent;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore", "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "queer", "nigger", "dike", "dyke", "retard", "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", "faggot", "gay", "f@g", "d1ck", "nig", "titanrift", "socialconquer", "wynncraft", "titan rift", "ass"));


    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {

        UUID uuid = event.getPlayer().getUniqueId();

        String fixedMessage = checkForBannedWords(event.getMessage());

        if (fixedMessage.startsWith("@") && !fixedMessage.contains("@i@")) {
            String playerName = fixedMessage.replace("@", "").split(" ")[0];
            Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(playerName)).forEach(player1 -> {
                event.getPlayer().sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO " + GameChat.getPreMessage(Bukkit.getPlayer(playerName)) + fixedMessage.toLowerCase().replace("@" + playerName.toLowerCase(), ""));
                player1.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "FROM " + GameChat.getPreMessage(event.getPlayer()) + fixedMessage.toLowerCase().replace("@" + event.getPlayer().getName().toLowerCase(), ""));
                Bukkit.getPluginManager().callEvent(new PlayerMessagePlayerEvent(event.getPlayer(), Bukkit.getPlayer(playerName), fixedMessage));
            });
            event.setCancelled(true);
            return;
        }

        boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, uuid);

        if (gChat) {
            if (fixedMessage.contains("@i@") && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR) {
                final Player p = event.getPlayer();
                String aprefix = GameChat.getPreMessage(p);
                String[] split = fixedMessage.split("@i@");
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
                event.setFormat(GameChat.getPreMessage(event.getPlayer()) + fixedMessage);
            }
        } else {
            if (API.getNearbyPlayers(event.getPlayer().getLocation(), 75).size() >= 2) {
                if (fixedMessage.contains("@i@") && event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType() != Material.AIR) {
                    final Player p = event.getPlayer();
                    String aprefix = GameChat.getPreMessage(p);
                    String[] split = fixedMessage.split("@i@");
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
                    API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(normal::sendToPlayer);
                    event.setCancelled(true);
                    return;
                }
                event.setCancelled(true);
                API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player -> player.sendMessage(GameChat.getPreMessage(event.getPlayer()) + fixedMessage));
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.GRAY + "No one heard you...");
            }

        }
    }

    public String checkForBannedWords(String message) {
        String returnMessage = "";
        if (!message.contains(" ")) {
            message += " ";
        }
        for (String string : message.split(" ")) {
            for (String word : bannedWords) {
                if (string.toLowerCase().contains(word.toLowerCase())) {
                    int wordLength = word.length();
                    String replacementCharacter = "";
                    while (wordLength > 0) {
                        replacementCharacter += "*";
                        wordLength--;
                    }
                    int censorStart = string.toLowerCase().indexOf(word);
                    int censorEnd = censorStart + word.length();
                    String badWord = string.substring(censorStart, censorEnd);
                    string = string.replaceAll(badWord, replacementCharacter);
                }
            }
            returnMessage += string + " ";
        }

        if (returnMessage.endsWith(" ")) {
            returnMessage = returnMessage.substring(0, returnMessage.lastIndexOf(" "));
        }

        return returnMessage;
    }

}
