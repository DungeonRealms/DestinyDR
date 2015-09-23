package net.dungeonrealms.items.armor;

import net.dungeonrealms.items.DamageMeta;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.NameGenerator;
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
 * Created by Nick on 9/21/2015.
 */
public class ArmorGenerator {
    /**
     * Get a defined ArmorStack.
     *
     * @param type
     * @param tier
     * @param modifier
     * @return
     * @since 1.0
     */
    public ItemStack getDefinedStack(Armor.EquipmentType type, Armor.ArmorTier tier, Armor.ArmorModifier modifier) {
        return getArmor(type, tier, modifier);
    }

    /**
     * Gets a random set of armor.
     *
     * @return
     * @since 1.0
     */
    public ItemStack next() {
        return getArmor(getRandomEquipmentType(), getRandomItemTier(), getRandomItemModifier());
    }

    /**
     * Used for the next() method above.
     *
     * @param tier
     * @param modifier
     * @return
     * @since 1.0
     */
    ItemStack getArmor(Armor.EquipmentType type, Armor.ArmorTier tier, Armor.ArmorModifier modifier) {
        ItemStack item = getBaseItem(type, tier);
        ArrayList<Armor.ArmorAttributeType> attributeTypes = getRandomAttributes(new Random().nextInt(tier.getAttributeRange()));
        ItemMeta meta = item.getItemMeta();
        List<String> list = new NameGenerator().next(type);
        meta.setDisplayName(list.get(0) + " " + list.get(1) + " " + list.get(2));
        List<String> itemLore = new ArrayList<>();

        HashMap<Armor.ArmorAttributeType, Integer> attributeTypeIntegerHashMap = new HashMap<>();

        for (Armor.ArmorAttributeType aType : attributeTypes) {
            int i = new DamageMeta().nextArmor(tier, modifier, aType);
            attributeTypeIntegerHashMap.put(aType, i);
            itemLore.add(ChatColor.GREEN + "+" + ChatColor.WHITE + i + " " + aType.getName());
        }
        itemLore.add(ChatColor.GRAY + "Requires Level: " + ChatColor.GOLD + String.valueOf(tier.getRangeValues()[0]));
        itemLore.add(ChatColor.GRAY + "Armor Level: " + ChatColor.GOLD + 738);
        itemLore.add(ChatColor.GRAY + "Armor Tier: " + ChatColor.GOLD + tier.getTierId());
        itemLore.add(ChatColor.GRAY + "Armor Rarity: " + modifier.getName());
        meta.setLore(itemLore);
        item.setItemMeta(meta);

        //Time for some NMS on the item, (Backend attributes for reading).
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag() == null ? new NBTTagCompound() : nmsStack.getTag();
        tag.set("type", new NBTTagString("armor"));

        //Settings NBT for the Attribute Class. () -> itemType, itemTier, itemModifier
        tag.set("armorType", new NBTTagInt(type.getId()));
        tag.set("armorTier", new NBTTagInt(tier.getId()));
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

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    /**
     * Gets a random Equipment.
     *
     * @return
     * @since 1.0
     */
    public Armor.EquipmentType getRandomEquipmentType() {
        return Armor.EquipmentType.getById(new Random().nextInt(Armor.EquipmentType.values().length - 0) + 0);
    }

    /**
     * Gets a random Armor Tier
     *
     * @return
     * @since 1.0
     */
    public Armor.ArmorTier getRandomItemTier() {
        return Armor.ArmorTier.getById(new Random().nextInt(Armor.ArmorTier.values().length - 0) + 0);
    }

    /**
     * Gets a random ArmorModifier
     *
     * @return
     * @since 1.0
     */
    public Armor.ArmorModifier getRandomItemModifier() {
        return Armor.ArmorModifier.getById(new Random().nextInt(Armor.ArmorModifier.values().length - 0) + 0);
    }

    /**
     * Returns a list of itemAttributes based on the param.
     *
     * @param amountOfAttributes
     * @return
     * @since 1.0
     */
    ArrayList<Armor.ArmorAttributeType> getRandomAttributes(int amountOfAttributes) {
        ArrayList<Armor.ArmorAttributeType> attributeList = new ArrayList<>();
        //We always want to add Damage to the Item. Since AttributeModifiers are removed. Completely.
        for (int i = 0; i < amountOfAttributes; i++) {
            int random = new Random().nextInt(Item.AttributeType.values().length);
            if (!attributeList.contains(Armor.ArmorAttributeType.getById(random))) {
                attributeList.add(Armor.ArmorAttributeType.getById(random));
            } else {
                i--;
            }
        }
        return attributeList;
    }

    /**
     * Returns ItemStack Material based on item type and tier.
     *
     * @param tier
     * @return
     * @since 1.0
     */
    ItemStack getBaseItem(Armor.EquipmentType type, Armor.ArmorTier tier) {
        switch (type) {
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
            case BOOTS:
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
        }
        return null;
    }
}

