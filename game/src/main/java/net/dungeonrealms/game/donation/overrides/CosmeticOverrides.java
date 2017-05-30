package net.dungeonrealms.game.donation.overrides;

import lombok.Getter;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;

/**
 * Created by Rar349 on 5/25/2017.
 */
@Getter
public enum CosmeticOverrides {

    WIZARD_HAT("Wizard Hat", "A stylish wizard hat", ChatColor.DARK_BLUE, Material.SAPLING, (short) 4, EquipmentSlot.HEAD, Purchaseables.WIZARD_HAT),
    CROWN("Golden Crown", "A stylish golden crown", ChatColor.GOLD, Material.SAPLING, (short) 2, EquipmentSlot.HEAD, Purchaseables.CROWN),
    DRAGON_MASK("Dragon Mask", "A scary Dragon Mask", ChatColor.LIGHT_PURPLE, Material.SKULL_ITEM, (short)5, EquipmentSlot.HEAD, Purchaseables.DRAGON_MASK),

    COAL_ORE_HAT("T1 Ore Helmet", "A helmet made of precious ore", ChatColor.BLACK, Material.COAL_ORE, (short) 0, EquipmentSlot.HEAD, Purchaseables.COAL_ORE_HAT),
    EMERALD_ORE_HAT("T2 Ore Helmet", "A helmet made of precious ore", ChatColor.GREEN, Material.EMERALD_ORE, (short) 0, EquipmentSlot.HEAD, Purchaseables.EMERALD_ORE_HAT),
    IRON_ORE_HAT("T3 Ore Helmet", "A helmet made of precious ore", ChatColor.WHITE, Material.IRON_ORE, (short) 0, EquipmentSlot.HEAD, Purchaseables.IRON_ORE_HAT),
    DIAMOND_ORE_HAT("T4 Ore Helmet", "A helmet made of precious ore", ChatColor.AQUA, Material.DIAMOND_ORE, (short) 0, EquipmentSlot.HEAD, Purchaseables.DIAMOND_ORE_HAT),
    GOLD_ORE_HAT("T5 Ore Helmet", "A helmet made of precious ore", ChatColor.GOLD, Material.GOLD_ORE, (short) 0, EquipmentSlot.HEAD, Purchaseables.GOLD_ORE_HAT);

    String displayName;
    String description;
    ChatColor nameColor;
    Material itemType;
    short durability;
    EquipmentSlot equipSlot;
    Purchaseables linkedPurchaseable;

    CosmeticOverrides(String displayName, String description, ChatColor nameColor, Material itemType, short durability, EquipmentSlot slot, Purchaseables linkedPurchaseable) {
        this.displayName = displayName;
        this.description = description;
        this.nameColor = nameColor;
        this.itemType = itemType;
        this.durability = durability;
        this.equipSlot = slot;
        this.linkedPurchaseable = linkedPurchaseable;
    }

    public static CosmeticOverrides getOverrideFromPurchaseable(Purchaseables webItem) {
        for (CosmeticOverrides toReturn : CosmeticOverrides.values()) {
            if (toReturn.getLinkedPurchaseable().equals(webItem)) return toReturn;
        }

        return null;
    }

    public static CosmeticOverrides getByName(String name) {
        return Arrays.stream(values()).filter(c -> c.name().equals(name)).findFirst().orElse(null);
    }
}
