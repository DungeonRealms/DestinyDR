package net.dungeonrealms.game.player.inventory.menus.guis;

import com.google.common.collect.Lists;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.shops.SoldShopItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class SalesManagerGUI extends GUIMenu {

    private static DecimalFormat format = new DecimalFormat("#,###.##");

    public SalesManagerGUI(Player player) {
        super(player, 54, "Sales Manager");
    }

    @Override
    protected void setItems() {
        LinkedList<SoldShopItem> items = ShopMechanics.getRecentlySoldItems();
        for (int i = 0; i < getSize(); i++) {
            if (i >= items.size()) break;
            SoldShopItem item = items.get(i);

            ItemStack is = item.getItemSold().clone();

            ItemMeta im = is.getItemMeta();
            List<String> lore = im.getLore() == null ? Lists.newArrayList() : im.getLore();

            lore.add("");
            lore.add(ChatColor.GRAY + ChatColor.STRIKETHROUGH.toString() + "----------------------------");
            lore.add("");
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Seller: " + ChatColor.GREEN + item.getSeller());
            lore.add(ChatColor.GRAY + "Purchased by " + ChatColor.GREEN + item.getBuyer() + ChatColor.GRAY + " for " +
                    ChatColor.GREEN + format.format(item.getSoldPrice()) + ChatColor.GREEN + ChatColor.BOLD + " GEM(S)");
            lore.add("");
            im.setLore(lore);
            is.setItemMeta(im);
            setItem(i, new GUIItem(is));
        }
    }
}
