package net.dungeonrealms.game.guild;

import net.dungeonrealms.game.guild.db.GuildDatabase;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.chat.Chat;
import net.md_5.bungee.api.ChatColor;
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
        return EnumPriority.ARCHBISHOPS;
    }


    @Override
    public void startInitialization() {


    }

    @Override
    public void stopInvocation() {

    }


    private boolean checkForBadWords(String text) {
        for (String s : Chat.bannedWords)
            if (text.equalsIgnoreCase(s) || text.contains(s))
                return true;
        return false;
    }



    public void GuildCreation(Player player) {
        Chat.listenForMessage(player, guildCreation -> {

            if (!guildCreation.getMessage().equalsIgnoreCase("y")) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Goodbye!");
                return;
            }


            if (GuildDatabase.getInstance().isGuildNull(player.getUniqueId())) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You are already part of a guild. You'll need to /gquit before creating another.");
                return;
            }


            if (((Integer) DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId())) < 5000) {
                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "You do not have enough GEM(s) -- 5,000, to create a guild.");
                return;
            }


            player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Ok, please enter your " + ChatColor.UNDERLINE + "formal guild name" + ChatColor.WHITE + ", this should be your FULL GUILD NAME, you will enter a shorter 'handle' later.");
            player.sendMessage(ChatColor.GRAY + "You may type 'cancel' at any time to stop this guild creation.");

            Chat.listenForMessage(player, guildNameRequest -> {
                String guildName = guildNameRequest.getMessage().replace(" ", "").toLowerCase();

                if (guildName.length() > 16) {
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name exceeds the maximum length of 16 characters.");
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "You were " + (guildName.length() - 16) + " characters over the limit.");
                    return;
                }

                Pattern pattern = Pattern.compile("^([A-Za-z]|[0-9])+$");
                Matcher matcher = pattern.matcher(guildName);

                if (!matcher.find()) {
                    player.sendMessage(ChatColor.RED + "Guild Registrar: You guild name can only contain alphanumerical values.");
                    return;
                }

                if (checkForBadWords(guildName)) {
                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name has an illegal/censored word in it. Please enter an alternative name.");
                    return;
                }

                GuildDatabase.getInstance().doesGuildNameExist(guildName, guildExists -> {
                    if (!guildExists) {
                        Chat.listenForMessage(player, guildClanTagRequest -> {

                            String clanTag = guildClanTagRequest.getMessage().replace(" ", "").toUpperCase();

                            if (clanTag.length() > 3) {
                                player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Your guild name exceeds the maximum length of 3 characters.");
                                return;
                            }


                            GuildDatabase.getInstance().doesClanTagExist(clanTag, guildTagExists -> {

                                if (!guildTagExists) {
                                    GuildDatabase.getInstance().createGuild(guildName, clanTag, player.getUniqueId(), onComplete -> {
                                        if (!onComplete) return;

                                        player.sendMessage("");
                                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "Congratulations, you are now the proud owner of the '" + guildName + "' guild!");
                                        player.sendMessage(ChatColor.GRAY + "You can now chat in your guild chat with " + ChatColor.BOLD + "/g <msg>" + ChatColor.GRAY + ", invite players with " + ChatColor.BOLD + "/ginvite <player>" + ChatColor.GRAY + " and much more -- Check out your character journal for more information!");

                                    });

                                } else
                                    player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild tag already exists. Please choose a different guild tag.");

                            });


                        }, player2 -> {


                        });
                    } else
                        player.sendMessage(ChatColor.GRAY + "Guild Registrar: " + ChatColor.WHITE + "I'm sorry, but a guild with the name '" + ChatColor.GRAY + guildName + ChatColor.WHITE + "' already exists. Please choose a different name.");
                });


            }, player1 -> {
            });

        }, cancelGuildCreation -> {

        });

    }

    public void doLogin(Player player) {


    }


}
