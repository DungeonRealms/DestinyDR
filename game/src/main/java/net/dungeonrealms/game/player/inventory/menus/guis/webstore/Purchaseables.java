package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 5/10/2017.
 */
@Getter
public enum Purchaseables {

    LOOT_BUFF_20("Loot Buff", "20% global loot buff across all\nshards for every player!", Material.DIAMOND, true, WebstoreCategories.GLOBAL_BUFFS,0),
    LOOT_BUFF_40("Loot Buff", "40% global loot buff across all\nshards for every player!", Material.DIAMOND, true, WebstoreCategories.GLOBAL_BUFFS,9),
    PROFESSION_BUFF_20("Profession Buff", "20% global profession buff across all\nshards for every player!", Material.GOLDEN_CARROT, true, WebstoreCategories.GLOBAL_BUFFS,4),
    PROFESSION_BUFF_40("Profession Buff", "40% global profession buff across all\nshards for every player!", Material.GOLDEN_CARROT, true, WebstoreCategories.GLOBAL_BUFFS,13),
    LEVEL_BUFF_20("Level Buff", "20% global level experience buff across all\nshards for every player!", Material.EXP_BOTTLE, true, WebstoreCategories.GLOBAL_BUFFS,8),
    LEVEL_BUFF_40("Level Buff", "40% global level experience buff across all\nshards for every player!", Material.EXP_BOTTLE, true, WebstoreCategories.GLOBAL_BUFFS,17);

    private String name;
    private boolean canHaveMultiple;
    private String description;
    private WebstoreCategories category;
    private Material itemType;
    private int guiSlot;

    Purchaseables(String name, String description, Material itemType, boolean hasMultiples, WebstoreCategories category, int guiSlot) {
        this.name = name;
        this.canHaveMultiple = hasMultiples;
        this.description = description;
        this.category = category;
        this.itemType = itemType;
        this.guiSlot = guiSlot;
    }

    public List<String> getDescription() {
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(description.split("\n")));
        return toReturn;
    }

    public static int getNumberOfItems(WebstoreCategories category) {
        int num = 0;
        for(Purchaseables item : Purchaseables.values()) {
            if(item.getCategory() == category) num++;
        }

        return num;
    }

}
