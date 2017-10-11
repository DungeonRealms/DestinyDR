package net.dungeonrealms.game.player.altars.items;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Rar349 on 8/4/2017.
 */
public class ItemFireResistPotion extends ItemEffectPotion {

    public ItemFireResistPotion(int time, int weight) {
        super(PotionEffectType.FIRE_RESISTANCE, time, weight, ItemType.ITEM_FIRE_RESIST_POTION);
    }

    public ItemFireResistPotion(ItemStack stack) {
        super(stack);
    }

    @Override
    protected String getDisplayName() {
        return "Fire Resistance Potion";
    }

    @Override
    protected String[] getLore() {
        return new String[] {"", ChatColor.GRAY.toString() + "Fire Resist. " + weight + " (" + time + "s)"};
    }
}
