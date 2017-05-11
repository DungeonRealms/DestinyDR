package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;


/**
 * Created by Rar349 on 5/10/2017.
 */
public class GlobalBuffGUI extends GUIMenu implements WebstoreGUI {


    public GlobalBuffGUI(Player player) {
        super(player, 18, "Global Buffs");
        setShouldOpenPreviousOnClose(true);
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;

        /*setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.BARRIER, ChatColor.GREEN + "Back"))
                .setClick(e -> new CategoryGUI(player).open(player,e.getAction())));*/

        for(Purchaseables webItem : Purchaseables.values()) {
            if(webItem.getCategory() != getCategory()) continue;
            setItem(webItem.getGuiSlot(), new GUIItem(webItem.getItemType()).setName(webItem.getName()).setLore(webItem.getDescription()).setClick((evt) -> {
                player.sendMessage("Buff button clicked!");
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
        return WebstoreCategories.GLOBAL_BUFFS;
    }

}
