package net.dungeonrealms.game.command;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.rifts.RiftMechanics;
import net.dungeonrealms.game.mechanic.rifts.WorldRift;
import net.dungeonrealms.game.world.item.Item;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandRifts extends BaseCommand {
    public CommandRifts() {
        super("rifts", "/<command>", "Rift command descr", null, Lists.newArrayList("rift"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (!Rank.isHeadGM((Player) sender)) return true;
        }

// /rift add <tier> <nearbyCity>
        if (args.length == 3 || args.length == 4) {
            Player player = (Player) sender;
            if (args[0].equalsIgnoreCase("add")) {
                if (StringUtils.isNumeric(args[1])) {
                    int tier = Integer.parseInt(args[1]);
                    String city = args[2];

                    Item.ElementalAttribute attr = args.length == 4 ? Item.ElementalAttribute.getByName(args[3]) : null;
                    WorldRift rift = new WorldRift(player.getLocation(), tier, attr, city);

                    RiftMechanics.getInstance().getWorldRiftLocations().add(rift);
                    RiftMechanics.getInstance().saveRifts();
                    sender.sendMessage(ChatColor.RED + "Rift Registered at " + Utils.getStringFromLocation(player.getLocation(), true) + " For tier " + tier + " near " + city);
                    return true;
                }
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                //list?
                sender.sendMessage(ChatColor.RED + "Listing all rifts...");
                List<WorldRift> rifts = RiftMechanics.getInstance().getWorldRiftLocations();
                for (int i = 0; i < rifts.size(); i++) {
                    WorldRift rift = rifts.get(i);
                    sender.sendMessage(ChatColor.RED.toString() + i + ". " + Utils.getStringFromLocation(rift.getLocation(), true) + "  Tier: " + rift.getTier());
                }

                WorldRift active = RiftMechanics.getInstance().getActiveRift();
                if (active != null) {
                    sender.sendMessage(ChatColor.RED + "Currently Active Rift: " + Utils.getStringFromLocation(active.getLocation(), true) + " Spawned: " + active.getSpawnedEntities().size());
                }
                return true;
            } else if (args[0].equalsIgnoreCase("summon")) {
                sender.sendMessage(ChatColor.RED + "Summoning Rift..");
                RiftMechanics.getInstance().spawnRift();
                return true;
            }
        } else if (args.length == 2) {
            //rift remove <index>
            if (args[0].equalsIgnoreCase("remove")) {
                int index = Integer.parseInt(args[1]);
                WorldRift found = RiftMechanics.getInstance().getWorldRiftLocations().remove(index);
                if (found != null) {
                    sender.sendMessage(ChatColor.RED + "Rift removed at " + Utils.getStringFromLocation(found.getLocation(), false));
                } else {
                    sender.sendMessage(ChatColor.RED + "No rift found with that index!");
                }

                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Rift Commands");
        sender.sendMessage(ChatColor.RED + "/rift add <tier> <Nearby_City_Name> [elemental]");
        sender.sendMessage(ChatColor.RED + "/rift list");
        sender.sendMessage(ChatColor.RED + "/rift remove <index>");

        return false;
    }
}
