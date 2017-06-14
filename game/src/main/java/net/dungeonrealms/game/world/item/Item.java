package net.dungeonrealms.game.world.item;


import lombok.Getter;
import lombok.NoArgsConstructor;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * A compilation of enumerations related to items including
 * ItemType, ItemTier, and ItemRarity along with convenience
 * functions
 *
 * @author Nick 9/19/2015
 * @author Alan Lu 5/10/2016
 */
public class Item {

    /**
     * This enum is for all gear basically.
     * Tierable equipment that can have attributes (stats).
     */
    public enum GeneratedItemType {
        // WEAPONS
        SWORD(AttributeBank.WEAPON, new Material[]{Material.WOOD_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.GOLD_SWORD}, new String[]{"Shortsword", "Broadsword", "Magic Sword", "Ancient Sword", "Legendary Sword"}, 2),
        POLEARM(AttributeBank.WEAPON, new Material[]{Material.WOOD_SPADE, Material.STONE_SPADE, Material.IRON_SPADE, Material.DIAMOND_SPADE, Material.GOLD_SPADE}, new String[]{"Spear", "Halberd", "Magic Polearm", "Ancient Polearm", "Legendary Polearm"}, 2),
        AXE(AttributeBank.WEAPON, new Material[]{Material.WOOD_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.DIAMOND_AXE, Material.GOLD_AXE}, new String[]{"Hatchet", "Great Axe", "War Axe", "Ancient Axe", "Legendary Axe"}, 2),
        STAFF(AttributeBank.WEAPON, new Material[]{Material.WOOD_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.DIAMOND_HOE, Material.GOLD_HOE}, new String[]{"Staff", "Battlestaff", "Wizard Staff", "Ancient Staff", "Legendary Staff"}, 2),
        BOW(AttributeBank.WEAPON, Material.BOW, new String[]{"Shortbow", "Longbow", "Magic Bow", "Ancient Bow", "Legendary Bow"}, 2),

        // ARMOR
        HELMET(AttributeBank.ARMOR, new Material[]{Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.GOLD_HELMET}, new String[]{"Leather Coif", "Medium Helmet", "Full Helmet", "Ancient Full Helmet", "Legendary Full Helmet"}, 1),
        CHESTPLATE(AttributeBank.ARMOR, new Material[]{Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.GOLD_CHESTPLATE}, new String[]{"Leather Chestplate", "Chainmail", "Platemail", "Magic Platemail", "Legendary Platemail"}, 3),
        LEGGINGS(AttributeBank.ARMOR, new Material[]{Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.GOLD_LEGGINGS}, new String[]{"Leather Leggings", "Chainmail Leggings", "Platemail Leggings", "Magic Platemail Leggings", "Legendary Platemail Leggings"}, 2),
        BOOTS(AttributeBank.ARMOR, new Material[]{Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.GOLD_BOOTS}, new String[]{"Leather Boots", "Chainmail Boots", "Platemail Boots", "Magic Platemail Boots", "Legendary Platemail Boots"}, 1),

        SHIELD(AttributeBank.SHIELD, Material.SHIELD, new String[]{"Broken Buckler", "Rusty Buckler", "Magic Buckler", "Ancient Buckler", "Legendary Buckler"}, 3),

        // PROFESSION
        PICKAXE(AttributeBank.PICKAXE, new Material[]{Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE}, new String[]{"", "", "", "", ""}, 0),
        FISHING_ROD(AttributeBank.FISHING_ROD, Material.FISHING_ROD, new String[]{"", "", "", "", ""}, 0);

        @Getter
        private Material[] materials;
        @Getter
        private String[] gearNames;
        @Getter
        private AttributeBank attributeBank;
        @Getter
        private int merchantScraps;

        GeneratedItemType(AttributeBank attributes, Material material, String[] names, int scraps) {
            this(attributes, new Material[]{material, material, material, material, material}, names, scraps);
        }

        GeneratedItemType(AttributeBank attributes, Material[] material, String[] names, int scraps) {
            this.materials = material;
            this.gearNames = names;
            this.attributeBank = attributes;
            this.merchantScraps = scraps;
        }

