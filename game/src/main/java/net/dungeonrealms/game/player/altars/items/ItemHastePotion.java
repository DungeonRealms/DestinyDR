package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class ItemHastePotion extends ItemEffectPotion {

    public ItemHastePotion(int time, int weight) {
        super(PotionEffectType.FAST_DIGGING, time, weight, ItemType.ITEM_HASTE_POTION);
    }

    public ItemHastePotion(ItemStack stack) {
        super(stack);
    }

    @Override
    protected String getDisplayName() {
        return "Haste Potion";
    }

    @Override
    protected String[] getLore() {
        return new String[] {"", ChatColor.GRAY.toString() +  "Haste " + weight + " (" + time + "s)"};
    }
}
