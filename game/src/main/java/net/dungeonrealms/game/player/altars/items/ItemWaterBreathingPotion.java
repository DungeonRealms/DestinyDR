package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class ItemWaterBreathingPotion extends ItemEffectPotion {

    public ItemWaterBreathingPotion(int time, int weight) {
        super(PotionEffectType.WATER_BREATHING, time, weight, ItemType.ITEM_WATER_BREATHING_POTION);
    }

    public ItemWaterBreathingPotion(ItemStack stack) {
        super(stack);
    }

    @Override
    protected String getDisplayName() {
        return "Water Breathing Potion";
    }

    @Override
    protected String[] getLore() {
        return new String[] {"", ChatColor.GRAY.toString() +  "Water Breathing " + weight + " (" + time + "s)"};
    }
}