        /**
         * Gets the ItemType from the specified Material
         *
         * @param m - the Material of the item
         * @return
         */
        public static GeneratedItemType getType(Material m) {
            for (GeneratedItemType i : values())
                for (Material mat : i.getMaterials())
                    if (mat == m)
                        return i;
            return null;
        }

        /**
         * Determines if an ItemStack is a weapon
         *
         * @param is - the ItemStack to check
         * @return - whether the ItemStack is a weapon or not
         */
        public static boolean isWeapon(ItemStack is) {
            GeneratedItemType type = getType(is.getType());
            return type != null && type.getId() <= 4;
        }

        /**
         * Determines if an ItemStack is armor
         *
         * @param is - the ItemStack to check
         * @return - whether the ItemStack is armor or not
         */
        public static boolean isArmor(ItemStack is) {
            GeneratedItemType type = getType(is.getType());
            return type != null && type.getId() >= 5;
        }

        /**
         * Gets the material of the specified ItemTier
         *
         * @param tier - the ItemTier of the item
         * @return
         */
        public Material getTier(ItemTier tier) {
            return this.getMaterials()[tier.getId() - 1];
        }

        /**
         * Gets the tier name of the specified ItemTier
         *
         * @param tier - the ItemTier of the item
         * @return
         */
        public String getTierName(ItemTier tier) {
            return this.getGearNames()[tier.getId() - 1];
        }

        public static GeneratedItemType getByName(String name) {
            for (GeneratedItemType i : values())
                if (i.toString().equalsIgnoreCase(name))
                    return i;
            return null;
        }

        public int getId() {
            return ordinal();
        }

        public static GeneratedItemType getById(int id) {
            for (GeneratedItemType it : values())
                if (it.getId() == id)
                    return it;
            return getById(0);
        }

        public static GeneratedItemType getRandomGear() {
            return values()[Utils.randInt(0, values().length - 1)];
        }
    }

    @Getter
    public enum ItemTier {
        TIER_1(0, new Integer[]{1, 5}, 2, ChatColor.WHITE, DyeColor.WHITE, Material.LEATHER, "Wooden", "Leather"),
        TIER_2(5, new Integer[]{5, 10}, 3, ChatColor.GREEN, DyeColor.GREEN, Material.IRON_FENCE, "Stone", "Chainmail"),
        TIER_3(10, new Integer[]{10, 20}, 4, ChatColor.AQUA, DyeColor.LIGHT_BLUE, Material.IRON_INGOT, "Iron"),
        TIER_4(15, new Integer[]{20, 25}, 5, ChatColor.LIGHT_PURPLE, DyeColor.PURPLE, Material.DIAMOND, "Diamond"),
        TIER_5(25, new Integer[]{25, 100}, 6, ChatColor.YELLOW, DyeColor.YELLOW, Material.GOLD_INGOT, "Gold");

        private int tierId;
        private int levelRequirement;
        private Integer[] rangeValues;
        private int attributeRange;
        private ChatColor color;
        private DyeColor dyeColor;
        private Material material;
        private String weaponName;
        private String armorName;

        ItemTier(int levelReq, Integer[] range, int attribute, ChatColor c, DyeColor color, Material m, String name) {
            this(levelReq, range, attribute, c, color, m, name, name);
        }

        ItemTier(int levelReq, Integer[] rangeValues, int attributeRange, ChatColor color, DyeColor dye, Material m, String weaponName, String armorName) {
            this.tierId = ordinal() + 1;
            this.dyeColor = dye;
            this.levelRequirement = levelReq;
            this.rangeValues = rangeValues;
            this.attributeRange = attributeRange;
            this.color = color;
            this.material = m;
            this.weaponName = weaponName;
            this.armorName = armorName;
        }

        public static ItemTier getByTier(int tier) {
            for (ItemTier it : values())
                if (it.getTierId() == tier)
                    return it;
            return null;
        }

        public static ItemTier getRandomTier() {
            return values()[RandomHelper.getRandomNumberBetween(0, values().length - 1)];
        }

        public int getId() {
            return getTierId();
        }

        public static ItemTier maxTier() {
            return values()[values().length - 1];
        }

    }

