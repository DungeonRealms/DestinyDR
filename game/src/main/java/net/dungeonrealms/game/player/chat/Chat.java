package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
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

    private static final Map<Player, Consumer<? super AsyncPlayerChatEvent>> chatListeners = new ConcurrentHashMap<>();
    private static final Map<Player, Consumer<? super Player>> orElseListeners = new ConcurrentHashMap<>();

    /**
     * Listens for a player message.
     * If <i>consumer</i> is null, the player is removed from memory (used in Quit event)
     *
     * @param consumer the Consumer that should listen for the event AND NOT BE NULL
     * @param orElse   the consumer that get called when another listener listens for a message (this one gets removed) or when the player quits
     */
    public static void listenForMessage(Player player, Consumer<? super AsyncPlayerChatEvent> consumer, Consumer<? super Player> orElse) {
        if (chatListeners.remove(player) != null) {
            Consumer<? super Player> old = orElseListeners.remove(player);
            if (old != null) {
                old.accept(player);
            }
        }
        if (consumer != null) {
            chatListeners.put(player, consumer);
            if (orElse != null) orElseListeners.put(player, orElse);
        }
    }

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore", "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "faggot", "queer", "nigger", "nigga", "dike", "dyke", "retard", " motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", " faggot", "blowjob", "handjob", "bast", "gay", "minecade", "unowild", "f@g", "d1ck", "titanrift", "wynncraft", "titan rift", "kys", "jigga", "jiggaboo", "hitler", "jews", "titanrift", "fucked"));

    public static void sendPrivateMessage(Player player, String playerName, String finalMessage) {
        GameAPI.submitAsyncWithAsyncCallback(() -> {
            String testUUID = DatabaseAPI.getInstance().getUUIDFromName(playerName);
            if (testUUID.equals("")) {
                player.sendMessage(ChatColor.RED + "It seems this user has not played DungeonRealms before.");
                return "";
            }
            UUID uuid = UUID.fromString(testUUID);
            if (!FriendHandler.getInstance().areFriends(player, uuid) && !Rank.getInstance().isGM(Bukkit.getOfflinePlayer(uuid))) {
                if (!(Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_RECEIVE_MESSAGE, uuid)) {
                    player.sendMessage(ChatColor.RED + "This user is only accepting messages from friends.");
                    return "";
                }
            }
            if (!((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid))) {
                player.sendMessage(ChatColor.RED + "That user is not currently online.");
                return "";
            }
            try {
                return DatabaseAPI.getInstance().getFormattedShardName(uuid);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }, result -> {
            String receivingShard = null;
            try {
                receivingShard = result.get();
                if (receivingShard.equals("")) {
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            String toPlayerRank = Rank.getInstance().getRank(UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)));
            String fromPlayerRank = Rank.getInstance().getRank(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO " + GameChat.getRankPrefix
                    (toPlayerRank) + GameChat.getName(playerName, toPlayerRank, true) + ChatColor.GRAY + " [" +
                    ChatColor.AQUA + receivingShard + ChatColor.GRAY + "]: " + ChatColor.WHITE + finalMessage);

            GameAPI.sendNetworkMessage("PrivateMessage", player.getName(), playerName, (ChatColor.GRAY.toString() +
                    ChatColor.BOLD + "FROM " + GameChat.getRankPrefix(fromPlayerRank) + GameChat.getName(player, fromPlayerRank, true) +
                    ChatColor.GRAY + " [" + ChatColor.AQUA + receivingShard + ChatColor.GRAY + "]: " + ChatColor
                    .WHITE + finalMessage));
            GameAPI.sendNetworkMessage("BroadcastSoundPlayer", playerName, Sound.ENTITY_CHICKEN_EGG.toString(), "2f", "1.2f");
        });
    }

    /**
     * Listens for chat event listener
     *
     * @param event Chat event
     * @since 1.0
     */
    public void doMessageChatListener(AsyncPlayerChatEvent event) {
        Consumer<? super AsyncPlayerChatEvent> messageListener = chatListeners.remove(event.getPlayer());
        if (messageListener != null) {
            messageListener.accept(event);
            orElseListeners.remove(event.getPlayer());
            event.setCancelled(true);
            return;
        }
    }

    /**
     * Monitor the players primary language also check for bad words.
     *
     * @param event Chat event
     * @since 1.0
     */
    public void doChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        String fixedMessage = checkForBannedWords(event.getMessage());
        event.setMessage(fixedMessage);

        if (fixedMessage.startsWith("@") && !fixedMessage.contains("@i@")) {
            String playerName = fixedMessage.replace("@", "").split(" ")[0];
            if (DungeonRealms.getInstance().getDevelopers().contains(playerName)) {
                Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.PM_DEV);
            }
            fixedMessage = fixedMessage.replace("@" + playerName, "");
            sendPrivateMessage(event.getPlayer(), playerName, fixedMessage);
            event.setCancelled(true);
            return;
        }

        boolean gChat = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_GLOBAL_CHAT, uuid);

        if (gChat) {
            if (!checkGlobalCooldown(event.getPlayer())) {
                event.setMessage(null);
                event.setCancelled(true);
                return;
            }
            if (fixedMessage.contains("@i@") && event.getPlayer().getEquipment().getItemInMainHand() != null && event.getPlayer().getEquipment().getItemInMainHand().getType() != Material.AIR) {
                final Player p = event.getPlayer();
                String aprefix = GameChat.getPreMessage(p);
                String[] split = fixedMessage.split("@i@");
                String after = "";
                String before = "";
                if (split.length > 0)
                    before = split[0];
                if (split.length > 1)
                    after = split[1];


                ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();

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
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            final String finalFixedMessage = fixedMessage;
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(GameChat.getPreMessage(event.getPlayer(), true, GameChat.getGlobalType(finalFixedMessage)) + finalFixedMessage));
        } else {
            if (fixedMessage.contains("@i@") && event.getPlayer().getEquipment().getItemInMainHand() != null && event.getPlayer().getEquipment().getItemInMainHand().getType() != Material.AIR) {
                final Player p = event.getPlayer();
                String aprefix = GameChat.getPreMessage(p);
                String[] split = fixedMessage.split("@i@");
                String after = "";
                String before = "";
                if (split.length > 0)
                    before = split[0];
                if (split.length > 1)
                    after = split[1];

                ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();

                List<String> hoveredChat = new ArrayList<>();
                ItemMeta meta = stack.getItemMeta();
                hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : stack.getType().name()));
                if (meta.hasLore())
                    hoveredChat.addAll(meta.getLore());
                final JSONMessage normal = new JSONMessage(ChatColor.WHITE + aprefix, ChatColor.WHITE);
                normal.addText(before + "");
                normal.addHoverText(hoveredChat, ChatColor.BOLD + ChatColor.UNDERLINE.toString() + "SHOW");
                normal.addText(after);

                GameAPI.getNearbyPlayers(event.getPlayer().getLocation(), 75).forEach(normal::sendToPlayer);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles local player chat
     *
     * @param event Chat event
     */

    public void doLocalChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        final String finalFixedMessage = event.getMessage();


        // HANDLE LOCAL CHAT
        if (GameAPI.getNearbyPlayers(event.getPlayer().getLocation(), 75).size() >= 2) {
            GameAPI.getNearbyPlayers(event.getPlayer().getLocation(), 75).forEach(player -> player.sendMessage(GameChat.getPreMessage(event.getPlayer()) + finalFixedMessage));
        } else {
            event.getPlayer().sendMessage(GameChat.getPreMessage(event.getPlayer()) + finalFixedMessage);
            event.getPlayer().sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "No one heard you...");
        }
        event.setCancelled(true);
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

    public static boolean checkGlobalCooldown(Player player) {
        if (Rank.isPMOD(player)) return true;
        if (player.hasMetadata("lastGlobalChat") && (System.currentTimeMillis() - player.getMetadata
                ("lastGlobalChat").get(0).asLong()) < 10000) {
            int timeRemaining = ((int) (10000 - (System.currentTimeMillis() - player.getMetadata("lastGlobalChat").get(0)
                    .asLong()))) / 1000;
            player.sendMessage(ChatColor.RED + "You must wait " + ChatColor.UNDERLINE + timeRemaining + " seconds" +
                    ChatColor.RED + " before sending another global chat message.");
            return false;
        }
        player.setMetadata("lastGlobalChat", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis()));
        return true;
    }
}
