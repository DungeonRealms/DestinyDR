package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class ItemStrengthPotion extends ItemEffectPotion {

    public ItemStrengthPotion(int time, int weight) {
        super(PotionEffectType.HARM, time, weight, ItemType.ITEM_STRENGTH_POTION);
    }

    public ItemStrengthPotion(ItemStack stack) {
        super(stack);
    }

    @Override
    protected String getDisplayName() {
        return "DPS Potion";
    }

    @Override
    protected String[] getLore() {
        int percent = weight == 0 ? 15 : weight == 1 ? 35 : weight == 2 ? 60 : 15;
        return new String[] {"", ChatColor.GRAY.toString() + percent + "% DPS (" + time + "s)"};
    }
}
