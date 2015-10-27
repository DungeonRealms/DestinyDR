package net.dungeonrealms.inventory;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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
        Inventory inv = Bukkit.createInventory(null, 27, "Mount Vendor");
        ItemStack t1Horse = new ItemStack(Material.SADDLE);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackT1 = CraftItemStack.asNMSCopy(t1Horse);
        NBTTagCompound tagT1 = nmsStackT1.getTag() == null ? new NBTTagCompound() : nmsStackT1.getTag();
        tagT1.set("mountType", new NBTTagString("T1HORSE"));
        tagT1.setInt("mountCost", 5000);
        nmsStackT1.setTag(tagT1);
        ItemStack diamondHorse = new ItemStack(Material.DIAMOND_BARDING);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackDiamond = CraftItemStack.asNMSCopy(diamondHorse);
        NBTTagCompound tagDiamond = nmsStackDiamond.getTag() == null ? new NBTTagCompound() : nmsStackDiamond.getTag();
        tagDiamond.set("mountType", new NBTTagString("DIAMONDHORSE"));
        tagDiamond.setInt("mountCost", 12500);
        nmsStackDiamond.setTag(tagDiamond);
        ItemStack goldHorse = new ItemStack(Material.SADDLE);
        net.minecraft.server.v1_8_R3.ItemStack nmsStackGold = CraftItemStack.asNMSCopy(goldHorse);
        NBTTagCompound tagGold = nmsStackGold.getTag() == null ? new NBTTagCompound() : nmsStackGold.getTag();
        tagGold.set("mountType", new NBTTagString("GOLDHORSE"));
        tagDiamond.setInt("mountCost", 25000);
        nmsStackGold.setTag(tagGold);

        inv.setItem(0, editItem(CraftItemStack.asBukkitCopy(nmsStackT1), ChatColor.GREEN + "Old Horse Mount", new String[]{
                ChatColor.RED + "Speed 120%"}));
        inv.setItem(2, editItem(CraftItemStack.asBukkitCopy(nmsStackDiamond), ChatColor.GREEN + "Traveler's Horse Mount", new String[]{
                ChatColor.RED + "Speed 140%",
                ChatColor.RED + "Jump 110%"}));
        inv.setItem(2, editItem(CraftItemStack.asBukkitCopy(nmsStackGold), ChatColor.GREEN + "Knight's Horse Mount", new String[]{
                ChatColor.RED + "Speed 160%",
                ChatColor.RED + "Jump 110%"}));
        //TODO: Add Mule when Chase finishes it.

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
