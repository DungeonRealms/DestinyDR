package net.dungeonrealms.game.world.items;


import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * A compilation of enumerations related to items including
 * ItemType, ItemTier, and ItemRarity along with convenience 
 * functions
 *
 * @author Nick 9/19/2015
 * @author Alan Lu 5/10/2016
 */
public class Item {

    public enum ItemType {
        // WEAPONS
        SWORD(0, Material.WOOD_SWORD, "Shortsword", Material.STONE_SWORD, "Broadsword", Material.IRON_SWORD, "Magic Sword", Material.DIAMOND_SWORD, "Ancient Sword", Material.GOLD_SWORD, "Legendary Sword"),
        POLEARM(1, Material.WOOD_SPADE, "Spear", Material.STONE_SPADE, "Halberd", Material.IRON_SPADE, "Magic Polearm", Material.DIAMOND_SPADE, "Ancient Polearm", Material.GOLD_SPADE, "Legendary Polearm"),
        AXE(2, Material.WOOD_AXE, "Hatchet", Material.STONE_AXE, "Great Axe", Material.IRON_AXE, "War Axe", Material.DIAMOND_AXE, "Ancient Axe", Material.GOLD_AXE, "Legendary Axe"),
        STAFF(3, Material.WOOD_HOE, "Staff", Material.STONE_HOE, "Battlestaff", Material.IRON_HOE, "Wizard Staff", Material.DIAMOND_HOE, "Ancient Staff", Material.GOLD_HOE, "Legendary Staff"),
        BOW(4, Material.BOW, "Shortbow", Material.BOW, "Longbow", Material.BOW, "Magic Bow", Material.BOW, "Ancient Bow", Material.BOW, "Legendary Bow"),
        // ARMOR
        HELMET(5, Material.LEATHER_HELMET, "Leather Coif", Material.CHAINMAIL_HELMET, "Medium Helmet", Material.IRON_HELMET, "Full Helmet", Material.DIAMOND_HELMET, "Ancient Full Helmet", Material.GOLD_HELMET, "Legendary Full Helmet"),
        CHESTPLATE(6, Material.LEATHER_CHESTPLATE, "Leather Chestplate", Material.CHAINMAIL_CHESTPLATE, "Chainmail", Material.IRON_CHESTPLATE, "Platemail", Material.DIAMOND_CHESTPLATE, "Magic Platemail", Material.GOLD_CHESTPLATE, "Legendary Platemail"),
        LEGGINGS(7, Material.LEATHER_LEGGINGS, "Leather Leggings", Material.CHAINMAIL_LEGGINGS, "Chainmail Leggings", Material.IRON_LEGGINGS, "Platemail Leggings", Material.DIAMOND_LEGGINGS, "Magic Platemail Leggings", Material.GOLD_LEGGINGS, "Legendary Platemail Leggings"),
        BOOTS(8, Material.LEATHER_BOOTS, "Leather Boots", Material.CHAINMAIL_BOOTS, "Chainmail Boots", Material.IRON_BOOTS, "Platemail Boots", Material.DIAMOND_BOOTS, "Magic Platemail Boots", Material.GOLD_BOOTS, "Legendary Platemail Boots");

        private int id; // for internal NBT tags
        private Material t1, t2, t3, t4, t5; // the Minecraft material representing each tier
        private String t1Name, t2Name, t3Name, t4Name, t5Name; // the item name representing each tier

        ItemType(int id, Material t1, String t1Name, Material t2, String t2Name, Material t3, String t3Name, Material t4, String t4Name, Material t5, String t5Name) {
            this.id = id;

            this.t1 = t1;
            this.t1Name = t1Name;

            this.t2 = t2;
            this.t2Name = t2Name;

            this.t3 = t3;
            this.t3Name = t3Name;

            this.t4 = t4;
            this.t4Name = t4Name;

            this.t5 = t5;
            this.t5Name = t5Name;
        }

        public static ItemType getRandomWeapon() {
            return ItemType.getById(Utils.randInt(0, 4));
        }

        public static ItemType getRandomArmor() {
            return ItemType.getById(Utils.randInt(5, 8));
        }

        /**
         * Gets the ItemType from the specified Material
         * @param m - the Material of the item
         * @return
         */
        public static ItemType getTypeFromMaterial(Material m) {
            for(ItemType i : values()) {
                if(i.t1 == m) return i;
                if(i.t2 == m) return i;
                if(i.t3 == m) return i;
                if(i.t4 == m) return i;
                if(i.t5 == m) return i;
            }
            return null;
        }

