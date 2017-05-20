package net.dungeonrealms.game.guild;

import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.guild.banner.BannerCreatorMenu;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.guild.token.GuildCreateToken;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuildMechanics {
	
	@Getter
    private static GuildMechanics instance = new GuildMechanics();

    @Getter
    private final Map<UUID, GuildCreateToken> GUILD_CREATE_TOKENS = new WeakHashMap<>();

    @Getter
    private final List<UUID> GUILD_CHAT = new ArrayList<>();


    @SneakyThrows
    public void doLogin(Player player) {
        PlayerWrapper playerWrapper = PlayerWrapper.getPlayerWrapper(player);
        if (playerWrapper == null) return;

        if (playerWrapper.getGuildID() >= 1) {
            GuildWrapper wrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
            if (wrapper == null) { //His guilds wrapper is null so we need to load it in.
                Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player.getUniqueId());
                SQLDatabaseAPI.getInstance().executeQuery("SELECT `guild_id` FROM `guild_members` WHERE `account_id` = '" + accountID + "';", (set) -> {
                    try {
                        if (set == null) return;
                        if (set.first()) {
                            int guildID = set.getInt("guild_id");
                            GuildWrapper newWrapper = new GuildWrapper(guildID);
                            GuildDatabase.getAPI().updateCache(newWrapper, true, (loaded) -> {
                                if (loaded == null || !loaded) return; //Couldnt load
                                GuildDatabase.getAPI().cached_guilds.put(guildID, newWrapper);
                                GuildMember member = newWrapper.getMembers().get(playerWrapper.getAccountID());
                                if (member != null && member.isAccepted()) {
                                    sendAlertFilter(newWrapper, player.getName() + " has joined shard " + DungeonRealms.getInstance().shardid);
                                    showMotd(player, newWrapper.getTag(), newWrapper.getMotd());
                                }
                            });
                        }
                        set.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            } else {
                GuildMember member = wrapper.getMembers().get(playerWrapper.getAccountID());
                if (member != null && member.isAccepted()) {
                    sendAlertFilter(wrapper, player.getName() + " has joined shard " + DungeonRealms.getInstance().shardid);
                    showMotd(player, wrapper.getTag(), wrapper.getMotd());
                }

            }
        }
    }

    public void doLogout(Player player) {
        GuildWrapper guild = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if (guild == null) return;
        GuildMember member = guild.getMembers().get(SQLDatabaseAPI.getInstance().getAccountIdFromUUID(player.getUniqueId()));
        if (member == null) return;
        String tag = guild.getTag();
        String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;

        if (member.isAccepted()) guild.sendGuildMessage(format + player.getName() + " has left your shard.", true);

        member.saveData(true, null);
        if (guild.getNumberOfGuildMembersOnThisShard() <= 1) {
            guild.saveData(true, (bool) -> {
                if (guild.getNumberOfGuildMembersOnThisShard() > 1) return;
                GuildDatabase.getAPI().cached_guilds.remove(guild.getGuildID());
            });
        }
    }

    public void sendAlertFilter(GuildWrapper guild, String message) {
        String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + guild.getTag() + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;
        guild.sendGuildMessage(format + message);
    }


    /**
     * @param guildName Name of guild
     * @param message   Alert message
     */
    public void sendAlert(String guildName, String message) {
        GuildWrapper wrapper = GuildDatabase.getAPI().getGuildWrapper(guildName);
        if (wrapper == null) return;
        String tag = wrapper.getTag();
        String format = ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.DARK_AQUA;

        GameAPI.sendNetworkMessage("Guilds", "message", DungeonRealms.getShard().getPseudoName(), guildName, format.concat(message));
    }

    public void showMotd(Player player, String tag, String motd) {
        player.sendMessage(getFormatted(tag, motd));
    }

    public String getFormatted(String tag, String motd) {
        return ChatColor.DARK_AQUA + "<" + ChatColor.BOLD + tag + ChatColor.DARK_AQUA + "> " + ChatColor.BOLD + "MOTD: " + ChatColor.DARK_AQUA + (motd.length() > 0 ? motd : "No message of the day set. Use /gmotd <motd> to create one.");
    }

    public void showGuildInfo(Player player, GuildWrapper theWrapper, boolean showMotd) {
        player.sendMessage(ChatColor.GRAY + "Loading guild information...");

        if (theWrapper == null) {
            player.sendMessage(ChatColor.RED + "No one in this guild is currently on this shard!");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "              *** " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guild Info" + ChatColor.GRAY + " ***");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GRAY + "Guild Name: " + ChatColor.WHITE + theWrapper.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "Guild Tag: " + ChatColor.DARK_AQUA + "[" + ChatColor.GRAY + theWrapper.getTag() + ChatColor.DARK_AQUA + "]");
        player.sendMessage(ChatColor.GRAY + "Guild Owner: " + theWrapper.getOwner().getPlayerName());
        player.sendMessage(" ");

        player.sendMessage(ChatColor.GRAY + "Guild Officers: " + ChatColor.WHITE + theWrapper.getNamesForInfo(GuildMember.GuildRanks.OFFICER));
        player.sendMessage(ChatColor.GRAY + "Guild Members: " + ChatColor.WHITE + theWrapper.getNamesForInfo(GuildMember.GuildRanks.MEMBER));

        player.sendMessage(" ");

        if (showMotd)
            player.sendMessage(ChatColor.GRAY + "Message of the Day: \"" + ChatColor.WHITE + theWrapper.getMotd() + ChatColor.GRAY + "\"");
    }


    /**
     * Kick a member from their guild
     *
     * @param kicker    Player who executed kick
     * @param player    Kicked
     * @param guildName Guild name
     */
    public void kickFromGuild(Player kicker, UUID player, String guildName) {
        sendAlert(guildName, kicker.getName() + " has kicked " + SQLDatabaseAPI.getInstance().getUsernameFromUUID(player) + " from the guild.");
        GameAPI.updatePlayerData(player, UpdateType.GUILD);
    }

    /**
     * Opens or reopens guild banner creators
     *
     * @param player Player
     */
    public void openGuildBannerCreator(Player player, String guildName, String guildTag, String displayName, ItemStack banner) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> new BannerCreatorMenu(player, guildName, guildTag, displayName, banner).open(player));
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


    public void createGuild(Player player, String guildName, String guildTag, String guildDisplayName, ItemStack banner) {
        PlayerWrapper hisPlayerWrapper = PlayerWrapper.getPlayerWrapper(player);
        if (hisPlayerWrapper == null) {
            Constants.log.info("Could not load players wrapper on guild creation creation for the player: " + player.getName());
            return;
        }
        int playerAccountID = hisPlayerWrapper.getAccountID();

        // Confirmation stage
        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Ok, thank you. Let me show you a quick summary of your guild.");
        player.sendMessage("");

        player.sendMessage(ChatColor.GRAY + "              *** " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guild Creation Confirmation" + ChatColor.GRAY + " ***");
        player.sendMessage(ChatColor.GRAY + "Guild Name: " + ChatColor.WHITE + guildDisplayName);
        player.sendMessage(ChatColor.GRAY + "Guild Tag: " + ChatColor.DARK_AQUA + "[" + ChatColor.GRAY + guildTag + ChatColor.DARK_AQUA + "]");
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
                    
                    GuildDatabase.getAPI().doesGuildNameExist(guildName, guildIDChecking -> {
                        if (guildIDChecking == null || guildIDChecking >= 0) {
                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "A guild with this name already exists!");
                            return;
                        }
                        GuildDatabase.getAPI().doesTagExist(guildTag, guildIDCheck -> {
                            if (guildIDCheck == null || guildIDCheck >= 0) {
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "A guild with this tag already exists!");
                                return;
                            }
                            
                            GuildWrapper newWrapper = new GuildWrapper(-1);

                            newWrapper.setName(guildName);
                            newWrapper.setTag(guildTag);
                            newWrapper.setDisplayName(guildDisplayName);
                            newWrapper.setBanner(banner);

                            GuildMember member = new GuildMember(playerAccountID, -1);
                            member.setAccepted(true);
                            member.setRank(GuildMember.GuildRanks.OWNER);
                            member.setWhenJoined(System.currentTimeMillis());
                            newWrapper.getMembers().put(playerAccountID, member);
                            newWrapper.insertIntoDatabase(true, (newID) -> {
                                if (newID == null) return;
                                hisPlayerWrapper.setGuildID(newID);
                                member.setGuildID(newID);
                                newWrapper.setGuildID(newID);
                                GuildDatabase.getAPI().cached_guilds.put(newID, newWrapper);
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "A guild with this tag already exists!");


                                Achievements.giveAchievement(player, EnumAchievements.CREATE_A_GUILD);

                                player.sendMessage("");
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Congratulations, you are now the proud owner of the '" + newWrapper.getDisplayName() + "' guild!");
                                player.sendMessage(ChatColor.GRAY + "You can now chat in your guild chat with " + ChatColor.BOLD + "/g <msg>" + ChatColor.GRAY + ", invite players with " + ChatColor.BOLD + "/ginvite <player>" + ChatColor.GRAY + " and much more -- Check out your character journal for more information!");
                                BankMechanics.takeGemsFromInventory(player, 5000);

                                // guild tags in scoreboard disabled
                                GamePlayer gp = GameAPI.getGamePlayer(player);
                                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                                if (gp != null)
                                    ScoreboardHandler.getInstance().setPlayerHeadScoreboard(player, wrapper.getAlignment().getNameColor(), wrapper.getLevel());

                                player.getInventory().addItem(newWrapper.getBanner().clone());

                            });
                        });
                    });
                }
            });
        }), 1L);
    }

    /**
     * This single type is used for the dialogue of successfully
     * creating a guild.
     *
     * @param player Player in dialogue interaction
     */
    public void startGuildCreationDialogue(Player player) {
        if (DungeonRealms.isEvent()) {
            player.sendMessage(ChatColor.RED + "You cannot create a guild on this shard.");
            return;
        }

        // They must be level 10
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);

        GuildWrapper hisCurrentWrapper = GuildDatabase.getAPI().getPlayersGuildWrapper(player.getUniqueId());
        if (wrapper == null) {
            Bukkit.getLogger().info("Null wrapper!");
            return;
        }
        if (wrapper.getLevel() < 10) {
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

            if (hisCurrentWrapper != null) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You are already in a Guild! Leave it before creating a new one!");
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
                        GuildDatabase.getAPI().doesGuildNameExist(guildName, guildIDChecking -> {
                            if (guildIDChecking == null) return;
                            if (guildIDChecking < 0) {

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

                                            GuildDatabase.getAPI().doesTagExist(tag, guildTagIdChecking -> {
                                                if (guildTagIdChecking < 0) {
                                                    openGuildBannerCreator(player, guildName, tag, guildDisplayName, null);
                                                } else
                                                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild tag already exists. Please choose a different guild tag.");

                                            });


                                        }), 1L);
                            } else
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild with the name '" + ChatColor.GRAY + guildDisplayName + ChatColor.WHITE + "' already exists. Please choose a different name.");
                        });

                    }), 1L);
        });

    }


    private boolean checkForProfanity(String text) {
        for (String s : Chat.bannedWords)
            if (text.equalsIgnoreCase(s) || text.toLowerCase().contains(s.toLowerCase()))
                return true;
        return false;
    }
}
