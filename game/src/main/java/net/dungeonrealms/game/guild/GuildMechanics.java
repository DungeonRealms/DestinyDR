package net.dungeonrealms.game.guild;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.guild.banner.BannerCreatorMenu;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.guild.token.GuildCreateToken;
import net.dungeonrealms.game.guild.token.GuildInfoToken;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class GuildMechanics {
    private static GuildMechanics instance = null;

    public static GuildMechanics getInstance() {
        if (instance == null) {
            instance = new GuildMechanics();
        }
        return instance;
    }

    @Getter
    private final Map<UUID, GuildCreateToken> GUILD_CREATE_TOKENS = new WeakHashMap<>();

    @Getter
    private final List<UUID> GUILD_CHAT = new ArrayList<>();


    public void doLogin(Player player) {
        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) return;
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());

        GuildDatabaseAPI.get().updateCache(guildName, true, () -> {
            String tag = GuildDatabaseAPI.get().getTagOf(guildName);
            String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;

            // Checks if guild still exists
            checkPlayerGuild(player.getUniqueId());


            if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) return;

            List<String> filter = new ArrayList<>(Collections.singletonList(player.getName()));

            GuildDatabaseAPI.get().getAllOfGuild(guildName)
                    .stream().filter(uuid -> Bukkit.getPlayer(uuid) != null && !uuid.equals(player.getUniqueId())).forEach(uuid -> {
                Bukkit.getPlayer(uuid).sendMessage(format.concat(player.getName() + " has joined your shard."));
                filter.add(Bukkit.getPlayer(uuid).getName());
            });

            sendAlertFilter(guildName, player.getName() + " has joined shard " + DungeonRealms.getInstance().shardid, filter.toArray(new String[filter.size()]));
            showMotd(player, guildName);
        });
    }

    public void doChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        if (GuildDatabaseAPI.get().isGuildNull(event.getPlayer().getUniqueId())) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (GUILD_CHAT.contains(player.getUniqueId())) {
            String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
            sendChat(guildName, player, message);
            event.setCancelled(true);

        }
    }

    public void doLogout(Player player) {
        GUILD_CHAT.remove(player.getUniqueId());
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        if (guildName.equals("") || (guildName == null)) return;
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;

        GuildDatabaseAPI.get().getAllOfGuild(guildName)
                .stream().filter(uuid -> Bukkit.getPlayer(uuid) != null && !uuid.equals(player.getUniqueId())).forEach(uuid -> Bukkit.getPlayer(uuid).sendMessage(format.concat(player.getName() + " has left your shard.")));

        if (getAllOnlineGuildMembers(guildName).size() <= 1) GuildDatabaseAPI.get().removeFromCache(guildName);
    }

    /**
     * Checks if guild still exists
     * if not player guild will be set to blank.
     *
     * @param uuid Target
     */
    public void checkPlayerGuild(UUID uuid) {
        if (!GuildDatabaseAPI.get().isGuildNull(uuid)) {
            String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, uuid);

            // Checks if guild still exists
            boolean guildExists = GuildDatabase.getAPI().doesGuildNameExist(guildName, null);

            if (!guildExists) {
                GuildDatabaseAPI.get().setGuild(uuid, "");
            } else if (guildExists && !GuildDatabaseAPI.get().isInGuild(uuid, guildName)) {
                GuildDatabaseAPI.get().setGuild(uuid, "");
            }
        }
    }


    /**
     * All local messages will be sent to
     * guild chat once this is toggled.
     *
     * @param player Player player
     */
    public void toggleGuildChat(Player player) {
        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);

        if (GUILD_CHAT.contains(player.getUniqueId())) {
            GUILD_CHAT.remove(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY + "Messages will now be default sent to local chat.");
        } else {
            GUILD_CHAT.add(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_AQUA + "Messages will now be default sent to <" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + ">. Type " + ChatColor.UNDERLINE + "/l <msg>" + ChatColor.DARK_AQUA + " to speak in local.");
            player.sendMessage(ChatColor.GRAY + "To change back to default local, type " + ChatColor.BOLD + "/g" + ChatColor.GRAY + " again.");
        }
    }

    public List<UUID> getAllOnlineGuildMembers(String guildName) {
        List<UUID> online = new ArrayList<>();

        GuildDatabaseAPI.get().getAllOfGuild(guildName)
                .stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).forEach(online::add);

        return online;
    }

    /**
     * @param guildName Name of guild
     * @param player    Sender
     * @param message   Chat message
     */
    public void sendChat(String guildName, Player player, String message) {
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String format = ChatColor.DARK_AQUA.toString() + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + ">" + ChatColor.GRAY + " " + player.getName() + ": " + ChatColor.GRAY;

        GameAPI.sendNetworkMessage("Guilds", "message", Arrays.asList(guildName, format.concat(message)).toArray(new String[2]));
    }

    /**
     * @param guildName Name of guild
     * @param message   Alert message
     * @param filters   Filters
     */
    public void sendAlertFilter(String guildName, String message, String... filters) {
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;

        GameAPI.sendNetworkMessage("Guilds", "message:" + getFilters(filters).toString(), Arrays.asList(guildName, format.concat(message)).toArray(new String[2]));
    }

    private StringBuilder getFilters(String[] filters) {
        StringBuilder players = new StringBuilder();
        for (int i = 0; i < filters.length; i++)
            if (i == 0) players.append(filters[i]);
            else players.append(":").append(filters[i]);
        return players;
    }


    /**
     * @param guildName Name of guild
     * @param message   Alert message
     */
    public void sendAlert(String guildName, String message) {
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;

        GameAPI.sendNetworkMessage("Guilds", "message", Arrays.asList(guildName, format.concat(message)).toArray(new String[2]));
    }

    /**
     * Displays the guild Message of the day
     *
     * @param player    Player to show Message of The Day
     * @param guildName Guild
     */
    public void showMotd(Player player, String guildName) {
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String motd = GuildDatabaseAPI.get().getMotdOf(guildName);

        player.sendMessage(ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.BOLD + "MOTD: " + ChatColor.DARK_AQUA + (motd.length() > 0 ? motd : "No message of the day set. Use /gmotd <motd> to create one."));
    }

    /**
     * Displays guild information in chat format
     *
     * @param player    Player that will receive the information
     * @param guildName Guild
     */
    public void showGuildInfo(Player player, String guildName, boolean showMotd) {
        player.sendMessage(ChatColor.GRAY + "Loading guild information...");

        // UPDATE CACHED DATA INFO //
        GuildDatabaseAPI.get().updateCache(guildName, true, () -> GameAPI.submitAsyncCallback(() -> {
            GuildInfoToken token = new GuildInfoToken();

            token.setOwner(getPlayerName(UUID.fromString(GuildDatabaseAPI.get().getOwnerOf(guildName))));
            token.getMember().addAll(getPlayerNames(GuildDatabaseAPI.get().getAllGuildMembers(guildName)));
            token.getOfficers().addAll(getPlayerNames(GuildDatabaseAPI.get().getGuildOfficers(guildName)));

            return token;
        }, tokenFuture -> {
            try {
                GuildInfoToken token = tokenFuture.get();

                String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);
                String tag = GuildDatabaseAPI.get().getTagOf(guildName);
                String motd = GuildDatabaseAPI.get().getMotdOf(guildName);

                StringBuilder members = getPlayers(token.getMember());
                StringBuilder officers = getPlayers(token.getOfficers());

                player.sendMessage(ChatColor.GRAY + "              *** " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guild Info" + ChatColor.GRAY + " ***");
                player.sendMessage(" ");
                player.sendMessage(ChatColor.GRAY + "Guild Name: " + ChatColor.WHITE + displayName);
                player.sendMessage(ChatColor.GRAY + "Guild Tag: " + ChatColor.DARK_AQUA + "[" + ChatColor.GRAY + tag + ChatColor.DARK_AQUA + "]");
                player.sendMessage(ChatColor.GRAY + "Guild Owner: " + token.getOwner());
                player.sendMessage(" ");

                player.sendMessage(ChatColor.GRAY + "Guild Officers: " + ChatColor.WHITE + (officers.length() == 0 ? "None" : officers));
                player.sendMessage(ChatColor.GRAY + "Guild Members: " + ChatColor.WHITE + (members.length() == 0 ? "None" : members));

                player.sendMessage(" ");

                if (showMotd)
                    player.sendMessage(ChatColor.GRAY + "Message of the Day: \"" + ChatColor.WHITE + motd + ChatColor.GRAY + "\"");

            } catch (InterruptedException | ExecutionException e) {
                player.sendMessage(ChatColor.RED + "Unable to load guild information");
                e.printStackTrace();
            }

        }));
    }

    private String getPlayerName(UUID uuid) {
        String name = DatabaseAPI.getInstance().getOfflineName(uuid);
        return Bukkit.getPlayer(uuid) != null ? ChatColor.GREEN + name : ChatColor.GRAY + name;
    }

    private Set<String> getPlayerNames(List<UUID> uuids) {
        return uuids.stream().map(this::getPlayerName).collect(Collectors.toSet());
    }

    private StringBuilder getPlayers(List<String> names) {
        StringBuilder players = new StringBuilder();

        for (int i = 0; i < names.size(); i++) {
            if (i == 0) players.append(names.get(i));
            else players.append(", ").append(names.get(i));
        }
        return players;
    }

    /**
     * Adds player to members in guild.
     *
     * @param player    Player to join guild
     * @param referrer  Player who sent invitation
     * @param guildName Guild
     */
    public void joinGuild(Player player, String referrer, String guildName) {
        String guildDisplayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);

        GuildDatabaseAPI.get().doesGuildNameExist(guildName, guildExists -> {

            if (guildExists) {
                player.sendMessage(ChatColor.DARK_AQUA + "You have joined '" + ChatColor.BOLD + guildDisplayName + "'" + ChatColor.DARK_AQUA + ".");
                player.sendMessage(ChatColor.GRAY + "To chat with your new guild, use " + ChatColor.BOLD + "/g" + ChatColor.GRAY + " OR " + ChatColor.BOLD + " /g <message>");
                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.GUILD_MEMBER);
                GuildDatabaseAPI.get().addPlayer(guildName, player.getUniqueId());
                GameAPI.updatePlayerData(player.getUniqueId());

                // guild tags in scoreboard disabled
                GamePlayer gp = GameAPI.getGamePlayer(player);
                if (gp != null) {
                    ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, gp.getPlayerAlignment().getAlignmentColor(), gp.getLevel());
                }


                sendAlert(guildName, player.getName() + ChatColor.GRAY.toString() + " has " +
                        ChatColor.UNDERLINE + "joined" + ChatColor.GRAY + " your guild." + (referrer != null ? " [INVITE: " + ChatColor.ITALIC + referrer + ChatColor.GRAY + "]" : ""));
                GameAPI.updateGuildData(guildName);

            } else {
                player.sendMessage(ChatColor.RED + "This guild no longer exists.");

            }
        });
    }


    /**
     * Kick a member from their guild
     *
     * @param kicker    Player who executed kick
     * @param player    Kicked
     * @param guildName Guild name
     */
    public void kickFromGuild(Player kicker, UUID player, String guildName) {
        sendAlert(guildName, kicker.getName() + " has kicked " + DatabaseAPI.getInstance().getOfflineName(player) + " from the guild.");
        GuildDatabaseAPI.get().removeFromGuild(guildName, player);

        GameAPI.updatePlayerData(player);
    }

    /**
     * Prompts user then,
     * Manually removes player from their guild if they confirm.
     *
     * @param player Targeted player
     */

    public void leaveGuild(Player player) {
        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId()))
            return;

        String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);
        List<UUID> officers = GuildDatabaseAPI.get().getGuildOfficers(guildName);

        player.sendMessage(ChatColor.GRAY + "Are you sure you want to QUIT the guild '" + ChatColor.DARK_AQUA + displayName + ChatColor.GRAY + "' - This cannot be undone. " + "(" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.GRAY + " / " + ChatColor.RED.toString() + ChatColor.BOLD + "N" + ChatColor.GRAY + ")");
        final boolean isOwner = GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName);

        if (isOwner && officers.size() == 0)
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.GRAY + "You are the " + ChatColor.UNDERLINE + "GUILD LEADER" + ChatColor.GRAY + " and there are no successors to watch after the guild. If you leave, this guild will be " + ChatColor.BOLD + "PERMANENTLY DELETED" + ChatColor.GRAY + ". All members will be kicked, and you will lose your 5,000g deposit.");

        Chat.listenForMessage(player, confirmation -> {
            if (!confirmation.getMessage().equalsIgnoreCase("y") || confirmation.getMessage().equalsIgnoreCase("n") || confirmation.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "/gquit - " + ChatColor.BOLD + "CANCELLED");
                return;
            }

            player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + "QUIT" + ChatColor.RED + " your guild.");
            sendAlert(guildName, player.getName() + " has left the guild.");

            GamePlayer gp = GameAPI.getGamePlayer(player);
            if (gp != null) {
                ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, gp.getPlayerAlignment().getAlignmentColor(), gp.getLevel());
            }

            boolean setOwner = false;
            boolean disbanded = false;

            if (isOwner) {
                if (officers.size() > 0) {
                    UUID sucessor = officers.get(0);
                    sendAlert(guildName, DatabaseAPI.getInstance().getOfflineName(sucessor) + " has been selected a the new " + ChatColor.UNDERLINE + "GUILD LEADER");
                    setOwner = true;
                } else {
                    // player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + "DISBANDED" + ChatColor.RED + " your guild.");
                    sendAlert(guildName, player.getName() + " has disbanded the guild.");

                    for (UUID uuid : GuildDatabaseAPI.get().getAllOfGuild(guildName)) {
                        GuildDatabaseAPI.get().removeFromGuild(guildName, uuid);
                    }

                    GuildDatabaseAPI.get().deleteGuild(guildName);
                    disbanded = true;
                }
            }

            if (!disbanded)
                GuildDatabaseAPI.get().removeFromGuild(guildName, player.getUniqueId());

            if (setOwner) GuildDatabaseAPI.get().setOwner(guildName, officers.get(0));

            GuildDatabaseAPI.get().updateCache(guildName, true);

            GameAPI.updateGuildData(guildName);
            GameAPI.updatePlayerData(player.getUniqueId());
        }, null);
    }

    /**
     * Opens or reopens guild banner creators
     *
     * @param player Player
     */
    public void openGuildBannerCreator(Player player) {
        GuildCreateToken guildInfo = grabCurrentCreateInfo(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> new BannerCreatorMenu(player, guildInfo).open(player));
    }


    /**
     * Pulls their current create information from the hashmap
     *
     * @param player Get's the players create information
     */
    public GuildCreateToken grabCurrentCreateInfo(Player player) {
        if (GUILD_CREATE_TOKENS.containsKey(player.getUniqueId())) return GUILD_CREATE_TOKENS.get(player.getUniqueId());

        GuildCreateToken info = new GuildCreateToken();
        GUILD_CREATE_TOKENS.put(player.getUniqueId(), info);
        return info;
    }

    /**
     * Sends a message to all members of a guild.
     *
     * @param guildName Guild
     * @param message   Message
     * @param filters   Filters
     */
    public void sendMessageToGuild(String guildName, String message, String... filters) {
        loop:
        for (UUID uuid : GuildDatabaseAPI.get().getAllOfGuild(guildName)) {
            Player player = Bukkit.getServer().getPlayer(uuid);

            if (player != null) {
                for (String s : filters)
                    if (player.getName().equalsIgnoreCase(s))
                        continue loop;
                player.sendMessage(message);
                GameAPI.runAsSpectators(player, (spectator) -> spectator.sendMessage(ChatColor.RED + "" + ChatColor.BOLD +"(AS " + player.getName() + ") " + message));
            }
        }
    }


    /**
     * Creates a guild... DA!
     *
     * @param player Player
     * @param info   Create information
     */
    public void createGuild(Player player, GuildCreateToken info) {
        GUILD_CREATE_TOKENS.remove(player.getUniqueId());

        // Confirmation stage
        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Ok, thank you. Let me show you a quick summary of your guild.");
        player.sendMessage("");

        player.sendMessage(ChatColor.GRAY + "              *** " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guild Creation Confirmation" + ChatColor.GRAY + " ***");
        player.sendMessage(ChatColor.GRAY + "Guild Name: " + ChatColor.WHITE + info.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "Guild Tag: " + ChatColor.DARK_AQUA + "[" + ChatColor.GRAY + info.getTag() + ChatColor.DARK_AQUA + "]");
        // player.sendMessage(ChatColor.GRAY + "Guild Color: " + ChatColor.WHITE + "" + color_rgb);
        player.sendMessage(ChatColor.GRAY + "Cost: " + ChatColor.DARK_AQUA + "5,000" + ChatColor.BOLD + "G");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Enter '" + ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "CONFIRM" + ChatColor.GRAY + "' to confirm your guild creation.");
        player.sendMessage("");
        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Guild creation is " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Type 'cancel' to void this entire purchase.");
        player.sendMessage("");

        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Chat.listenForMessage(player, confirmation -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                // Cancels dialogue
                if (confirmation.getMessage().equalsIgnoreCase("cancel")) {
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                    return;
                }


                // Confirms purchase
                if (confirmation.getMessage().equalsIgnoreCase("confirm")) {
                    if (BankMechanics.getGemsInInventory(player) < 5000) {
                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You do not have enough GEM(s) -- 5,000, to create a guild.");
                        return;
                    }

                    BannerMeta meta = (BannerMeta) info.getCurrentBanner().getItemMeta();
                    meta.setLore(new ArrayList<>());
                    meta.setDisplayName(ChatColor.GREEN + info.getDisplayName() + "'s Guild banner");
                    meta.setLore(Collections.singletonList(ChatColor.RED + "Right click to equip"));
                    info.getCurrentBanner().setItemMeta(meta);

                    String itemString = ItemSerialization.itemStackToBase64(info.getCurrentBanner());

                    // Registers guild in database
                    GuildDatabaseAPI.get().createGuild(info.getGuildName(), info.getDisplayName(), info.getTag(), player.getUniqueId(), itemString, onComplete -> {
                        if (!onComplete) {
                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.RED + "We have an error. Failed to create guild in database. Please try again later");
                            return;
                        }

                        Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.CREATE_A_GUILD);

                        player.sendMessage("");
                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Congratulations, you are now the proud owner of the '" + info.getDisplayName() + "' guild!");
                        player.sendMessage(ChatColor.GRAY + "You can now chat in your guild chat with " + ChatColor.BOLD + "/g <msg>" + ChatColor.GRAY + ", invite players with " + ChatColor.BOLD + "/ginvite <player>" + ChatColor.GRAY + " and much more -- Check out your character journal for more information!");
                        BankMechanics.takeGemsFromInventory(player, 5000);

                        // guild tags in scoreboard disabled
                        GamePlayer gp = GameAPI.getGamePlayer(player);
                        if (gp != null)
                            ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, gp.getPlayerAlignment().getAlignmentColor(), gp.getLevel());

                        player.getInventory().addItem(info.getCurrentBanner());
                        GameAPI.updatePlayerData(player.getUniqueId());

                    });
                }
            });

        }, null), 1L);
    }

    /**
     * This single type is used for the dialogue of successfully
     * creating a guild.
     *
     * @param player Player in dialogue interaction
     */
    public void startGuildCreationDialogue(Player player) {
        if (DungeonRealms.getInstance().isEventShard) {
            player.sendMessage(ChatColor.RED + "You cannot create a guild on this shard.");
            return;
        }

        // They must be level 10
        if (((Integer) DatabaseAPI.getInstance().getData(EnumData.LEVEL, player.getUniqueId()) < 10)) {
            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You must be at least " + ChatColor.WHITE + "" + ChatColor.BOLD + "LEVEL 10" + ChatColor.WHITE + " to create a guild.");
            return;
        }

        // This is where the player greets the NPC
        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Hello, " + ChatColor.UNDERLINE + player.getName() + ChatColor.WHITE + ", I'm the guild registrar, would you like to create a guild today? Please note that it will cost 5,000 GEM(s). (" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.WHITE + " / " + ChatColor.RED.toString() + ChatColor.BOLD + "N" + ChatColor.WHITE + ")");

        // Asks if they would like to create a guild.
        Chat.listenForMessage(player, guildCreation -> {

            // If the answer is not use exit
            if (!guildCreation.getMessage().equalsIgnoreCase("y") || guildCreation.getMessage().equalsIgnoreCase("n") || guildCreation.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                return;
            }


            // Checks if they are already in a guild
            if (!GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You are already part of a guild. You'll need to /gquit before creating another.");
                return;
            }

            // 5000 gems is require to register a guild
            if ((BankMechanics.getGemsInInventory(player) < 5000)) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You do not have enough GEM(s) -- 5,000, to create a guild.");
                return;
            }


            // Prompts the user for requested Guild name
            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Ok, please enter your " + ChatColor.UNDERLINE + "formal guild name" + ChatColor.WHITE + ", this should be your FULL GUILD NAME, you will enter a shorter 'tag' later.");
            player.sendMessage(ChatColor.GRAY + "You may type 'cancel' at any time to stop this guild creation.");

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Chat.listenForMessage(player, guildNameRequest -> {

                        // Cancels dialogue
                        if (guildNameRequest.getMessage().equalsIgnoreCase("cancel")) {
                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                            return;
                        }


                        String guildDisplayName = guildNameRequest.getMessage();
                        String guildName = guildDisplayName.replaceAll("\\s", "").toLowerCase();

                        // Name must be below 16 characters
                        if (guildDisplayName.length() > 16) {
                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name exceeds the maximum length of 16 characters.");
                            player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (guildDisplayName.length() - 16) + " characters over the limit.");
                            return;
                        }

                        Pattern pattern = Pattern.compile("^([A-Za-z]|[0-9])+$");
                        Matcher matcher = pattern.matcher(guildName);

                        // Name must be alphanumerical
                        if (!matcher.find()) {
                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name can only contain alphanumerical values.");
                            return;
                        }

                        // Checks for profanity
                        if (checkForProfanity(guildDisplayName)) {
                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name has an illegal/censored word in it. Please enter an alternative name.");
                            return;
                        }


                        // Checks if guild already exists
                        GuildDatabaseAPI.get().doesGuildNameExist(guildName, guildExists -> {
                            if (!guildExists) {

                                // Prompts the player for desired guild tag
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Ok, your guild will be formally known as        " + ChatColor.DARK_AQUA + guildDisplayName + ChatColor.WHITE + ", now please enter a " + ChatColor.UNDERLINE + "guild prefix tag.");
                                player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "This 'prefix tag' can be between 2-4 letters and will appear before all chat messages sent by guild members.");

                                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->

                                        Chat.listenForMessage(player, guildTagRequest -> {

                                            // Cancels dialogue
                                            if (guildTagRequest.getMessage().equalsIgnoreCase("cancel")) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                                                return;
                                            }

                                            // Tag is converted to all caps
                                            String tag = guildTagRequest.getMessage().replace(" ", "").toUpperCase();

                                            // Name must be below 4 characters
                                            if (tag.length() > 4) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild tag exceeds the maximum length of 4 characters.");
                                                return;
                                            }

                                            // Name must be above 2 characters
                                            if (tag.length() < 2) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild tag must be at least 2 characters.");
                                                return;
                                            }

                                            // Checks for profanity
                                            if (checkForProfanity(tag)) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild tag has an illegal/censored word in it. Please enter an alternative name.");
                                                return;
                                            }

                                            GuildDatabaseAPI.get().doesTagExist(tag, guildTagExists -> {
                                                if (!guildTagExists) {
                                                    GUILD_CREATE_TOKENS.remove(player.getUniqueId());
                                                    GuildCreateToken info = grabCurrentCreateInfo(player);

                                                    info.setGuildName(guildName);
                                                    info.setTag(tag);
                                                    info.setOwner(player.getUniqueId());
                                                    info.setDisplayName(guildDisplayName);
                                                    openGuildBannerCreator(player);
                                                } else
                                                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild tag already exists. Please choose a different guild tag.");

                                            });


                                        }, null), 1L);
                            } else
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild with the name '" + ChatColor.GRAY + guildDisplayName + ChatColor.WHITE + "' already exists. Please choose a different name.");
                        });

                    }
                    , null), 1L);


        }, null);

    }


    private boolean checkForProfanity(String text) {
        for (String s : Chat.bannedWords)
            if (text.equalsIgnoreCase(s) || text.toLowerCase().contains(s.toLowerCase()))
                return true;
        return false;
    }
}
