package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.functional.ecash.ItemDPSDummy;
import net.dungeonrealms.game.item.items.functional.ecash.ItemLightningRod;
import net.dungeonrealms.game.item.items.functional.ecash.ItemNameTag;
import net.dungeonrealms.game.item.items.functional.ecash.jukebox.ItemJukebox;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by Rar349 on 6/30/2017.
 */
public class PlayerEffectsGUI extends GUIMenu implements WebstoreGUI {

    public PlayerEffectsGUI(Player player) {
        super(player, fitSize(Purchaseables.getNumberOfItems(WebstoreCategories.PLAYER_EFFECTS)), "Player Effects");
    }

    @Override
    public WebstoreCategories getCategory() {
        return WebstoreCategories.PLAYER_EFFECTS;
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
                                if(wrapper.getActiveSpecialEffect() != null && wrapper.getActiveSpecialEffect().getParticleEnum().equals(effectEnum)) {
                                    wrapper.setActiveSpecialEffect(null);
                                    player.sendMessage(ChatColor.GREEN + "You have deactivated your current player effect!");
                                    return;
                                }
                                wrapper.setActiveSpecialEffect(SpecialParticles.constrauctEffectFromName(effectEnum.getInternalName(), player));
                                player.sendMessage(ChatColor.GREEN + "Enjoy your Player Effect!");
                            } else {
                                sendNotUnlocked(player);
                            }

                            setShouldOpenPreviousOnClose(false);
                            player.closeInventory();
                    }));
        }
    }
}
