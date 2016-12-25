package net.dungeonrealms.game.player.chat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.handler.FriendHandler;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bson.Document;
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
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore",
            "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "faggot", "queer", "nigger", "nigga", "dike", "dyke", "retard", " " +
                    "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", " faggot", "blowjob", "handjob", "bast", "minecade", "@ss", "mystic " +
                    "runes", "mysticrunes", "f@g", "d1ck", "titanrift", "wynncraft", "titan rift", "jigga",
            "jiggaboo", "hitler", "jews", "titanrift", "fucked",
            "MysticRunes.net", "play.wynncraft.com", "mineca.de", "niger"));

    /**
     * Send a private message over the network.
     *
     * @param player
     * @param recipientName
     * @param finalMessage
     */
    public static void sendPrivateMessage(Player player, String recipientName, String finalMessage) {
        if (Bukkit.getPlayer(recipientName) != null && DatabaseAPI.getInstance().PLAYERS.containsKey(Bukkit.getPlayer(recipientName).getUniqueId())) {
            sendPrivateMessage(player, recipientName, DatabaseAPI.getInstance().PLAYERS.get(Bukkit.getPlayer(recipientName).getUniqueId()), finalMessage);
        } else
            DatabaseAPI.getInstance().retrieveDocumentFromUsername(recipientName, document -> sendPrivateMessage(player, recipientName, document, finalMessage));
    }

    private static void sendPrivateMessage(Player player, String recipientName, Document document, String finalMessage) {
        String testUUID = DatabaseAPI.getInstance().getData(EnumData.UUID, document).toString();

        if (testUUID.equals("")) {
            player.sendMessage(ChatColor.RED + "It seems this user has not played DungeonRealms before.");
            return;
        }

        if (!FriendHandler.getInstance().areFriends(player, UUID.fromString(testUUID), document) && !Rank.isGM(player))
            if (!(Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_RECEIVE_MESSAGE, document)) {
                player.sendMessage(ChatColor.RED + "This user is only accepting messages from friends.");
                return;
            }

        if (!((Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, document)) || (GameAPI.isPlayerHidden(document) && !Rank.isGM(player))) {
            player.sendMessage(ChatColor.RED + "That user is not currently online.");
            return;
        }

        ShardInfo shard = ShardInfo.getByPseudoName((String) DatabaseAPI.getInstance().getData(EnumData.CURRENTSERVER, document));

        player.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO " + GameChat.getFormattedName
                (recipientName) + ChatColor.GRAY + " [" + ChatColor.AQUA + shard.getShardID() + ChatColor.GRAY + "]: " +
                ChatColor.WHITE + finalMessage);

        GameAPI.sendNetworkMessage("PrivateMessage", player.getName(), recipientName, (ChatColor.GRAY.toString() +
                ChatColor.BOLD + "FROM " + GameChat.getFormattedName(player) + ChatColor.GRAY + " [" + ChatColor
                .AQUA + DungeonRealms.getInstance().shardid + ChatColor.GRAY + "]: " + ChatColor.WHITE +
                finalMessage));

        GameAPI.sendNetworkMessage("BroadcastSoundPlayer", recipientName, Sound.ENTITY_CHICKEN_EGG.toString(), "2f", "1.2f");
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

        if (fixedMessage.contains(".com") || fixedMessage.contains(".net") || fixedMessage.contains(".org") || fixedMessage.contains("http://") || fixedMessage.contains("www."))
            if (!Rank.isDev(event.getPlayer())) {
                event.getPlayer().sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in chat!");
                return;
            }

        event.setMessage(fixedMessage);

        // Player only types @i
        if(fixedMessage.startsWith("@i") && !fixedMessage.contains("@i@")) {
            event.setCancelled(true);
        }
        if (fixedMessage.startsWith("@") && !fixedMessage.contains("@i@")) {
            // You cannot private message nobody.
            if (fixedMessage.equals("@")) {
                event.setCancelled(true);
                return;
            }

            //
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

    public String checkForBannedWords(String msg) {
        String result = msg;

        for (String word : bannedWords) result = replaceOperation(result, word);

        StringTokenizer st = new StringTokenizer(result);
        String string = "";

        while (st.hasMoreTokens()) {
            String token = st.nextToken();

            for (String word : bannedWords)
                if (token.contains(word)) {
                    List<Integer> positions = new ArrayList<>();

                    for (int i = 0; i < token.length(); i++)
                        if (Character.isUpperCase(token.charAt(i))) positions.add(i);

                    if (token.toLowerCase().contains(word.toLowerCase())) {
                        token = token.toLowerCase().replaceAll(word.toLowerCase(), " " + toCensor(word.length()));
                    }

                    for (int i : positions)
                        if (i < token.length()) Character.toUpperCase(token.charAt(i));
                }
            string += token + " ";
        }
        return string.trim();
    }

    private String toCensor(int characters) {
        String result = "";
        for (int i = 0; i < characters; i++) result = result.concat("*");
        return result;
    }


    private String replaceOperation(String source, String search) {
        int length = search.length();
        if (length < 2) return source;

        // - Ignore the same character mutliple times in a row
        // - Ignore any non-alphabetic characters
        // - Ignore any digits and whitespaces between characters
        StringBuilder sb = new StringBuilder(4 * length - 3);
        for (int i = 0; i < length - 1; i++) {
            sb.append("([\\W\\d]*").append(Pattern.quote("" + search.charAt(i))).append(")+");
        }
        sb.append("([\\W\\d\\s]*)+");
        sb.append(search.charAt(length - 1));

        String temp = source.replaceAll("(?i)" + sb.toString(), search).trim();
        int wordCount = temp.split("\\s").length;

        String replace = source;

        if (wordCount <= 2) {
            replace = " " + source;
        }

        return replace.replaceAll("(?i)" + sb.toString(), " " + search).trim();
    }


    public static boolean checkGlobalCooldown(Player player) {
        if (Rank.isPMOD(player) || Rank.isSubscriber(player)) return true;
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
