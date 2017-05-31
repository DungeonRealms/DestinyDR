package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.overrides.CosmeticOverrides;
import net.dungeonrealms.game.donation.overrides.OverrideListener;
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
        super(player, 18, "Cosmetic Hat Selection");
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            List<String> lore = webItem.getDescription();
            lore.add(" ");
            boolean unlocked = webItem.isUnlocked(wrapper);
            String unlockedLine = webItem.getOwnedDisplayString(wrapper);

            boolean selected = unlocked && wrapper.getActiveHatOverride() != null && wrapper.getActiveHatOverride().getLinkedPurchaseable().equals(webItem);
            if (selected) {
                unlockedLine = ChatColor.GREEN + ChatColor.BOLD.toString() + "SELECTED";
            }

            lore.add(unlockedLine);
            if (selected)
                lore.add(ChatColor.GRAY + "Click to deactivate this hat!");
            else if (unlocked)
                lore.add(ChatColor.GRAY + "Click to activate this hat!");

            ItemStack toDisplay = new ItemStack(webItem.getItemType());
            toDisplay.setDurability((short) webItem.getMeta());
            setItem(webItem.getGuiSlot(), new GUIItem(toDisplay).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + webItem.getName()).setLore(lore).setClick((evt) -> {
                if (!unlocked) {
                    sendNotUnlocked(player);
                    return;
                }

                CosmeticOverrides override = CosmeticOverrides.getOverrideFromPurchaseable(webItem);

                if (wrapper.getActiveHatOverride() == override) {
                    wrapper.setActiveHatOverride(null);
                    player.sendMessage(webItem.getName(false) + ChatColor.RED + " - " + ChatColor.BOLD + "DISABLED");
                    reconstructGUI(player);
                    return;
                }

                player.sendMessage(webItem.getName(false) + ChatColor.GREEN + " - " + ChatColor.GREEN + ChatColor.BOLD + "ACTIVATED");
                wrapper.setActiveHatOverride(override);
                OverrideListener.updatePlayersHat(player);
                reconstructGUI(player);
            }));
        }
    }

    @Override
    public WebstoreCategories getCategory() {
        return WebstoreCategories.HATS;
    }
}
