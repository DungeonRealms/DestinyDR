package net.dungeonrealms.items;

import net.dungeonrealms.mastery.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Nick on 9/19/2015.
 */
public class ItemGenerator {

    public ItemStack next() {
        return getWeapon(getRandomItemType(), getRandomItemTier(), getRandomItemModifier());
    }

    ItemStack getWeapon(Item.ItemType type, Item.ItemTier tier, Item.ItemModifier modifier) {
        ItemStack item = getBaseItem(type, tier);
        ArrayList<Item.AttributeType> attributeTypes = getRandomAttributes(tier, new Random().nextInt(6));
        ItemMeta meta = item.getItemMeta();
        List<String> list = new NameGenerator().next();
        meta.setDisplayName(list.get(0) + " " + list.get(1) + " " + list.get(2));
        List<String> itemLore = new ArrayList<>();
        itemLore.add(ChatColor.WHITE + "Held in Main Hand");
        for (Item.AttributeType aTypes : attributeTypes) {
            itemLore.add(ChatColor.GREEN + "+" + ChatColor.WHITE + new DamageMeta().next(tier, modifier, aTypes) + " " + aTypes.getName());
        }
        itemLore.add(ChatColor.WHITE + "Requires Level " + String.valueOf(tier.getRangeValues()[0]));
        itemLore.add(ChatColor.WHITE + "Item level NILL");
        itemLore.add(ChatColor.GRAY + "Rarity: " + modifier.getName());
        itemLore.add("");
        meta.setLore(itemLore);
        item.setItemMeta(meta);
        return item;
    }

    public Item.ItemType getRandomItemType() {
        return Item.ItemType.getById(new Random().nextInt(Item.ItemType.values().length));
    }

    public Item.ItemTier getRandomItemTier() {
        return Item.ItemTier.getById(new Random().nextInt(Item.ItemTier.values().length));
    }

    public Item.ItemModifier getRandomItemModifier() {
        return Item.ItemModifier.getById(new Random().nextInt(Item.ItemModifier.values().length));
    }

    ArrayList<Item.AttributeType> getRandomAttributes(Item.ItemTier tier, int amountOfAttributes) {
        ArrayList<Item.AttributeType> attributeList = new ArrayList<>();
        attributeList.add(Item.AttributeType.DAMAGE);
        for (int i = 0; i < amountOfAttributes; i++) {
            if (!attributeList.contains(Item.AttributeType.getById(amountOfAttributes))) {
                attributeList.add(Item.AttributeType.getById(new Random().nextInt(Item.AttributeType.values().length)));
            }
        }
        return attributeList;
    }

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
                Utils.log.warning("ItemGenerator couldn't find getBaseItem().. " + type.getName() + " " + tier.getMaterial());
        }
        return null;
    }
}
