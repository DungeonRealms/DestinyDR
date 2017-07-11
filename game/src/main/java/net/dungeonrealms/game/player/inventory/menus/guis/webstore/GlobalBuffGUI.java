package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.items.core.AuraType;
import net.dungeonrealms.game.item.items.functional.ItemLootAura;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

import java.util.List;


/**
 * Created by Rar349 on 5/10/2017.
 */
public class GlobalBuffGUI extends GUIMenu implements WebstoreGUI {


    public GlobalBuffGUI(Player player) {
        super(player, 27, "Global Buffs and Auras");
        setShouldOpenPreviousOnClose(true);
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;

        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            GlobalBuffs buff = GlobalBuffs.getGlobalBuff(webItem);

            List<String> lore = webItem.getDescription();
            lore.add("");
            lore.add(webItem.getOwnedDisplayString(wrapper));
            setItem(webItem.getGuiSlot(), new GUIItem(webItem.getItemType()).setName(webItem.getName()).setLore(lore).setClick((evt) -> {
                int numOwned = webItem.getNumberOwned(wrapper);//Could be different now when we are clicking? Dont use the old one from above.
                if (buff == null) {
                    if (webItem == Purchaseables.LOOT_AURA) {
                        if (numOwned <= 0) {
                            player.sendMessage(ChatColor.RED + "You do not have any Loot Auras left!");
                            player.sendMessage(ChatColor.GRAY + "You can receive Daily Loot Aura's with " + ChatColor.GREEN + ChatColor.BOLD + "SUB" + ChatColor.GRAY + " at " + ChatColor.UNDERLINE + Constants.SHOP_URL);
                            return;
                        }
                        webItem.setNumberOwned(wrapper, numOwned - 1);
                        GameAPI.giveOrDropItem(player, new ItemLootAura(AuraType.LOOT, 10, 600).generateItem());
                        reconstructGUI(player);
                        return;
                    }
                }
                if (numOwned <= 0) {
                    player.sendMessage(ChatColor.RED + "You do not have any " + buff.getBuffPower() + "% " + buff.getBuffCategory().getFriendlyName() + "s left!");
                    player.sendMessage(ChatColor.GRAY + "You can get some more at " + ChatColor.UNDERLINE + Constants.SHOP_URL);
                    return;
                }

                setShouldOpenPreviousOnClose(false);
                player.closeInventory();

                player.sendMessage("");
                Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                        ChatColor.BOLD + buff.getBuffCategory().getFriendlyName().toUpperCase() + " CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                player.sendMessage(ChatColor.GOLD + "Are you sure you want to use this item? It will apply a " + buff.getBuffPower() +
                        "% buff to all across all servers for " + buff.getFormattedTime()
                        + ". This cannot be undone once it has begun.");

                if (DonationEffects.getInstance().hasBuff(buff.getBuffCategory()))
                    player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing " + buff.getBuffCategory().getItemName() + " buff, so your buff " +
                            "will be activated afterwards. Cancel if you do not wish to queue yours.");

                player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                player.sendMessage("");
                Chat.promptPlayerConfirmation(player, () -> {
                    GameAPI.sendNetworkMessage("buff", buff.getBuffCategory().name(), buff.getDuration() + "", buff.getBuffPower() + "",
                            PlayerWrapper.getWrapper(player).getChatName(), DungeonRealms.getShard().getShardID());
                    webItem.setNumberOwned(wrapper, numOwned - 1);
                }, () -> {
                    player.sendMessage(ChatColor.RED + buff.getBuffCategory().getFriendlyName().toUpperCase() + " - CANCELLED");
                });
            }));

        }
    }


    @Override
    public GUIMenu getPreviousGUI() {
        return new CategoryGUI(player);
    }

    @Override
    public void open(Player player, InventoryAction action) {
        super.open(player, action);
    }

    public WebstoreCategories getCategory() {
        return WebstoreCategories.GLOBAL_BUFFS;
    }

}
