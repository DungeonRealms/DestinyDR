package net.dungeonrealms.game.world.items;

import net.dungeonrealms.API;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.items.Item.AttributeType;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.armor.Armor;
import net.dungeonrealms.game.world.items.armor.Armor.ArmorAttributeType;
import net.dungeonrealms.game.world.items.armor.Armor.ArmorModifier;
import net.dungeonrealms.game.world.items.armor.Armor.ArmorTier;
import net.dungeonrealms.game.world.items.armor.Armor.EquipmentType;
import net.dungeonrealms.game.world.items.armor.ArmorGenerator;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/19/2015.
 */
public class ItemGenerator {

    /**
     * Get a defined "random"istic item.
     *
     * @param type
     * @param tier
     * @param modifier
     * @return ItemStack
     * @since 1.0
     */
    public ItemStack getDefinedStack(Item.ItemType type, Item.ItemTier tier, Item.ItemModifier modifier) {
        return getWeapon(type, tier, modifier);
    }

    /**
     * allows, new ItemGenerator().next() -> ItemStack.
     *
     * @return getWeapon
     * @since 1.0
     */
    public ItemStack next() {
        return getWeapon(getRandomItemType(), getRandomItemTier(), getRandomItemModifier());
    }

    /**
     * allows, new ItemGenerator().next() -> ItemStack.
     *
     * @return getWeapon
     * @since 1.0
     */
    public ItemStack next(Item.ItemTier tier) {
        return getWeapon(getRandomItemType(), tier, getRandomItemModifier());
    }

    /**
     * allows, new ItemGenerator().next() -> ItemStack.
     *
     * @return getWeapon
     * @since 1.0
     */
    public ItemStack next(Item.ItemType type, Item.ItemTier tier) {
        return getWeapon(type, tier, getRandomItemModifier());
    }

    public ItemStack next(Item.ItemType type, Item.ItemTier tier, Item.ItemModifier itemModifier) {
        return getWeapon(type, tier, itemModifier);
    }

    /**
     * Used for the next() method above.
     *
     * @param type
     * @param tier
     * @param modifier
     * @return ItemStack
     * @since 1.0
     */
    private ItemStack getWeapon(Item.ItemType type, Item.ItemTier tier, Item.ItemModifier modifier) {
        ItemStack item = getBaseItem(type, tier);
        ArrayList<Item.AttributeType> attributeTypes = getRandomAttributes(new Random().nextInt(tier.getAttributeRange()), type);
        assert item != null;
        ItemMeta meta = item.getItemMeta();
        //List<String> list = new NameGenerator().next(type);
        //meta.setDisplayName(tier.getChatColorOfTier(tier) + list.get(0) + " " + list.get(1) + " " + list.get(2));
        List<String> itemLore = new ArrayList<>();

        HashMap<Item.AttributeType, Integer> attributeTypeIntegerHashMap = new HashMap<>();

        attributeTypes.stream().filter(aType -> aType != null).forEach(aType -> {
            int i = new DamageMeta().nextWeapon(tier, modifier, aType);
            if (aType == Item.AttributeType.DAMAGE) {
                switch (type) {
                    case AXE:
                        i *= 1.25;
                        break;
                    case BOW:
                        i *= 1.75;
                        break;
                    case STAFF:
                        i *= 0.8;
                        break;
                    case POLE_ARM:
                        i *= 0.65;
                        break;
                    default:
                        break;
                }
            }
            attributeTypeIntegerHashMap.put(aType, i);
            itemLore.add(setCorrectItemLore(aType, i, tier.getTierId()));
        });
        meta.setDisplayName(tier.getChatColorOfTier(tier) + getWeaponName(type, attributeTypes));
        itemLore.add(modifier.getChatColorOfModifier(modifier) + modifier.getName());
        meta.setLore(itemLore);
        item.setItemMeta(meta);

        RepairAPI.setCustomItemDurability(item, 1500);

        //Time for some NMS on the item, (Backend attributes for reading).
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("weapon"));

        //Settings NBT for the Attribute Class. () -> itemType, itemTier, itemModifier
        tag.set("itemType", new NBTTagInt(type.getId()));
        tag.set("itemTier", new NBTTagInt(tier.getTierId()));
        tag.set("itemModifier", new NBTTagInt(modifier.getId()));
        tag.set("bound", new NBTTagString("false"));

        /*
        The line below removes the weapons attributes.
        E.g. Diamond Sword says, "+7 Attack Damage"
         */
        tag.set("AttributeModifiers", new NBTTagList());

