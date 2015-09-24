package net.dungeonrealms.items;

import net.dungeonrealms.mastery.Utils;
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
     * @return
     * @since 1.0
     */
    public ItemStack getDefinedStack(Item.ItemType type, Item.ItemTier tier, Item.ItemModifier modifier) {
        return getWeapon(type, tier, modifier);
    }

    /**
     * allows, new ItemGenerator().next() -> ItemStack.
     *
     * @return
     * @since 1.0
     */
    public ItemStack next() {
        return getWeapon(getRandomItemType(), getRandomItemTier(), getRandomItemModifier());
    }

    public ItemStack next(Item.ItemType type, Item.ItemTier tier) {
        return getWeapon(type, tier, getRandomItemModifier());
    }

    /**
     * Used for the next() method above.
     *
     * @param type
     * @param tier
     * @param modifier
     * @return
     * @since 1.0
     */
    ItemStack getWeapon(Item.ItemType type, Item.ItemTier tier, Item.ItemModifier modifier) {
        ItemStack item = getBaseItem(type, tier);
        ArrayList<Item.AttributeType> attributeTypes = getRandomAttributes(new Random().nextInt(tier.getAttributeRange()));
        ItemMeta meta = item.getItemMeta();
        List<String> list = new NameGenerator().next(type);
        meta.setDisplayName(ChatColor.GRAY + "[" + ChatColor.WHITE + "T" + tier.getTierId() + ChatColor.GRAY + "]" + " " + list.get(0) + " " + list.get(1) + " " + list.get(2));
        List<String> itemLore = new ArrayList<>();
        itemLore.add(ChatColor.WHITE + "One handed          " + type.getName());

        HashMap<Item.AttributeType, Integer> attributeTypeIntegerHashMap = new HashMap<>();

        for (Item.AttributeType aType : attributeTypes) {
            int i = new DamageMeta().nextWeapon(tier, modifier, aType);
            attributeTypeIntegerHashMap.put(aType, i);
            itemLore.add(ChatColor.GREEN + "+" + ChatColor.WHITE + i + " " + aType.getName());
        }
        itemLore.add(ChatColor.GRAY + "Requires Level: " + ChatColor.GOLD + String.valueOf(tier.getRangeValues()[0]));
        itemLore.add(ChatColor.GRAY + "Item Level: " + ChatColor.GOLD + 738);
        itemLore.add(ChatColor.GRAY + "Item Tier: " + ChatColor.GOLD + tier.getTierId());
        itemLore.add(ChatColor.GRAY + "Item Rarity: " + modifier.getName());
        meta.setLore(itemLore);
        item.setItemMeta(meta);

        //Time for some NMS on the item, (Backend attributes for reading).
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("weapon"));

        //Settings NBT for the Attribute Class. () -> itemType, itemTier, itemModifier
        tag.set("itemType", new NBTTagInt(type.getId()));
        tag.set("itemTier", new NBTTagInt(tier.getId()));
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

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Gets a random ItemType
     *
     * @return
     * @since 1.0
     */
    public Item.ItemType getRandomItemType() {
        return Item.ItemType.getById(new Random().nextInt(Item.ItemType.values().length - 0) + 0);
    }

    /**
     * Gets a radnom ItemTier
     *
     * @return
     * @since 1.0
     */
    public Item.ItemTier getRandomItemTier() {
        return Item.ItemTier.getById(new Random().nextInt(Item.ItemTier.values().length - 0) + 0);
    }

    /**
     * Gets a random ItemModifier
     *
     * @return
     * @since 1.0
     */
    public Item.ItemModifier getRandomItemModifier() {
        return Item.ItemModifier.getById(new Random().nextInt(Item.ItemModifier.values().length - 0) + 0);
    }

    /**
     * Returns a list of itemAttributes based on the param.
     *
     * @param amountOfAttributes
     * @return
     * @since 1.0
     */
    ArrayList<Item.AttributeType> getRandomAttributes(int amountOfAttributes) {
        ArrayList<Item.AttributeType> attributeList = new ArrayList<>();
        //We always want to add Damage to the Item. Since AttributeModifiers are removed. Completely.
        attributeList.add(Item.AttributeType.DAMAGE);
        for (int i = 0; i < amountOfAttributes; i++) {
            int random = new Random().nextInt(Item.AttributeType.values().length);
            if (!attributeList.contains(Item.AttributeType.getById(random))) {
                attributeList.add(Item.AttributeType.getById(random));
            } else {
                i--;
            }
        }
        return attributeList;
    }

    /**
     * Returns ItemStack Material based on item type and tier.
     *
     * @param type
     * @param tier
     * @return
     * @since 1.0
     */
    ItemStack getBaseItem(Item.ItemType type, Item.ItemTier tier) {
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
