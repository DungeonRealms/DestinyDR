package net.dungeonrealms.game.player.inventory.menus;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.item.items.core.ShopItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.function.Consumer;

public class GUIItem extends ShopItem {

    @Getter
    private Consumer<InventoryClickEvent> clickCallback;

    public GUIItem(ItemStack item) {
        super(item);
        item.getItemMeta().addItemFlags(ItemFlag.values());
    }

    public GUIItem(Material material) {
        this(new ItemStack(material, 1));
    }

    @Override
    public void loadItem() {
    }

    public GUIItem setEnchanted(boolean enchant) {

        if (enchant)
            this.item.addUnsafeEnchantment(EnchantmentAPI.getGlowEnchant(), 1);
        else if (item.containsEnchantment(EnchantmentAPI.getGlowEnchant()))
            this.item.removeEnchantment(EnchantmentAPI.getGlowEnchant());

        return this;
    }

    public GUIItem setClick(Consumer<InventoryClickEvent> event) {
        this.clickCallback = event;
        return this;
    }

    public GUIItem setECashCost(int ecash) {
        this.setECash(ecash);
        ItemMeta im = item.getItemMeta();
        List<String> lore = im.getLore() == null ? Lists.newArrayList() : im.getLore();
        lore.add(ChatColor.WHITE.toString() + ecash + ChatColor.GREEN + " E-Cash");
        im.setLore(lore);
        item.setItemMeta(im);
        return this;
    }

    public GUIItem setSkullOwner(String name) {
        ItemMeta im = item.getItemMeta();
        if (!(im instanceof SkullMeta)) return this;
        SkullMeta sm = (SkullMeta) im;
        sm.setOwner(name);
        item.setItemMeta(sm);
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