    public enum ItemRarity {
        COMMON("Common", ChatColor.GRAY, 1000, Material.DIRT),
        UNCOMMON("Uncommon", ChatColor.GREEN, 150, Material.IRON_BLOCK),
        RARE("Rare", ChatColor.AQUA, 30, Material.DIAMOND_BLOCK),
        UNIQUE("Unique", ChatColor.YELLOW, 10, Material.GOLD_BLOCK);

        @Getter
        private ChatColor color;
        private String name;
        @Getter
        private Material material;
        @Getter
        private int dropChance;

        ItemRarity(String name, ChatColor color, int dropChance, Material mat) {
            this.name = name;
            this.color = color;
            this.material = mat;
            this.dropChance = dropChance;
        }

        public int getId() {
            return ordinal();
        }

        public String getName() {
            return this.getColor() + "" + ChatColor.ITALIC + this.name + ChatColor.RESET;
        }

        public static ItemRarity getById(int id) {
            for (ItemRarity im : values())
                if (im.getId() == id)
                    return im;
            return null;
        }

        public static ItemRarity getByName(String name) {
            for (ItemRarity ir : values())
                if (ir.name.equalsIgnoreCase(name))
                    return ir;
            return null;
        }

        public static ItemRarity getRandomRarity() {
            return getRandomRarity(false);
        }

        /**
         * Returns a random rarity, taking into effect droprates.
         */
        public static ItemRarity getRandomRarity(boolean isElite) {
            int chance = RandomHelper.getRandomNumberBetween(1, ItemRarity.COMMON.getDropChance());
            if (isElite)
                chance *= 0.9;

            for (int i = ItemRarity.values().length - 1; i > 0; i--)
                if (chance <= ItemRarity.values()[i].getDropChance())
                    return ItemRarity.values()[i];
            return ItemRarity.COMMON;
        }
    }

    public interface AttributeType {
        int getId();

        int getChance();

        String getPrefix();

        String getSuffix();

        String getDisplayPrefix();

        String getDisplaySuffix(boolean b);

        int getDisplayPriority();

        String getNBTName();

        boolean isPercentage();

        boolean isRange();

        boolean isIncludeOnReroll();


        public static AttributeType getAttributeTypeFromPrefix(String name) {
            name = ChatColor.stripColor(name).trim();
            for (WeaponAttributeType type : WeaponAttributeType.values()) {
                if (ChatColor.stripColor(type.getPrefix().replace("+", "").trim()).equalsIgnoreCase(name)) return type;
            }

            for (ArmorAttributeType type : ArmorAttributeType.values()) {
                if (ChatColor.stripColor(type.getPrefix().replace("+", "").trim()).equalsIgnoreCase(name)) return type;
            }
            return null;
        }
    }

    public interface ProfessionAttribute extends AttributeType {
        int[] getPercentRange();

        default int getRandomValueFromTier(ItemTier tier) {
            int minRange = tier.ordinal() - 1;
            int maxRange = tier.ordinal();
            if (maxRange >= getPercentRange().length)
                maxRange = getPercentRange()[getPercentRange().length - 1];

            return Math.max(Utils.randInt(getPercentRange()[minRange], getPercentRange()[maxRange]), 1);
        }

        default int getMaxFromTier(ItemTier tier) {
            int maxRange = tier.ordinal();
            if (maxRange >= getPercentRange().length)
                maxRange = getPercentRange()[getPercentRange().length - 1];
            return getPercentRange()[maxRange];
        }

        default int getMinFromTier(ItemTier tier) {
            int maxRange = tier.ordinal();
            if (maxRange < 0)
                maxRange = 0;
            return getPercentRange()[maxRange];
        }
    }

