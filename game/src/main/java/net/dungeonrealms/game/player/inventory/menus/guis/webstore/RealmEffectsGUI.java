package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import com.google.common.collect.Lists;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.Realms;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Rar349 on 6/30/2017.
 */
public class RealmEffectsGUI extends GUIMenu implements WebstoreGUI {

    public RealmEffectsGUI(Player player) {
        super(player, fitSize(Purchaseables.getNumberOfItems(WebstoreCategories.REALM_EFFECTS)), "Realm Effects");
    }

    @Override
    public WebstoreCategories getCategory() {
        return WebstoreCategories.REALM_EFFECTS;
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
                                Realm realm = Realms.getInstance().getRealm(player.getUniqueId());
                                if(wrapper.getActiveRealmEffect() != null && wrapper.getActiveRealmEffect().equals(effectEnum)) {
                                    wrapper.setActiveRealmEffect(null);
                                    if(realm != null) realm.setRealmEffect(null);
                                    player.sendMessage(ChatColor.GREEN + "You have deactivated your current realm effect!");
                                    return;
                                }
                                wrapper.setActiveRealmEffect(effectEnum);
                                if(realm != null) realm.applyRealmEffect(effectEnum);
                                player.sendMessage(ChatColor.GREEN + "Enjoy your Realm Effect!");
                            } else {
                                sendNotUnlocked(player);
                            }

                            setShouldOpenPreviousOnClose(false);
                            player.closeInventory();
                    }));
        }
    }
}
