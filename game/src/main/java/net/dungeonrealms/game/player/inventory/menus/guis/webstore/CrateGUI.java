package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.overrides.CosmeticOverrides;
import net.dungeonrealms.game.donation.overrides.OverrideListener;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.Crate;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.Crates;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Rar349 on 5/13/2017.
 */
public class CrateGUI extends GUIMenu implements WebstoreGUI {

    public CrateGUI(Player player) {
        super(player, 9, "Mystery Crate Selection");
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            List<String> lore = webItem.getDescription();
            boolean unlocked = webItem.isUnlocked(wrapper);

            /*boolean selected = unlocked && wrapper.getActiveHatOverride() != null && wrapper.getActiveHatOverride().getLinkedPurchaseable().equals(webItem);
            if (selected) {
                unlockedLine = ChatColor.GREEN + ChatColor.BOLD.toString() + "SELECTED";
            }

            lore.add(unlockedLine);
            if (selected)
                lore.add(ChatColor.GRAY + "Click to deactivate this hat!");
            else if (unlocked)
                lore.add(ChatColor.GRAY + "Click to activate this hat!");*/

            lore.add("");
            lore.add(webItem.getOwnedDisplayString(wrapper));
            lore.add("");
            lore.add(ChatColor.GREEN + "Left Click:" + ChatColor.GRAY + " Open Crate");
            lore.add(ChatColor.GREEN + "Right Click:" + ChatColor.GRAY + " View Rewards");

            ItemStack toDisplay = new ItemStack(webItem.getItemType());
            toDisplay.setDurability((short) webItem.getMeta());
            setItem(webItem.getGuiSlot(), new GUIItem(toDisplay).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + webItem.getName()).setLore(lore).setClick((evt) -> {
                Crates crate = null;
                if (webItem == Purchaseables.VOTE_CRATE) crate = Crates.VOTE_CRATE;
                if(evt.getClick().equals(ClickType.LEFT)) {
                    if (!unlocked) {
                        sendNotUnlocked(player);
                        return;
                    }
//                    BlockIterator itr = new BlockIterator(this, maxDistance);

                    List<Block> sight = player.getLineOfSight((Set<Material>)null,2);

                    Location toPlay = sight.get(sight.size() - 1).getLocation();
                    Crate crateObj = crate.getCrate(player, toPlay);
                    webItem.setNumberOwned(wrapper, webItem.getNumberOwned(wrapper) - 1);
                    crateObj.setOnComplete(() -> {
//                        webItem.setNumberOwned(wrapper, webItem.getNumberOwned(wrapper) - 1);
                    });
                    crateObj.open();
                    player.closeInventory();
                } else {
                    player.closeInventory();
                    new CrateRewardGUI(player, crate).open(player,evt.getAction());
                }
            }));
        }
    }

    @Override
    public WebstoreCategories getCategory() {
        return WebstoreCategories.CRATES;
    }
}
