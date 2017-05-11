package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 5/10/2017.
 */
@Getter
public enum WebstoreCategories {

    LOOT_BUFFS("Loot Buffs", "Click here to view loot buffs!", Material.GOLDEN_CARROT);

    private String name;
    private String description;
    private Material displayItem;
    WebstoreCategories(String name, String description, Material displayItem) {
        this.name = name;
        this.description = description;
        this.displayItem = displayItem;
    }

    public List<String> getDescription() {
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(description.split("\n")));
        return toReturn;
    }

    public static GUIMenu getGUI(WebstoreCategories category, Player player) {
        if(category == LOOT_BUFFS) return new LootBuffGUI(player);
        return null;
    }

}
