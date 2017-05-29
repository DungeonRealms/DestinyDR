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
    CROWN("Golden Crown", "A stylish golden crown", ChatColor.GOLD, Material.SAPLING, (short) 2, EquipmentSlot.HEAD, Purchaseables.CROWN);

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
