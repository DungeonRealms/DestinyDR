package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


/**
 * Created by Rar349 on 5/10/2017.
 */
public class LootBuffGUI extends GUIMenu implements WebstoreGUI {


    public LootBuffGUI(Player player) {
        super(player, fitSize(Purchaseables.getNumberOfItems(WebstoreCategories.LOOT_BUFFS)), "Loot Buffs");
    }

    @Override
    protected void setItems() {
        int slot = 0;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.BARRIER, ChatColor.GREEN + "Back"))
                .setClick(e -> player.sendMessage("Back button click!")));

        for(Purchaseables webItem : Purchaseables.values()) {
            if(webItem.getCategory() != getCategory()) continue;
            ItemStack toDisplay = new ItemStack(webItem.getItemType());
            ItemMeta meta = toDisplay.getItemMeta();
            meta.setDisplayName(webItem.getName());
            meta.setLore(webItem.getDescription());
            toDisplay.setItemMeta(meta);
            setItem(slot++, new GUIItem(toDisplay).setClick((evt) -> {
                player.sendMessage("Buff button clicked!");
            }));
        }

    }

    public WebstoreCategories getCategory() {
        return WebstoreCategories.LOOT_BUFFS;
    }

}
