package net.dungeonrealms.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.anticheat.AntiCheat;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;

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
        List<String> list = new NameGenerator().next(type);
        meta.setDisplayName(tier.getChatColorOfTier(tier) + list.get(0) + " " + list.get(1) + " " + list.get(2));
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
                        i *= 2;
                        break;
                    case STAFF:
                        i *= 0.8;
                        break;
                    default:
                        break;
                }
            }
            attributeTypeIntegerHashMap.put(aType, i);
            itemLore.add(setCorrectItemLore(aType, i, tier.getTierId()));
        });
        itemLore.add(modifier.getChatColorOfModifier(modifier).toString() + modifier.getName());
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
    private Item.ItemType getRandomItemType() {
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
        return Item.ItemModifier.getById(new Random().nextInt(Item.ItemModifier.values().length));
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
                return "" + ChatColor.RED + Math.round((i - (i / damageRandomizer))) + ChatColor.WHITE + " - " + ChatColor.RED + Math.round((i + (i / damageRandomizer))) + ChatColor.WHITE + " " + aType.getName();
            case VS_MONSTERS:
                return "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
            case VS_PLAYER:
                return "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
            case CRITICAL_HIT:
                return "" + ChatColor.RED + i + "% " + ChatColor.YELLOW + aType.getName();
            case LIFE_STEAL:
                return "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
            case FIRE_DAMAGE:
                return "" + ChatColor.RED + i + " " + ChatColor.DARK_RED + aType.getName();
            case ICE_DAMAGE:
                return "" + ChatColor.RED + i + " " + ChatColor.BLUE + aType.getName();
            case POISON_DAMAGE:
                return "" + ChatColor.RED + i + " " + ChatColor.DARK_GREEN + aType.getName();
            case ACCURACY:
                return "" + ChatColor.RED + i + "% " + ChatColor.WHITE + aType.getName();
            default:
                return "" + ChatColor.RED + i + " " + ChatColor.WHITE + aType.getName();
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
}
