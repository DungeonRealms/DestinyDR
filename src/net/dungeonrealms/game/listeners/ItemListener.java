package net.dungeonrealms.game.listeners;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

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
     * Handles Right Click of Character Journal
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseMap(PlayerInteractEvent event){
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getItemInHand() == null || p.getItemInHand().getType() != Material.EMPTY_MAP) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getItemInHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("type")){
        	event.setCancelled(true);
        }
    }
    
    
    
    /**
     * Handles Right Click of Character Journal
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerUseCharacterJournal(PlayerInteractEvent event){
        if (!(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getItemInHand() == null || p.getItemInHand().getType() != Material.WRITTEN_BOOK) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(p.getItemInHand());
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null) return;
        if (tag.hasKey("journal") && !(tag.getString("journal").equalsIgnoreCase("true"))) return;
        ItemStack stack = ItemManager.createCharacterJournal(p);
        
        p.getInventory().setItem(7, stack);
        p.updateInventory(); 
    }
    
    
    /**
     * Handles player right clicking a stat reset book
     * 
     * @param event
     * @since 1.0
     */
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void useEcashItem(PlayerInteractEvent event) {
    	if (event.getItem() != null) {
    	if (event.getItem().getType() == Material.ENCHANTED_BOOK) {
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
    		if (nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("reset")) {
    			AnvilGUIInterface gui = AnvilApi.createNewGUI(event.getPlayer(), e -> {
					if (e.getSlot() == AnvilSlot.OUTPUT) {
						if (e.getName().equalsIgnoreCase("Yes") || e.getName().equalsIgnoreCase("y")) {
							if (event.getItem().getAmount() > 1) {
								event.getItem().setAmount(event.getItem().getAmount() - 1);
							} else {
                                event.getPlayer().getInventory().remove(event.getItem());
                                API.getGamePlayer(event.getPlayer()).getStats().unallocateAllPoints();
                                event.getPlayer().sendMessage(ChatColor.YELLOW + "All Stat Points have been unallocated!");
                                e.destroy();
                            }
						} else {
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
				Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), gui::open, 100L);
    		}
    	} else if (event.getItem().getType() == Material.ENDER_CHEST ){
    		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(event.getItem());
    			if (nms.hasTag() && nms.getTag().hasKey("type")) {
    				if (nms.getTag().getString("type").equalsIgnoreCase("upgrade")) {
    					Player player = event.getPlayer();
    					int invlvl = (int) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_LEVEL, player.getUniqueId());
    					if(invlvl == 6){
    						player.sendMessage(ChatColor.RED + "Sorry you've reached the current maximum storage size!");
    						return;
    					}
    					DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.INVENTORY_LEVEL, invlvl + 1, true);
    					Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->
    					BankMechanics.getInstance().getStorage(player.getUniqueId()).update(), 20);
    					if(event.getPlayer().getItemInHand().getAmount() == 1){
    						event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
    					}else{
    						ItemStack item = event.getPlayer().getItemInHand();
    						item.setAmount(item.getAmount() - 1);
    						event.getPlayer().setItemInHand(item);
    					}
    					event.getPlayer().sendMessage(ChatColor.YELLOW + "Your banks storage has been increased by 9 slots.");
    				}
    			}
    		}
    	}
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getItem()));
        if (nmsItem == null || nmsItem.getTag() == null) return;
        if (!nmsItem.getTag().hasKey("type")) return;
        if (nmsItem.getTag().getString("type").equalsIgnoreCase("healthPotion")) {
            event.setCancelled(true);
            if (HealthHandler.getInstance().getPlayerHPLive(event.getPlayer()) < HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer())) {
                event.setItem(new ItemStack(Material.AIR));
                event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsItem.getTag().getInt("healAmount"));
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "You are already at full HP!");
            }
        } else if (nmsItem.getTag().getString("type").equalsIgnoreCase("healingFood")) {
            if (event.getPlayer().getFoodLevel() >= 20) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            if (CombatLog.isInCombat(event.getPlayer())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot eat this while in combat!");
                event.getPlayer().updateInventory();
                return;
            }
            if (event.getPlayer().hasMetadata("FoodRegen")) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot eat this while you have another food bonus active!");
                event.getPlayer().updateInventory();
                return;
            }
            if (event.getPlayer().isSprinting()) {
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot eat this while sprinting!");
                event.getPlayer().updateInventory();
                return;
            }
            ItemStack foodItem = event.getItem();
            if (foodItem.getAmount() > 1) {
                foodItem.setAmount(foodItem.getAmount() - 1);
                event.getPlayer().setItemInHand(foodItem);
            } else {
                event.getPlayer().setItemInHand(new ItemStack(Material.AIR));
            }
            event.getPlayer().updateInventory();
            event.getPlayer().setFoodLevel(event.getPlayer().getFoodLevel() + 6);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Healing " + ChatColor.BOLD + nmsItem.getTag().getInt("healAmount") + ChatColor.GREEN + "HP/s for 15 Seconds!");
            event.getPlayer().setMetadata("FoodRegen", new FixedMetadataValue(DungeonRealms.getInstance(), "True"));
            int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (!event.getPlayer().isSprinting() && HealthHandler.getInstance().getPlayerHPLive(event.getPlayer()) < HealthHandler.getInstance().getPlayerMaxHPLive(event.getPlayer()) && !CombatLog.isInCombat(event.getPlayer())) {
                    HealthHandler.getInstance().healPlayerByAmount(event.getPlayer(), nmsItem.getTag().getInt("healAmount"));
                } else {
                    if (event.getPlayer().hasMetadata("FoodRegen")) {
                        event.getPlayer().removeMetadata("FoodRegen", DungeonRealms.getInstance());
                        event.getPlayer().sendMessage(ChatColor.RED + "Healing Cancelled!");
                    }
                }
            },0L, 20L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                Bukkit.getScheduler().cancelTask(taskID);
                if (event.getPlayer().hasMetadata("FoodRegen")) {
                    event.getPlayer().removeMetadata("FoodRegen", DungeonRealms.getInstance());
                }
            }, 300L);
        }else if(event.getItem().getType() == Material.COOKED_FISH && nmsItem.getTag().getString("type").equalsIgnoreCase("fishBuff") && nmsItem.getTag().hasKey("buff")){
        	if(Fishing.fishBuffs.containsKey(event.getPlayer().getUniqueId())){
        		event.getPlayer().sendMessage(ChatColor.RED + "You have an active fish buff already!");
        		return;
        	}
        	Fishing.fishBuffs.put(event.getPlayer().getUniqueId(), nmsItem.getTag().getString("buff"));
        	event.getPlayer().sendMessage("    " + ChatColor.BOLD.toString() + ChatColor.YELLOW + nmsItem.getTag().getString("buff") + " Active for 10 seconds!");
        	Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->Fishing.fishBuffs.remove(event.getPlayer().getUniqueId()));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = (CraftItemStack.asNMSCopy(event.getPotion().getItem()));
        if (nmsItem != null && nmsItem.getTag() != null) {
            if (nmsItem.getTag().hasKey("type") && nmsItem.getTag().getString("type").equalsIgnoreCase("splashHealthPotion")) {
                event.setCancelled(true);
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (!API.isPlayer(entity)) {
                        continue;
                    }
                    HealthHandler.getInstance().healPlayerByAmount((Player) entity, nmsItem.getTag().getInt("healAmount"));
                }
            }
        }
    }
}
