package net.dungeonrealms.game.player.chat;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.punishment.PunishAPI;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.handler.FriendHandler;
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
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Nick on 9/26/2015.
 */
public class Chat {

	@Getter private static Chat instance = new Chat();

    private static final Map<Player, Consumer<? super AsyncPlayerChatEvent>> chatListeners = new ConcurrentHashMap<>();
    private static final Map<Player, Consumer<? super Player>> orElseListeners = new ConcurrentHashMap<>();

    /**
     * Is this player currently being listened on by a chat prompt?
     */
    public static boolean listened(Player player) {
        return chatListeners.containsKey(player) || orElseListeners.containsKey(player);
    }
    
    public static void listenForMessage(Player player, Consumer<? super AsyncPlayerChatEvent> consumer) {
    	listenForMessage(player, consumer, (Runnable)null);
    }
    
    public static void listenForMessage(Player player, Consumer<? super AsyncPlayerChatEvent> consumer, Runnable fail) {
    	listenForMessage(player, consumer, p -> {
    		if (fail != null)
    			fail.run();
    	});
    }
    
    /**
     * Listens for a player message.
     * If <i>consumer</i> is null, the player is removed from memory (used in Quit event)
     *
     * @param consumer the Consumer that should listen for the event AND NOT BE NULL
     * @param orElse   the consumer that get called when another listener listens for a message (this one gets removed) or when the player quits
     */
    public static void listenForMessage(Player player, Consumer<? super AsyncPlayerChatEvent> consumer, Consumer<? super Player> orElse) {

        if (player.getOpenInventory() != null && !player.getOpenInventory().equals(player.getInventory()) && !player.getOpenInventory().getTitle().equals("container.crafting")) {
//            player.closeInventory();
        }

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

    public static void promptPlayerConfirmation(Player player, Runnable confirm, Runnable cancel) {
        listenForMessage(player, event -> {
            String message = event.getMessage();
            if (message.equalsIgnoreCase("confirm") || message.equalsIgnoreCase("yes") || message.equalsIgnoreCase("y") || message.equalsIgnoreCase("accept")) {
                confirm.run();
            } else if (message.equalsIgnoreCase("no") || message.equalsIgnoreCase("n") || message.equalsIgnoreCase("cancel") || message.equalsIgnoreCase("deny")) {
                cancel.run();
            } else {
                player.sendMessage(ChatColor.RED + "Unknown response.");
                cancel.run();
            }
        }, p -> cancel.run());
    }

    public static void listenForNumber(Player player, Consumer<Integer> successCallback, Runnable failCallback) {
        listenForNumber(player, Integer.MIN_VALUE, Integer.MAX_VALUE, successCallback, failCallback);
    }

    /**
     * Listens for a number.
     * Cancel callback will be run if "cancel", a non-number, a number larger than the max, or a number smaller than the minimum is entered.
     *
     */
    public static void listenForNumber(Player player, int min, int max, Consumer<Integer> successCallback, Runnable failCallback) {
        Chat.listenForMessage(player, (evt) -> {
            evt.setCancelled(true);
        	int num;

            if (evt.getMessage().equalsIgnoreCase("cancel") || evt.getMessage().equalsIgnoreCase("c")) {
                failCallback.run();
                return;
            }

            try {
                num = Integer.parseInt(evt.getMessage());
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "That is not a valid number!");
                failCallback.run();
                return;
            }

            if (num > max || num < min) {
                player.sendMessage(ChatColor.RED + "Invalid Number. Range = [" + min + "," + max + "]");
                failCallback.run();
                return;
            }
            successCallback.accept(num);
        }, (p) -> {
            if (failCallback != null)
                failCallback.run();
        });
    }

    public static List<String> bannedWords = new ArrayList<>(Arrays.asList("shit", "fuck", "cunt", "bitch", "whore",
            "slut", "wank", "asshole", "cock",
            "dick", "clit", "homo", "fag", "faggot", "queer", "nigger", "n1gger", "n1gg3r", "nigga", "dike", "dyke", "retard", " " +
                    "motherfucker", "vagina", "boob", "pussy", "rape", "gay", "penis",
            "cunt", "titty", "anus", " faggot", "blowjob", "handjob", "bast", "minecade", "@ss", "mystic " +
                    "runes", "mysticrunes", "f@g", "d1ck", "titanrift", "wynncraft", "titan rift", "jigga", "atherialrunes", "atherial",
            "autism", "autismrealms", "jiggaboo", "hitler", "jews", "titanrift", "fucked", "mckillzone",
            "MysticRunes.net", "play.wynncraft.com", "mineca.de", "play.atherialrunes.net", "autismrealms.us", "play.mckillzone.net", "niger", "kys"));

    /**
     * Send a private message over the network.
     *
     * @param player
     * @param recipientName
     * @param finalMessage
     */

