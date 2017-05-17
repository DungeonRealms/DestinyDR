package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.FunctionalItemEvent;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * A basic class for items that behave a reactionary behavior.
 *
 * @author Kneesnap
 */
public abstract class FunctionalItem extends ItemGeneric {

    // Some basic predefined "event packs".
    protected static final ItemUsage[] INTERACT = {ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY,
            ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_ENTITY};
    protected static final ItemUsage[] INTERACT_LEFT_CLICK = {ItemUsage.LEFT_CLICK_AIR, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.LEFT_CLICK_ENTITY};
    protected static final ItemUsage[] INTERACT_RIGHT_CLICK = {ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_ENTITY};
    protected static final ItemUsage[] INVENTORY_PICKUP = {ItemUsage.INVENTORY_PICKUP_ITEM, ItemUsage.INVENTORY_SWAP_PICKUP};
    protected static final ItemUsage[] INVENTORY_PLACE = {ItemUsage.INVENTORY_PLACE_ITEM, ItemUsage.INVENTORY_SWAP_PLACE};

    public FunctionalItem(ItemStack item) {
        super(item);
        setAntiDupe(true);
    }

    public FunctionalItem(ItemType type) {
        super(type);
        setAntiDupe(true);
    }

    @Override
    public void updateItem() {
        String[] lore = getLore();
        if (lore != null)
            for (String line : lore)
                addLore(line);

        //We have a custom renamed item, dont overwrite.
        if (customDisplayName != null)
            getMeta().setDisplayName(customDisplayName);
        else
            getMeta().setDisplayName(getDisplayName());

        super.updateItem();
    }

    /**
     * Handles a FunctionalItemEvent related to this item.
     */
    public static void attemptUseItem(FunctionalItemEvent ice) {
        if (ice.getVanillaItem() == null || ice.getVanillaItem().getType() == Material.AIR)
            return;

        if(ice.getPlayer().hasMetadata("sharding"))return;

        //Check that the FunctionalItem is not null, and that the usagetype supplied is allowed for this item.
        if (ice.getItem() == null || !Arrays.asList(ice.getItem().getUsage()).contains(ice.getUsage())) {
            return;
        }

        try {
            ice.handle();
        } catch (Exception e) {
            e.printStackTrace();
            GameAPI.sendIngameDevMessage(ChatColor.RED + "Error using " + ice.getPlayer().getName() + "'s " + ice.getItem().getClass().getSimpleName());
            ice.getPlayer().sendMessage(ChatColor.RED + "There was an error while using this item. The developers have been notified.");
        }
    }

    /**
     * Used to skip trying out new ItemUsage[] {}
     */
    @SuppressWarnings("unchecked")
    protected <T> T[] arr(T... a) {
        return a;
    }

    /**
     * Gets the display name for this item.
     */
    protected abstract String getDisplayName();

    /**
     * Gets the lore to set for this item, if any.
     */
    protected abstract String[] getLore();

    /**
     * Returns the ways to use this item that DR is listening for.
     */
    protected abstract ItemUsage[] getUsage();
}
