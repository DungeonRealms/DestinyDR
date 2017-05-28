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
            boolean unlocked = webItem.isUnlocked(wrapper);
            String unlockedLine = webItem.getOwnedDisplayString(wrapper);

            if(unlocked && wrapper.getActiveHatOverride() != null && wrapper.getActiveHatOverride().getLinkedPurchaseable().equals(webItem)) {
                    unlockedLine = ChatColor.AQUA + ChatColor.BOLD.toString() + "SELECTED";
            }

            lore.add(unlockedLine);

            ItemStack toDisplay = new ItemStack(webItem.getItemType());
            toDisplay.setDurability((short) webItem.getMeta());
            setItem(webItem.getGuiSlot(), new GUIItem(toDisplay).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + webItem.getName()).setLore(lore).setClick((evt) -> {
                if(!unlocked) {
                    sendNotUnlocked(player);
                    return;
                }
                player.sendMessage(ChatColor.GREEN + "You have activated your " + ChatColor.UNDERLINE.toString() + webItem.getName(false));
                wrapper.setActiveHatOverride(CosmeticOverrides.getOverrideFromPurchaseable(webItem));
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
