package net.dungeonrealms.game.command.moderation;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.loot.LootSpawner;
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

public class CommandLootChest extends BaseCommand {

    public static Map<Location, Hologram> shownLootChests = new ConcurrentHashMap<>();

    public CommandLootChest() {
        super("lootchest", "/<command>", "Loot Chest command");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player pl = (Player) sender;


        if (!Rank.isGM(pl)) return true;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("edit")) {
                if (pl.hasMetadata("lootchestedit")) {
                    pl.removeMetadata("lootchestedit", DungeonRealms.getInstance());
                    pl.sendMessage(ChatColor.RED + "Loot Chest Edit disabled!");
                } else {
                    pl.setMetadata("lootchestedit", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
                    pl.sendMessage(ChatColor.RED + "Loot Chest Edit enabled!");
                }
                return true;
            }
        } else if (args.length == 2) {
            int radius = args[1].equalsIgnoreCase("all") ? -1 : StringUtils.isNumeric(args[1]) ? Integer.parseInt(args[1]) : 10;

            if (args[0].equalsIgnoreCase("show")) {
                int shown = 0;
                for (LootSpawner spawner : LootManager.LOOT_SPAWNERS) {
                    if (spawner.getLocation().getWorld().equals(pl.getWorld()) && spawner.getLocation().distanceSquared(pl.getLocation()) <= radius * radius) {
                        if (shownLootChests.containsKey(spawner.getLocation())) continue;

                        spawner.getBlock().setType(Material.CHEST);
                        spawner.setContents();
                        createHologram(spawner);
                        shown++;
                    }
                }
                pl.sendMessage("Showing " + shown + " Loot Chests.");
                return true;
            } else if (args[0].equalsIgnoreCase("hide")) {
                shownLootChests.forEach((loc, holo) -> {
                    if (radius == -1 || (loc.getWorld().equals(pl.getWorld()) && loc.distanceSquared(pl.getLocation()) <= radius * radius)) {
                        shownLootChests.remove(loc);
                        holo.delete();
                    }

                });
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "/lootchest edit - Enter Loot chest edit mode");
        sender.sendMessage(ChatColor.RED + "/lootchest show/hide <radius/all>");
        return false;
    }

    public static void removeHologram(Location location) {
        Hologram found = shownLootChests.get(location);
        if (found != null) {
            found.delete();
        }
    }

    public static void createHologram(LootSpawner spawner) {
        Hologram holo = HologramsAPI.createHologram(DungeonRealms.getInstance(), spawner.getLocation().clone().add(.5, 2.5, 0.5));
        holo.appendTextLine(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Loot Chest");
        holo.appendTextLine(ChatColor.YELLOW + "Loot Type: " + spawner.getLootType().fileName);
        holo.appendTextLine(ChatColor.YELLOW + "Respawn Timer: " + (spawner.getDelay() - 1200) + "s");
        shownLootChests.put(spawner.getLocation(), holo);
    }
}
