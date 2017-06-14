package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketItem;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrinketMechanics implements GenericMechanic, Listener {
    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    private Map<UUID, TrinketItem> lastTrinketItem = new HashMap<>();

    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (PlayerWrapper wrapper : PlayerWrapper.getPlayerWrappers().values()) {
                if (wrapper.getPlayer() == null || !wrapper.getPlayer().isOnline()) continue;
                Player player = wrapper.getPlayer();
                TrinketItem item = Trinket.getActiveTrinketItem(player);
                TrinketItem tItem = lastTrinketItem.get(player.getUniqueId());
                boolean needsUpdating = false;
                if (item != null) {
                    if (tItem != null && tItem.equals(item)) //No change
                        continue;

                    player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 5, 1.1F);
                    this.lastTrinketItem.put(player.getUniqueId(), item);
                    player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + (tItem != null ? tItem.getDisplayName() : "Nothing") + " " + ChatColor.GRAY + "-> " + ChatColor.WHITE + item.getDisplayName());
                    needsUpdating = true;
                } else if (tItem != null) {
                    //No more item in this slot?
                    player.playSound(player.getLocation(), Sound.ENTITY_ARMORSTAND_BREAK, 5, 1.6F);
                    player.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + tItem.getDisplayName() + " " + ChatColor.GRAY + "-> " + ChatColor.GRAY + "Nothing");
                    this.lastTrinketItem.remove(player.getUniqueId());
                    needsUpdating = true;
                }

                if (needsUpdating)
                    wrapper.calculateAllAttributes();
                
            }
        }, 20, 20);
    }

    @Override
    public void stopInvocation() {

    }
}
