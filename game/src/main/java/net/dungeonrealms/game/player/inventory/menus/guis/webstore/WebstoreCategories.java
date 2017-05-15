package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import lombok.Getter;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
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

    SUBSCRIPTIONS("Subscriptions", "\nClick here to view all subscription ranks!", Material.EMERALD,0, ChatColor.GREEN),
    GLOBAL_BUFFS("Global Buffs", "\nClick here to view all global buffs!", Material.DIAMOND,8, ChatColor.AQUA),
    HATS("Cosmetic Hat Overrides", "\nClick here to view all hat overrides!", Material.SAPLING,4, ChatColor.WHITE, ChatColor.DARK_BLUE),
    MISCELLANEOUS("Misc Items", "\nClick here to view all miscellaneous items!", Material.BLAZE_ROD,5, ChatColor.GOLD);

    private String name;
    private String description;
    private Material displayItem;
    private ChatColor displayNameColor;
    private ChatColor displayDescriptionColor;
    private int guiSlot;

    WebstoreCategories(String name, String description, Material displayItem, int guiSlot) {
        this(name,description,displayItem, guiSlot,ChatColor.WHITE);
    }

    WebstoreCategories(String name, String description, Material displayItem,int guiSlot, ChatColor displayNameColor) {
        this(name,description,displayItem, guiSlot,displayNameColor, ChatColor.GRAY);
    }

    WebstoreCategories(String name, String description, Material displayItem,int guiSlot, ChatColor displayNameColor, ChatColor displayDescColor) {
        this.name = name;
        this.description = description;
        this.displayItem = displayItem;
        this.displayNameColor = displayNameColor;
        this.displayDescriptionColor = displayDescColor;
        this.guiSlot = guiSlot;
    }

    public List<String> getDescription(boolean includeColor) {
        List<String> toReturn = new ArrayList<>();
        toReturn.addAll(Arrays.asList(description.split("\n")));
        if(includeColor) {
            for (int index = 0; index < toReturn.size(); index++) {
                String line = toReturn.get(index);
                toReturn.set(index, getDisplayDescriptionColor().toString() + line);
            }
        }
        return toReturn;
    }

    public List<String> getDescription() {
        return getDescription(true);
    }

    public static GUIMenu getGUI(WebstoreCategories category, Player player) {
        if(category == GLOBAL_BUFFS) return new GlobalBuffGUI(player);
        if(category == SUBSCRIPTIONS) return new SubscriptionsGUI(player);
        if(category == MISCELLANEOUS) return new MiscGUI(player);
        if(category == HATS) return new HatGUI(player);
        return null;
    }

}
