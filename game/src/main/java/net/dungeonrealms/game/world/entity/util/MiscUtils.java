package net.dungeonrealms.game.world.entity.util;

import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 5/10/2017.
 */
public class MiscUtils {

    public static void debugItem(ItemStack stack) {
        System.out.println("======= Starting Listing Item Data =======\n");
        if(stack == null) {
            System.out.println("Null Stack!");
            return;
        }
        System.out.println("Material: " + stack.getType().name());
        if(stack.getType().equals(Material.AIR)) {
            System.out.println("======= Finished Listing Item Data =======");
            return;
        }
        String name = "N/A";
        if(stack.getItemMeta().hasDisplayName()) {
            name = stack.getItemMeta().getDisplayName();
        }
        System.out.println("Display Name: " + name);
        System.out.println("\nListing All NBT...");
        // get all the nbt tags of the item
        NBTTagCompound tag = CraftItemStack.asNMSCopy(stack).getTag();
        tag.c().forEach(key -> System.out.println(key + ": " + tag.get(key).toString()));
        System.out.println("\nFinished Listing NBT...\n");
        System.out.println("======= Finished Listing Item Data =======");
    }
}
