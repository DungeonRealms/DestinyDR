package net.dungeonrealms.miscellaneous;

import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 11/3/2015.
 */
public class SandS implements GenericMechanic {

    static SandS instance = null;

    public static SandS getInstance() {
        if (instance == null) {
            instance = new SandS();
        }
        return instance;
    }


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {

    }

    @Override
    public void stopInvocation() {

    }

    public ItemStack getScroll(ScrollType type, int tier) {
        switch (type) {
            case ENCHANTMENT_SCROLL:
                switch (tier) {
                    case 1:
                        return new ItemBuilder().setItem(new ItemStack(Material.PAPER), ChatColor.GREEN + "", new String[]{
                                "",
                        }).setNBTString("scrollTier", String.valueOf(tier)).setNBTString("scrollType", String.valueOf(type.getId())).build();
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                }
                break;
        }
        return null;
    }

    public boolean isScroll(ItemStack itemStack) {
        return CraftItemStack.asNMSCopy(itemStack).hasTag() && CraftItemStack.asNMSCopy(itemStack).getTag().hasKey("scrollTier");
    }

    enum ScrollType {
        /**
         * 8 levels.
         * Each time you enchant something it levels up +1,+2,+3
         * 5 different type of enchantment scrolls.
         * Left click on item, (booster on terms of core stats)
         * either,
         * increase Hp by 5% regardless..
         * If they have HP regen goes up by 5% OR if it has energy regen increase 1%.
         * ____________________
         * WEAPONS
         * minimum and maximum damage are increased by 5% regardless of ANYTHING.
         * Can use to +1,+2,+3 from +4 to +12 each increasing level of enchant.
         */
        ENCHANTMENT_SCROLL(0),
        /**
         * PROTECTION SCROLLS
         * protects items, doesn't stop enchant from failing but if it does fail
         * the item doesn't get destroyed.
         */
        WHITE_SCROLL(1),;

        private int id;

        ScrollType(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static ScrollType getById(int id) {
            for (ScrollType st : values()) {
                if (st.getId() == id) {
                    return st;
                }
            }
            return null;
        }
    }

    public void applyScroll(ItemStack itemStack, ItemStack scrollItem) {
        if (!isScroll(scrollItem)) return;

    }

}
