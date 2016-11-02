package net.dungeonrealms.old.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.old.game.player.chat.GameChat;
import net.dungeonrealms.old.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.vgame.Game;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Kieran on 10/9/2015.
 */
public class CommandEss extends BaseCommand {

    public CommandEss(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.isOp() && !(commandSender instanceof ConsoleCommandSender)) {
            return false;
        }

        if (args.length > 0) {
            switch (args[0]) {
                case "hearthstone":
                    if (args.length == 3) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            String locationName = args[2];
                            String locationFriendly = locationName.toUpperCase().replace("_", " ");
                            if (TeleportAPI.getLocationFromString(locationName) == null) {
                                commandSender.sendMessage(ChatColor.RED + "The hearthstone location " + ChatColor.BOLD + ChatColor.UNDERLINE + locationFriendly + ChatColor.RED + " does not exist.");
                                return false;
                            }
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.HEARTHSTONE, locationName, true, doAfter -> {
                                commandSender.sendMessage(ChatColor.GREEN + "Successfully set the hearthstone of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + locationFriendly + ChatColor.GREEN + ".");
                                GameAPI.updatePlayerData(uuid);
                            });
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr hearthstone <player> <location>");
                        return false;
                    }
                    break;
                case "pet":
                    if (args.length == 3) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            String petType = args[2];
                            List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, uuid);
                            String petName;
                            petName = petType;
                            String petNameFriendly = petName.toUpperCase().replace("_", " ");

                            if (!GameAPI.isStringPet(petName)) {
                                commandSender.sendMessage(ChatColor.RED + "The pet " + ChatColor.BOLD + ChatColor.UNDERLINE + petNameFriendly + ChatColor.RED + " does not exist.");
                                return false;
                            }

