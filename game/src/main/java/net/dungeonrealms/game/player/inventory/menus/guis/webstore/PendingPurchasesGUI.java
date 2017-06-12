package net.dungeonrealms.game.player.inventory.menus.guis.webstore;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.banks.CurrencyTab;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rar349 on 5/13/2017.
 */
public class PendingPurchasesGUI extends GUIMenu {

    public PendingPurchasesGUI(Player player, int size) {
        super(player, fitSize(size), player.getName() + "'s Pending Purchases");
    }

    @Override
    protected void setItems() {
        int slot = 0;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return;

        List<PendingPurchaseable> pendingItems = new ArrayList<>();
        pendingItems.addAll(wrapper.getPendingPurchaseablesUnlocked());

        pendingItems.sort(new Comparator<PendingPurchaseable>() {
            @Override
            public int compare(PendingPurchaseable o1, PendingPurchaseable o2) {
                return o1.getTimePurchased().compareTo(o2.getTimePurchased());
            }
        });

        for (PendingPurchaseable item : pendingItems) {
            List<String> lore = new ArrayList<>();
            lore.addAll(item.getPurchaseables().getDescription());
            lore.add("");
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Amount: " + ChatColor.GREEN + ChatColor.UNDERLINE.toString() + item.getNumberPurchased());
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Purchased By: " + ChatColor.GREEN + ChatColor.UNDERLINE.toString() + item.getWhoPurchased() + " (" + item.getWhoPurchaseEnjinID() + ")");
            lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "Purchased: " + ChatColor.GREEN + ChatColor.UNDERLINE.toString() + item.getTimePurchased());
            lore.add("");
            lore.add(ChatColor.GRAY + ChatColor.BOLD.toString() + "Left Click" + ChatColor.GRAY + " to " + ChatColor.GREEN + ChatColor.BOLD + "CONFIRM");
            lore.add(ChatColor.GRAY + ChatColor.BOLD.toString() + "Right Click" + ChatColor.GRAY + " to " + ChatColor.RED + ChatColor.BOLD + "DENY");
            System.out.println(item.getPurchaseables().name() + " has meta " + item.getPurchaseables().getMeta());
            GUIItem pendingItem = new GUIItem(item.getPurchaseables().getItemType()).setDurability((short) item.getPurchaseables().getMeta()).setName(item.getPurchaseables().getName(true)).setLore(lore);
            if (item.getPurchaseables().getCategory().equals(WebstoreCategories.PETS)) {
                EnumPets pets = (EnumPets) item.getPurchaseables().getSpecialArgs()[0];
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString("id", pets.getEntityType().getName());
                pendingItem.getTag().set("EntityTag", compound);
            }
            pendingItem.setClick((evt) -> {
                if (evt.getClick() == ClickType.LEFT) {

                    Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                            ChatColor.BOLD + item.getPurchaseables().getName(false).toUpperCase() + " CLAIM CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                    Utils.sendCenteredMessage(player, ChatColor.GOLD + "Are you sure you want to claim this purchase?");
                    Utils.sendCenteredMessage(player, ChatColor.RED + "This cannot be undone!");

                    player.sendMessage("");

                    player.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + "WARNING!" + ChatColor.RED + " If you claim this and the buyer " + ChatColor.RED + ChatColor.UNDERLINE + "charges back" + ChatColor.RED + " you will be " + ChatColor.RED + ChatColor.BOLD.toString() + "PERMANENTLY BANNED!");
//                    player.sendMessage( ChatColor.DARK_RED + ChatColor.BOLD.toString() + "WARNING!" + ChatColor.RED + " Claiming items you did not purchase could result in a " + ChatColor.RED + ChatColor.BOLD.toString() + "PERMANENT BAN!");
//                    player.sendMessage(ChatColor.RED + "If you claim this and the buyer charges back you will be" + ChatColor.DARK_RED + ChatColor.BOLD.toString() + " PERMANENTLY BANNED");

                    player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + ChatColor.BOLD + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                    player.sendMessage("");
                    Chat.promptPlayerConfirmation(player, () -> {
                        if (item.getPurchaseables().isSpecialCaseClaim()) {
                            handleSpecialCaseClaim(wrapper, item);
                        } else {
                            boolean didRemove = wrapper.getPendingPurchaseablesUnlocked().remove(item);
                            if (!didRemove) {
                                player.sendMessage(ChatColor.RED + "Oops! Something went wrong, sorry! Please try again!");
                                return;
                            }
                            item.getPurchaseables().addNumberUnlocked(wrapper, item.getNumberPurchased(), null);
                            player.sendMessage(ChatColor.GREEN + "Successfully claimed " + item.getNumberPurchased() + " " + item.getPurchaseables().getName() + ChatColor.GREEN + " from " + item.getWhoPurchased());
                            wrapper.updatePurchaseLog("claimed", item.getTransactionId(), System.currentTimeMillis(), player.getUniqueId().toString());
                        }
                    }, () -> {
                        player.sendMessage(ChatColor.RED + item.getPurchaseables().getName(false).toUpperCase() + " CLAIMING - CANCELLED");
                    });
                } else if (evt.getClick() == ClickType.RIGHT) {
                    Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                            ChatColor.BOLD + item.getPurchaseables().getName(false).toUpperCase() + " DENY CONFIRMATION" + ChatColor.DARK_GRAY + "***");
                    player.sendMessage(ChatColor.GOLD + "Are you sure you want to deny this purchase? This cannot be undone!");

                    player.sendMessage("");
                    player.sendMessage(ChatColor.DARK_RED + ChatColor.BOLD.toString() + " WARNING!" + ChatColor.RED + " Denying pending purchases is permanent! Your purchase will NOT be refunded!");
                    Utils.sendCenteredMessage(player, ChatColor.RED + "If you deny this you can never get it back!");

                    Utils.sendCenteredMessage(player, ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                    player.sendMessage("");
                    Chat.promptPlayerConfirmation(player, () -> {
                        boolean didRemove = wrapper.getPendingPurchaseablesUnlocked().remove(item);
                        if (!didRemove) {
                            player.sendMessage(ChatColor.RED + "Oops! Something went wrong, sorry! Please try again!");
                            return;
                        }
                        SQLDatabaseAPI.getInstance().executeUpdate(null, wrapper.getQuery(QueryType.UPDATE_PURCHASES, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID()));
                        player.sendMessage(ChatColor.GREEN + "Successfully denied " + item.getNumberPurchased() + " " + item.getPurchaseables().getName() + ChatColor.GREEN + " from " + item.getWhoPurchased());
                        wrapper.updatePurchaseLog("denied", item.getTransactionId(), System.currentTimeMillis(), player.getUniqueId().toString());
                    }, () -> {
                        player.sendMessage(ChatColor.RED + item.getPurchaseables().getName(false).toUpperCase() + " CLAIMING - CANCELLED");
                    });
                }
            });
            setItem(slot++, pendingItem);
        }
    }

    public void handleSpecialCaseClaim(PlayerWrapper wrapper, PendingPurchaseable toClaim) {
        Purchaseables purchaseable = toClaim.getPurchaseables();
        if (purchaseable.equals(Purchaseables.SUB_PLUS_PLUS)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB_PLUS_PLUS, 0);
        } else if (purchaseable.equals(Purchaseables.SUB_MONTHLY)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(30));
        } else if (purchaseable.equals(Purchaseables.SUB_PLUS_MONTHLY)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB_PLUS, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(30));
        } else if (purchaseable.equals(Purchaseables.SUB_ONE_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(30));
        } else if (purchaseable.equals(Purchaseables.SUB_PLUS_ONE_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB_PLUS, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(30));
        } else if (purchaseable.equals(Purchaseables.SUB_THREE_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(90));
        } else if (purchaseable.equals(Purchaseables.SUB_PLUS_THREE_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB_PLUS, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(90));
        } else if (purchaseable.equals(Purchaseables.SUB_SIX_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(180));
        } else if (purchaseable.equals(Purchaseables.SUB_PLUS_SIX_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB_PLUS, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(180));
        } else if (purchaseable.equals(Purchaseables.SUB_TWELVE_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(365));
        } else if (purchaseable.equals(Purchaseables.SUB_PLUS_TWELVE_MONTH)) {
            handleSpecialCaseClaimRank(wrapper, toClaim, PlayerRank.SUB_PLUS, (System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(365));
        } else if (purchaseable.equals(Purchaseables.SCRAP_TAB)) {
            CurrencyTab tab = wrapper.getCurrencyTab();
            if (tab != null) {
                tab.hasAccess = true;
            } else {
                tab = new CurrencyTab(wrapper.getUuid());
                tab.hasAccess = true;
                wrapper.setCurrencyTab(tab);
            }
        } else {
            throw new IllegalArgumentException("Missing logic for special case purchaseable!");
        }

        /*

                                        CurrencyTab tab = wrapper.getCurrencyTab();
                                if (tab != null) {
                                    tab.hasAccess = access;
                                } else if (access) {
                                    tab = new CurrencyTab(online.getUniqueId());
                                    tab.hasAccess = true;
                                    wrapper.setCurrencyTab(tab);
                                }
         */
    }

    public void handleSpecialCaseClaimRank(PlayerWrapper wrapper, PendingPurchaseable toClaim, PlayerRank toSet, long expireTime) {
        wrapper.setRank(toSet);
        wrapper.setRankExpiration((int) expireTime);
        Rank.getCachedRanks().put(wrapper.getUuid(), wrapper.getRank());
        SQLDatabaseAPI.getInstance().executeUpdate((rows) -> {
            if (rows == null || rows <= 0) {
                wrapper.getPlayer().sendMessage(ChatColor.RED + "Something went wrong while trying to claim your rank!");
                wrapper.getPlayer().sendMessage(ChatColor.GRAY + "Please try again later or contact a staff member if the problem persists!");
                return;
            }
            boolean didRemove = wrapper.getPendingPurchaseablesUnlocked().remove(toClaim);
            if (!didRemove) {
                player.sendMessage(ChatColor.RED + "Oops! Something went wrong, sorry! Please try again!");
                return;
            }
            wrapper.updatePurchaseLog("claimed", toClaim.getTransactionId(), System.currentTimeMillis(), player.getUniqueId().toString());
            wrapper.executeUpdate(QueryType.UPDATE_PURCHASES, null, wrapper.getPurchaseablesUnlocked(), wrapper.getSerializedPendingPurchaseables(), wrapper.getAccountID());
            GameAPI.sendNetworkMessage("Rank", wrapper.getUuid().toString(), toSet.getInternalName());
        }, QueryType.SET_RANK.getQuery(wrapper.getRank().getInternalName(), wrapper.getAccountID()));
    }
}
