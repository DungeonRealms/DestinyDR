package net.dungeonrealms.game.command.moderation;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.game.profession.Fishing;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CommandFishing extends BaseCommand {


    public static Map<Location, Hologram> shownFishingSpots = new ConcurrentHashMap<>();

    public CommandFishing() {
        super("fishing", "/<command>", "Fishing command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!Rank.isGM(player)) {
            return true;
        }

        ///fishing show <radius>

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("show")) {

                int radius = StringUtils.isNumeric(args[1]) ? Integer.parseInt(args[1]) : 10;

                AtomicInteger shown = new AtomicInteger(0);
                Fishing.getLocations().forEach((loc, tier) -> {

                    if (loc.distanceSquared(player.getLocation()) <= radius * radius) {
                        //SHOOWWWW
                        if (shownFishingSpots.containsKey(loc)) return;

                        createHologram(loc, tier);

                        loc.getBlock().setType(Material.WATER_LILY);
                        shown.incrementAndGet();
                    }
                });

                player.sendMessage(ChatColor.RED + "Showing " + shown.get() + " Nearby Fishing Spots...");
                return true;
            } else if (args[0].equalsIgnoreCase("hide")) {
                int radius = StringUtils.isNumeric(args[1]) ? Integer.parseInt(args[1]) : args[1].equalsIgnoreCase("all") ? -1 : 10;
                AtomicInteger hid = new AtomicInteger(0);

                shownFishingSpots.forEach((loc, holo) -> {
                    if (radius == -1 || loc.getWorld().equals(player.getWorld()) && loc.distanceSquared(player.getLocation()) <= radius * radius) {
                        holo.delete();
                        shownFishingSpots.remove(loc);
                        hid.incrementAndGet();
                        loc.getBlock().setType(Material.AIR);
                    }
                });

                player.sendMessage(ChatColor.RED + "Hiding " + hid.get() + " Shown Fishing Spots..");
                return true;
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (player.hasMetadata("fishEdit")) {
                    player.removeMetadata("fishEdit", DungeonRealms.getInstance());
                    player.sendMessage(ChatColor.RED + "No longer editting fishing spots.");
                    return true;
                } else {
                    player.setMetadata("fishEdit", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
                    player.sendMessage(ChatColor.RED + "You are now editting fishing spots.");
                    player.sendMessage(ChatColor.GREEN + "Place a lilypad down to create a new fishing spot.");
                    return true;
                }
            }
        }

        sender.sendMessage(ChatColor.RED + "/fishing show/hide <radius/all>");
        sender.sendMessage(ChatColor.RED + "/fishing edit - Allows editting of fishing spots.");
        return true;
    }

    public static void removeHologram(Location location) {
        Hologram found = shownFishingSpots.remove(location);
        if (found != null) {
            found.delete();
        }
    }

    public static void createHologram(Location location, int tier) {
        Hologram holo = HologramsAPI.createHologram(DungeonRealms.getInstance(), location.clone().add(0, 1.5, 0));
        holo.appendTextLine(ChatColor.GREEN.toString() + ChatColor.BOLD + "Fishing Spot");
        holo.appendTextLine(ChatColor.GREEN + "Tier: " + tier);

        shownFishingSpots.put(location, holo);
    }
}
