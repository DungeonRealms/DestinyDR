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
                if (it.getId() == id) {
                    return it;
                }
            }
            return getById(0);
        }
    }

    public enum ItemTier {
        TIER_1(0, 1, new Integer[]{1, 10}, 3),
        TIER_2(1, 2, new Integer[]{10, 20}, 4),
        TIER_3(2, 3, new Integer[]{20, 30}, 6),
        TIER_4(3, 4, new Integer[]{30, 40}, 8),
        TIER_5(4, 5, new Integer[]{40, 50}, 13),;

        private int id;
        private int tierId;
        private Integer[] rangeValues;
        private int attributeRange;

        ItemTier(int id, int tierId, Integer[] rangeValues, int attributeRange) {
            this.id = id;
            this.tierId = tierId;
            this.rangeValues = rangeValues;
            this.attributeRange = attributeRange;
        }

        public int getId() {
            return id;
        }

        public int getTierId() {
            return tierId;
        }

        public Integer[] getRangeValues() {
            return rangeValues;
        }

        public int getAttributeRange() {
            return attributeRange;
        }

        public static ItemTier getById(int id) {
            for (ItemTier it : values()) {
                if (it.getId() == id) {
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
                if (im.getId() == id) {
                    return im;
                }
            }
            return null;
        }
    }

    public enum AttributeType {
        DAMAGE(0, "Damage", "damage"),
        PURE_DAMAGE(1, "Pure Damage", "pureDamage"),
        CRITICAL_HIT(2, "Critical Hit", "criticalHit"),
        ARMOR_PENETRATION(3, "ArmorPenetration", "armorPenetration"),
        VS_MONSTERS(4, "VS Monsters", "vsMonsters"),
        VS_PLAYER(5, "VS Players", "vsPlayers"),
        BLIND(6, "Blind", "blind"),
        KNOCK_BACK(7, "KnockBack", "knockback"),
        LIFE_STEAL(8, "LifeSteal", "lifesteal"),
        VITALITY(9, "Vitality", "vitality"),
        DEXTERITY(10, "Dexterity", "dexterity"),
        ICE_DAMAGE(11, "IceDamage", "iceDamage"),
        FIRE_DAMAGE(12, "FireDamage", "fireDamage"),
        POISON_DAMAGE(13, "PoisonDamage", "poisonDamage"),
        ACCURACY(14, "Accuracy", "accuracy"),
        STRENGTH(15, "Strength", "strength"),
        INTELLECT(16, "Intellect", "intellect");

        private int id;
        private String name;
        private String NBTName;

        AttributeType(int id, String name, String NBTName) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getNBTName() {
            return NBTName;
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
}