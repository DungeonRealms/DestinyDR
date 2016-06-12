package net.dungeonrealms.game.guild;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.guild.banner.menus.BannerCreatorMenu;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/2/2016
 */

public class GuildMechanics implements GenericMechanic {
    private static GuildMechanics instance = null;

    public static GuildMechanics getInstance() {
        if (instance == null) {
            instance = new GuildMechanics();
        }
        return instance;
    }

    public class GuildCreateInfo {
        @Getter
        @Setter
        private UUID owner;

        @Getter
        @Setter
        private String guildName, displayName, tag;

        @Getter
        @Setter
        private ItemStack currentBanner = new ItemStack(Material.BANNER, 1, (byte) 15);
    }

    @Getter
    private final Map<UUID, GuildCreateInfo> guildCreateInfo = new WeakHashMap<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }


    @Override
    public void startInitialization() {
        //TODO
    }

    @Override
    public void stopInvocation() {
        //TODO
    }

    public void doLogin(Player player) {
        if (GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) return;

        String guildName = (String) DatabaseAPI.getInstance().getData(EnumData.GUILD, player.getUniqueId());

        // Checks if guild still exists
        GuildDatabaseAPI.get().doesGuildNameExist(guildName, guildExists -> {
            if (!guildExists)
                GuildDatabaseAPI.get().setGuild(player.getUniqueId(), "");
        });

        showMotd(player, guildName);
    }

    /**
     * @param guildName Name of guild
     * @param message   Alert message
     */
    public void sendAlert(String guildName, String message) {
        // String temp = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + " " + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;


        NetworkAPI.getInstance();
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
        String displayName = GuildDatabaseAPI.get().getDisplayNameOf(guildName);
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String motd = GuildDatabaseAPI.get().getMotdOf(guildName);
        String owner = DatabaseAPI.getInstance().getOfflineName(UUID.fromString(GuildDatabaseAPI.get().getOwnerOf(guildName)));

        StringBuilder members = getPlayers(GuildDatabaseAPI.get().getAllGuildMembers(guildName));
        StringBuilder officers = getPlayers(GuildDatabaseAPI.get().getGuildOfficers(guildName));

        player.sendMessage(ChatColor.GRAY + "              *** " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guild Info" + ChatColor.GRAY + " ***");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GRAY + "Guild Name: " + ChatColor.WHITE + displayName);
        player.sendMessage(ChatColor.GRAY + "Guild Tag: " + ChatColor.DARK_AQUA + "[" + ChatColor.GRAY + tag + ChatColor.DARK_AQUA + "]");
        player.sendMessage(ChatColor.GRAY + "Guild Owner: " + ChatColor.WHITE + owner);
        player.sendMessage(" ");

        player.sendMessage(ChatColor.GRAY + "Guild Officers: " + ChatColor.WHITE + (officers.length() == 0 ? "None" : officers));
        player.sendMessage(ChatColor.GRAY + "Guild Members: " + ChatColor.WHITE + (members.length() == 0 ? "None" : members));

        player.sendMessage(" ");

        if (showMotd)
            player.sendMessage(ChatColor.GRAY + "Message of the Day: \"" + ChatColor.WHITE + motd + ChatColor.GRAY + "\"");
    }


    private StringBuilder getPlayers(List<UUID> uuids) {
        StringBuilder players = new StringBuilder();
        for (int i = 0; i < uuids.size(); i++)
            if (i == 0) players.append(DatabaseAPI.getInstance().getOfflineName(uuids.get(i)));
            else players.append(", ").append(DatabaseAPI.getInstance().getOfflineName(uuids.get(i)));
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

                sendAlert(guildName, player.getName() + ChatColor.GRAY.toString() + " has " +
                        ChatColor.UNDERLINE + "joined" + ChatColor.GRAY + " your guild." + (referrer != null ? "[INVITE: " + ChatColor.ITALIC + referrer + ChatColor.GRAY + "]" : ""));
            } else {
                player.sendMessage(ChatColor.RED + "This guild no longer exists.");

            }
        });
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
        boolean isOwner = GuildDatabaseAPI.get().isOwner(player.getUniqueId(), guildName);

        if (isOwner && officers.size() == 0)
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.GRAY + "You are the " + ChatColor.UNDERLINE + "GUILD LEADER" + ChatColor.GRAY + " and there are not successors to watch after the guild, if you leave this guild it will be " + ChatColor.BOLD + "PERMANENTLY DELETED" + ChatColor.GRAY + ". All members will be kicked, and you will lose your 5,000g deposit.");

        Chat.listenForMessage(player, confirmation -> {
            if (!confirmation.getMessage().equalsIgnoreCase("y") || confirmation.getMessage().equalsIgnoreCase("n") || confirmation.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.RED + "/gquit - " + ChatColor.BOLD + "CANCELLED");
                return;
            }

            player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + "QUIT" + ChatColor.RED + " your guild.");
            GuildDatabaseAPI.get().removeFromGuild(guildName, player.getUniqueId());
            sendAlert(guildName, player.getName() + "has left the guild.");


            if (isOwner) {
                if (officers.size() > 0) {
                    UUID sucessor = officers.get(0);
                    GuildDatabaseAPI.get().setOwner(guildName, sucessor);
                    sendAlert(guildName, DatabaseAPI.getInstance().getOfflineName(sucessor) + " has been selected a the new " + ChatColor.UNDERLINE + "GUILD LEADER");
                } else {
                    // player.sendMessage(ChatColor.RED + "You have " + ChatColor.BOLD + "DISBANDED" + ChatColor.RED + " your guild.");
                    sendAlert(guildName, player.getName() + " has disbanded the guild.");
                    GuildDatabaseAPI.get().deleteGuild(guildName);
                }
            }

        }, null);
    }

    /**
     * Opens or reopens guild banner creators
     *
     * @param player Player
     */
    public void openGuildBannerCreator(Player player) {
        GuildCreateInfo guildInfo = grabCurrentCreateInfo(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> new BannerCreatorMenu(player, guildInfo).open(player));
    }


    /**
     * Pulls their current create information from the hashmap
     *
     * @param player Get's the players create information
     */
    public GuildCreateInfo grabCurrentCreateInfo(Player player) {
        if (guildCreateInfo.containsKey(player.getUniqueId())) return guildCreateInfo.get(player.getUniqueId());

        GuildCreateInfo info = new GuildCreateInfo();
        guildCreateInfo.put(player.getUniqueId(), info);
        return info;
    }


    /**
     * Creates a guild... DA!
     *
     * @param player Player
     * @param info   Create information
     */
    public void createGuild(Player player, GuildCreateInfo info) {
        guildCreateInfo.remove(player.getUniqueId());

        player.closeInventory();

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
            // Cancels dialogue
            if (confirmation.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                return;
            }


            // Confirms purchase
            if (confirmation.getMessage().equalsIgnoreCase("confirm")) {
                if ((BankMechanics.getInstance().getTotalGemsInInventory(player) < 5000)) {
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You do not have enough GEM(s) -- 5,000, to create a guild.");
                    return;
                }

                BannerMeta meta = (BannerMeta) info.getCurrentBanner().getItemMeta();
                meta.setLore(new ArrayList<>());
                meta.setDisplayName(ChatColor.GREEN + info.getDisplayName() + "'s guild banner");
                info.getCurrentBanner().setItemMeta(meta);

                // Registers guild in database
                GuildDatabaseAPI.get().createGuild(info.getGuildName(), info.getDisplayName(), info.getTag(), player.getUniqueId(), info.getCurrentBanner().toString(), onComplete -> {
                    if (!onComplete) {
                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.RED + "We have an error. Failed to create guild in database. Please try again later");
                        return;
                    }

                    Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.CREATE_A_GUILD);

                    player.sendMessage("");
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Congratulations, you are now the proud owner of the '" + info.getDisplayName() + "' guild!");
                    player.sendMessage(ChatColor.GRAY + "You can now chat in your guild chat with " + ChatColor.BOLD + "/g <msg>" + ChatColor.GRAY + ", invite players with " + ChatColor.BOLD + "/ginvite <player>" + ChatColor.GRAY + " and much more -- Check out your character journal for more information!");
                    BankMechanics.getInstance().takeGemsFromInventory(5000, player);


                    player.getInventory().addItem(info.getCurrentBanner());

                });

            }
        }, null), 1L);
    }

    /**
     * This single method is used for the dialogue of successfully
     * creating a guild.
     *
     * @param player Player in dialogue interaction
     */
    public void startGuildCreationDialogue(Player player) {

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
            if ((BankMechanics.getInstance().getTotalGemsInInventory(player) < 5000)) {
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
                                player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "This 'prefix tag' can be between 2-3 letters and will appear before all chat messages sent by guild members.");

                                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () ->

                                        Chat.listenForMessage(player, guildTagRequest -> {

                                            // Cancels dialogue
                                            if (guildTagRequest.getMessage().equalsIgnoreCase("cancel")) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                                                return;
                                            }

                                            // Tag is converted to all caps
                                            String tag = guildTagRequest.getMessage().replace(" ", "").toUpperCase();

                                            // Name must be below 3 characters
                                            if (tag.length() > 3) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild tag exceeds the maximum length of 3 characters.");
                                                return;
                                            }

                                            // Checks for profanity
                                            if (checkForProfanity(tag)) {
                                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild tag has an illegal/censored word in it. Please enter an alternative name.");
                                                return;
                                            }

                                            GuildDatabaseAPI.get().doesTagExist(tag, guildTagExists -> {
                                                if (!guildTagExists) {
                                                    guildCreateInfo.remove(player.getUniqueId());
                                                    GuildCreateInfo info = grabCurrentCreateInfo(player);

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