                            if (!playerPets.isEmpty()) {
                                for (String pet : playerPets) {
                                    if (pet.contains(petType.toUpperCase())) {
                                        commandSender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + playerName + ChatColor.RED + " already has the " + ChatColor.BOLD + ChatColor.UNDERLINE + petNameFriendly + ChatColor.RED + " pet.");
                                        return false;
                                    }
                                }
                            }
                            GameAPI.submitAsyncCallback(() -> {
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.PETS, petType.toUpperCase(), false);
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ACTIVE_PET, petType.toUpperCase(), false);
                                return true;
                            }, result -> {
                                commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + petNameFriendly + ChatColor.GREEN + " pet to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                GameAPI.updatePlayerData(uuid);
                            });
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr pet <player> <pet>");
                        return false;
                    }
                    break;
                case "mount":
                    if (args.length == 3) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            String mountType = args[2];
                            String mountFriendly = mountType.toUpperCase().replace("_", " ");
                            if (!GameAPI.isStringMount(mountType)) {
                                commandSender.sendMessage(ChatColor.RED + "The mount " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.RED + " does not exist.");
                                return false;
                            }
                            List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, uuid);
                            if (!playerMounts.isEmpty()) {
                                if (playerMounts.contains(mountType.toUpperCase())) {
                                    commandSender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + playerName + ChatColor.RED + " already has the " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.RED + " mount.");
                                    return false;
                                }
                            }
                            GameAPI.submitAsyncCallback(() -> {
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.MOUNTS, mountType.toUpperCase(), false);
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ACTIVE_MOUNT, mountType.toUpperCase(), false);
                                return true;
                            }, result -> {
                                commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.GREEN + " mount to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                GameAPI.updatePlayerData(uuid);
                            });
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }

                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr mount <player> <mount>");
                        return false;
                    }
                    break;
                case "trail":
                case "playertrail":
                    if (args.length == 3) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            String trailType = args[2];
                            String trailFriendly = trailType.toUpperCase().replace("_", " ");

                            if (!GameAPI.isStringTrail(trailType)) {
                                commandSender.sendMessage(ChatColor.RED + "The trail " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.RED + " does not exist.");
                                return false;
                            }

                            List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, uuid);
                            if (!playerTrails.isEmpty()) {
                                if (playerTrails.contains(trailType.toUpperCase())) {
                                    commandSender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + playerName + ChatColor.RED + " already has the " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.RED + " trail.");
                                    return false;
                                }
                            }
                            GameAPI.submitAsyncCallback(() -> {
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.PARTICLES, trailType.toUpperCase(), false);
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ACTIVE_TRAIL, trailType.toUpperCase(), false);
                                    return true;
                            }, result -> {
                                commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.GREEN + " trail to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                GameAPI.updatePlayerData(uuid);
                            });
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr trail <player> <trail>");
                        return false;
                    }
                    break;
                case "ecash":
                    if (args.length == 4) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            int amount = Math.abs(Integer.parseInt(args[3]));

                            switch (args[2]) {
                                case "add":
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.ECASH, amount, true, doAfter -> {
                                        commandSender.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " E-Cash to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                        GameAPI.updatePlayerData(uuid);
                                    });
                                    break;
                                case "set":
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ECASH, amount, true, doAfter -> {
                                        commandSender.sendMessage(ChatColor.GREEN + "Successfully set the E-Cash of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + ".");
                                        GameAPI.updatePlayerData(uuid);
                                    });
                                    break;
                                case "remove":
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.ECASH, (amount * -1), true, doAfter -> {
                                        commandSender.sendMessage(ChatColor.GREEN + "Successfully removed " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " E-Cash from " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                        GameAPI.updatePlayerData(uuid);
                                    });
                                    break;
                                default:
                                    commandSender.sendMessage(ChatColor.RED + "Invalid modification type, please use: ADD | SET | REMOVE");
                                    return false;
                            }
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr ecash <player> <add|set|remove> <amount>");
                        return false;
                    }
                    break;
                case "sub":
                case "subscription":
                    if (args.length == 5) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            String rankName = args[2].toUpperCase();
                            String modifyType = args[3].toLowerCase();
                            String currentRank = Rank.getInstance().getRank(uuid);
                            int days = Integer.parseInt(args[4]) * 86400;
                            int subscriptionLength = Integer.parseInt(DatabaseAPI.getInstance().getData(EnumData.RANK_SUB_EXPIRATION, uuid).toString());

                            if (rankName.equalsIgnoreCase("sub") || rankName.equalsIgnoreCase("sub+")) {
                                if (!currentRank.equalsIgnoreCase("default") && !currentRank.equalsIgnoreCase(rankName) && (rankName.equalsIgnoreCase("sub") || (rankName.equalsIgnoreCase("sub+") && !currentRank.equalsIgnoreCase("sub")))) {
                                    commandSender.sendMessage(ChatColor.RED + "Cannot change the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + ", they're currently " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank.toUpperCase() + ChatColor.RED + "!");
                                    return false;
                                }

                                if (modifyType.equalsIgnoreCase("add") && subscriptionLength > 0) {
                                    subscriptionLength = subscriptionLength + days;
                                } else if (modifyType.equalsIgnoreCase("set") || (modifyType.equalsIgnoreCase("add") && subscriptionLength <= 0)) {
                                    subscriptionLength = (int) (System.currentTimeMillis() / 1000) + days;
                                } else if (modifyType.equalsIgnoreCase("remove")) {
                                    subscriptionLength = subscriptionLength - days;
                                    if (subscriptionLength < 0) subscriptionLength = 0;
                                } else {
                                    commandSender.sendMessage(ChatColor.RED + "Invalid modification type, please use: ADD | SET | REMOVE");
                                    return false;
                                }
                                int finalSubLength = subscriptionLength;
                                GameAPI.submitAsyncCallback(() -> {
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, rankName, false);
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK_SUB_EXPIRATION, finalSubLength, false);
                                    return true;
                                }, result -> {
                                    GameAPI.updatePlayerData(uuid);
                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully updated the subscription of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                });
                            } else {
                                commandSender.sendMessage(ChatColor.RED + "Invalid rank, please use: SUB | SUB+");
                                return false;
                            }
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr subscription <name> <rank> <add|set|remove> <days>");
                        return false;
                    }
                    break;
                case "purchase":
                    if (args.length >= 4) {
                        try {
                            String playerName = args[1];
                            UUID uuid = Bukkit.getPlayer(playerName) != null && Bukkit.getPlayer(playerName).getDisplayName().equalsIgnoreCase(playerName) ? Bukkit.getPlayer(playerName).getUniqueId() : UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName));
                            String type = args[2].toLowerCase();
                            String rankName = args[3].toUpperCase();

                            switch (type) {
                                case "rank":
                                    String currentRank = DatabaseAPI.getInstance().getData(EnumData.RANK, uuid).toString().toUpperCase();
                                    if (currentRank.equals("DEFAULT") || currentRank.startsWith("SUB")) {
                                        if (rankName.equalsIgnoreCase("SUB++")) {
                                            if (Bukkit.getPlayer(playerName) != null) {
                                                Rank.getInstance().setRank(uuid, rankName);
                                            } else {
                                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, rankName, true, doAfter -> {
                                                    GameAPI.updatePlayerData(uuid);
                                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully updated the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + rankName + ChatColor.GREEN + ".");
                                                });
                                            }
                                        } else {
                                            commandSender.sendMessage(ChatColor.RED + "The rank " + ChatColor.BOLD + ChatColor.UNDERLINE + type + ChatColor.RED + " is invalid or unsupported through this command.");
                                            return false;
                                        }
                                    } else {
                                        commandSender.sendMessage(ChatColor.RED + "Failed to update the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.RED + " because they're " + ChatColor.BOLD + ChatColor.UNDERLINE + currentRank + ChatColor.RED + ".");
                                        return false;
                                    }
                                    break;

                                default:
                                    commandSender.sendMessage(ChatColor.RED + "Invalid purchase type: " + type + ".");
                                    return false;
                            }
                        } catch (IllegalArgumentException ex) {
                            commandSender.sendMessage(ChatColor.RED + "I couldn't find the  user " + ChatColor.BOLD + ChatColor.UNDERLINE + args[1] + ChatColor.RED + ", maybe they've not played Dungeon Realms before?");
                            return false;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Invalid usage /dr purchase <player> <type> [args]");
                        return false;
                    }
                    break;
                case "resetmule":
                    DatabaseAPI.getInstance().update(((Player)commandSender).getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, 1, true);
                    commandSender.sendMessage(ChatColor.GREEN + "Your mule level has been reset.");
                    break;
                case "buff":
                    if (args.length != 4) {
                        commandSender.sendMessage(ChatColor.RED + "Syntax: /dr buff <level|loot|profession> <duration in s> <bonusAmount>");
                        break;
                    }
                    String buffType = args[1].toLowerCase();
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        Integer.parseInt(args[2]);
                        //noinspection ResultOfMethodCallIgnored
                        Float.parseFloat(args[3]);
                    }
                    catch (NumberFormatException ex) {
                        commandSender.sendMessage(ChatColor.RED + "Invalid duration or bonus amount! Syntax: /dr buff <level|loot|profession> <duration in s> <bonusAmount>");
                        break;
                    }
                    String duration = args[2];
                    String bonusAmount = args[3];
                    switch (buffType) {
                        case "level":
                            GameAPI.sendNetworkMessage("levelBuff", duration, bonusAmount, commandSender instanceof
                                    Player ? GameChat.getFormattedName((Player) commandSender) : commandSender.getName(),
                                    Game.getGame().getGameShard().getBungeeIdentifier());
                            break;
                        case "loot":
                            GameAPI.sendNetworkMessage("lootBuff", duration, bonusAmount, commandSender instanceof
                                    Player ? GameChat.getFormattedName((Player) commandSender) : commandSender.getName(),
                                    Game.getGame().getGameShard().getBungeeIdentifier());
                            break;
                        case "profession":
                            GameAPI.sendNetworkMessage("professionBuff", duration, bonusAmount, commandSender instanceof
                                    Player ? GameChat.getFormattedName((Player) commandSender) : commandSender.getName(),
                                    Game.getGame().getGameShard().getBungeeIdentifier());
                            break;
                        default:
                            commandSender.sendMessage(ChatColor.RED + "Invalid buff type! Syntax: /dr buff <level|loot|profession> <duration in s> <bonusAmount>");
                            break;
                    }
                    break;
                default:
                    commandSender.sendMessage(ChatColor.RED + "The command " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0].toUpperCase() + ChatColor.RED + " does not exist.");
                    break;
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Invalid usage! /dr <command> [args]");
        }
        return true;
    }
}