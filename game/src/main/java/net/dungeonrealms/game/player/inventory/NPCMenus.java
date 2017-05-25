package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.stats.PlayerStats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Kieran on 10/26/2015.
 */
public class NPCMenus {

    public static void openWizardMenu(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        int totalResets = wrapper.getPlayerStats().resetAmounts + 1; //Ours start at 0, old DR started at 1.

        int resetCost = (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000));
        resetCost = (resetCost > 60000 ? 60000 : (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000)));
        
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Stat Reset Confirmation" + ChatColor.DARK_GRAY + " ***");
        player.sendMessage(ChatColor.DARK_GRAY + "           TOTAL Points: " + ChatColor.GREEN + wrapper.getLevel() * PlayerStats.POINTS_PER_LEVEL + ChatColor.DARK_GRAY + "          SPENT Points: " + ChatColor.GREEN + (wrapper.getLevel() * PlayerStats.POINTS_PER_LEVEL - wrapper.getPlayerStats().getFreePoints()));
        player.sendMessage(ChatColor.DARK_GRAY + "           Reset Cost: " + ChatColor.GREEN + "" + resetCost + " Gem(s)" + ChatColor.DARK_GRAY + "  TOTAL Resets: " + ChatColor.GREEN + totalResets);
        player.sendMessage(ChatColor.DARK_GRAY + "           E-Cash Cost: " + ChatColor.GREEN + "500 ECASH");
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "Enter the code '" + ChatColor.BOLD + "confirm" + ChatColor.GREEN + "' to confirm your gem purchase of a reset." + ChatColor.GREEN + " Or enter the code '" + ChatColor.BOLD + "ecash" + ChatColor.GREEN + "' to purchase using E-CASH.");
        player.sendMessage("");
        player.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Stat resets are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Each time you reset your stats the price will increase for the next reset. Type 'cancel' to void this request.");
        player.sendMessage("");

        int finalResetCost = resetCost;
        Chat.listenForMessage(player, event -> {
            if (event.getMessage().equalsIgnoreCase("confirm")) {
                if (BankMechanics.takeGemsFromInventory(player, finalResetCost)) {
                    wrapper.getPlayerStats().addReset();
                    wrapper.getPlayerStats().unallocateAllPoints();
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + finalResetCost + "G taken from your account.");
                } else {
                    player.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + finalResetCost + "G " + ChatColor.RED + "insufficient funds.");
                }
            } else if (event.getMessage().equalsIgnoreCase("ecash")) {
                if (wrapper.getEcash() >= 500) {
                    DonationEffects.getInstance().removeECashFromPlayer(player, 500);
                    wrapper.getPlayerStats().addReset();
                    wrapper.getPlayerStats().unallocateAllPoints();
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + 500 + "E-CASH taken from your account.");
                } else {
                    player.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: 500 ECASH" + ChatColor.RED + " insufficient funds.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
            }
        });
    }


    public static void openMerchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Merchant");

        inv.setItem(4, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(13, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        //inv.setItem(13, ItemManager.createItem(Material.MAP, ChatColor.BLUE + "Trade Information", "")); TODO: List all trades.
        inv.setItem(22, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(0, ItemManager.createItem(Material.INK_SACK, ChatColor.YELLOW + "Click to ACCEPT", (short) 8, "", ChatColor.GRAY + "Display Item"));

        player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.f, 1.f);
        player.openInventory(inv);
    }

    public static ItemStack editItem(ItemStack itemStack, String name, String[] lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        itemStack.setAmount(1);
        return itemStack;
    }

    private static ItemStack addDisplayLore(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(ChatColor.GRAY + "Display Item");
        String[] arr = lore.toArray(new String[lore.size()]);
        stack = NPCMenus.editItem(stack, stack.getItemMeta().getDisplayName(), arr);
        return stack;
    }
}