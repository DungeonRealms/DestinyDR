package net.dungeonrealms.game.player.inventory.menus;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
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

    public GUIItem setClick(Consumer<InventoryClickEvent> event) {
        this.clickCallback = event;
        return this;
    }

    public GUIItem setLore(String... strings) {
        return setLore(Lists.newArrayList(strings));
    }

    public GUIItem setLore(List<String> string) {
        ItemMeta im = item.getItemMeta();
        im.setLore(string);
        item.setItemMeta(im);
        return this;
    }
}
