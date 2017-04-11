package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;

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
        GamePlayer gp = GameAPI.getGamePlayer(player);

        Player p = gp.getPlayer();
        int totalResets = gp.getStats().resetAmounts + 1; //Ours start at 0, old DR started at 1.

        int resetCost = (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000));
        resetCost = (resetCost > 60000 ? 60000 : (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000)));
        p.sendMessage("");
        p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Stat Reset Confirmation" + ChatColor.DARK_GRAY + " ***");
        p.sendMessage(ChatColor.DARK_GRAY + "           TOTAL Points: " + ChatColor.GREEN + gp.getStats().getLevel() * gp.getStats().POINTS_PER_LEVEL + ChatColor.DARK_GRAY + "          SPENT Points: " + ChatColor.GREEN + (gp.getLevel() * gp.getStats().POINTS_PER_LEVEL - gp.getStats().getFreePoints()));
        p.sendMessage(ChatColor.DARK_GRAY + "           Reset Cost: " + ChatColor.GREEN + "" + resetCost + " Gem(s)" + ChatColor.DARK_GRAY + "  TOTAL Resets: " + ChatColor.GREEN + totalResets);
        p.sendMessage(ChatColor.DARK_GRAY + "           E-Cash Cost: " + ChatColor.GREEN + "500 ECASH");
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "Enter the code '" + ChatColor.BOLD + "confirm" + ChatColor.GREEN + "' to confirm your gem purchase of a reset." + ChatColor.GREEN + " Or enter the code '" + ChatColor.BOLD + "ecash" + ChatColor.GREEN + "' to purchase using E-CASH.");
        p.sendMessage("");
        p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Stat resets are " + ChatColor.BOLD + ChatColor.RED + "NOT" + ChatColor.RED + " reversible or refundable. Each time you reset your stats the price will increase for the next reset. Type 'cancel' to void this request.");
        p.sendMessage("");

        int finalResetCost = resetCost;
        Chat.listenForMessage(p, event -> {
            if (event.getMessage().equalsIgnoreCase("confirm")) {
                if (BankMechanics.takeGemsFromInventory(p, finalResetCost)) {
                    gp.getStats().addReset();
                    gp.getStats().unallocateAllPoints();
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + finalResetCost + "G taken from your account.");
                } else {
                    p.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
                    p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + finalResetCost + "G " + ChatColor.RED + "insufficient funds.");
                }
            } else if (event.getMessage().equalsIgnoreCase("ecash")) {
                if (gp.getEcashBalance() >= 500) {
                    DonationEffects.getInstance().removeECashFromPlayer(player, 500);
                    gp.getStats().addReset();
                    gp.getStats().unallocateAllPoints();
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + 500 + "E-CASH taken from your account.");
                } else {
                    p.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
                    p.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: 500 ECASH" + ChatColor.RED + " insufficient funds.");
                }
            } else {
                p.sendMessage(ChatColor.RED + "Stat Reset - Cancelled");
            }
        }, null);


    }

    public static void openECashPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "E-Cash Vendor");

        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.MONSTER_EGG), ChatColor.GOLD + "Pets", new String[]{
                ChatColor.GRAY + "View the available E-Cash Pets.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.GLOWSTONE_DUST), ChatColor.GOLD + "Effects", new String[]{
                ChatColor.GRAY + "View the available E-Cash Effects.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(5, new ItemBuilder().setItem(new ItemStack(Material.SKULL_ITEM), ChatColor.GOLD + "Skins", new String[]{
                ChatColor.GRAY + "View the available E-Cash Skins.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(7, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK), ChatColor.GOLD + "Miscellaneous", new String[]{
                ChatColor.GRAY + "View the available E-Cash Miscellaneous Items.",
                ChatColor.GRAY + "Display Item"
        }).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Our Store", new String[]{
                ChatColor.AQUA + "Click here to visit our store!",
                ChatColor.GRAY + "Display Item"}).setNBTString("donationStore", "ProxyIsAwesome").build());
        inv.setItem(17, new ItemBuilder().setItem(new ItemStack(Material.GOLDEN_APPLE), ChatColor.GREEN + "Current E-Cash", new String[]{
                ChatColor.AQUA + "Your E-Cash Balance is: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + GameAPI.getGamePlayer(player).getEcashBalance(),
                ChatColor.GRAY + "Display Item"}).build());

        player.openInventory(inv);
    }

    public static void openMerchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Merchant");

        inv.setItem(4, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(13, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(22, addDisplayLore(new ItemStack(Material.THIN_GLASS)));
        inv.setItem(0, new ItemBuilder().setItem(Material.INK_SACK, (short) 8, ChatColor.YELLOW + "Click to ACCEPT", new String[]{
                "",
                ChatColor.GRAY + "Display Item"
        }).setNBTString("acceptButton", "whynot").build());
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