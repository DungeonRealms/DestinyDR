package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 5/10/2017.
 */
public class CategoryGUI extends GUIMenu {

    public CategoryGUI(Player player) {
        super(player, fitSize(WebstoreCategories.values().length + 1), "Purchaseable Categories");
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;



        for(WebstoreCategories cat : WebstoreCategories.values()) {
            ItemStack displayStack = new ItemStack(cat.getDisplayItem());
            if(cat.equals(WebstoreCategories.HATS)) displayStack.setDurability((short)4);
            setItem(cat.getGuiSlot(), new GUIItem(displayStack).setName(cat.getDisplayNameColor().toString() + ChatColor.BOLD + cat.getName()).setLore(cat.getDescription()).setClick((evt) -> {
                if(evt.getClick() == ClickType.LEFT) {
                    WebstoreCategories.getGUI(cat,player).open(player,evt.getAction());
                }
            }));
        }

        setItem(3, new GUIItem(Material.NAME_TAG).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Pets").setLore("", ChatColor.GRAY + "Click here to display all pets!").setClick((evt) -> {
            if(evt.getClick() == ClickType.LEFT) {
                new PetSelectionGUI(player, this).open(player,evt.getAction());
            }
        }));

    }
}
