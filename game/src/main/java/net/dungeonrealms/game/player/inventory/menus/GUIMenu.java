package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import net.dungeonrealms.game.player.inventory.ShopMenuListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public abstract class GUIMenu extends ShopMenu {

    public GUIMenu(Player player, int size, String title) {
        super(player, size, title);
    }

    @Override
    protected abstract void setItems();

    public void setItem(int index, GUIItem shopItem) {
        this.items.put(index, shopItem);
        this.inventory.setItem(index, shopItem.getItem());
    }

    public void setItem(int index, ItemStack item) {
        GUIItem is = new GUIItem(item);
        this.items.put(index, is);
        this.inventory.setItem(index, is.getItem());
    }

    public GUIMenu setCloseCallback(Consumer<Player> event) {
        this.closeCallback = event;
        return this;
    }

    public void open(Player player, InventoryAction action) {
        if (player == null)
            return;
        this.player = player;


        ShopMenu menu = ShopMenuListener.getMenus().get(player);
        if (menu != null) {
            if (menu.getTitle() != null && menu.getTitle().equals(getTitle()) && menu.getSize() == getSize()) {
                setItems();
                return;
            }
            //Delay the next inventory click by 1.
            if (action != null && action.name().startsWith("PICKUP_")) {
                //CAnt close the inventory on a pickup_all action etc otherwise throws exceptions.
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    player.closeInventory();
                    reopenWithDelay(player);
                });
                return;
            }
            reopenWithDelay(player);
            return;
        }

        this.setItems();
        player.openInventory(getInventory());
        ShopMenuListener.getMenus().put(player, this);
    }

}
