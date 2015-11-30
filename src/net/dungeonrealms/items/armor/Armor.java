package net.dungeonrealms.items.armor;

import org.bukkit.ChatColor;

/**
 * Created by Nick on 9/21/2015.
 */
public class Armor {

    public enum EquipmentType {
        HELMET(0, "Helmet"),
        CHESTPLATE(1, "Chestplate"),
        LEGGINGS(2, "Leggings"),
        BOOTS(3, "Boots");

        private int id;
        private String name;

        EquipmentType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static EquipmentType getById(int id) {
            for (EquipmentType it : values()) {
                if (it.getId() == id) {
                    return it;
                }
            }
            return getById(0);
        }
    }

    public enum ArmorTier {
        TIER_1(0, 1, new Integer[]{1, 10}, 1),
        TIER_2(1, 2, new Integer[]{10, 20}, 2),
        TIER_3(2, 3, new Integer[]{20, 30}, 3),
        TIER_4(3, 4, new Integer[]{30, 40}, 4),
        TIER_5(4, 5, new Integer[]{40, 50}, 5),;

        private int id;
        private int tierId;
        private Integer[] rangeValues;
        private int attributeRange;

        ArmorTier(int id, int tierId, Integer[] rangeValues, int attributeRange) {
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

        public static ArmorTier getById(int id) {
            for (ArmorTier it : values()) {
                if (it.getId() == id) {
                    return it;
                }
            }
            return null;
        }

        public static ArmorTier getByTier(int tier) {
            for (ArmorTier at : values()) {
                if (at.getTierId() == tier) {
                    return at;
                }
            }
            return null;
        }

        public ChatColor getChatColorOfTier(ArmorTier itemTier) {
            switch (itemTier) {
                case TIER_1:
                    return ChatColor.WHITE;
                case TIER_2:
                    return ChatColor.GREEN;
                case TIER_3:
                    return ChatColor.AQUA;
                case TIER_4:
                    return ChatColor.LIGHT_PURPLE;
                case TIER_5:
                    return ChatColor.YELLOW;
                default:
                    return ChatColor.WHITE;
            }
        }
    }

    public enum ArmorModifier {
        COMMON(0, ChatColor.GRAY + "Common" + ChatColor.RESET),
        UNCOMMON(1, ChatColor.GREEN + "Uncommon" + ChatColor.RESET),
        RARE(2, ChatColor.AQUA + "Rare" + ChatColor.RESET),
        UNIQUE(3, ChatColor.YELLOW + "Unique" + ChatColor.RESET),
        LEGENDARY(4, ChatColor.GOLD + "Legendary" + ChatColor.RESET),;

        private int id;
        private String name;

        ArmorModifier(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static ArmorModifier getById(int id) {
            for (ArmorModifier im : values()) {
                if (im.getId() == id) {
                    return im;
                }
            }
            return null;
        }

        public ChatColor getChatColorOfModifier(ArmorModifier armorModifier) {
            switch (armorModifier) {
                case COMMON:
                    return ChatColor.GRAY;
                case UNCOMMON:
                    return ChatColor.GREEN;
                case RARE:
                    return ChatColor.AQUA;
                case UNIQUE:
                    return ChatColor.YELLOW;
                case LEGENDARY:
                    return ChatColor.DARK_PURPLE;
                default:
                    return ChatColor.GRAY;
            }
        }
    }

    public enum ArmorAttributeType {
        ARMOR(0, "ARMOR", "armor"), //Percentage
        HEALTH_POINTS(1, "HP", "healthPoints"),
        HEALTH_REGEN(2, "HP REGEN", "healthRegen"),
        ENERGY_REGEN(3, "ENERGY REGEN", "energyRegen"), //Percentage
        INTELLECT(4, "INT", "intellect"),
        FIRE_RESISTANCE(5, "FIRE RESISTANCE", "fireResistance"),
        BLOCK(6, "BLOCK", "block"), //Percentage
        LUCK(7, "LUCK", "luck"), //Percentage
        THORNS(8, "THORNS", "thorns"), //Percentage
        STRENGTH(9, "STR", "strength"),
        VITALITY(10, "VIT", "vitality"),
        DODGE(11, "DODGE", "dodge"), //Percentage
        DAMAGE(12, "DMG", "damage"), //Percentage
        DEXTERITY(13, "DEX", "dexterity");

        private int id;
        private String name;
        private String NBTName;

        ArmorAttributeType(int id, String name, String NBTName) {
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

        public static ArmorAttributeType getById(int id) {
            for (ArmorAttributeType at : values()) {
                if (at.getId() == id) {
                    return at;
                }
            }
            return null;
        }

        public static ArmorAttributeType getByString(String name) {
            for (ArmorAttributeType at : values()) {
                if (at.getNBTName().equals(name)) {
                    return at;
                }
            }
            return null;
        }
    }

}
