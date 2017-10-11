package net.dungeonrealms.game.player.inventory.menus.guis.merchant;

import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.common.util.ChatUtil;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import net.dungeonrealms.game.player.inventory.menus.guis.merchant.rewards.AbstractMerchantReward;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rar349 on 8/10/2017.
 */
public class ItemMerchant extends GUIMenu {

    private int page;

    public ItemMerchant(Player player) {
        this(player,0);
    }

    public ItemMerchant(Player player, int page) {
        super(player, 27, "Item Merchant (" + (page + 1) + "/" + MerchantItems.getNumberOfPages() + ")");
        this.page = page;
    }

    @Override
    protected void setItems() {
        for(MerchantItems merchantItem : MerchantItems.values()) {
            if(merchantItem.getGuiPageNumber() != page) continue;
            AbstractMerchantReward reward = merchantItem.getReward();
            List<String> lore = new ArrayList<>();
            lore.add(" ");
            lore.addAll(reward.getLore());
            lore.add(" ");
            lore.add(ChatColor.GRAY + "Ingredients:");
            lore.addAll(reward.getIngredients());
            setItem(merchantItem.getGuiSlot(), new GUIItem(reward.getDisplay().getItemType()).setDurability(reward.getDisplay().getData()).setName(merchantItem.getDisplayName()).setLore(lore).setClick((evt) -> {
                try {
                    player.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + "Merchant");
                    player.sendMessage(ChatColor.GREEN + "Please enter the number you wish to purchase");
                    player.closeInventory();
                    Chat.listenForNumber(getPlayer(), (purchasing) -> {
                        if (purchasing == null || purchasing <= 0) {
                            player.sendMessage(ChatColor.RED + "You must purchase at least one!");
                            return;
                        }

                        int maxNumberCanPurchase = reward.getMaxCanAfford(player);
                        if (maxNumberCanPurchase <= 0) {
                            player.sendMessage(ChatColor.RED + "You can not afford " + (reward.getNumberOfItemsPerPurchase()) + " " + merchantItem.getDisplayName());
                            open(player, null);
                            return;
                        }

                        if (purchasing > maxNumberCanPurchase) purchasing = maxNumberCanPurchase;

                        if (!reward.canPurchase(player, purchasing)) {
                            player.sendMessage(ChatColor.RED + "You can not purchase this!");
                            open(player, null);
                            return;
                        }


                        if (!reward.canAfford(player, purchasing)) {
                            player.sendMessage(ChatColor.RED + "You can not afford " + (purchasing * reward.getNumberOfItemsPerPurchase()) + " " + merchantItem.getDisplayName());
                            open(player, null);
                            return;
                        }


                        if (reward.takeRequirements(player, purchasing)) {
                            reward.giveReward(player, purchasing);
                            player.sendMessage(ChatColor.GREEN + "Successfully purchased " + (purchasing * reward.getNumberOfItemsPerPurchase()) + " " + merchantItem.getDisplayName());
                            open(player, null);
                        } else {
                            player.sendMessage(ChatColor.RED + "You can not afford this!");
                            open(player, null);
                            return;
                        }

                    }, () -> {
                        player.sendMessage(ChatColor.RED + "Merchant purchase cancelled!");
                        open(player, null);
                        return;
                    });
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        if(page > 0) {
            setItem(18, new GUIItem(Material.BARRIER).setName(ChatColor.RED + "Previous Page").setClick((evt) -> {
                page--;
                open(player, null);
            }));
        }
        if(MerchantItems.getNumberOfPages() > page) {
            setItem(26, new GUIItem(Material.ARROW).setName(ChatColor.GOLD + "Next Page").setClick((evt) -> {
                page++;
                open(player,null);
            }));
        }

        for(int k = 0; k < getSize(); k++) {
            if(getItem(k) != null) continue;
            setItem(k, new GUIItem(Material.STAINED_GLASS_PANE).setName(" ").setDurability(DyeColor.WHITE.getWoolData()));
        }
    }
}
