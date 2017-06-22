package net.dungeonrealms.game.player.inventory.menus;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.items.functional.ecash.ItemGlobalMessager;
import net.dungeonrealms.game.item.items.functional.ecash.ItemRetrainingBook;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.guis.ParticleEffectGUI;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.GlobalBuffs;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ShopECashVendor extends GUIMenu {
    public ShopECashVendor(Player player) {
        this(player, null);
    }

    public ShopECashVendor(Player player, GUIMenu menu) {
        super(player, 18, "E-Cash Vendor", menu);
        open(player, null);
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        AtomicInteger slot = new AtomicInteger(0);
        for (EnumBuff buff : EnumBuff.values()) {

            Arrays.stream(GlobalBuffs.values()).filter(b -> b.getBuffCategory() == buff && b.getEcashCost() > 0).forEach(bff -> {

                List<String> lore = Lists.newArrayList(
                        ChatColor.GOLD + "Duration: " + ChatColor.GRAY + buff.getFormattedTime(bff.getDuration()),
                        ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1",
                        ChatColor.GRAY + "" + ChatColor.ITALIC + buff.getDescription(),
                        ChatColor.GRAY + "" + ChatColor.ITALIC + "by " + bff.getBuffPower() + "% across " + ChatColor.UNDERLINE + "ALL SHARDS.");

                lore.add("");
                lore.add(ChatColor.WHITE.toString() + bff.getEcashCost() + " " + ChatColor.GREEN + "E-Cash");
                lore.add(ChatColor.GREEN + "Click to purchase and " + ChatColor.UNDERLINE + "activate" + ChatColor.GREEN + " this buff!");

                setItem(slot.getAndIncrement(), new GUIItem(ItemManager.createItem(buff.getIcon(), buff.getItemName())).setLore(lore).setClick(e -> {
                    if (wrapper.getEcash() < bff.getEcashCost()) {
                        player.sendMessage(ChatColor.GRAY + "You do not have enough " + ChatColor.GREEN + "E-Cash" + ChatColor.GRAY + " for this " + buff.getItemName() + ChatColor.RED + "!");
                        player.sendMessage(ChatColor.GRAY + "Current " + ChatColor.GREEN + "E-Cash: " + ChatColor.WHITE + wrapper.getEcash());
                        return;
                    }

                    player.sendMessage("");
                    Utils.sendCenteredMessage(player, ChatColor.GOLD.toString() + ChatColor.BOLD +
                            ChatColor.stripColor(buff.getItemName()).toUpperCase() + " ACTIVATION CONFIRMATION");
                    Utils.sendCenteredMessage(player, ChatColor.GOLD + "Are you sure you want to activate this Buff?");

                    Utils.sendCenteredMessage(player, ChatColor.GOLD + "It will apply a " + ChatColor.BOLD + bff.getBuffPower() +
                            "% buff" + ChatColor.GOLD + " to all " + ChatColor.GOLD + ChatColor.UNDERLINE.toString() + buff.getMiniDescription());
                    Utils.sendCenteredMessage(player, ChatColor.GOLD + "across all servers for " + ChatColor.BOLD + ChatColor.UNDERLINE + buff.getFormattedTime(bff.getDuration())
                            + ChatColor.GOLD + ".");
                    Utils.sendCenteredMessage(player, ChatColor.RED.toString() + "This cannot be undone once it has begun.");

                    if (DonationEffects.getInstance().hasBuff(buff))
                        player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing " + buff.getItemName() + " buff, so your buff " +
                                "will be activated afterwards. Cancel if you do not wish to queue yours.");
                    player.sendMessage("");
                    Utils.sendCenteredMessage(player, ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
                    player.sendMessage("");
                    Chat.promptPlayerConfirmation(player, () -> {
                        if (wrapper.getEcash() < bff.getEcashCost()) {
                            player.sendMessage(ChatColor.GRAY + "You do not have enough " + ChatColor.GREEN + "E-Cash" + ChatColor.GRAY + " for this " + buff.getItemName() + ChatColor.RED + "!");
                            player.sendMessage(ChatColor.GRAY + "Current " + ChatColor.GREEN + "E-Cash: " + ChatColor.WHITE + wrapper.getEcash());
                            return;
                        }
                        wrapper.withdrawEcash(bff.getEcashCost());
                        GameAPI.sendNetworkMessage("buff", buff.name(), bff.getDuration() + "", bff.getBuffPower() + "",
                                wrapper.getChatName(), DungeonRealms.getShard().getShardID());
                    }, () -> {
                        player.sendMessage(ChatColor.RED + buff.getItemName() + " - CANCELLED");
                    });
                }));
            });
        }


        ItemRetrainingBook book = new ItemRetrainingBook();
        ItemGlobalMessager globalMessage = new ItemGlobalMessager();
        setItem(slot.getAndIncrement(), new GUIItem(book.generateItem()).setECashCost(550).setClick(e -> {
            GameAPI.giveOrDropItem(player, book.generateItem());
            player.sendMessage(ChatColor.RED + "Retraining Book has been added to your Inventory.");
            setItems();
        }));

        //Only calls the callback if it was able to take the E-Cash.
        setItem(slot.getAndIncrement(), new GUIItem(globalMessage.generateItem()).setECashCost(200).setClick(e -> {
            GameAPI.giveOrDropItem(player, globalMessage.generateItem());
            player.sendMessage(ChatColor.RED + "Global Messenger has been added to your Inventory.");
            setItems();
        }));

        setItem(slot.getAndIncrement(), new GUIItem(ItemManager.createItem(Material.GOLD_BLOCK,
                ChatColor.GOLD + "Effects and Trails",
                "",
                ChatColor.GRAY + "Stand out amongst the rest", ChatColor.GRAY + "with powerful effects."))
                .setClick(e -> new ParticleEffectGUI(player, this).open(player, e.getAction())));

        setItem(getSize() - 1, ItemManager.createItem(Material.GOLDEN_APPLE, ChatColor.GREEN + "Current E-Cash",
                ChatColor.AQUA + "E-Cash Balance: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + wrapper.getEcash()));
    }

    private boolean giveLink(Player player) {
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), player::closeInventory);
        final JSONMessage normal4 = new JSONMessage(ChatColor.GOLD + "To Purchase E-Cash from our Shop, Click ", ChatColor.GOLD);
        normal4.addURL(ChatColor.AQUA.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE", ChatColor.AQUA, Constants.SHOP_URL);
        normal4.sendToPlayer(player);
        return false;
    }
}
