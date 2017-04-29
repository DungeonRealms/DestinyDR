package net.dungeonrealms.game.command.gameplay;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.GraveyardMechanic;
import net.dungeonrealms.game.miscellaneous.Graveyard;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGraveyard extends BaseCommand {
    public CommandGraveyard() {
        super("graveyard", "/<command>", "Graveyard command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (Rank.isGM(player)) {
            //Alter graveyard
            GraveyardMechanic grave = GraveyardMechanic.get();

            //Graveyard add <name>
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    String name = args[1];
//                    if(grave.)

                    Graveyard yard = grave.getGraveyard(name);
                    if (yard != null) {
                        sender.sendMessage(ChatColor.RED + "Graveyard already exists for " + name + " at " + Utils.getStringFromLocation(yard.getLocation(), true));
                        return true;
                    }

                    grave.addGraveyard(new Graveyard(name, player.getLocation()));
                    player.sendMessage(ChatColor.RED + name + " Graveyard created at your current location.");
                    return true;
                } else if (args[0].equalsIgnoreCase("remove")) {
                    String name = args[1];

                    Graveyard yard = grave.getGraveyard(name);
                    if (yard == null) {
                        sender.sendMessage(ChatColor.RED + "Graveyard doesnt exists!");
                        return true;
                    }

                    grave.removeGraveyard(yard);
                    sender.sendMessage(ChatColor.RED + "Graveyard removed..");
                    return true;
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    sender.sendMessage("");
                    Graveyard closest = grave.getClosestGraveyard(player.getLocation());
                    if (closest != null) {
                        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Closest Graveyard: " + ChatColor.RED + closest.getName() + " (" + closest.getLocation().distanceSquared(player.getLocation()) + ")");
                    }

                    if (!grave.getGraveyards().isEmpty()) {
                        sender.sendMessage(ChatColor.RED + "Listing " + grave.getGraveyards().size() + " Graveyards: ");
                        for (Graveyard yard : grave.getGraveyards()) {
                            JSONMessage message = new JSONMessage("", ChatColor.GREEN);
                            message.addRunCommand(ChatColor.GREEN + yard.getName() + " - " + Utils.getStringFromLocation(yard.getLocation(), true), ChatColor.GREEN, "/tp " + yard.getLocation().getBlockX() + " " + yard.getLocation().getBlockY() + " " + yard.getLocation().getBlockZ(), "Teleport here.");
                            message.sendToPlayer(player);
                        }

                        sender.sendMessage(ChatColor.RED + "Click a Graveyard location to teleport to it.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "No graveyards found.");
                    }
                }
                return true;
            }

            sender.sendMessage(ChatColor.RED + "/graveyard remove <name> - Remove a given graveyard..");
            sender.sendMessage(ChatColor.RED + "/graveyard add <name> - Create new graveyard with the given name.");
            sender.sendMessage(ChatColor.RED + "/graveyard list - List all graveyards..");

            return true;
        }
        return false;
    }
}
