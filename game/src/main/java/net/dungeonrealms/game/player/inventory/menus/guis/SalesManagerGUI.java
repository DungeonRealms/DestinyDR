package net.dungeonrealms.game.player.inventory.menus.guis;

import com.google.common.collect.Lists;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.shops.SoldShopItem;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class SalesManagerGUI extends GUIMenu {

    private static DecimalFormat format = new DecimalFormat("#,###.##");

    private int page = 1;

    public SalesManagerGUI(Player player) {
        super(player, 54 + 9, "Sales Manager (1 / " + (int) Math.ceil(ShopMechanics.getRecentlySoldItems().size() / (double) (ShopMechanics.getRecentlySoldItems().size() > 54 ? 53 : 54)) + ")");
    }

    @Override
    protected void setItems() {

        clear();
        LinkedList<SoldShopItem> items = ShopMechanics.getRecentlySoldItems();
        int perPage = 54;
        int totalPages = (int) Math.ceil(items.size() / (double) perPage);

        int startIndex = perPage * page - perPage;
        int endIndex = startIndex + perPage;

        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (i >= items.size()) {
                break;
            }
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
            setItem(slot++, new GUIItem(is));
        }

        for (int i = getSize() - 9; i < getSize(); i++) {
            setItem(i, new GUIItem(ItemManager.createItem(Material.STAINED_GLASS_PANE, (short) DyeColor.GRAY.getWoolData(), "")));
        }

        if (totalPages > 1) {
            //Next Page still available?
            if (page < totalPages) {
                setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.ARROW, ChatColor.GRAY + ChatColor.BOLD.toString() + "Next Page",
                        ChatColor.GRAY + "Click to view the next page.")).setClick(event -> {
                    page = Math.min(page + 1, totalPages);
                    setItems();
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, .2F, 1.4F);
                }));
            }

            if (page > 1) { //Previous page.
                setItem(getSize() - 9, new GUIItem(ItemManager.createItem(Material.ARROW, ChatColor.GRAY + ChatColor.BOLD.toString() + "Previous Page",
                        ChatColor.GRAY + "Click to view the previous page.")).setClick(event -> {
                    page = Math.max(page - 1, 1);
                    setItems();
                    player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, .2F, 1.4F);
                }));
            }
        }

        updateWindowTitle(player, "Sales Manager (" + page + " / " + totalPages + ")");
        player.updateInventory();
    }
}
