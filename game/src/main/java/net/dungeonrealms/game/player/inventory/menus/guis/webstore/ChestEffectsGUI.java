package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import com.google.common.collect.Lists;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Rar349 on 6/30/2017.
 */
public class ChestEffectsGUI extends GUIMenu implements WebstoreGUI {

    public ChestEffectsGUI(Player player) {
        super(player, fitSize(Purchaseables.getNumberOfItems(WebstoreCategories.CHEST_EFFECTS)), "Chest Effects");
    }

    @Override
    public WebstoreCategories getCategory() {
        return WebstoreCategories.CHEST_EFFECTS;
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            List<String> lore = Lists.newArrayList();
            lore.addAll(webItem.getDescription(true));
            lore.add("");
            lore.add(webItem.getOwnedDisplayString(wrapper));
            setItem(webItem.getGuiSlot(), new GUIItem(webItem.getItemType()).setName(webItem.getName())
                    .setLore(lore).setDurability((short)0)
                    .setClick(evt -> {
                        boolean unlocked = webItem.isUnlocked(wrapper);
                            if (unlocked) {
                                SpecialParticles effectEnum = (SpecialParticles) webItem.getSpecialArgs()[0];
                                Shop currentOpenShop = ShopMechanics.getShop(player.getName());
                                if(wrapper.getActiveChestEffect() != null && wrapper.getActiveChestEffect().equals(effectEnum)) {
                                    wrapper.setActiveChestEffect(null);
                                    if(currentOpenShop != null) currentOpenShop.setChestEffect(null);
                                    player.sendMessage(ChatColor.GREEN + "You have deactivated your current chest effect!");
                                    return;
                                }
                                wrapper.setActiveChestEffect(effectEnum);
                                if(currentOpenShop != null) currentOpenShop.applyChestEffect(effectEnum);
                                player.sendMessage(ChatColor.GREEN + "Enjoy your Chest Effect!");
                            } else {
                                sendNotUnlocked(player);
                            }

                            setShouldOpenPreviousOnClose(false);
                            player.closeInventory();
                    }));
        }
    }
}
