package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class ItemSpeedPotion extends ItemEffectPotion {

    public ItemSpeedPotion(int time, int weight) {
        super(PotionEffectType.SPEED, time, weight, ItemType.ITEM_SPEED_POTION);
    }

    public ItemSpeedPotion(ItemStack stack) {
        super(stack);
    }

    @Override
    protected String getDisplayName() {
        return "Speed Potion";
    }

    @Override
    protected String[] getLore() {
        return new String[] {"", ChatColor.GRAY.toString() + "Speed " + weight + " (" + time + "s)"};
    }
}