        /**
         * Determines if an ItemStack is a weapon
         * @param is - the ItemStack to check
         * @return - whether the ItemStack is a weapon or not
         */
        public static boolean isWeapon(ItemStack is) {
            ItemType type = getTypeFromMaterial(is.getType());
            return type != null && type.getId() <= 4;
        }

        /**
         * Determines if an ItemStack is armor
         * @param is - the ItemStack to check
         * @return - whether the ItemStack is armor or not
         */
        public static boolean isArmor(ItemStack is) {
            ItemType type = getTypeFromMaterial(is.getType());
            return type != null && type.getId() >= 5;
        }

        /**
         * Gets the material of the specified ItemTier
         * @param tier - the ItemTier of the item
         * @return
         */
        public Material getTier(ItemTier tier) {
            if(tier == ItemTier.TIER_1) return t1;
            if(tier == ItemTier.TIER_2) return t2;
            if(tier == ItemTier.TIER_3) return t3;
            if(tier == ItemTier.TIER_4) return t4;
            if(tier == ItemTier.TIER_5) return t5;
            return null;
        }

        /**
         * Gets the tier name of the specified ItemTier
         * @param tier - the ItemTier of the item
         * @return
         */
        public String getTierName(ItemTier tier){
            if(tier == ItemTier.TIER_1) return t1Name;
            if(tier == ItemTier.TIER_2) return t2Name;
            if(tier == ItemTier.TIER_3) return t3Name;
            if(tier == ItemTier.TIER_4) return t4Name;
            if(tier == ItemTier.TIER_5) return t5Name;
            return null;
        }

        public static ItemType getByName(String name) {
            for (ItemType i : values()) {
                if (i.toString().equalsIgnoreCase(name)) return i;
            }
            return null;
        }

