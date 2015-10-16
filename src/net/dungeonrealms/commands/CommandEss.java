package net.dungeonrealms.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.teleportation.TeleportAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kieran on 10/9/2015.
 */
public class CommandEss implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!commandSender.isOp() && !(commandSender instanceof ConsoleCommandSender)) {
            commandSender.sendMessage("You are not OP/Console!");
            return false;
        }
        if (args.length > 0) {
            switch (args[0]) {
                case "hearthstone":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage("This player is not online!");
                            return false;
                        }
                        String locationName = args[2];
                        if (TeleportAPI.getLocationFromString(locationName) == null) {
                            commandSender.sendMessage("This location is not correct!");
                            return false;
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, "info.hearthstone", locationName, true);
                        player.sendMessage("Your HearthStone location has been set to " + locationName.toUpperCase() + "!");
                        break;
                    } else {
                        commandSender.sendMessage("Wrong arguments. (E.g. /Essentials hearthstone Proxying Cyrennica)");
                        return false;
                    }
                case "pet":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage("This player is not online!");
                            return false;
                        }
                        String petType = args[2];
                        if (!API.isStringPet(petType)) {
                            commandSender.sendMessage("This pet is not a real pet!");
                            return false;
                        }
                        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, player.getUniqueId());
                        if (playerPets.contains(petType.toUpperCase())) {
                            commandSender.sendMessage("The player already has this pet!");
                            return false;
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, "collectibles.pets", petType.toUpperCase(), true);
                        player.sendMessage("You have recieved the " + petType + " pet!");
                        break;
                    } else {
                        commandSender.sendMessage("Wrong arguments. (E.g. /Essentials pet Proxying snowman)");
                        return false;
                    }
                case "mount":
                    if (args.length == 3) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            commandSender.sendMessage("This player is not online!");
                            return false;
                        }
                        String mountType = args[2];
                        if (!API.isStringMount(mountType)) {
                            commandSender.sendMessage("This pet is not a real mount!");
                            return false;
                        }
                        List<String> playerMounts = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.MOUNTS, player.getUniqueId());
                        if (playerMounts.contains(mountType.toUpperCase())) {
                            commandSender.sendMessage("The player already has this mount!");
                            return false;
                        }
                        DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, "collectibles.mounts", mountType.toUpperCase(), true);
                        player.sendMessage("You have recieved the " + mountType + " mount!");
                        break;
                    } else {
                        commandSender.sendMessage("Wrong arguments. (E.g. /Essentials mount Proxying skeletonhorse)");
                        return false;
                    }
                default:
                    break;
            }
        }
        return true;
    }
}