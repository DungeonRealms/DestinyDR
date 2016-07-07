package net.dungeonrealms.game.player.inventory;

import net.dungeonrealms.API;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.miscellaneous.ItemBuilder;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.entities.types.mounts.EnumMounts;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kieran on 10/26/2015.
 */
public class NPCMenus {

    public static void openMountPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Animal Tamer");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.SADDLE), ChatColor.GREEN + EnumMounts.TIER1_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 120%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "An old brown starter horse.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "3000g"}).setNBTString("mountType", EnumMounts.TIER1_HORSE.getRawName()).setNBTInt("mountCost", 3000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.IRON_BARDING), ChatColor.AQUA + EnumMounts.TIER2_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 140%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A horse fit for a humble squire.",
                ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.GREEN + EnumMounts.TIER1_HORSE.getDisplayName(),
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7000g"}).setNBTString("mountType", EnumMounts.TIER2_HORSE.getRawName()).setNBTInt("mountCost", 7000).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.DIAMOND_BARDING), ChatColor.LIGHT_PURPLE + EnumMounts.TIER3_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 170%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A well versed travelling companion.",
                ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.AQUA + EnumMounts.TIER2_HORSE.getDisplayName(),
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "15000g"}).setNBTString("mountType", EnumMounts.TIER3_HORSE.getRawName()).setNBTInt("mountCost", 15000).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.GOLD_BARDING), ChatColor.YELLOW + EnumMounts.TIER4_HORSE.getDisplayName(), new String[]{
                ChatColor.RED + "Speed 200%",
                ChatColor.RED + "Jump 110%",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "A mount fit for even the best of adventurers.",
                ChatColor.RED.toString() + ChatColor.BOLD + "REQ: " + ChatColor.RESET + ChatColor.LIGHT_PURPLE + EnumMounts.TIER3_HORSE.getDisplayName(),
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "30000g"}).setNBTString("mountType", EnumMounts.TIER4_HORSE.getRawName()).setNBTInt("mountCost", 30000).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.LEASH), ChatColor.GREEN + "Storage Mule", new String[]{
                ChatColor.RED + "Storage Size: 9 Items",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + "An old worn-out storage mule.",
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "5000g"}).setNBTString("mountType", EnumMounts.MULE.getRawName()).setNBTInt("mountCost", 5000).build());
        player.openInventory(inv);
    }

    public static void openProfessionPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Profession Vendor");
        ItemStack pickAxe = ItemManager.createPickaxe(1);
        ItemStack fishingRod = ItemManager.createFishingPole(1);
        ItemMeta meta = pickAxe.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
        String[] arr = lore.toArray(new String[lore.size()]);
        inv.addItem(editItem(pickAxe, pickAxe.getItemMeta().getDisplayName(), arr));

        ItemMeta meta2 = fishingRod.getItemMeta();
        List<String> lore2 = meta2.getLore();
        lore2.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "100g");
        String[] arr2 = lore2.toArray(new String[lore2.size()]);
        inv.addItem(editItem(fishingRod, fishingRod.getItemMeta().getDisplayName(), arr2));
        player.openInventory(inv);
    }

    public static void openWizardMenu(Player player) {
        GamePlayer gp = API.getGamePlayer(player);

        Player p = gp.getPlayer();
        int totalResets = gp.getStats().resetAmounts + 1; //Ours start at 0, old DR started at 1.

        int resetCost = (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000));
        resetCost = (resetCost > 60000 ? 60000 : (int) ((1000. * Math.pow(1.8, (totalResets + 1))) - ((1000. * Math.pow(1.8, (totalResets + 1))) % 1000)));
        p.sendMessage("");
        p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.GREEN + ChatColor.BOLD + "Stat Reset Confirmation" + ChatColor.DARK_GRAY + " ***");
        p.sendMessage(ChatColor.DARK_GRAY + "           TOTAL Points: " + ChatColor.GREEN + gp.getStats().getLevel() * gp.getStats().POINTS_PER_LEVEL + ChatColor.DARK_GRAY + "          SPENT Points: " + ChatColor.GREEN + (gp.getLevel() * gp.getStats().POINTS_PER_LEVEL - gp.getStats().freePoints));
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
                if (BankMechanics.getInstance().takeGemsFromInventory(finalResetCost, p)) {
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
                ChatColor.GRAY + "View the available E-Cash Pets."
        }).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.GLOWSTONE_DUST), ChatColor.GOLD + "Effects", new String[]{
                ChatColor.GRAY + "View the available E-Cash Effects."
        }).build());
        inv.setItem(5, new ItemBuilder().setItem(new ItemStack(Material.SKULL_ITEM), ChatColor.GOLD + "Skins", new String[]{
                ChatColor.GRAY + "View the available E-Cash Skins."
        }).build());
        inv.setItem(7, new ItemBuilder().setItem(new ItemStack(Material.INK_SACK), ChatColor.GOLD + "Miscellaneous", new String[]{
                ChatColor.GRAY + "View the available E-Cash Miscellaneous Items."
        }).build());
        inv.setItem(9, new ItemBuilder().setItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Our Store", new String[]{
                ChatColor.AQUA + "Click here to visit our store!"}).setNBTString("donationStore", "ProxyIsAwesome").build());
        inv.setItem(17, new ItemBuilder().setItem(new ItemStack(Material.GOLDEN_APPLE), ChatColor.GREEN + "Current E-Cash", new String[]{
                ChatColor.AQUA + "Your E-Cash Balance is: " + ChatColor.YELLOW.toString() + ChatColor.BOLD + API.getGamePlayer(player).getEcashBalance()}).build());

        player.openInventory(inv);
    }

    public static void openHearthstoneRelocateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Hearthstone Re-Location");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Cyrennica", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "1000g"}).setNBTString("hearthstoneLocation", "CYRENNICA").setNBTInt("gemCost", 1000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Harrison Fields", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "1500g"}).setNBTString("hearthstoneLocation", "HARRISON_FIELD").setNBTInt("gemCost", 1500).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Dark Oak Tavern", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "3500g"}).setNBTString("hearthstoneLocation", "DARK_OAK").setNBTInt("gemCost", 3500).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Gloomy Hollows", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "3500g"}).setNBTString("hearthstoneLocation", "GLOOMY_HOLLOWS").setNBTInt("gemCost", 3500).build());
        inv.setItem(4, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Tripoli", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7500g"}).setNBTString("hearthstoneLocation", "TRIPOLI").setNBTInt("gemCost", 7500).build());
        inv.setItem(5, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Trollsbane Tavern", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "7500g"}).setNBTString("hearthstoneLocation", "TROLLSBANE").setNBTInt("gemCost", 7500).build());
        inv.setItem(6, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Crestguard Keep", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "15000g"}).setNBTString("hearthstoneLocation", "CRESTGUARD").setNBTInt("gemCost", 15000).build());
        inv.setItem(7, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Deadpeaks Mountain", new String[]{
                ChatColor.GREEN + "Price: " + ChatColor.WHITE + "25000g"}).setNBTString("hearthstoneLocation", "DEADPEAKS").setNBTInt("gemCost", 25000).build());

        player.openInventory(inv);
    }

    public static void openDungeoneerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Dungeoneer");

        inv.setItem(0, new ItemBuilder().setItem(ItemManager.createMuleUpgrade(2)).addLore(ChatColor.WHITE + "5000" + ChatColor.AQUA + " Portal Key Shards")
                .setNBTInt("shardCost", 5000).setNBTInt("shardTier", 3).build());
        inv.setItem(1, new ItemBuilder().setItem(ItemManager.createMuleUpgrade(3)).addLore(ChatColor.WHITE + "8000" + ChatColor.LIGHT_PURPLE + " Portal Key Shards")
                .setNBTInt("shardCost", 8000).setNBTInt("shardTier", 4).build());
        inv.setItem(2, new ItemBuilder().setItem(ItemManager.createProtectScroll(1)).addLore(ChatColor.WHITE + "1500 Portal Shards").setNBTInt("shardTier", 1)
                .setNBTString("shardColor", ChatColor.WHITE.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(3, new ItemBuilder().setItem(ItemManager.createProtectScroll(2)).addLore(ChatColor.WHITE + "1500 " + ChatColor.GREEN + "Portal Shards").setNBTInt("shardTier", 2)
                .setNBTString("shardColor", ChatColor.GREEN.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(4, new ItemBuilder().setItem(ItemManager.createProtectScroll(3)).addLore(ChatColor.WHITE + "1500 " + ChatColor.AQUA + "Portal Shards").setNBTInt("shardTier", 3)
                .setNBTString("shardColor", ChatColor.AQUA.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(5, new ItemBuilder().setItem(ItemManager.createProtectScroll(4)).addLore(ChatColor.WHITE + "1500 " + ChatColor.LIGHT_PURPLE + "Portal Shards").setNBTInt("shardTier", 4)
                .setNBTString("shardColor", ChatColor.LIGHT_PURPLE.toString()).setNBTInt("shardCost", 1500).build());
        inv.setItem(6, new ItemBuilder().setItem(ItemManager.createProtectScroll(5)).addLore(ChatColor.WHITE + "1500 " + ChatColor.YELLOW + "Portal Shards").setNBTInt("shardTier", 5)
                .setNBTString("shardColor", ChatColor.YELLOW.toString()).setNBTInt("shardCost", 1500).build());

        player.openInventory(inv);
    }

    public static void openMerchantMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Merchant");

        inv.setItem(4, new ItemStack(Material.THIN_GLASS));
        inv.setItem(13, new ItemStack(Material.THIN_GLASS));
        inv.setItem(22, new ItemStack(Material.THIN_GLASS));
        inv.setItem(0, new ItemBuilder().setItem(Material.INK_SACK, (short) 8, ChatColor.YELLOW + "Click to ACCEPT", new String[]{
                ""
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

    public static void openItemVendorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Item Vendor");

        ItemStack orbOfPeace = ShopMechanics.addPrice(ItemManager.createOrbofPeace(), 500);
        ItemStack orbOfFlight = ShopMechanics.addPrice(ItemManager.createOrbofFlight(), 500);

        if (!GuildDatabaseAPI.get().isGuildNull(player.getUniqueId())) {
            String guildName = GuildDatabaseAPI.get().getGuildOf(player.getUniqueId());
            ItemStack item = ItemSerialization.itemStackFromBase64(GuildDatabaseAPI.get().getBannerOf(guildName));
            ItemStack guildBanner = ShopMechanics.addPrice(item, 1000);

            inv.setItem(0, guildBanner);
            inv.setItem(1, orbOfFlight);
            inv.setItem(2, orbOfPeace);


        } else {

            inv.setItem(0, orbOfFlight);
            inv.setItem(1, orbOfPeace);

        }

        player.openInventory(inv);
    }

    public static void openFoodVendorMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, "Food Vendor");
        ItemStack potato = ShopMechanics.addPrice(ItemManager.createHealingFood(1, Item.ItemRarity.COMMON), 2);
        ItemStack loadedPotato = ShopMechanics.addPrice(ItemManager.createHealingFood(1, Item.ItemRarity.RARE), 4);
        ItemStack apple = ShopMechanics.addPrice(ItemManager.createHealingFood(1, Item.ItemRarity.UNIQUE), 8);

        ItemStack unCookedChicken = ShopMechanics.addPrice(ItemManager.createHealingFood(2, Item.ItemRarity.COMMON), 10);
        ItemStack RoastedChicken = ShopMechanics.addPrice(ItemManager.createHealingFood(2, Item.ItemRarity.RARE), 14);
        ItemStack pumpkinPie = ShopMechanics.addPrice(ItemManager.createHealingFood(2, Item.ItemRarity.UNIQUE), 18);


        ItemStack saltedPork = ShopMechanics.addPrice(ItemManager.createHealingFood(3, Item.ItemRarity.COMMON), 20);
        ItemStack seasonedPork = ShopMechanics.addPrice(ItemManager.createHealingFood(3, Item.ItemRarity.RARE), 25);
        ItemStack mushroomSoup = ShopMechanics.addPrice(ItemManager.createHealingFood(3, Item.ItemRarity.UNIQUE), 30);

        ItemStack frozenSteak = ShopMechanics.addPrice(ItemManager.createHealingFood(4, Item.ItemRarity.COMMON), 35);
        ItemStack sizzlingSteak = ShopMechanics.addPrice(ItemManager.createHealingFood(4, Item.ItemRarity.RARE), 45);
        ItemStack grilledRabbit = ShopMechanics.addPrice(ItemManager.createHealingFood(4, Item.ItemRarity.UNIQUE), 55);

        ItemStack kingsApple = ShopMechanics.addPrice(ItemManager.createHealingFood(5, Item.ItemRarity.COMMON), 95);
        ItemStack enchantedApple = ShopMechanics.addPrice(ItemManager.createHealingFood(5, Item.ItemRarity.RARE), 100);
        ItemStack goldCarrot = ShopMechanics.addPrice(ItemManager.createHealingFood(5, Item.ItemRarity.UNIQUE), 128);

        inv.setItem(0, potato);
        inv.setItem(1, loadedPotato);
        inv.setItem(2, apple);

        inv.setItem(3, unCookedChicken);
        inv.setItem(4, RoastedChicken);
        inv.setItem(5, pumpkinPie);

        inv.setItem(6, saltedPork);
        inv.setItem(7, seasonedPork);
        inv.setItem(8, mushroomSoup);

        inv.setItem(9, frozenSteak);
        inv.setItem(10, sizzlingSteak);
        inv.setItem(11, grilledRabbit);

        inv.setItem(12, kingsApple);
        inv.setItem(13, enchantedApple);
        inv.setItem(14, goldCarrot);
        player.openInventory(inv);
    }


}
