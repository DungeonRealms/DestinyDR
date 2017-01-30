package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandMuleSee extends BaseCommand {
    public CommandMuleSee() {
        super("mulesee", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Lists.newArrayList("mls"));
    }

    @Getter
    private static Map<UUID, UUID> offlineMuleSee = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];

        //Inefficient to constantly get .getPlayer as that requires a full iteration of all online players each time.
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {

            Inventory muleInventory = MountUtils.inventories.get(player.getUniqueId());
            if (muleInventory != null) {
                sender.openInventory(muleInventory);
            } else {
                sender.sendMessage(ChatColor.RED + "No mule inventory loaded into memory from " + player.getName());
            }
        } else {

            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {

                //Database things outside.
                String pulledUUID = DatabaseAPI.getInstance().getUUIDFromName(playerName);

                boolean foundUUID = !pulledUUID.equals("");

                UUID p_uuid = foundUUID ? UUID.fromString(pulledUUID) : null;

                boolean isPlaying = foundUUID ? (Boolean) DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, p_uuid) : false;

                String inventoryData = !isPlaying && foundUUID ? (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_MULE, p_uuid) : null;

                int muleLevel = !isPlaying && foundUUID ? Math.min(3, (int) DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, p_uuid)) : 1;
                MuleTier tier = MuleTier.getByTier(muleLevel);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {

                    if (!foundUUID) {
                        sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                        return;
                    }

                    // check if they're logged in on another shard
                    if (isPlaying) {
                        String shard = DatabaseAPI.getInstance().getFormattedShardName(p_uuid);
                        sender.sendMessage(ChatColor.RED + "That player is currently playing on shard " + shard + ". " +
                                "Please /mulesee on that shard to avoid concurrent modification.");
                        return;
                    }


                    Inventory inv;
                    if (inventoryData != null && inventoryData.length() > 0 && (!inventoryData.equalsIgnoreCase("null") &&
                            !inventoryData.equalsIgnoreCase("empty")) && tier != null) {
                        try {
                            inv = ItemSerialization.fromString(inventoryData, tier.getSize());
                        } catch (Exception e) {
                            sender.sendMessage(ChatColor.RED + "Fatal Error trying to decode " + playerName + "'s Mule Inventory, Tier: " + tier);
                            Bukkit.getLogger().info("Encoded Inventory: " + inventoryData);
                            return;
                        }
                    } else if (tier == null) {
                        sender.sendMessage(ChatColor.RED + "Unable to get mule tier with Mule Level: " + muleLevel);
                        return;
                    } else {
                        sender.sendMessage(ChatColor.RED + "That player's mule storage is empty.");
                        return;
                    }

                    offlineMuleSee.put(sender.getUniqueId(), p_uuid);
                    sender.openInventory(inv);
                });
            });
        }
        return false;
    }
}
