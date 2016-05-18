package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.achievements.Achievements;
import net.dungeonrealms.game.mongo.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

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

    private static final Queue<Consumer<? super AsyncPlayerChatEvent>> chatQueue = new ConcurrentLinkedDeque<>();

    /**
     * Adds a new listener to the chat queue. If this is the first in line, the next chat event will be passed to this Consumer
     *
     * @param consumer the Consumer that should listen for the event AND NOT BE NULL
     */
    public static void listenForMessage(Consumer<? super AsyncPlayerChatEvent> consumer) {
        chatQueue.add(consumer);
    }

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore", "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "queer", "nigger", "dike", "dyke", "retard", "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", "faggot", "gay", "f@g", "d1ck", "titanrift", "wynncraft", "titan rift", "titanrift", "fucked"));


    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {

        Consumer<? super AsyncPlayerChatEvent> messageListener = chatQueue.poll();
        if (messageListener != null) {
            messageListener.accept(event);
            return;
        }

        UUID uuid = event.getPlayer().getUniqueId();

        String fixedMessage = checkForBannedWords(event.getMessage());

        if (fixedMessage.startsWith("@") && !fixedMessage.contains("@i@")) {
            String playerName = fixedMessage.replace("@", "").split(" ")[0];
            if(playerName.equalsIgnoreCase("Kayaba")){
            	Achievements.getInstance().giveAchievement(uuid, EnumAchievements.PM_KAYABA);
                event.setCancelled(true);
            	return;
            }
            fixedMessage = fixedMessage.replace("@" + playerName, "");
            String tempFixedMessage = fixedMessage.replace("@" + playerName, "");
            Bukkit.getOnlinePlayers().stream().filter(player -> player.getName().equalsIgnoreCase(playerName)).limit(1).forEach(theTargetPlayer -> {
                theTargetPlayer.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "FROM: " + ChatColor.AQUA + event.getPlayer().getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + tempFixedMessage);
                event.getPlayer().sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO: " + ChatColor.AQUA + theTargetPlayer.getName() + ChatColor.GRAY + ": " + ChatColor.WHITE + tempFixedMessage);
                theTargetPlayer.playSound(theTargetPlayer.getLocation(), Sound.NOTE_PLING, 1f, 63f);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.NOTE_PLING, 1f, 63f);
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
                final String finalFixedMessage = fixedMessage;
                API.getNearbyPlayers(event.getPlayer().getLocation(), 75).stream().forEach(player -> player.sendMessage(GameChat.getPreMessage(event.getPlayer()) + finalFixedMessage));
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
