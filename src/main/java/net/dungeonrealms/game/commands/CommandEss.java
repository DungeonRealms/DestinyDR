package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
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
public class CommandEss extends BasicCommand {

    public CommandEss(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.isOp() && !(commandSender instanceof ConsoleCommandSender)) {
            return false;
        }
        
        if(commandSender instanceof Player){
        	Player player = (Player)commandSender;
            if (!Rank.isDev(player)) {
                return false;
            }
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
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.HEARTHSTONE, locationName, true);
                            commandSender.sendMessage(ChatColor.GREEN + "Successfully set the hearthstone of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + locationFriendly + ChatColor.GREEN + ".");
                            API.updatePlayerData(uuid);
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

                            if (!API.isStringPet(petName)) {
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
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.PETS, petType.toUpperCase(), false);
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ACTIVE_PET, petType.toUpperCase(), true);
                            commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + petNameFriendly + ChatColor.GREEN + " pet to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                            API.updatePlayerData(uuid);
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
                            if (!API.isStringMount(mountType)) {
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
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.MOUNTS, mountType.toUpperCase(), false);
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ACTIVE_MOUNT, mountType.toUpperCase(), true);
                            commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + mountFriendly + ChatColor.GREEN + " mount to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                            API.updatePlayerData(uuid);
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

                            if (!API.isStringTrail(trailType)) {
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
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$PUSH, EnumData.PARTICLES, trailType.toUpperCase(), false);
                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ACTIVE_TRAIL, trailType.toUpperCase(), true);
                            commandSender.sendMessage(ChatColor.GREEN + "Successfully added the " + ChatColor.BOLD + ChatColor.UNDERLINE + trailFriendly + ChatColor.GREEN + " trail to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                            API.updatePlayerData(uuid);
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
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.ECASH, amount, true);
                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " E-Cash to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                    break;
                                case "set":
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.ECASH, amount, true);
                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully set the E-Cash of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + ".");
                                    break;
                                case "remove":
                                    DatabaseAPI.getInstance().update(uuid, EnumOperators.$INC, EnumData.ECASH, (amount * -1), true);
                                    commandSender.sendMessage(ChatColor.GREEN + "Successfully removed " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " E-Cash from " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                                    break;
                                default:
                                    commandSender.sendMessage(ChatColor.RED + "Invalid modification type, please use: ADD | SET | REMOVE");
                                    return false;
                            }
                            API.updatePlayerData(uuid);
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
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, rankName, true);
                                DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK_SUB_EXPIRATION, subscriptionLength, true);
                                API.updatePlayerData(uuid);
                                commandSender.sendMessage(ChatColor.GREEN + "Successfully updated the subscription of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
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
                                            DatabaseAPI.getInstance().update(uuid, EnumOperators.$SET, EnumData.RANK, rankName, true);
                                            if (Bukkit.getPlayer(playerName) != null) {
                                                Rank.getInstance().setRank(uuid, rankName);
                                            } else {
                                                API.updatePlayerData(uuid);
                                            }
                                            commandSender.sendMessage(ChatColor.GREEN + "Successfully updated the rank of " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + rankName + ChatColor.GREEN + ".");
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
                    DatabaseAPI.getInstance().update(((Player)commandSender).getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, 1, false);
                    commandSender.sendMessage(ChatColor.GREEN + "Your mule level has been reset.");
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