        public int getId() {
            return id;
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
        TIER_2(1, 2, new Integer[]{10, 20}, 3),
        TIER_3(2, 3, new Integer[]{20, 30}, 4),
        TIER_4(3, 4, new Integer[]{30, 40}, 5),
        TIER_5(4, 5, new Integer[]{40, 100}, 6),;

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

        public ChatColor getTierColor(){
            switch(this){
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
            }

            return null;
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

    }

    public enum ItemRarity {
        COMMON(0, ChatColor.GRAY.toString() + ChatColor.ITALIC + "Common" + ChatColor.RESET),
        UNCOMMON(1, ChatColor.GREEN.toString() + ChatColor.ITALIC + "Uncommon" + ChatColor.RESET),
        RARE(2, ChatColor.AQUA.toString() + ChatColor.ITALIC + "Rare" + ChatColor.RESET),
        UNIQUE(3, ChatColor.YELLOW.toString() + ChatColor.ITALIC + "Unique" + ChatColor.RESET);

        private int id;
        private String name;

        ItemRarity(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static ItemRarity getById(int id) {
            for (ItemRarity im : values()) {
                if (im.getId() == id) {
                    return im;
                }
            }
            return null;
        }

        public String getChatColorOfModifier(ItemRarity itemModifier) {
            switch (itemModifier) {
                case COMMON:
                    return ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
                case UNCOMMON:
                    return ChatColor.GREEN.toString() + ChatColor.ITALIC.toString();
                case RARE:
                    return ChatColor.AQUA.toString() + ChatColor.ITALIC.toString();
                case UNIQUE:
                    return ChatColor.YELLOW + ChatColor.ITALIC.toString();
                default:
                    return ChatColor.GRAY.toString() + ChatColor.ITALIC.toString();
            }
        }

        public static ItemRarity getByName(String name) {
            for (ItemRarity i : values()) {
                if (i.toString().equalsIgnoreCase(name)) return i;
            }
            return null;
        }
    }

    public interface AttributeType {
        public int getId();
        public String getName();
        public String getNBTName();
        public boolean isPercentage();
        public boolean isRange();
    }

    public enum WeaponAttributeType implements AttributeType {
        DAMAGE(0, "DMG", "damage", false, true),
        PURE_DAMAGE(1, "PURE DMG", "pureDamage"),
        CRITICAL_HIT(2, "CRITICAL HIT", "criticalHit", true), //Percentage
        ARMOR_PENETRATION(3, "ARMOR PENETRATION", "armorPenetration", true), //Percentage
        VS_MONSTERS(4, "vs. MONSTERS", "vsMonsters", true), //Percentage
        VS_PLAYER(5, "vs. PLAYERS", "vsPlayers", true), //Percentage
        LIFE_STEAL(6, "LIFE STEAL", "lifesteal", true), //Percentage
        VITALITY(7, "VIT", "vitality"),
        DEXTERITY(8, "DEX", "dexterity"),
        ICE_DAMAGE(9, "ICE DMG", "iceDamage"),
        FIRE_DAMAGE(10, "FIRE DMG", "fireDamage"),
        POISON_DAMAGE(11, "POISON DMG", "poisonDamage"),
        ACCURACY(12, "ACCURACY", "accuracy", true), //Percentage
        STRENGTH(13, "STR", "strength"),
        INTELLECT(14, "INT", "intellect"),
        KNOCKBACK(15, "KNOCKBACK", "knockback", true), //Percentage
        BLIND(16, "BLIND", "blind", true), //Percentage
        SLOW(17, "SLOW", "slow", true); //Percentage

        private int id;
        private String name;
        private String NBTName;
        private boolean isPercentage;
        private boolean isRange;

        /**
         * @return the isRange
         */
        public boolean isRange() {
            return isRange;
        }

        /**
         * @return the isPercentage
         */
        public boolean isPercentage() {
            return isPercentage;
        }

        WeaponAttributeType(int id, String name, String NBTName) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
            this.isPercentage = false;
            this.isRange = false;
        }

        WeaponAttributeType(int id, String name, String NBTName, boolean percentage) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
            this.isPercentage = percentage;
            this.isRange = false;
        }

        WeaponAttributeType(int id, String name, String NBTName, boolean percentage, boolean range) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
            this.isPercentage = percentage;
            this.isRange = range;
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

        public static WeaponAttributeType getById(int id) {
            for (WeaponAttributeType at : values()) {
                if (at.id == id) {
                    return at;
                }
            }
            return null;
        }

        public static WeaponAttributeType getByName(String name) {
            for (WeaponAttributeType at : values()) {
                if (at.getName().equals(name)) {
                    return at;
                }
            }
            return null;
        }

        public static WeaponAttributeType getByNBTName(String name) {
            for (WeaponAttributeType at : values()) {
                if (at.getNBTName().equals(name)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum ArmorAttributeType implements AttributeType {
        ARMOR(0, "ARMOR", "armor", true, true), //Percentage
        HEALTH_POINTS(1, "HP", "healthPoints"),
        HEALTH_REGEN(2, "HP REGEN", "healthRegen"),
        ENERGY_REGEN(3, "ENERGY REGEN", "energyRegen", true), //Percentage
        INTELLECT(4, "INT", "intellect"),
        FIRE_RESISTANCE(5, "FIRE RESISTANCE", "fireResistance", true), //Percentage
        BLOCK(6, "BLOCK", "block", true), //Percentage
        LUCK(7, "LUCK", "luck", true), //Percentage
        THORNS(8, "THORNS", "thorns", true), //Percentage
        STRENGTH(9, "STR", "strength"),
        VITALITY(10, "VIT", "vitality"),
        DODGE(11, "DODGE", "dodge", true), //Percentage
        DAMAGE(12, "DPS", "dps", true, true), //Percentage
        DEXTERITY(13, "DEX", "dexterity"),
        REFLECTION(14, "REFLECTION", "reflection", true), //Percentage
        GEM_FIND(15, "GEM FIND", "gemFind", true), //Percentage
        ITEM_FIND(16, "ITEM FIND", "itemFind", true), //Percentage
        ICE_RESISTANCE(17, "ICE RESISTANCE", "iceResistance", true), //Percentage
        POISON_RESISTANCE(18, "POISON RESISTANCE", "poisonResistance", true); //Percentage

        private int id;
        private String name;
        private String NBTName;
        private boolean isPercentage;
        private boolean isRange;

        /**
         * @return the isRange
         */
        public boolean isRange() {
            return isRange;
        }

        /**
         * @return the isPercentage
         */
        public boolean isPercentage() {
            return isPercentage;
        }

        ArmorAttributeType(int id, String name, String NBTName) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
            this.isPercentage = false;
            this.isRange = false;
        }

        ArmorAttributeType(int id, String name, String NBTName, boolean percentage) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
            this.isPercentage = percentage;
            this.isRange = false;
        }

        ArmorAttributeType(int id, String name, String NBTName, boolean percentage, boolean range) {
            this.id = id;
            this.name = name;
            this.NBTName = NBTName;
            this.isPercentage = percentage;
            this.isRange = range;
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

        public static ArmorAttributeType getByName(String name) {
            for (ArmorAttributeType at : values()) {
                if (at.getName().equals(name)) {
                    return at;
                }
            }
            return null;
        }

        public static ArmorAttributeType getByNBTName(String name) {
            for (ArmorAttributeType at : values()) {
                if (at.getNBTName().equals(name)) {
                    return at;
                }
            }
            return null;
        }
    }

    // UTILITY FUNCTIONS
    /**
     * Returns ItemStack Material based on item type and tier.
     *
     * @param type
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    public static ItemStack getBaseItem(Item.ItemType type, Item.ItemTier tier) {
        switch (type) {
            case SWORD:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.WOOD_SWORD);
                    case TIER_2:
                        return new ItemStack(Material.STONE_SWORD);
                    case TIER_3:
                        return new ItemStack(Material.IRON_SWORD);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_SWORD);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_SWORD);
                }
            case AXE:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.WOOD_AXE);
                    case TIER_2:
                        return new ItemStack(Material.STONE_AXE);
                    case TIER_3:
                        return new ItemStack(Material.IRON_AXE);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_AXE);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_AXE);
                }
            case POLEARM:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.WOOD_SPADE);
                    case TIER_2:
                        return new ItemStack(Material.STONE_SPADE);
                    case TIER_3:
                        return new ItemStack(Material.IRON_SPADE);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_SPADE);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_SPADE);
                }
            case STAFF:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.WOOD_HOE);
                    case TIER_2:
                        return new ItemStack(Material.STONE_HOE);
                    case TIER_3:
                        return new ItemStack(Material.IRON_HOE);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_HOE);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_HOE);
                }
            case BOW:
                return new ItemStack(Material.BOW);
            case HELMET:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.LEATHER_HELMET);
                    case TIER_2:
                        return new ItemStack(Material.CHAINMAIL_HELMET);
                    case TIER_3:
                        return new ItemStack(Material.IRON_HELMET);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_HELMET);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_HELMET);
                }
            case CHESTPLATE:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.LEATHER_CHESTPLATE);
                    case TIER_2:
                        return new ItemStack(Material.CHAINMAIL_CHESTPLATE);
                    case TIER_3:
                        return new ItemStack(Material.IRON_CHESTPLATE);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_CHESTPLATE);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_CHESTPLATE);
                }
            case LEGGINGS:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.LEATHER_LEGGINGS);
                    case TIER_2:
                        return new ItemStack(Material.CHAINMAIL_LEGGINGS);
                    case TIER_3:
                        return new ItemStack(Material.IRON_LEGGINGS);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_LEGGINGS);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_LEGGINGS);
                }
            case BOOTS:
                switch (tier) {
                    case TIER_1:
                        return new ItemStack(Material.LEATHER_BOOTS);
                    case TIER_2:
                        return new ItemStack(Material.CHAINMAIL_BOOTS);
                    case TIER_3:
                        return new ItemStack(Material.IRON_BOOTS);
                    case TIER_4:
                        return new ItemStack(Material.DIAMOND_BOOTS);
                    case TIER_5:
                        return new ItemStack(Material.GOLD_BOOTS);
                }
            default:
                Utils.log.warning("ItemGenerator couldn't find getBaseItem().. " + type.toString());
        }
        return null;
    }

    public static boolean isItemType(ItemType type, Material material) {
        return type.t1 == material || type.t2 == material || type.t3 == material || type.t4 == material || type.t5 == material;
    }

    /**
     * Gets the ItemTier from a material
     * @param m
     * @return
     */
    public static ItemTier getTierFromMaterial(Material m) {
        for(ItemType i : ItemType.values()) {
            if(i.t1 == m) return ItemTier.TIER_1;
            if(i.t2 == m) return ItemTier.TIER_2;
            if(i.t3 == m) return ItemTier.TIER_3;
            if(i.t4 == m) return ItemTier.TIER_4;
            if(i.t5 == m) return ItemTier.TIER_5;
        }
        return null;
    }
}
