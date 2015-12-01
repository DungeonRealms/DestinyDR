package net.dungeonrealms.game.world.items;


import org.bukkit.ChatColor;

/**
 * Created by Nick on 9/19/2015.
 */
public class Item {

    public enum ItemType {
        SWORD(0, "Sword"),
        POLE_ARM(1, "Polearm"),
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
        TIER_1(0, 1, new Integer[]{1, 10}, 2),
        TIER_2(1, 2, new Integer[]{1, 10}, 3),
        TIER_3(2, 3, new Integer[]{1, 10}, 4),
        TIER_4(3, 4, new Integer[]{40, 60}, 5),
        TIER_5(4, 5, new Integer[]{60, 100}, 6),;

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

        public static ItemTier getByTier(int tier) {
            for (ItemTier it : values()) {
                if (it.getTierId() == tier) {
                    return it;
                }
            }
            return null;
        }

        public ChatColor getChatColorOfTier(ItemTier itemTier) {
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

    public enum ItemModifier {
        COMMON(0, ChatColor.GRAY.toString() + ChatColor.ITALIC + "Common" + ChatColor.RESET),
        UNCOMMON(1, ChatColor.GREEN.toString() + ChatColor.ITALIC + "Uncommon" + ChatColor.RESET),
        RARE(2, ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare" + ChatColor.RESET),
        UNIQUE(3, ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique" + ChatColor.RESET),
        //not used, for the lols.
        LEGENDARY(4, ChatColor.DARK_PURPLE.toString() + ChatColor.ITALIC + "Legendary" + ChatColor.RESET),;

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

        public String getChatColorOfModifier(ItemModifier itemModifier) {
            switch (itemModifier) {
                case COMMON:
                    return ChatColor.GRAY.toString();
                case UNCOMMON:
                    return ChatColor.GREEN.toString();
                case RARE:
                    return ChatColor.AQUA.toString();
                case UNIQUE:
                    return ChatColor.YELLOW + ChatColor.ITALIC.toString();
                case LEGENDARY:
                    return ChatColor.DARK_PURPLE + ChatColor.ITALIC.toString();
                default:
                    return ChatColor.GRAY.toString();
            }
        }
    }

    public enum AttributeType {
        DAMAGE(0, "DMG", "damage"),
        PURE_DAMAGE(1, "PURE DMG", "pureDamage"),
        CRITICAL_HIT(2, "CRITICAL HIT", "criticalHit"), //Percentage
        ARMOR_PENETRATION(3, "ARMOR PENETRATION", "armorPenetration"),
        VS_MONSTERS(4, "vs. MONSTERS", "vsMonsters"), //Percentage
        VS_PLAYER(5, "vs. PLAYERS", "vsPlayers"), //Percentage
        LIFE_STEAL(6, "LIFE STEAL", "lifesteal"), //Percentage
        VITALITY(7, "VIT", "vitality"),
        DEXTERITY(8, "DEX", "dexterity"),
        ICE_DAMAGE(9, "ICE DMG", "iceDamage"),
        FIRE_DAMAGE(10, "FIRE DMG", "fireDamage"),
        POISON_DAMAGE(11, "POISON DMG", "poisonDamage"),
        ACCURACY(12, "ACCURACY", "accuracy"), //Percentage
        STRENGTH(13, "STR", "strength"),
        INTELLECT(14, "INT", "intellect");

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

        public static AttributeType getByString(String name) {
            for (AttributeType at : values()) {
                if (at.getNBTName().equals(name)) {
                    return at;
                }
            }
            return null;
        }
    }
}