    public enum AttributeBank {
        WEAPON(WeaponAttributeType.values(), 0.2, new double[]{0.07, 0.7, 1, 3.35, 4.48}),
        ARMOR(ArmorAttributeType.values(), 0.24, new double[]{1, 1.25, 1.5, 3.25, 4.75}),
        SHIELD(new AttributeType[]{ArmorAttributeType.MELEE_ABSORBTION, ArmorAttributeType.MAGE_ABSORBTION, ArmorAttributeType.RANGE_ABSORBTION, ArmorAttributeType.REFLECTION, ArmorAttributeType.FIRE_RESISTANCE, ArmorAttributeType.ICE_RESISTANCE, ArmorAttributeType.POISON_RESISTANCE, ArmorAttributeType.HEALTH_POINTS, ArmorAttributeType.ITEM_FIND, ArmorAttributeType.GEM_FIND}, 0.24, new double[]{1, 1.25, 1.5, 3.25, 4.75}),
        FISHING_ROD(FishingAttributeType.values(), 0.8, new double[]{0.5, 0.75, 1, 2, 3}),
        PICKAXE(PickaxeAttributeType.values(), 0.8, new double[]{0.5, 0.75, 1, 2, 3});

        @Getter
        private AttributeType[] attributes;
        private double[] repairMultipliers;
        @Getter
        private double globalRepairMultiplier;

        AttributeBank(AttributeType[] types, double global, double[] repairMult) {
            this.attributes = types;
            this.globalRepairMultiplier = global;
            this.repairMultipliers = repairMult;
        }

        public double getRepairMultiplier(ItemTier tier) {
            return repairMultipliers[tier.getId() - 1];
        }
    }

    @NoArgsConstructor
    public enum FishingAttributeType implements ProfessionAttribute {
        DOUBLE_CATCH("DOUBLE CATCH", "doubleCatch", 5, 5, 9, 13, 24),
        TRIPLE_CATCH("TRIPLE CATCH", "tripleCatch", 2, 2, 3, 4, 5),
        DURABILITY("DURABILITY", "durability", 5, 10, 15, 20, 25),
        CATCH_SUCCESS("FISHING SUCCESS", "catchSuccess", 2, 2, 2, 2, 6),
        JUNK_FIND("JUNK FIND", "junkFind", 2, 5, 10, 13, 15),
        TREASURE_FIND("TREASURE FIND", "treasureFind", -1, -1, 1, 2, 3);

        private String prefix;
        @Getter
        private String NBTName;
        @Getter
        private int[] percentRange;

        FishingAttributeType(String prefix, String nbt, int... range) {
            this.prefix = ChatColor.RED + prefix;
            this.NBTName = nbt;
            this.percentRange = range;
        }

        @Override
        public String getPrefix() {
            return this.prefix + ": +";
        }

        @Override
        public int getId() {
            return ordinal();
        }

        @Override
        public int getChance() {
            return 0;
        }

        @Override
        public String getSuffix() {
            return "%";
        }

        @Override
        public String getDisplayPrefix() {
            return "";
        }

        @Override
        public String getDisplaySuffix(boolean b) {
            return "";
        }

        @Override
        public int getDisplayPriority() {
            return getId();
        }

        @Override
        public boolean isPercentage() {
            return true;
        }

        @Override
        public boolean isRange() {
            return false;
        }

        @Override
        public boolean isIncludeOnReroll() {
            return false;
        }
    }

    @NoArgsConstructor
    public enum PickaxeAttributeType implements ProfessionAttribute {

        DOUBLE_ORE("DOUBLE ORE", "doubleOre", 5, 5, 9, 13, 17),
        GEM_FIND("GEM FIND", "gemFind", 3, 3, 5, 8, 11),
        MINING_SUCCESS("MINING SUCCESS", "miningSuccess", 2, 2, 3, 4, 5),
        TRIPLE_ORE("TRIPLE ORE", "tripleOre", 2, 2, 3, 4, 5),
        DURABILITY("DURABILITY", "durability", 5, 10, 15, 20, 25),
        TREASURE_FIND("TREASURE FIND", "treasureFind", -1, -1, 2, 3, 3);

        private String prefix;
        @Getter
        private String NBTName;
        @Getter
        private int[] percentRange;

        PickaxeAttributeType(String prefix, String nbt, int... percentageRange) {
            this.prefix = ChatColor.RED + prefix;
            this.NBTName = nbt;
            this.percentRange = percentageRange;
        }

        public int getId() {
            return ordinal();
        }

        public String getPrefix() {
            return this.prefix + ": +";
        }

        public String getSuffix() {
            return "%";
        }

