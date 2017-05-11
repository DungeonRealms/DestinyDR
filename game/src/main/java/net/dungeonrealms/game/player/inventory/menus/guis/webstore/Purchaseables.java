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

    LOOT_BUFF_20("Loot Buff", "20% global loot buff accross all\nshards for every player!", Material.GOLDEN_CARROT, true, WebstoreCategories.LOOT_BUFFS);

    private String name;
    private boolean canHaveMultiple;
    private String description;
    private WebstoreCategories category;
    private Material itemType;

    Purchaseables(String name, String description, Material itemType, boolean hasMultiples, WebstoreCategories category) {
        this.name = name;
        this.canHaveMultiple = hasMultiples;
        this.description = description;
        this.category = category;
        this.itemType = itemType;
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