    public static void sendPrivateMessage(Player player, String recipientName, String finalMessage) {

        PlayerWrapper sendingWrapper = PlayerWrapper.getPlayerWrapper(player);
        if(sendingWrapper == null) return;

        SQLDatabaseAPI.getInstance().getUUIDFromName(recipientName, false, (uuid) -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "It seems this user has not played DungeonRealms before.");
                return;
            }
            PlayerWrapper.getPlayerWrapper(uuid, false, true, wrapper -> {
                if (!FriendHandler.getInstance().areFriends(player, uuid) && !Rank.isTrialGM(player)) {
                    if (!wrapper.getToggles().getState(Toggles.ENABLE_PMS)) {
                        player.sendMessage(ChatColor.RED + "This user is only accepting messages from friends.");
                        return;
                    }
                }

                if (!wrapper.isPlaying() || wrapper.getToggles().getState(Toggles.VANISH)) {
                    player.sendMessage(ChatColor.RED + "That user is not currently online.");
                    return;
                }

                //The recipient is ignoring the player, not themselves.
                boolean ignoringPlayer = wrapper.getIgnoredFriends().containsKey(player.getUniqueId());

                System.out.println("Ignored: " + ignoringPlayer + " " + wrapper.getIgnoredFriends().size());
                ShardInfo shard = ShardInfo.getByPseudoName(wrapper.getShardPlayingOn());

                if(shard == null){
                    player.sendMessage(ChatColor.RED + "Unable to find shard to send to: " + wrapper.getShardPlayingOn());
                    return;
                }
                
                player.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "TO " + wrapper.getChatName()
                		+ ChatColor.GRAY + " [" + ChatColor.AQUA + shard.getShardID() + ChatColor.GRAY + "]: " + ChatColor.WHITE + finalMessage);

                if (!ignoringPlayer) {
                    GameAPI.sendNetworkMessage("PrivateMessage", player.getName(), player.getUniqueId().toString(), recipientName, (ChatColor.GRAY.toString() +
                            ChatColor.BOLD + "FROM " + sendingWrapper.getChatName() + ChatColor.GRAY + " [" + ChatColor
                            .AQUA + DungeonRealms.getInstance().shardid + ChatColor.GRAY + "]: " + ChatColor.WHITE +
                            finalMessage));

                    GameAPI.sendNetworkMessage("BroadcastSoundPlayer", recipientName, Sound.ENTITY_CHICKEN_EGG.toString(), "2f", "1.2f");
                } else {
                    Bukkit.getLogger().info("Supressing message from ignored player " + player.getName() + " to " + recipientName + " Message: " + finalMessage);
                }
            });
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
            event.setCancelled(true);
            GameAPI.runAsSpectators(event.getPlayer(), (player) -> player.sendMessage(ChatColor.RED + event.getPlayer().getName() + " answered a chat prompt> " + event.getMessage()));
            messageListener.accept(event);
            orElseListeners.remove(event.getPlayer());
            return;
        }
    }

    public static List<Player> getRecipients(Player sender, ChatChannel channel) {
    	Stream<? extends Player> search = (channel == ChatChannel.LOCAL ?
    			GameAPI.getNearbyPlayers(sender.getLocation(), 75) : Bukkit.getOnlinePlayers()).stream();
    	
        return search.filter(pl -> {
                    PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(pl);
                    return wrapper != null && (channel != ChatChannel.TRADE || wrapper.getToggles().getState(Toggles.TRADE_CHAT));
        		}).collect(Collectors.toList());
    }
    
    /**
     * Send a chat message as the supplied player.
     */
    public static void sendChatMessage(Player player, String message, boolean forceGlobal) {
    	PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
    	if (wrapper == null)
    		return;
    	
    	if (wrapper.isMuted()) {
            player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return;
        }
    	
    	if (!Rank.isGM(player))
    		message = Chat.checkForBannedWords(message);
    	
    	if ((message.contains(".com") || message.contains(".net") || message.contains(".org") || message.contains("http://") || message.contains("www."))) {
    		if (!Rank.isGM(player)) {
               	player.sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in chat!");
                return;
            }
		}
    	
    	// Handle party chat.
    	if (Affair.isPartyChat(player)) {
    		Affair.getInstance().sendPartyChat(player, message);
			return;
		}
    	
    	// Handle guild chat.
    	if (wrapper.isInGuild() && wrapper.getToggles().getState(Toggles.GUILD_CHAT)) {
    		wrapper.getGuild().sendGuildMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + wrapper.getGuild().getTag() +
    				ChatColor.DARK_AQUA + ChatColor.GRAY + player.getName() + ": " + message, false);
    		return;
    	}
    	
    	//@Username Message
    	if (message.startsWith("@") && !message.startsWith("@i@")) {
        	String[] split = message.substring(1).split(" ");
        	String playerName = split[0];
            if (playerName.length() == 0)
            	return;
            
            split[0] = ""; // Don't include username in the message
            String cmd = "msg " + playerName + String.join(" ", split);
            Bukkit.getLogger().info(player.getName() + "> /" + cmd);
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> Bukkit.dispatchCommand(player, cmd));
            return;
        }
    	
    	boolean global = forceGlobal || wrapper.getToggles().getState(Toggles.GLOBAL_CHAT);
    	
    	if (global && !checkGlobalCooldown(player))
    		return;
    	
    	sendChat(player, message, global ? ChatChannel.getChannel(message) : ChatChannel.LOCAL);
    }
    
    private static void sendChat(Player sender, String message, ChatChannel channel) {
        PlayerWrapper pw = PlayerWrapper.getWrapper(sender);
        
        if (channel == ChatChannel.TRADE && !pw.getToggles().getState(Toggles.TRADE_CHAT)) {
        	sender.sendMessage(ChatColor.RED + "You cannot talk in trade chat while it's toggled off.");
        }
        
        ItemStack held = sender.getEquipment().getItemInMainHand();

        List<Player> recipients = getRecipients(sender, channel);
        String finalMessage = channel.getPrefix() + PlayerWrapper.getWrapper(sender).getChatName() + ChatColor.RESET + ": " + message;
        JSONMessage show = message.contains("@i@") && held != null && held.getType() != Material.AIR ? applyShowItem(sender, message) : null;
        
        if (show != null) {
        	recipients.forEach(show::sendToPlayer);
        } else {
        	recipients.forEach(p -> p.sendMessage(finalMessage));
        }
        
        if (channel == ChatChannel.LOCAL && recipients.size() <= 1)
        	sender.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "No one heard you... Type /gl [message] to talk in global.");
    }

    public static String checkForBannedWords(String msg) {
        String result = msg;
        result = result.replace("ð", "");


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

    private static String toCensor(int characters) {
        String result = "";
        for (int i = 0; i < characters; i++)
        	result = result.concat("*");
        return result;
    }


    private static String replaceOperation(String source, String search) {
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

    public static boolean containsIllegal(String s) {
        //return s.matches("\\p{L}+") || s.matches("\\w+");
        //Probably have an array of allowed characters aswell.
        return !s.replace(" ", "").matches("[\\w\\Q!\"#$%&'()*çáéíóúâêôãõàüñ¿¡+,-./:;<=>?@[\\]^_`{|}~\\E]+");
    }

    public static boolean checkGlobalCooldown(Player player) {
        if (Rank.isSUB(player)) return true;
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
    
    public static JSONMessage applyShowItem(Player player, String s) {
    	ItemStack item = player.getEquipment().getItemInMainHand();
    	
    	if (!s.contains("@i@") || item == null || item.getType() == Material.AIR)
    		return new JSONMessage(s);
    	
    	String[] split = s.split("@i@");
    	String before = split.length > 0 ? split[0] : "";
    	String after = split.length > 1 ? split[1] : "";
    	
    	// Generate display.
        List<String> hoveredChat = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        hoveredChat.add((meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name()));
        if (meta.hasLore())
            hoveredChat.addAll(meta.getLore());
        
        final JSONMessage normal = new JSONMessage("", org.bukkit.ChatColor.WHITE);
        normal.addText(before + "");
        normal.addHoverText(hoveredChat, org.bukkit.ChatColor.BOLD + org.bukkit.ChatColor.UNDERLINE.toString() + "SHOW");
        normal.addText(after);
        
        return normal;
    }
    
    public enum ChatChannel {
    	LOCAL(),
    	TRADE("T", ChatColor.GREEN, "wtb", "wts", "wtt", "trade", "trading", "buying", "selling", "casino"),
    	RECRUIT("GR", ChatColor.RED, "recruiting", "guild", "guilds"),
    	GLOBAL("G", ChatColor.AQUA);
    	
    	private String prefix;
    	private ChatColor color;
    	@Getter private String[] keywords;
    	
    	ChatChannel() {
    		this(null, null);
    	}
    	
    	ChatChannel(String prefix, ChatColor color, String... keywords) {
    		this.prefix = prefix;
    		this.color = color;
    		this.keywords = keywords;
    	}
    	
    	public String getPrefix() {
    		return prefix != null ? color + "<" + ChatColor.BOLD + prefix + color + "> " + ChatColor.RESET : "";
    	}
    	
    	/**
    	 * Gets the chat channel based on the keywords in a chat message.
    	 * Defaults to global.
    	 */
    	public static ChatChannel getChannel(String message) {
    		message = message.toLowerCase();
    		for (ChatChannel c : values())
    			for (String keyword : c.getKeywords())
    				if (message.startsWith(keyword) || message.contains(" " + keyword + " ") || message.endsWith(" " + keyword))
    					return c;
    		return ChatChannel.GLOBAL;
    	}
    }
}
