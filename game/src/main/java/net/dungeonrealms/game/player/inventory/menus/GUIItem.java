package net.dungeonrealms.game.player.inventory.menus;

import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GUIItem extends ShopItem {

    @Getter
    private Consumer<InventoryClickEvent> clickCallback;
    public GUIItem(ItemStack item) {
        super(item);
    }

    @Override
    public void loadItem() {
    }

    public GUIItem setClick(Consumer<InventoryClickEvent> event){
        this.clickCallback = event;
        return this;
    }

}
