package net.dungeonrealms.listeners;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.jobs.Mining;
import net.dungeonrealms.mechanics.LootManager;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.LootSpawner;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * Created by Nick on 9/18/2015.
 */
public class BlockListener implements Listener {

    /**
     * Disables the placement of core items that have NBTData of `important` in
     * `type` field.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getItemInHand() == null) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemInHand());
        if (nmsItem == null) return;
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
        event.setCancelled(true);
    }

    /**
     * Handles breaking a shop
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void blockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block == null) return;
        if (block.getType() == Material.CHEST) {
            Shop shop = ShopMechanics.getShop(block);
            if (shop == null) {
                return;
            } else {
                e.setCancelled(true);
                if (e.getPlayer().isOp()) {
                    shop.deleteShop();
                }
            }
        } else if (block.getType() == Material.ARMOR_STAND) {
            ArrayList<MobSpawner> list = SpawningMechanics.getSpawners();
            for (int i = 0; i < list.size(); i++) {
                MobSpawner current = list.get(i);
                if (current.loc == block.getLocation()) {
                    SpawningMechanics.remove(i);
                }
            }
        } else {
            return;
        }

    }

    /**
     * Handles breaking ore
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void breakOre(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (block == null) return;
        if (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR) return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
            ItemStack stackInHand = e.getPlayer().getItemInHand();
            if (Mining.isDRPickaxe(stackInHand)) {
                Player p = e.getPlayer();
                Material type = block.getType();
                int tier = Mining.getBlockTier(type);
                int pickTier = Mining.getPickTier(stackInHand);
                if (pickTier < tier) {
                    p.sendMessage("Your pick is to weak to break that ore");
                    e.setCancelled(true);
                    return;
                }
                int experienceGain = Mining.getExperienceGain(stackInHand, type);
                Mining.addExperience(stackInHand, experienceGain, p);
                p.getItemInHand().setDurability((short) (stackInHand.getDurability() + tier));
                e.setCancelled(true);
                if (new Random().nextInt(100) <= 75)//TODO INCORPORATE CHANCE INTO PICKS
                    p.getInventory().addItem(new ItemStack(type));
                e.getBlock().setType(Material.STONE);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> e.getBlock().setType(type), 20 * 30);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickAnvil(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.ANVIL) return;
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR) return;
        ItemStack item = event.getPlayer().getItemInHand();
        if (RepairAPI.isItemArmorOrWeapon(item)) {
            if (RepairAPI.canItemBeRepaired(item)) {
                int cost = RepairAPI.getItemRepairCost(item);
                Player player = event.getPlayer();
                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
                    if (e.getSlot() == AnvilSlot.OUTPUT) {
                        String text = e.getName();
                        if (text.equalsIgnoreCase("yes") || text.equalsIgnoreCase("y")) {
                            if (BankMechanics.getInstance().takeGemsFromInventory(cost, player)) {
                                RepairAPI.setCustomItemDurability(player.getItemInHand(), 1499);
                                player.updateInventory();
                            } else {
                                player.sendMessage("You do not have " + cost + "g");
                            }
                        } else {
                            e.destroy();
                            e.setWillClose(true);
                        }
                    }
                });
                Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                    ItemMeta meta = stack.getItemMeta();
                    meta.setDisplayName("Repair for " + cost + "?");
                    stack.setItemMeta(meta);
                    gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                    gui.open();
                });
            } else {
                event.getPlayer().sendMessage("This item is already repaired all the way!");
            }
        } else {
            event.setCancelled(true);
        }
    }

    /**
     * Handling Shops being Right clicked.
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickChest(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.CHEST) return;
        for (LootSpawner loot : LootManager.spawners) {
            if (loot.location.getBlockX() == block.getX() && loot.location.getBlockY() == block.getY() && loot.location.getBlockZ() == block.getLocation().getZ()) {
                Collection<Entity> list = API.getNearbyMonsters(loot.location, 10);
                if (list.isEmpty()) {
                    Action actionType = e.getAction();
                    switch (actionType) {
                        case RIGHT_CLICK_BLOCK:
                            e.setCancelled(true);
                            e.getPlayer().openInventory(loot.inv);
                            break;
                        case LEFT_CLICK_BLOCK:
                            e.setCancelled(true);
                            for (ItemStack stack : loot.inv.getContents()) {
                                if (stack == null)
                                    continue;
                                loot.inv.remove(stack);
                                if (stack.getType() != Material.AIR)
                                    e.getPlayer().getWorld().dropItemNaturally(loot.location, stack);
                            }
                            loot.update();
                            break;
                    }
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED.toString() + "You can't open this while monsters are around!");
                    e.setCancelled(true);
                }
            }
        }

        Shop shop = ShopMechanics.getShop(block);
        if (shop == null)
            return;
        Action actionType = e.getAction();
        switch (actionType) {
            case RIGHT_CLICK_BLOCK:
                if (shop.isopen || shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
                    e.setCancelled(true);
                    e.getPlayer().openInventory(shop.getInv());
                } else if (!shop.isopen) {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED.toString() + "This shop is closed!");
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (shop.getOwner().getUniqueId() == e.getPlayer().getUniqueId()) {
                    e.setCancelled(true);
                    shop.deleteShop();
                }
                break;
            default:
        }
    }

    /**
     * Handling setting up shops.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockDamaged(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (event.getClickedBlock() == null || event.getClickedBlock().getType() == Material.AIR) return;
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItem());
            if (nmsItem == null) return;
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null || !tag.getString("type").equalsIgnoreCase("important")) return;
            event.setCancelled(true);
            if (event.getPlayer().isSneaking()) {
                ItemStack item = event.getPlayer().getItemInHand();
                net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
                if (nms.getTag().hasKey("usage") && nms.getTag().getString("usage").equalsIgnoreCase("profile")) {
                    if (ShopMechanics.PLAYER_SHOPS.get(event.getPlayer().getUniqueId()) != null) {
                        event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You already have an active shop");
                        return;
                    }
                    ShopMechanics.setupShop(event.getClickedBlock(), event.getPlayer().getUniqueId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void snowmanMakeSnow(EntityBlockFormEvent event) {
        if (event.getNewState().getType() == Material.SNOW) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlock().setType(Material.AIR), 60L);
        } else {
            event.setCancelled(true);
        }
    }
}
