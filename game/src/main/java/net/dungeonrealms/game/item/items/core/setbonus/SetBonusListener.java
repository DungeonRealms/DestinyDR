package net.dungeonrealms.game.item.items.core.setbonus;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.miscellaneous.NBTWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SetBonusListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorEquip(ArmorEquipEvent e) {
        SetBonuses active = getBonus(e.getPlayer());
        SetBonuses bonusNow = calculateSetBonus(e.getPlayer(), e.getOldArmorPiece(), e.getNewArmorPiece());

        if (bonusNow != null) {
            if (active != null) {
                active.getSetBonus().onSetBonusDeactivate(e.getPlayer());
            }
            bonusNow.getSetBonus().onSetBonusActivate(e.getPlayer());
            SetBonus.activeSetBonuses.put(e.getPlayer(), bonusNow);
        } else if (active != null) {
            active.getSetBonus().onSetBonusDeactivate(e.getPlayer());
            SetBonus.activeSetBonuses.remove(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        SetBonuses bonusNow = calculateSetBonus(event.getPlayer(), null, null);
        SetBonus.activeSetBonuses.put(event.getPlayer(), bonusNow);
        if (bonusNow != null) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                //Still same bonus..
                if (event.getPlayer().isOnline() && bonusNow.equals(calculateSetBonus(event.getPlayer(), null, null))) {
                    bonusNow.getSetBonus().onSetBonusActivate(event.getPlayer());
                } else {
                    SetBonus.activeSetBonuses.remove(event.getPlayer());
                }
            }, 10);
        }
    }

    public SetBonuses calculateSetBonus(Player player, ItemStack oldItem, ItemStack newItem) {
        String customID = null;
        int idCount = 0;

        List<ItemStack> armors = Lists.newArrayList(player.getInventory().getArmorContents());
        for (int i = 0; i < armors.size(); i++) {
            ItemStack is = armors.get(i);
            if (is != null && oldItem != null && is.equals(oldItem))
                armors.set(i, newItem);
        }

        if (newItem != null && !armors.contains(newItem))
            armors.add(newItem);

        for (ItemStack armor : armors) {
            if (armor != null && armor.getType() != Material.AIR) {
                NBTWrapper wrapper = new NBTWrapper(armor);

                String id = wrapper.getString("customId");
                if (id != null && !id.isEmpty()) {
                    if (customID != null && id.equals(customID)) {
                        idCount++;
                        continue;
                    } else {
                        idCount = 1;
                    }
                    customID = id;
                }
            }
        }

        if (idCount == 4 && customID != null) {
            return SetBonuses.getFromCustomID(customID);
        }
        return null;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        SetBonuses bonus = SetBonus.getSetBonus(event.getPlayer());
        if (bonus != null && (bonus == SetBonuses.CARROT_FARMER || bonus == SetBonuses.WHEAT_FARMER)) {
            if (event.getItem() != null) {
                Material m = event.getItem().getType();
                if (bonus == SetBonuses.CARROT_FARMER && (m == Material.CARROT || m == Material.POTATO_ITEM || m == Material.BAKED_POTATO || m == Material.APPLE)) {
                    event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() + 4);
                } else if (bonus == SetBonuses.WHEAT_FARMER && m == Material.BREAD) {
                    event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() + 4);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        SetBonuses bonus = SetBonus.activeSetBonuses.remove(event.getPlayer());
        if (bonus != null) {
            bonus.getSetBonus().onSetBonusDeactivate(event.getPlayer());
        }
    }

    public SetBonuses getBonus(Player player) {
        return SetBonus.activeSetBonuses.get(player);
    }
}