        public String getDisplayPrefix() {
            return prefix;
        }

        public String getDisplaySuffix(boolean b) {
            return null;
        }

        public int getDisplayPriority() {
            return getId();
        }

        public boolean isPercentage() {
            return true;
        }

        public boolean isRange() {
            return false;
        }

        public boolean isIncludeOnReroll() {
            return false;
        }

        public int getChance() {
            return 100;
        }

    }

    public enum WeaponAttributeType implements AttributeType {
        DAMAGE("DMG: ", "", "damage", 100, true, true, "", "", -1),
        VS_MONSTERS("vs. MONSTERS: +", "% DMG", "vsMonsters", "", "Slaying", 9),
        VS_PLAYER("vs. PLAYERS: +", "% DMG", "vsPlayers", "", "Slaughter", 10),
        PRECISION("PRECISION: ", "%", "precision", "Precise", "", 1),
        LIFE_STEAL("LIFE STEAL: ", "%", "lifesteal", "Vampyric", "", 5),
        ACCURACY("ACCURACY: ", "%", "accuracy", "Accurate", "", 2),
        CRITICAL_HIT("CRITICAL HIT: ", "%", "criticalHit", "Deadly", "", 6),
        ARMOR_PENETRATION("ARMOR PENETRATION: ", "%", "armorPenetration", "Penetrating", "", 7),
        SLOW("SLOW: ", "%", "slow", "Snaring", "", 4),
        KNOCKBACK("KNOCKBACK: ", "%", "knockback", "Brute", "", 3),
        BLIND("BLIND: ", "%", "blind", "", "Blindness", 8),
        ICE_DAMAGE("ICE DMG: +", "", "iceDamage", "", "Ice", 12),
        FIRE_DAMAGE("FIRE DMG: +", "", "fireDamage", "", "Fire", 11),
        POISON_DAMAGE("POISON DMG: +", "", "poisonDamage", "", "Poison", 13),
        PURE_DAMAGE("PURE DMG: +", "", "pureDamage", "Pure", "", 0);

        @Getter
        private String prefix;
        @Getter
        private String suffix;
        @Getter
        private int displayPriority;
        @Getter
        private String displayPrefix;
        private String displaySuffix;
        @Getter
        private String NBTName;
        @Getter
        private int chance;
        @Getter
        private boolean percentage;
        @Getter
        private boolean range;
        @Getter
        private boolean includeOnReroll;

        WeaponAttributeType(String prefix, String suffix, String nbtName, String displayPrefix, String displaySuffix, int displayPriority) {
            this(prefix, suffix, nbtName, -1, displayPrefix, displaySuffix, displayPriority);
        }

        WeaponAttributeType(String prefix, String suffix, String nbtName, int chance, String displayPrefix, String displaySuffix, int displayPriority) {
            this(prefix, suffix, nbtName, chance, false, displayPrefix, displaySuffix, displayPriority);
        }

        WeaponAttributeType(String prefix, String suffix, String nbtName, int chance, boolean reRoll, String displayPrefix, String displaySuffix, int displayPriority) {
            this(prefix, suffix, nbtName, chance, reRoll, false, displayPrefix, displaySuffix, displayPriority);
        }

        WeaponAttributeType(String prefix, String suffix, String NBTName, int chance, boolean includeOnReroll, boolean range, String displayPrefix, String displaySuffix, int displayPriority) {
            this.prefix = ChatColor.RED + prefix;
            this.suffix = suffix;
            this.chance = chance;
            this.NBTName = NBTName;
            this.percentage = suffix.contains("%");
            this.range = range;
            this.includeOnReroll = includeOnReroll;
            this.displayPrefix = displayPrefix;
            this.displaySuffix = displaySuffix;
            this.displayPriority = displayPriority;
        }

        public int getId() {
            return ordinal();
        }

        private String getName() {
            return ChatColor.stripColor(getPrefix()).split(":")[0];
        }

        public static WeaponAttributeType getById(int id) {
            for (WeaponAttributeType at : values())
                if (at.getId() == id)
                    return at;
            return null;
        }

        public static WeaponAttributeType getByName(String name) {
            for (WeaponAttributeType at : values())
                if (at.getName().equals(name))
                    return at;
            return null;
        }

