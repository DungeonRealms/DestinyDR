package net.dungeonrealms.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.inventory.PlayerMenus;
import net.dungeonrealms.teleportation.TeleportAPI;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

/**
 * Created by Kieran on 9/18/2015.
 */
public class ItemListener implements Listener {
    /**
     * Used to stop player from dropping items that are
     * valuable e.g. hearthstone or profile head.
     *
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemDrop().getItemStack());
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You can't drop important game items!");
    }

    /**
     * Handles player clicking with a teleportation item
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseTeleportItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.BOOK) return;
        ItemStack itemStack = player.getItemInHand();
        if (!(CombatLog.isInCombat(event.getPlayer()))) {
            if (TeleportAPI.isPlayerCurrentlyTeleporting(player.getUniqueId())) {
                player.sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            if (TeleportAPI.isTeleportBook(itemStack)) {
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
                Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, nmsItem.getTag());
                if (player.getItemInHand().getAmount() == 1) {
                    player.setItemInHand(new ItemStack(Material.AIR));
                } else {
                    player.getItemInHand().setAmount((player.getItemInHand().getAmount() - 1));
                }
            } else {
                player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "This item cannot be used to Teleport!");
            }
        } else {
            player.sendMessage(
                    ChatColor.GREEN.toString() + ChatColor.BOLD + "TELEPORT " + ChatColor.RED + "You are in combat! " + ChatColor.RED.toString() + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player.getUniqueId()) + "s" + ChatColor.RED + ")");
        }
    }

    /**
     * Handles player clicking with their profile
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseProfileItem(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        if (player.getItemInHand() == null || player.getItemInHand().getType() != Material.SKULL_ITEM) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(player.getItemInHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (!(tag.getString("type").equalsIgnoreCase("important")) && !(tag.getString("usage").equalsIgnoreCase("profile"))) return;
        PlayerMenus.openPlayerProfileMenu(player);
    }
    
    /**
     * Handles player right clicking a stat reset book
     * 
     * @param event
     * @since 1.0
     */
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseStatResetBook(PlayerInteractEvent event) {
    	if(event.getItem() != null && event.getItem().getType() == Material.ENCHANTED_BOOK){
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
    		if(nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("reset")){
    			AnvilGUIInterface gui = AnvilApi.createNewGUI(event.getPlayer(), e -> {
					if (e.getSlot() == AnvilSlot.OUTPUT) {
						if(e.getName().equalsIgnoreCase("Yes") || e.getName().equalsIgnoreCase("y")){
							if(event.getItem().getAmount() > 1){
								event.getItem().setAmount(event.getItem().getAmount() - 1);
								
							}else
								event.getPlayer().getInventory().remove(event.getItem());
							API.getGamePlayer(event.getPlayer()).getStats().unallocateAllPoints();
							event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
							e.destroy();
						}else{
							e.setWillClose(true);
							e.destroy();
						}
					}
				});
				ItemStack stack = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
				ItemMeta meta = stack.getItemMeta();
				meta.setDisplayName("Reset stat points?");
				stack.setItemMeta(meta);
				gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
				event.getPlayer().sendMessage("Opening stat reset confirmation...");
				Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
				gui.open();
				}, 20 * 5);
    		}
    	}
    }
}