        for (Map.Entry<Item.AttributeType, Integer> entry : attributeTypeIntegerHashMap.entrySet()) {
            tag.set(entry.getKey().getNBTName(), new NBTTagInt(entry.getValue()));
        }

        nmsStack.setTag(tag);

        return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
    }

    /**
     * Gets a random ItemType
     *
     * @return Item.ItemType
     * @since 1.0
     */
    public static Item.ItemType getRandomItemType() {
        return Item.ItemType.getById(new Random().nextInt(Item.ItemType.values().length));
    }

    /**
     * Gets a radnom ItemTier
     *
     * @return Item.ItemTier
     * @since 1.0
     */
    private Item.ItemTier getRandomItemTier() {
        return Item.ItemTier.getById(new Random().nextInt(Item.ItemTier.values().length));
    }

    /**
     * Gets a random ItemModifier
     *
     * @return Item.ItemModifier
     * @since 1.0
     */
    public static Item.ItemModifier getRandomItemModifier() {
        return API.getItemModifier();
    }

    /**
     * Gets a random ItemModifier
     *
     * @return Item.ItemModifier
     * @since 1.0
     */
    public static Item.AttributeType getRandomItemAttribute() {
        return Item.AttributeType.getById(new Random().nextInt(Item.AttributeType.values().length));
    }

    /**
     * Returns a list of itemAttributes based on the param.
     *
     * @param amountOfAttributes
     * @param itemType
     * @return ArrayList
     * @since 1.0
     */
    private ArrayList<Item.AttributeType> getRandomAttributes(int amountOfAttributes, Item.ItemType itemType) {
        ArrayList<Item.AttributeType> attributeList = new ArrayList<>();
        //We always want to add Damage to the Item. Since AttributeModifiers are removed. Completely.
        attributeList.add(Item.AttributeType.DAMAGE);
        for (int i = 0; i < amountOfAttributes; i++) {
            int random = new Random().nextInt(Item.AttributeType.values().length);
            if ((!attributeList.contains(Item.AttributeType.getById(random))) && canAddAttribute(Item.AttributeType.getById(random), itemType, attributeList)) {
                attributeList.add(Item.AttributeType.getById(random));
            } else {
                i--;
            }
        }
        return attributeList;
    }


    /**
     * Returns if the attribute selected
     * can be applied to the weapon
     *
     * @param attributeType
     * @param attributeList
     * @param itemType
     * @return boolean
     * @since 1.0
     */
    private static boolean canAddAttribute(Item.AttributeType attributeType, Item.ItemType itemType, ArrayList<Item.AttributeType> attributeList) {
        if (attributeType == Item.AttributeType.FIRE_DAMAGE || attributeType == Item.AttributeType.ICE_DAMAGE || attributeType == Item.AttributeType.POISON_DAMAGE) {
            return !attributeList.contains(Item.AttributeType.FIRE_DAMAGE) && !attributeList.contains(Item.AttributeType.ICE_DAMAGE) && !attributeList.contains(Item.AttributeType.POISON_DAMAGE);
        }
        if (attributeType == Item.AttributeType.VS_PLAYER || attributeType == Item.AttributeType.VS_MONSTERS) {
            return !attributeList.contains(Item.AttributeType.VS_MONSTERS) && !attributeList.contains(Item.AttributeType.VS_PLAYER);
        }
        if (attributeType == Item.AttributeType.VITALITY || attributeType == Item.AttributeType.DEXTERITY || attributeType == Item.AttributeType.INTELLECT || attributeType == Item.AttributeType.STRENGTH) {
            return !attributeList.contains(Item.AttributeType.VITALITY) && !attributeList.contains(Item.AttributeType.DEXTERITY) && !attributeList.contains(Item.AttributeType.INTELLECT) && !attributeList.contains(Item.AttributeType.STRENGTH);
        }
        if (attributeType == Item.AttributeType.PURE_DAMAGE || attributeType == Item.AttributeType.ARMOR_PENETRATION) {
            return itemType == Item.ItemType.AXE;
        }
        return attributeType != Item.AttributeType.ACCURACY || itemType == Item.ItemType.SWORD;
    }

    /**
     * Calculates the weapons name based
     * on the stats it has
     *
     * @param attributeList
     * @param itemType
     * @return String
     * @since 1.0
     */
    public static String getWeaponName(ItemType itemType, ArrayList<AttributeType> attributeList) {
        String weaponName = itemType.getName();
        if (attributeList.contains(AttributeType.PURE_DAMAGE)) {
            weaponName = "Pure " + weaponName;
        }
        if (attributeList.contains(AttributeType.ACCURACY)) {
            weaponName = "Accurate " + weaponName;
        }
        if (attributeList.contains(AttributeType.LIFE_STEAL)) {
            if (itemType == ItemType.BOW) {
                weaponName = "Lifestealing " + weaponName;
            } else {
                weaponName = "Vampyric " + weaponName;
            }
        }
        if (attributeList.contains(AttributeType.CRITICAL_HIT)) {
            weaponName = "Deadly " + weaponName;
        }
        if (attributeList.contains(AttributeType.ARMOR_PENETRATION)) {
            weaponName = "Penetrating " + weaponName;
        }
        if (attributeList.contains(AttributeType.VS_PLAYER)) {
            if (!weaponName.contains("of")) {
                weaponName += " of Slaughter";
            } else {
                weaponName += " Slaughter";
            }
        }
        if (attributeList.contains(AttributeType.VS_MONSTERS)) {
            if (!weaponName.contains("of")) {
                weaponName += " of Slaying";
            } else {
                weaponName += " Slaying";
            }
        }
        if (attributeList.contains(AttributeType.ICE_DAMAGE)) {
            if (!weaponName.contains("of")) {
                weaponName += " of Ice";
            } else {
                weaponName += " Ice";
            }
        }
        if (attributeList.contains(AttributeType.FIRE_DAMAGE)) {
            if (!weaponName.contains("of")) {
                weaponName += " of Fire";
            } else {
                weaponName += " Fire";
            }
        }
        if (attributeList.contains(AttributeType.POISON_DAMAGE)) {
            if (!weaponName.contains("of")) {
                weaponName += " of Poison";
            } else {
                weaponName += " Poison";
            }
        }
        return weaponName;
    }

    /**
     * Returns Max/Min damage variable on weapons
     *
     * @param itemTier
     * @return int
     * @since 1.0
     */
    public static int getRandomDamageVariable(int itemTier) {
        switch (itemTier) {
            case 1:
                return 2;
            case 2:
                return 5;
            case 3:
                return 7;
            case 4:
                return 11;
            case 5:
                return 14;
            default:
                return 8;
        }
    }

    /**
     * Returns Max/Min lore for a weapon
     * based on the Attribute Type
     * includes chat colouring
     *
     * @param aType
     * @param i
     * @param tierID
     * @return String
     * @since 1.0
     */
    public static String setCorrectItemLore(Item.AttributeType aType, int i, int tierID) {
        switch (aType) {
            case DAMAGE:
                int damageRandomizer = getRandomDamageVariable(tierID);
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + Math.round((i - (i / damageRandomizer))) + ChatColor.WHITE + " - " + ChatColor.RED + Math.round((i + (i / damageRandomizer)));
            case VS_MONSTERS:
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + i + "%";
            case VS_PLAYER:
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + i + "%";
            case CRITICAL_HIT:
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + i + "%";
            case LIFE_STEAL:
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + i + "%";
            case ACCURACY:
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + i + "%";
            default:
                return ChatColor.WHITE + aType.getName() + ": " + ChatColor.RED + i;
        }
    }

    /**
     * Returns ItemStack Material based on item type and tier.
     *
     * @param type
     * @param tier
     * @return ItemStack
     * @since 1.0
     */
    private ItemStack getBaseItem(Item.ItemType type, Item.ItemTier tier) {
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
            case POLE_ARM:
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
            default:
                Utils.log.warning("ItemGenerator couldn't find getBaseItem().. " + type.getName());
        }
        return null;
    }


    /**
     * reroll stats on weapons and armor
     *
     * @param stack
     * @return
     */
    public ItemStack reRoll(ItemStack stack) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
        Attribute attribute = new Attribute(stack);
        if (API.isWeapon(stack)) {
            ArrayList<Item.AttributeType> attributeTypes = getRandomAttributes(new Random().nextInt(attribute.getItemTier().getAttributeRange()), attribute.getItemType());
            ItemTier tier = attribute.getItemTier();
            ItemMeta meta = stack.getItemMeta();
            List<String> itemLore = new ArrayList<>();

            if (meta.getLore() != null)
                itemLore.addAll(meta.getLore().stream().filter(lore -> lore.startsWith(ChatColor.WHITE + "DMG: ")).collect(Collectors.toList()));
            HashMap<Item.AttributeType, Integer> attributeTypeIntegerHashMap = new HashMap<>();
            ItemType type = attribute.getItemType();
            attributeTypes.stream().filter(aType -> aType != null && aType != AttributeType.DAMAGE).forEach(aType -> {
                int i = new DamageMeta().nextWeapon(attribute.getItemTier(), attribute.getItemModifier(), aType);
                attributeTypeIntegerHashMap.put(aType, i);
                itemLore.add(setCorrectItemLore(aType, i, tier.getTierId()));
            });
            itemLore.add(attribute.getItemModifier().getChatColorOfModifier(attribute.getItemModifier()) + attribute.getItemModifier().getName());
            meta.setLore(itemLore);
            stack.setItemMeta(meta);

            RepairAPI.setCustomItemDurability(stack, 1500);
            NBTTagCompound tag = new NBTTagCompound();
            tag.set("type", new NBTTagString("weapon"));

            //Settings NBT for the Attribute Class. () -> itemType, itemTier, itemModifier
            tag.set("itemType", new NBTTagInt(type.getId()));
            tag.set("itemTier", new NBTTagInt(tier.getTierId()));
            tag.set("itemModifier", new NBTTagInt(attribute.getItemModifier().getId()));

        /*
        The line below removes the weapons attributes.
        E.g. Diamond Sword says, "+7 Attack Damage"
         */
            tag.set("AttributeModifiers", new NBTTagList());

            for (Map.Entry<Item.AttributeType, Integer> entry : attributeTypeIntegerHashMap.entrySet()) {
                tag.set(entry.getKey().getNBTName(), new NBTTagInt(entry.getValue()));
            }

            nmsStack.setTag(tag);

            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));
        } else if (API.isArmor(stack)) {
            ArrayList<ArmorAttributeType> attributeTypes = new ArmorGenerator().getRandomAttributes(new Random().nextInt(attribute.getArmorTier().getAttributeRange()));
            List<String> itemLore = new ArrayList<>();
            ArmorTier tier = attribute.getArmorTier();
            ItemMeta meta = stack.getItemMeta();
            HashMap<ArmorAttributeType, Integer> attributeTypeIntegerHashMap = new HashMap<>();
            EquipmentType type = attribute.getArmorType();
            int modifierID = nmsStack.getTag().getInt("armorModifier");
            if (meta.getLore() != null)
                itemLore.addAll(meta.getLore().stream().filter(lore -> lore.contains("HP:") || lore.contains("HP REGEN:") || lore.contains("ENERGY REGEN: ")).collect(Collectors.toList()));


            attributeTypes.stream().filter(aType -> aType != null && aType != ArmorAttributeType.HEALTH_POINTS && aType != ArmorAttributeType.HEALTH_REGEN && aType != ArmorAttributeType.ENERGY_REGEN).forEach(aType -> {
                int i = new DamageMeta().nextArmor(tier, ArmorModifier.getById(modifierID), aType);
                attributeTypeIntegerHashMap.put(aType, i);
                itemLore.add(ArmorGenerator.setCorrectArmorLore(aType, i));
            });
            ArmorModifier modifier = ArmorModifier.getById(modifierID);
            itemLore.add(modifier.getChatColorOfModifier(modifier) + modifier.getName());
            meta.setLore(itemLore);
            stack.setItemMeta(meta);

            RepairAPI.setCustomItemDurability(stack, 1500);
            NBTTagCompound tag = new NBTTagCompound();
            tag.set("type", new NBTTagString("armor"));

            //Settings NBT for the Attribute Class. () -> itemType, itemTier, itemModifier
            tag.set("armorType", new NBTTagInt(type.getId()));
            tag.set("armorTier", new NBTTagInt(tier.getTierId()));
            tag.set("armorModifier", new NBTTagInt(modifier.getId()));

            /*
            The line below removes the weapons attributes.
            E.g. Diamond Sword says, "+7 Attack Damage"
             */
            tag.set("AttributeModifiers", new NBTTagList());

            for (Map.Entry<Armor.ArmorAttributeType, Integer> entry : attributeTypeIntegerHashMap.entrySet()) {
                tag.set(entry.getKey().getNBTName(), new NBTTagInt(entry.getValue()));
            }

            nmsStack.setTag(tag);

            return AntiCheat.getInstance().applyAntiDupe(CraftItemStack.asBukkitCopy(nmsStack));


        }
        return stack;
    }

}