        public static WeaponAttributeType getByNBTName(String name) {
            for (WeaponAttributeType at : values())
                if (at.getNBTName().equals(name))
                    return at;
            return null;
        }

        public static WeaponAttributeType getByPrefix(String name) {
            for (WeaponAttributeType type : values()) {
                String nme = type.getPrefix().replace("+", "");
                //if (nme.equals(name))
            }
            return null;
        }

        @Override
        public String getDisplaySuffix(boolean b) {
            return this.displaySuffix;
        }
    }

    public enum ArmorAttributeType implements AttributeType {
        DAMAGE("DPS: ", "%", "dps", 50, true, true, "", "", -1),
        ARMOR("ARMOR: ", "%", "armor", 100, true, true, "", "", -1),
        HEALTH_POINTS("HP: +", "", "healthPoints", 100, true, "", "Fortitude", "Fortitude", -1),
        ENERGY_REGEN("ENERGY REGEN: +", "%", "energyRegen", 50, true, "", "", "", 4),
        HEALTH_REGEN("HP REGEN: +", " HP/s", "healthRegen", 100, true, "Mending", "", "", 2),
        STRENGTH("STR: +", "", "strength"),
        DEXTERITY("DEX: +", "", "dexterity"),
        VITALITY("VIT: +", "", "vitality"),
        INTELLECT("INT: +", "", "intellect"),
        REFLECTION("REFLECTION: ", "%", "reflection", "Reflective", "", 1),
        BLOCK("BLOCK: ", "%", "block", "Protective", "", 3),
        DODGE("DODGE: ", "%", "dodge", "Agile", "", 0),
        THORNS("THORNS: ", "% DMG", "thorns", "", "Spikes", "Thorns", 10),
        FIRE_RESISTANCE("FIRE RESISTANCE: ", "%", "fireResistance", "", "Fire Resist", 5),
        ICE_RESISTANCE("ICE RESISTANCE: ", "%", "iceResistance", "", "Ice Resist", 6),
        POISON_RESISTANCE("POISON RESISTANCE: ", "%", "poisonResistance", "Poison Resist", "", 7),
        GEM_FIND("GEM FIND: ", "%", "gemFind", "", "Golden", "Pickpocketing", 8),
        ITEM_FIND("ITEM FIND: +", "%", "itemFind", "", "Treasure", 9),
        MELEE_ABSORBTION("MELEE ABSORB: +", "%", "meleeAbsorb", 100, true, "", "Melee Absorption", "", 11),
        MAGE_ABSORBTION("MAGIC ABSORB: +", "%", "mageAbsorb", 100, true, "", "Magic Absorption", "", 11),
        RANGE_ABSORBTION("RANGE ABSORB: +", "%", "rangeAbsorb", 100, true, "", "Range Absorption", "", 11);

        @Getter
        private String prefix;
        @Getter
        private String suffix;
        @Getter
        private String displayPrefix;
        private String displaySuffix;
        private String secondaryDisplaySuffix;
        @Getter
        private int displayPriority;
        @Getter
        private String NBTName;
        @Getter
        private int chance;
        @Getter
        private boolean includeOnReroll;
        @Getter
        private boolean percentage;
        @Getter
        private boolean range;

        ArmorAttributeType(String prefix, String suffix, String NBTName) {
            this(prefix, suffix, NBTName, -1, "", "", "", -1);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, String displayPrefix, String displaySuffix, int displayPriority) {
            this(prefix, suffix, NBTName, -1, displayPrefix, displaySuffix, displaySuffix, displayPriority);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, String displayPrefix, String secondary, String displaySuffix, int displayPriority) {
            this(prefix, suffix, NBTName, -1, displayPrefix, secondary, displaySuffix, displayPriority);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, int chance, String displayPrefix, String displaySuffix, int displayPriority) {
            this(prefix, suffix, NBTName, chance, displayPrefix, displaySuffix, displaySuffix, displayPriority);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, int chance, String displayPrefix, String displaySuffix, String secondaryDisplaySuffix, int displayPriority) {
            this(prefix, suffix, NBTName, chance, false, displayPrefix, displaySuffix, secondaryDisplaySuffix, displayPriority);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, int chance, boolean rerollInclude, String displayPrefix, String displaySuffix, String secondaryDisplaySuffix, int displayPriority) {
            this(prefix, suffix, NBTName, chance, rerollInclude, false, displayPrefix, displaySuffix, secondaryDisplaySuffix, displayPriority);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, int chance, boolean rerollInclude, boolean range, String displayPrefix, String displaySuffix, int displayPriority) {
            this(prefix, suffix, NBTName, chance, rerollInclude, range, displayPrefix, displaySuffix, displaySuffix, displayPriority);
        }

