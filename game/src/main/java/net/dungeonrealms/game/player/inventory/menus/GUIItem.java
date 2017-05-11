package net.dungeonrealms.game.player.inventory.menus;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.Material;
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
    public GUIItem(Material material) {
        super(new ItemStack(material, 1));
    }

    @Override
    public void loadItem() {
    }

    public GUIItem setClick(Consumer<InventoryClickEvent> event) {
        this.clickCallback = event;
        return this;
    }

    public GUIItem setDurability(short data) {
        item.setDurability(data);
        return this;
    }

    public GUIItem setName(String name) {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);
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
