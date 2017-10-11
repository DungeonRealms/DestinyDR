package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class ItemNightVisionPotion extends ItemEffectPotion {

    public ItemNightVisionPotion(int time, int weight) {
        super(PotionEffectType.NIGHT_VISION, time, weight, ItemType.ITEM_NIGHT_VISION_POTION);
    }

    public ItemNightVisionPotion(ItemStack stack) {
        super(stack);
    }

    @Override
    protected String getDisplayName() {
        return "Night Vision Potion";
    }

    @Override
    protected String[] getLore() {
        return new String[] {"", ChatColor.GRAY.toString() +  "Night Vision " + weight + " (" + time + "s)"};
    }
}
