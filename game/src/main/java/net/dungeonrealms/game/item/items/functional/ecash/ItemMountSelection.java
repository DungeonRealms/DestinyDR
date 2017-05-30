package net.dungeonrealms.game.item.items.functional.ecash;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.player.inventory.menus.guis.MountSelectionGUI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemMountSelection extends FunctionalItem implements ItemInventoryEvent.ItemInventoryListener {

    public ItemMountSelection() {
        super(ItemType.MOUNT_SELECTION);
        setUndroppable(true);
    }

    public ItemMountSelection(ItemStack item) {
    	super(item);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Mount Menu";
    }

    @Override
    protected String[] getLore() {
        return new String[]{ChatColor.GRAY + "Click to view available mounts!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INVENTORY_PICKUP;
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.SADDLE, 1);
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        evt.setCancelled(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> new MountSelectionGUI(evt.getPlayer(), null).open(evt.getPlayer(), evt.getEvent().getAction()), 1);
    }
}