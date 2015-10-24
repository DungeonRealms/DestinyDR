package net.dungeonrealms.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.teleportation.TeleportAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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
            commandSender.sendMessage(ChatColor.RED + "You are not OP/Console!");
            return false;
        }
        if (args.length > 0) {
            switch (args[0]) {
                case "hearthstone":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage(ChatColor.RED + "This player is not online!");
                            return false;
                        }
                        String locationName = args[2];
                        if (TeleportAPI.getLocationFromString(locationName) == null) {
                            commandSender.sendMessage(ChatColor.RED + "This location is not correct!");
                            return false;
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.hearthstone", locationName, true);
                        player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Your HearthStone location has been set to " + ChatColor.AQUA + locationName.toUpperCase() + ChatColor.YELLOW + "!");
                        break;
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Wrong arguments. (E.g. /Essentials hearthstone Proxying Cyrennica)");
                        return false;
                    }
                case "pet":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage(ChatColor.RED + "This player is not online!");
                            return false;
                        }
                        String petType = args[2];
                        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, player.getUniqueId());
                        String petName;
                        String particleType = "";
                        if (!petType.contains("-")) {
                            petName = petType;
                        } else {
                            petName = petType.split("-")[0];
                            particleType = petType.split("-")[1];
                        }
                        if (!API.isStringPet(petName)) {
                            commandSender.sendMessage(ChatColor.RED + "This pet is not a real pet!");
                            return false;
                        }
                        if (!particleType.equals("")) {
                            if (!API.isStringTrail(particleType)) {
                                commandSender.sendMessage(ChatColor.RED + "This pet cannot have that trail!");
                                return false;
                            }
                        }
                        if (!playerPets.isEmpty()) {
                            if (playerPets.contains(petType.toUpperCase())) {
                                commandSender.sendMessage(ChatColor.RED + "The player already has this pet!");
                                return false;
                            }
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, "collectibles.pets", petType.toUpperCase(), true);
                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You have received the " + ChatColor.GREEN + petType.toUpperCase() + ChatColor.AQUA + " pet!");
                        break;
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Wrong arguments. (E.g. /Essentials pet Proxying snowman)");
                        return false;
                    }
                case "mount":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage(ChatColor.RED + "This player is not online!");
                            return false;
                        }
                        String mountType = args[2];
                        if (!API.isStringMount(mountType)) {
                            commandSender.sendMessage(ChatColor.RED + "This mount is not a real mount!");
                            return false;
                        }
                        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                        if (!playerMounts.isEmpty()) {
                            if (playerMounts.contains(mountType.toUpperCase())) {
                                commandSender.sendMessage(ChatColor.RED + "The player already has this mount!");
                                return false;
                            }
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, "collectibles.mounts", mountType.toUpperCase(), true);
                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You have received the " + ChatColor.GREEN + mountType.toUpperCase() + ChatColor.AQUA + " mount!");
                        break;
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Wrong arguments. (E.g. /Essentials mount Proxying skeletonhorse)");
                        return false;
                    }
                case "playertrail":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage(ChatColor.RED + "This player is not online!");
                            return false;
                        }
                        String trailType = args[2];
                        if (!API.isStringTrail(trailType)) {
                            commandSender.sendMessage(ChatColor.RED + "This is not a real trail!");
                            return false;
                        }
                        List<String> playerTrails = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, player.getUniqueId());
                        if (!playerTrails.isEmpty()) {
                            if (playerTrails.contains(trailType.toUpperCase())) {
                                commandSender.sendMessage(ChatColor.RED + "The player already has this trail!");
                                return false;
                            }
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, "collectibles.particles", trailType.toUpperCase(), true);
                        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You have received the " + ChatColor.GREEN + trailType.toUpperCase() + ChatColor.AQUA + " player trail!");
                        break;
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Wrong arguments. (E.g. /Essentials playertrail Proxying flame)");
                        return false;
                    }
                case "ecash":
                    if (args.length == 4) {
                        int amount = Math.abs(Integer.parseInt(args[3]));
                        Player player = Bukkit.getPlayer(args[2]);
                        if (player == null) {
                            commandSender.sendMessage(ChatColor.RED + "This player is not online!");
                            return false;
                        }
                        int previousAmount = (int) DatabaseAPI.getInstance().getData(EnumData.ECASH, player.getUniqueId());
                        switch (args[1]) {
                            case "add":
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, "info.ecash", amount, true);
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You have received " + ChatColor.YELLOW + amount + ChatColor.AQUA + " E-Cash! Your new balance is " + ChatColor.YELLOW + (previousAmount + amount) + ChatColor.AQUA + "!");
                                break;
                            case "take":
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$INC, "info.ecash", (amount * -1), true);
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "] " + ChatColor.YELLOW + amount + ChatColor.AQUA + " E-Cash has been taken from you! Your new balance is " + ChatColor.YELLOW + (previousAmount - amount) + ChatColor.AQUA + "!");
                                break;
                            case "set":
                                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.ecash", amount, true);
                                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GOLD.toString() + ChatColor.BOLD + "DONATE" + ChatColor.WHITE + "]" + ChatColor.AQUA + " Your E-Cash balance has been set to " + ChatColor.YELLOW + amount + ChatColor.AQUA + "!");
                                break;
                            default:
                                commandSender.sendMessage(ChatColor.RED + "Wrong arguments. (E.g. /Essentials ecash add Proxying 100)");
                                break;
                        }
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Wrong arguments. (E.g. /Essentials ecash add Proxying 100)");
                        return false;
                    }
                default:
                    break;
            }
        }
        return true;
    }
}