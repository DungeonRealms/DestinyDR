package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 5/10/2017.
 */
@Getter
public enum WebstoreCategories {

    GLOBAL_BUFFS("Global Buffs", "Click here to view all global buffs!", Material.DIAMOND);

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
        if(category == GLOBAL_BUFFS) return new GlobalBuffGUI(player);
        return null;
    }

}
