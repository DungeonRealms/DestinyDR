package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.player.inventory.menus.guis.PlayerProfileGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemPlayerProfile extends FunctionalItem implements ItemInventoryListener {

    private Player player;

    public ItemPlayerProfile(ItemStack item) {
        this((Player) null);
    }

    public ItemPlayerProfile(Player player) {
        super(ItemType.OPEN_PROFILE);
        setUndroppable(true);
        this.player = player;
    }

    @Override
    public void updateItem() {
        getItem().setDurability((short) 3);
        if (this.player != null)
            ((SkullMeta) getMeta()).setOwner(this.player.getName());
        super.updateItem();
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.WHITE + "" + ChatColor.BOLD + "Character Profile";
    }

    @Override
    protected String[] getLore() {
        return new String[]{ChatColor.GREEN + "Open Profile"};
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        evt.setCancelled(true);
        //Delay that open like 1 click..
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> new PlayerProfileGUI(evt.getPlayer(), null).open(evt.getPlayer(), evt.getEvent().getAction()), 1);
    }


    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.INVENTORY_PICKUP_ITEM, ItemUsage.INVENTORY_PLACE_ITEM, ItemUsage.INVENTORY_SWAP_PICKUP, ItemUsage.INVENTORY_SWAP_PLACE};
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.SKULL_ITEM);
    }
}
