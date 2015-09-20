package net.dungeonrealms.items;


import org.bukkit.ChatColor;

/**
 * Created by Nick on 9/19/2015.
 */
public class Item {

    public enum ItemType {
        SWORD(0, "Sword"),
        POLE_ARM(1, "Pole Arm"),
        AXE(2, "Axe"),
        STAFF(3, "Staff"),
        BOW(4, "Bow");

        private int id;
        private String name;

        ItemType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static ItemType getById(int id) {
            for (ItemType it : values()) {
                if (it.id == id) {
                    return it;
                }
            }
            return getById(0);
        }
    }

    public enum ItemTier {
        TIER_1(1, new Integer[]{1, 10}, ItemMaterial.WOOD),
        TIER_2(2, new Integer[]{10, 20}, ItemMaterial.STONE),
        TIER_3(3, new Integer[]{20, 30}, ItemMaterial.IRON),
        TIER_4(4, new Integer[]{30, 40}, ItemMaterial.DIAMOND),
        TIER_5(5, new Integer[]{40, 50}, ItemMaterial.GOLD),;

        private int id;
        private Integer[] rangeValues;
        private ItemMaterial material;

        ItemTier(int id, Integer[] rangeValues, ItemMaterial material) {
            this.id = id;
            this.rangeValues = rangeValues;
            this.material = material;
        }

        public int getId() {
            return id;
        }

        public Integer[] getRangeValues() {
            return rangeValues;
        }

        public ItemMaterial getMaterial() {
            return material;
        }

        public static ItemTier getById(int id) {
            for (ItemTier it : values()) {
                if (it.id == id) {
                    return it;
                }
            }
            return null;
        }
    }

    public enum ItemModifier {
        COMMON(0, ChatColor.GRAY + "Common" + ChatColor.RESET),
        UNCOMMON(1, ChatColor.GREEN + "Uncommon" + ChatColor.RESET),
        RARE(2, ChatColor.AQUA + "Rare" + ChatColor.RESET),
        UNIQUE(3, ChatColor.YELLOW + "Unique" + ChatColor.RESET),
        //not used, for the lols.
        LEGENDARY(4, ChatColor.GOLD + "Legendary" + ChatColor.RESET),;

        private int id;
        private String name;

        ItemModifier(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static ItemModifier getById(int id) {
            for (ItemModifier im : values()) {
                if (im.id == id) {
                    return im;
                }
            }
            return null;
        }
    }

    public enum AttributeType {
        DAMAGE(0, "Damage"),
        PURE_DAMAGE(1, "Pure Damage"),
        CRITICAL_HIT(2, "Critical Hit"),
        ARMOR_PENETRATION(3, "ArmorPenetration"),
        VS_MONSTERS(4, "VS Monsters"),
        VS_PLAYER(5, "VS Players"),
        BLIND(6, "Blind"),
        KNOCK_BACK(7, "KnockBack"),
        LIFE_STEAL(8, "LifeSteal"),
        VITALITY(9, "Vitality"),
        DEXTERITY(10, "Dexterity"),
        ICE_DAMAGE(11, "IceDamage"),
        FIRE_DAMAGE(12, "FireDamage"),
        ACCURACY(13, "Accuracy"),;

        private int id;
        private String name;

        AttributeType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static AttributeType getById(int id) {
            for (AttributeType at : values()) {
                if (at.id == id) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum ItemMaterial {
        WOOD(0, ItemTier.TIER_1),
        STONE(1, ItemTier.TIER_2),
        IRON(2, ItemTier.TIER_3),
        DIAMOND(3, ItemTier.TIER_4),
        GOLD(4, ItemTier.TIER_5),;

        private int id;
        private ItemTier tier;

        ItemMaterial(int id, ItemTier tier) {
            this.id = id;
            this.tier = tier;
        }

        public int getId() {
            return id;
        }

        public ItemTier getTier() {
            return tier;
        }

        public static ItemMaterial getById(int id) {
            for (ItemMaterial at : values()) {
                if (at.id == id) {
                    return at;
                }
            }
            return getById(0);
        }
    }
}