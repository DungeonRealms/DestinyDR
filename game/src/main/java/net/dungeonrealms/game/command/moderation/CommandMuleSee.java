package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
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

            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED + "No UUID found in our database with that name..");
                    return;
                }

                PlayerWrapper.getPlayerWrapper(uuid, false, false, (wrapper) -> {
                    if (wrapper.isPlaying()) {
                        //Dont let them invsee..
                        sender.sendMessage(ChatColor.RED + playerName + " is currently on shard " + wrapper.getFormattedShardName() + ", Please /mulesee on that shard to avoid concurrent modification.");
                        return;
                    }

                    if (wrapper.getPendingMuleInventory() == null) {
                        sender.sendMessage(ChatColor.RED + "No mule inventory found for " + playerName + "!");
                        return;
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        offlineMuleSee.put(sender.getUniqueId(), uuid);
                        sender.openInventory(wrapper.getPendingMuleInventory());
                    });
                });
            });
        }
        return false;
    }
}
