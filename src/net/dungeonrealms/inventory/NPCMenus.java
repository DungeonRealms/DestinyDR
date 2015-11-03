package net.dungeonrealms.inventory;

import net.dungeonrealms.items.ItemBuilder;
import net.dungeonrealms.mechanics.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Kieran on 10/26/2015.
 */
public class NPCMenus {

    public static void openMountPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Mount Vendor");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.SADDLE), ChatColor.GREEN + "Old Horse Mount", new String[]{
                ChatColor.RED + "Speed 120%",
                ChatColor.AQUA + "5000 Gems"}).setNBTString("mountType", "T1HORSE").setNBTInt("mountCost", 5000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.DIAMOND_BARDING), ChatColor.GREEN + "Traveler's Horse Mount", new String[]{
                ChatColor.RED + "Speed 140%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "12500 Gems"}).setNBTString("mountType", "DIAMONDHORSE").setNBTInt("mountCost", 12500).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.GOLD_BARDING), ChatColor.GREEN + "Knight's Horse Mount", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "25000 Gems"}).setNBTString("mountType", "GOLDHORSE").setNBTInt("mountCost", 25000).build());
        //TODO: Add Mule when Chase finishes it.

        player.openInventory(inv);
    }

    public static void openProfessionPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Profession Vendor");
        ItemStack pickAxe = ItemManager.createPickaxe(1);
        ItemStack fishingRod = ItemManager.createFishingPole(1);
		String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
    	inv.addItem(editItem(pickAxe, pickAxe.getItemMeta().getDisplayName(), new String[]{expBar, ChatColor.AQUA + "100 Gems"}));
    	inv.addItem(editItem(fishingRod, fishingRod.getItemMeta().getDisplayName(), new String[]{expBar, ChatColor.AQUA + "100 Gems"}));

        player.openInventory(inv);
    }

    public static void openECashPurchaseMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "E-Cash Vendor");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.BLAZE_POWDER), ChatColor.RED + "Flame Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "FLAME").setNBTInt("ecashCost", 649).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.JUKEBOX), ChatColor.LIGHT_PURPLE + "Musical Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "NOTE").setNBTInt("ecashCost", 649).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Cloud Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "CLOUD").setNBTInt("ecashCost", 649).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.POTION), ChatColor.DARK_PURPLE + "Cursed Trail", new String[]{
                ChatColor.AQUA + "649 E-Cash"}).setNBTString("playerTrailType", "WITCHMAGIC").setNBTInt("ecashCost", 649).build());
        inv.setItem(4, new ItemBuilder().setItem(Material.SKULL_ITEM, (short) 2, ChatColor.GREEN + "Zombie Horse", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("mountType", "ZOMBIEHORSE").setNBTInt("ecashCost", 749).build());
        inv.setItem(5, new ItemBuilder().setItem(Material.SKULL_ITEM, (short) 0, ChatColor.GRAY + "Skeleton Horse", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%",
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("mountType", "SKELETONHORSE").setNBTInt("ecashCost", 749).build());
        inv.setItem(6, new ItemBuilder().setItem(Material.MONSTER_EGG, (short) 101, ChatColor.YELLOW + "Rabbit Pet", new String[]{
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("petType", "RABBIT").setNBTInt("ecashCost", 749).build());
        inv.setItem(7, new ItemBuilder().setItem(Material.MONSTER_EGG, (short) 98, ChatColor.YELLOW + "Ocelot Pet", new String[]{
                ChatColor.AQUA + "749 E-Cash"}).setNBTString("petType", "OCELOT").setNBTInt("ecashCost", 749).build());

        player.openInventory(inv);
    }

    public static void openHearthstoneRelocateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Hearthstone Re-Location");

        inv.setItem(0, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Cyrennica", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "CYRENNICA").setNBTInt("gemCost", 1000).build());
        inv.setItem(1, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Harrison Fields", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "HARRISON_FIELD").setNBTInt("gemCost", 1500).build());
        inv.setItem(2, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Dark Oak Tavern", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "DARK_OAK").setNBTInt("gemCost", 3500).build());
        inv.setItem(3, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Gloomy Hollows", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "GLOOMY_HOLLOWS").setNBTInt("gemCost", 3500).build());
        inv.setItem(4, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Tripoli", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "TRIPOLI").setNBTInt("gemCost", 7500).build());
        inv.setItem(5, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Trollsbane Tavern", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "TROLLSBANE").setNBTInt("gemCost", 7500).build());
        inv.setItem(6, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Crestguard Keep", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "CRESTGUARD").setNBTInt("gemCost", 15000).build());
        inv.setItem(7, new ItemBuilder().setItem(new ItemStack(Material.BEACON), ChatColor.WHITE + "Deadpeaks Mountain", new String[]{
                ChatColor.AQUA + "1000 Gems"}).setNBTString("hearthstoneLocation", "DEADPEAKS").setNBTInt("gemCost", 25000).build());

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
}
