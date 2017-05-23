package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;

import java.util.List;
import java.util.UUID;

/**
 * Created by Rar349 on 5/10/2017.
 */
public class SubscriptionsGUI extends GUIMenu implements WebstoreGUI {


    public SubscriptionsGUI(Player player) {
        super(player, 9, "Subscriptions");
        setShouldOpenPreviousOnClose(true);
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;


        for (Purchaseables webItem : Purchaseables.values()) {
            if (webItem.getCategory() != getCategory()) continue;
            List<String> lore = webItem.getDescription();
            lore.add(" ");
            boolean isUnlocked = isSubUnlocked(webItem);
            lore.add(isUnlocked ? ChatColor.GREEN.toString() + ChatColor.BOLD + "UNLOCKED" : ChatColor.RED.toString() + ChatColor.BOLD + "LOCKED");
            if (isUnlocked) {
                lore.add("");
                lore.add(ChatColor.RED + ChatColor.BOLD.toString() + "Expires in");
                String expires = getRankExpireTime();
                lore.add(ChatColor.RED + expires);
//                lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "EXPIRES IN " + expires);
            }
            setItem(webItem.getGuiSlot(), new GUIItem(webItem.getItemType()).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + webItem.getName()).setLore(lore).setClick((evt) -> {
                if(isUnlocked) {
                    player.sendMessage(ChatColor.GREEN + "Thank you for purchasing " + webItem.getName());
                    player.sendMessage(ChatColor.GRAY + "Extend your subscription at " + ChatColor.UNDERLINE + Constants.STORE_URL);
                } else {
                    player.sendMessage(ChatColor.RED + "You have not purchased this rank!");
                    player.sendMessage(ChatColor.GRAY + "Unlock it at " + ChatColor.UNDERLINE + Constants.STORE_URL);
                }
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
        return WebstoreCategories.SUBSCRIPTIONS;
    }

    public boolean isSubUnlocked(Purchaseables webItem) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return false;
        if (wrapper.getRank().isSUB() && (webItem.equals(Purchaseables.SUB_PLUS_PLUS))) return true;
        if (wrapper.getRank().isSubPlus() && (webItem.equals(Purchaseables.SUB_PLUS))) return true;
        if (wrapper.getRank().isSUB() && (webItem.equals(Purchaseables.SUB))) return true;
        return false;
    }

    public String getRankExpireTime() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return "ERROR";
        int subLength = checkSubscription(player.getUniqueId(), wrapper.getRankExpiration());
        return subLength == -1 ? "NEVER EXPIRES" : ((subLength) + " DAY" + (subLength != 1 ? "S" : ""));
    }

    public int checkSubscription(UUID uuid, int expiration) {
        PlayerRank rank = Rank.getPlayerRank(uuid);
        if (rank == PlayerRank.SUB || rank == PlayerRank.SUB_PLUS) {
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            int endTime = expiration;
            int timeRemaining = (currentTime == 0 || endTime == 0 ? 0 : (endTime - currentTime));
            return (int) (timeRemaining <= 0 ? 0 : Math.ceil(timeRemaining / 86400.0));
        }
        return -1;
    }

}
