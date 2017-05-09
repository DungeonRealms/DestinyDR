package net.dungeonrealms.game.listener.world;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.miscellaneous.Repair;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveUseAnvil;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.Realms;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by Nick on 9/18/2015.
 */
public class BlockListener implements Listener {

    private Map<Location, Repair> repairMap = new HashMap<>();
    
    private static final List<Material> CANCEL_OPEN = Arrays.asList(Material.HOPPER, Material.FURNACE, Material.HOPPER_MINECART,
    		Material.TRAPPED_CHEST, Material.BREWING_STAND, Material.ENCHANTMENT_TABLE);

    /**
     * Disable placing blocks in the main world.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE || event.getItemInHand() == null)
        	return;
        if (!Realms.getInstance().isRealm(event.getPlayer().getWorld()))
            event.setCancelled(true);

        Realm realm = Realms.getInstance().getRealm(event.getBlock().getWorld());
        if (realm == null || !realm.isOpen() || !realm.canBuild(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place Realm Chests here.");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakBlock(BlockBreakEvent e) {
        if (!e.getPlayer().isOp() && !Realms.getInstance().isRealm(e.getBlock().getWorld()))
        	e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void cancelPlayersBlockOpen(PlayerInteractEvent event) {
    	Block block = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null)
            return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE)
        	return;
        
        Material mat = block.getType();
        
        if (mat == Material.DISPENSER && Realms.getInstance().getRealm(block.getWorld()) == null)
        	event.setCancelled(true);
        
        if (CANCEL_OPEN.contains(mat))
        	event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickAnvil(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ANVIL)
        	return;
        event.setCancelled(true);
        
        if (!GameAPI.isMainWorld(block.getLocation()))
        	return;


        Player player = event.getPlayer();
        if (player.getEquipment().getItemInMainHand() == null || player.getEquipment().getItemInMainHand().getType() == Material.AIR) {
            player.sendMessage(ChatColor.YELLOW + "Equip the item to repair and " + ChatColor.UNDERLINE + "RIGHT CLICK" + ChatColor.RESET + ChatColor.YELLOW + " the ANVIL.");
            player.sendMessage(ChatColor.GRAY + "Or, if you have an item scrap, drag it on top of the item in your inventory.");
            return;
        }
        ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
        if (!ItemGear.isCustomTool(item))
            return;
        
        ItemGear gear = (ItemGear)PersistentItem.constructItem(item);
        if (ProfessionItem.isProfessionItem(item)) {
        	ProfessionItem prof = (ProfessionItem)gear;
        	if (prof.getLevel() >= 100) {
        		player.sendMessage(ChatColor.RED + "This item is much too warn to be repaired.");
        		return;
        	}
        }
        
        if (!gear.canRepair()) {
        	player.sendMessage(ChatColor.YELLOW + "This item is " + ChatColor.UNDERLINE + "NOT" + ChatColor.YELLOW + " damaged.");
        	return;
        }
        
        if (repairMap.containsKey(block.getLocation())) {
        	Repair repair = repairMap.get(block.getLocation());
        	for (Entity nearby : player.getNearbyEntities(20, 20, 20)) {
                if (nearby instanceof Player) {
                    Player pl = (Player) nearby;
                    if (pl.getName().equals(repair.getRepairing())) {
                        player.sendMessage(ChatColor.RED + "This anvil is currently in use by " + repair.getRepairing() + "!");
                        return;
                    }
                }
            }
        	
        	//Return the item?
            Player pl = Bukkit.getPlayer(repair.getRepairing());
            if (pl != null) {
            	//They are too far away.
            	pl.sendMessage(ChatColor.RED + "You were > 10 blocks from the anvil.");
                Chat.listenForMessage(pl, null);
            }
        }

        int newCost = gear.getRepairCost();
        if (BankMechanics.getGemsInInventory(player) < newCost) {
        	player.sendMessage(ChatColor.RED + "You do not have enough gems to repair this item.");
        	player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + ChatColor.RED + newCost + ChatColor.BOLD.toString() + " GEM(s)");
        	return;
        }

        Location middle = block.getLocation().add(.5, 1.3, .5);
        //Set the item on the anvil
        player.getEquipment().setItemInMainHand(null);
        player.updateInventory();
        
        Item itemEntity = block.getWorld().dropItem(middle, item);
        itemEntity.teleport(middle);
        itemEntity.setVelocity(new Vector());
        itemEntity.setPickupDelay(Integer.MAX_VALUE);
        
        Repair repair = new Repair(item, itemEntity, player.getName());
        repairMap.put(block.getLocation(), repair);

        //  CHAT DISPLAY  //
        String name = Utils.getItemName(item);
        player.setCanPickupItems(false);
        player.sendMessage(ChatColor.YELLOW + "It will cost " + ChatColor.GREEN + ChatColor.BOLD.toString() + newCost + "G" + ChatColor.YELLOW + " to repair '" + name + ChatColor.YELLOW + "'");
        player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + ChatColor.BOLD.toString() + "Y" + ChatColor.GRAY + " to confirm this repair. Or type " + ChatColor.RED + ChatColor.BOLD.toString() + "N" + ChatColor.GRAY + " to cancel.");
        
        //  CHAT PROMPT  //
        Chat.promptPlayerConfirmation(player, () -> {
        	if (BankMechanics.takeGemsFromInventory(player, newCost)) {
        		gear.repair(); // Repair Item
        		
        		// Remove from anvil.
        		itemEntity.remove();
        		middle.getWorld().playEffect(middle, Effect.STEP_SOUND, Material.IRON_BLOCK);
        		middle.getWorld().playSound(middle, Sound.BLOCK_ANVIL_USE, 3, 1.4F);
        		
        		player.sendMessage(ChatColor.RED + "-" + newCost + ChatColor.BOLD.toString() + "G");
        		player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "ITEM REPAIRED");
        		
        		// Return item.
        		GameAPI.giveOrDropItem(player, gear.generateItem());
        		player.setCanPickupItems(true);
        		player.updateInventory();
        		
        		repairMap.remove(block.getLocation());
        		Quests.getInstance().triggerObjective(player, ObjectiveUseAnvil.class);
        	} else {
        		player.sendMessage(ChatColor.RED + "You do not have enough gems to repair this item.");
    			player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + ChatColor.RED + newCost + ChatColor.BOLD.toString() + " GEMS(s)");
        	}
        }, () -> {
        	// Return item.
        	itemEntity.remove();
        	repairMap.remove(block.getLocation());
        	player.sendMessage(ChatColor.RED + "Item Repair - " + ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED");
        	GameAPI.giveOrDropItem(player, item);
        	player.setCanPickupItems(true);
        });
    }

    /**
     * Removes snow that snowmen pets
     * create after 3 seconds.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void snowmanMakeSnow(EntityBlockFormEvent event) {
        if (event.getNewState().getType() == Material.SNOW) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlock().setType(Material.AIR), 60L);
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Cancels Portals changing to Air if
     * they are not surrounded by obsidian.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysicsChange(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.PORTAL && event.getChangedType() == Material.AIR)
            event.setCancelled(true);
    }
}
