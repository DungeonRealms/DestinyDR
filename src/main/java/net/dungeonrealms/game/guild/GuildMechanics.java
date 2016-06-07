package net.dungeonrealms.game.guild;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        //TODO
    }


    /**
     * Displays guild information in chat format
     *
     * @param player    Player that will receive the information
     * @param guildName Guild
     */
    public void showGuildInfo(Player player, String guildName) {
        String tag = GuildDatabaseAPI.get().getTagOf(guildName);
        String motd = GuildDatabaseAPI.get().getMotdOf(guildName);

        player.sendMessage(org.bukkit.ChatColor.GRAY + "              *** " + org.bukkit.ChatColor.DARK_AQUA + org.bukkit.ChatColor.BOLD + "Guild Info" + org.bukkit.ChatColor.GRAY + " ***");
        player.sendMessage(" ");
        player.sendMessage(org.bukkit.ChatColor.GRAY + "Guild Name: " + org.bukkit.ChatColor.WHITE + guildName);
        player.sendMessage(org.bukkit.ChatColor.GRAY + "Guild Tag: " + org.bukkit.ChatColor.DARK_AQUA + "[" + org.bukkit.ChatColor.GRAY + tag + org.bukkit.ChatColor.DARK_AQUA + "]");
        player.sendMessage(" ");
        player.sendMessage(org.bukkit.ChatColor.GRAY + "Message of the Day: \"" + org.bukkit.ChatColor.WHITE + motd + org.bukkit.ChatColor.GRAY + "\"");
    }


    /**
     * Manually removes player from their guild.
     *
     * @param player Targeted player
     */
    public void leaveGuild(Player player) {
        //TODO
    }


    /**
     * This single method is used for the dialogue of successfully
     * creating a guild.
     *
     * @param player Player in dialogue interaction
     */
    public void startGuildCreationDialogue(Player player) {

        // This is where the player greets the NPC
        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Hello, " + ChatColor.UNDERLINE + player.getName() + ChatColor.WHITE + ", I'm the guild registrar, would you like to create a guild today? Please note that it will cost 5,000 GEM(s). (" + ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.WHITE + " / " + ChatColor.RED.toString() + ChatColor.BOLD + "N" + ChatColor.WHITE + ")");

        // Asks if they would like to create a guild.
        Chat.listenForMessage(player, guildCreation -> {

            // If the answer is not use exit
            if (!guildCreation.getMessage().equalsIgnoreCase("y") || guildCreation.getMessage().equalsIgnoreCase("cancel")) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                return;
            }


            // They must be level 10
            if (((Integer) DatabaseAPI.getInstance().getData(EnumData.LEVEL, player.getUniqueId()) < 10)) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You must be at least " + ChatColor.WHITE + "" + ChatColor.BOLD + "LEVEL 10" + ChatColor.WHITE + " to create a guild.");
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

                // Name must be below 16 characters
                if (guildDisplayName.length() > 16) {
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name exceeds the maximum length of 16 characters.");
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (guildDisplayName.length() - 16) + " characters over the limit.");
                    return;
                }

                Pattern pattern = Pattern.compile("^([A-Za-z]|[0-9])+$");
                Matcher matcher = pattern.matcher(guildDisplayName);

                // Name must be alphanumerical
                if (!matcher.find()) {
                    player.sendMessage(ChatColor.RED + "Guild Registrar: You guild name can only contain alphanumerical values.");
                    return;
                }

                // Checks for profanity
                if (checkForProfanity(guildDisplayName)) {
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name has an illegal/censored word in it. Please enter an alternative name.");
                    return;
                }


                // Checks if guild already exists
                GuildDatabaseAPI.get().doesGuildNameExist(guildDisplayName, guildExists -> {
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

                                            // Confirmation stage
                                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Ok, thank you. Let me show you a quick summary of your guild.");
                                            player.sendMessage("");

                                            player.sendMessage(ChatColor.GRAY + "              *** " + ChatColor.DARK_AQUA + ChatColor.BOLD + "Guild Creation Confirmation" + ChatColor.GRAY + " ***");
                                            player.sendMessage(ChatColor.GRAY + "Guild Name: " + ChatColor.WHITE + guildDisplayName);
                                            player.sendMessage(ChatColor.GRAY + "Guild Tag: " + ChatColor.DARK_AQUA + "[" + ChatColor.GRAY + tag + ChatColor.DARK_AQUA + "]");
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


                                                    // Registers guild in database
                                                    GuildDatabaseAPI.get().createGuild(guildDisplayName, tag, player.getUniqueId(), onComplete -> {
                                                        if (!onComplete) {
                                                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.RED + "We have an error. Failed to create guild in database. Please try again later");
                                                            return;
                                                        }

                                                        player.sendMessage("");
                                                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Congratulations, you are now the proud owner of the '" + guildDisplayName + "' guild!");
                                                        player.sendMessage(ChatColor.GRAY + "You can now chat in your guild chat with " + ChatColor.BOLD + "/g <msg>" + ChatColor.GRAY + ", invite players with " + ChatColor.BOLD + "/ginvite <player>" + ChatColor.GRAY + " and much more -- Check out your character journal for more information!");
                                                        BankMechanics.getInstance().takeGemsFromInventory(5000, player);
                                                    });

                                                }

                                            }, null), 1L);


                                        } else
                                            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild tag already exists. Please choose a different guild tag.");

                                    });


                                }, null), 1L);
                    } else
                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild with the name '" + ChatColor.GRAY + guildDisplayName + ChatColor.WHITE + "' already exists. Please choose a different name.");
                });


            }, null), 1L);


        }, null);

    }


    private boolean checkForProfanity(String text) {
        for (String s : Chat.bannedWords)
            if (text.equalsIgnoreCase(s) || s.contains(text))
                return true;
        return false;
    }
}
