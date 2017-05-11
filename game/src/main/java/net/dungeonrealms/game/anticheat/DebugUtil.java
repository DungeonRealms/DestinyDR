package net.dungeonrealms.game.anticheat;

import net.dungeonrealms.database.PlayerWrapper;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

public class DebugUtil {
	
	public static void debugReport(Player p) {
		PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(p);
		String playerReport = p.getName() + " - Debug Report\n"
        		+ "Shard: " + DungeonRealms.getShard().getShardID() + "\n"
				+ "Position: " + p.getLocation().getBlockX() + " " + p.getLocation().getBlockY() + " " + p.getLocation().getBlockZ() + "\n"
        		+ "World: " + p.getWorld().getName() + "\n"
				+ "Open Inventory: " + (p.getOpenInventory() != null ? p.getOpenInventory().getTitle() : "None") + "\n"
        		+ "Inventory: " + (p.getInventory() != null ? p.getInventory().getTitle() : "None") + "\n"
        		+ (wrapper == null ? "PlayerWrapper is null!\n" : ""
        			+ "Level: " + wrapper.getLevel() + "\n"
        			+ "Health: " + HealthHandler.getHP(p) + " / " + HealthHandler.getMaxHP(p) + "\n")
        		+ "Packetlog Started for 30 seconds.";
        
        GameAPI.sendDevMessage(playerReport);
        PacketLogger.logPlayerTime(p, 30);
	}
	
	public static void itemReport(ItemStack stack) {
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
