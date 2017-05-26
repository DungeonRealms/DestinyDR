package net.dungeonrealms.game.donation.overrides;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Created by Rar349 on 5/25/2017.
 */
@Getter
public enum CosmeticOverrides {

    WIZARD_HAT("Wizard Hat", "A stylish wizard hat", ChatColor.DARK_BLUE, Material.SAPLING, (short)4);

    String displayName;
    String description;
    ChatColor nameColor;
    Material itemType;
    short durability;

    CosmeticOverrides(String displayName, String description, ChatColor nameColor, Material itemType, short durability) {
        this.displayName = displayName;
        this.description = description;
        this.nameColor = nameColor;
        this.itemType = itemType;
        this.durability = durability;
    }
}
