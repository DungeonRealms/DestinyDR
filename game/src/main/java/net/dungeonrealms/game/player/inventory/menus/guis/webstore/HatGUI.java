package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Rar349 on 5/13/2017.
 */
public class HatGUI extends GUIMenu implements WebstoreGUI {

    public HatGUI(Player player) {
        super(player, 9,"Cosmetic Hat Overrides");
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            List<String> lore = webItem.getDescription();
            lore.add(" ");
            lore.add(webItem.getOwnedDisplayString(wrapper));

            ItemStack toDisplay = new ItemStack(webItem.getItemType());
            toDisplay.setDurability((short) webItem.getMeta());
            setItem(webItem.getGuiSlot(), new GUIItem(toDisplay).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + webItem.getName()).setLore(lore).setClick((evt) -> {
                player.sendMessage("Hat button clicked!");
            }));
        }
    }

    @Override
    public WebstoreCategories getCategory() {
        return WebstoreCategories.HATS;
    }
}