        ArmorAttributeType(String prefix, String suffix, String NBTName, int chance, boolean rerollInclude, boolean range, String displayPrefix, String displaySuffix, String secondaryDisplaySuffix, int displayPriority) {
            this.prefix = ChatColor.RED + prefix;
            this.suffix = suffix;
            this.NBTName = NBTName;
            this.chance = chance;
            this.includeOnReroll = rerollInclude;
            this.percentage = suffix.contains("%");
            this.range = range;
            this.displayPrefix = displayPrefix;
            this.displaySuffix = displaySuffix;
            this.secondaryDisplaySuffix = secondaryDisplaySuffix;
            this.displayPriority = displayPriority;
        }

        public String getDisplaySuffix(boolean contains) {
            return contains ? this.displaySuffix : this.secondaryDisplaySuffix;
        }

        public int getId() {
            return ordinal();
        }

        private String getName() {
            return ChatColor.stripColor(getPrefix()).split(":")[0];
        }

        public static ArmorAttributeType getById(int id) {
            for (ArmorAttributeType at : values())
                if (at.getId() == id)
                    return at;
            return null;
        }

        public static ArmorAttributeType getByName(String name) {
            for (ArmorAttributeType at : values())
                if (at.getName().equals(name))
                    return at;
            return null;
        }

        public static ArmorAttributeType getByNBTName(String name) {
            for (ArmorAttributeType at : values())
                if (at.getNBTName().equals(name))
                    return at;
            return null;
        }
    }

    @Getter
    public enum ElementalAttribute {

        FIRE(ChatColor.RED, WeaponAttributeType.FIRE_DAMAGE, null, ArmorAttributeType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE, DamageCause.FIRE_TICK, DamageCause.FIRE, DamageCause.LAVA),
        ICE(ChatColor.BLUE, WeaponAttributeType.ICE_DAMAGE, PotionEffectType.SLOW, ArmorAttributeType.ICE_RESISTANCE, PotionEffectType.WATER_BREATHING, new DamageCause[0]),
        POISON(ChatColor.DARK_GREEN, WeaponAttributeType.POISON_DAMAGE, PotionEffectType.POISON, ArmorAttributeType.POISON_RESISTANCE, PotionEffectType.SLOW_DIGGING, DamageCause.POISON),
        PURE(ChatColor.GOLD, WeaponAttributeType.PURE_DAMAGE, null, null, PotionEffectType.WITHER, new DamageCause[0]);

        private ChatColor color;
        private WeaponAttributeType attack;
        private ArmorAttributeType resist;
        private PotionEffectType attackPotion;
        private PotionEffectType defensePotion;
        private DamageCause[] damageCauses;

        ElementalAttribute(ChatColor color, WeaponAttributeType attack, PotionEffectType attackPotion, ArmorAttributeType resist, PotionEffectType defensePotion, DamageCause... causes) {
            this.color = color;
            this.attack = attack;
            this.resist = resist;
            this.attackPotion = attackPotion;
            this.defensePotion = defensePotion;
            this.damageCauses = causes;
        }

        public static ElementalAttribute getByName(String name) {
            for (ElementalAttribute ea : values())
                if (ea.name().equalsIgnoreCase(name))
                    return ea;
            return null;
        }

        public static ElementalAttribute getByAttribute(AttributeType type) {
            for (ElementalAttribute ea : values())
                if (ea.getAttack() == type || ea.getResist() == type)
                    return ea;
            return null;
        }

        public String getPrefix() {
            return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
        }
    }
}
