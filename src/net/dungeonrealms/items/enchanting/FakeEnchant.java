package net.dungeonrealms.items.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran on 9/25/2015.
 */
public class FakeEnchant extends EnchantmentWrapper {

    public FakeEnchant(int id) {
        super(id);
    }

    @Override
    public boolean canEnchantItem(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean conflictsWith(Enchantment enchantment) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public String getName() {
        return "DREnchant";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }
}
