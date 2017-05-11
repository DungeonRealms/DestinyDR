package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;


/**
 * Created by Rar349 on 5/10/2017.
 */
public class MiscGUI extends GUIMenu implements WebstoreGUI {


    public MiscGUI(Player player) {
        super(player, 9, "Miscellaneous");
        setShouldOpenPreviousOnClose(true);
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for(Purchaseables webItem : Purchaseables.values()) {
            if(webItem.getCategory() != getCategory()) continue;
            ItemStack displayItem = new ItemStack(webItem.getItemType());
            displayItem.setDurability(DyeColor.YELLOW.getDyeData());
            setItem(webItem.getGuiSlot(), new GUIItem(displayItem).setName(webItem.getName()).setLore(webItem.getDescription()).setClick((evt) -> {
                player.sendMessage("Misc button clicked!");
            }));
        }
    }


    @Override
    public GUIMenu getPreviousGUI() {
        return new CategoryGUI(player);
    }

    @Override
    public void open(Player player, InventoryAction action) {
        super.open(player, action);
    }

    public WebstoreCategories getCategory() {
        return WebstoreCategories.MISCELLANEOUS;
    }

}
