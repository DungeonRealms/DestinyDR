package net.dungeonrealms.game.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.items.repairing.RepairAPI;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.loot.LootSpawner;
import net.dungeonrealms.game.world.realms.Instance;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getItemInHand() == null) return;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getItemInHand());
        if (nmsItem == null) return;
        if (!event.getBlockPlaced().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        NBTTagCompound tag = nmsItem.getTag();
        event.setCancelled(true);
    }

    /**
     * Handles breaking a shop
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        if (!e.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        Block block = e.getBlock();
        if (block == null) return;
        if(block.getType() == Material.ARMOR_STAND) {
            SpawningMechanics.ALLSPAWNERS.stream().filter(spawner -> spawner.loc == block.getLocation()).forEach(SpawningMechanics::remove);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakBlock(BlockBreakEvent e) {
    	if(e.getPlayer().isOp() || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
    	if(e.getBlock().getWorld() == Bukkit.getWorlds().get(0) || e.getBlock().getWorld().getName().contains("DUNGEON"))
    		e.setCancelled(true);
    }

    
    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) {
    	if(event.getLocation().getWorld().getName().contains("DUNGEON")){
        	event.setCancelled(true);
        	event.getLocation().getWorld().playEffect(event.getLocation(), Effect.EXPLOSION_HUGE, 10);
    		List<Block> list = event.blockList();
        	for(Block b : list){
        		b.setType(Material.AIR);
        	}
        	event.blockList().clear();
    	}
    }
    
    /**
     * Handles breaking ore
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void breakOre(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!e.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        if (block == null) return;
        if (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR) return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
            e.setCancelled(true);
            ItemStack stackInHand = e.getPlayer().getItemInHand();
            if (Mining.isDRPickaxe(stackInHand)) {
                Player p = e.getPlayer();
                Material type = block.getType();
                int tier = Mining.getBlockTier(type);
                int pickTier = Mining.getPickTier(stackInHand);
                if (pickTier < tier) {
                    p.sendMessage(ChatColor.RED + "Your pick is not strong enough to mine this ore!");
                    e.setCancelled(true);
                    return;
                }
                int experienceGain = Mining.getOreEXP(stackInHand, type);
                if (API.getGamePlayer(e.getPlayer()) != null) {
                    API.getGamePlayer(e.getPlayer()).addExperience((experienceGain / 8), false);
                }
                RepairAPI.subtractCustomDurability(p, p.getEquipment().getItemInHand(), RandomHelper.getRandomNumberBetween(2, 5));
                int break_chance = Mining.getBreakChance(stackInHand);
                int do_i_break = new Random().nextInt(100);
                if (do_i_break < break_chance) {
                    Mining.addExperience(stackInHand, experienceGain, p);
                    if((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId()))
                    	p.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD.toString() +"   +" + experienceGain +"EXP for mining ore!");
                    p.getInventory().addItem(new ItemStack(type));
                }else{
                    p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "You fail to gather any ore.");
                }
                e.getBlock().setType(Material.STONE);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> e.getBlock().setType(type), (Mining.getOreRespawnTime(type) * 15));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void cookFish(PlayerInteractEvent e){
    	if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();	
        if (block == null) return;
        if(block.getType() == Material.TORCH || block.getType() == Material.BURNING_FURNACE || block.getType() == Material.FURNACE){
        if (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getType() == Material.AIR) return;
        if(e.getPlayer().getItemInHand().getType() == Material.RAW_FISH){
        	e.setCancelled(true);
        	e.getPlayer().getItemInHand().setType(Material.COOKED_FISH);
        	e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.GHAST_FIREBALL, 1, 1);
        }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleMiningFatigue(PlayerAnimationEvent event){
    	if(event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR) return;
        if(!API.isWeapon(event.getPlayer().getItemInHand()) && !Mining.isDRPickaxe(event.getPlayer().getItemInHand())) return;
        if(API.isWeapon(event.getPlayer().getItemInHand())){	
        	int tier = CraftItemStack.asNMSCopy(event.getPlayer().getItemInHand()).getTag().getInt("itemTier");
        	int playerLvl = API.getGamePlayer(event.getPlayer()).getLevel();
        	switch(tier){
                case 2:
                    if(playerLvl < 10){
                        event.setCancelled(true);
                        int slot = event.getPlayer().getInventory().getHeldItemSlot() + 1;
                        if(slot < 9)
                            event.getPlayer().getInventory().setHeldItemSlot(slot);
                        else
                            event.getPlayer().getInventory().setHeldItemSlot(0);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot equip this item! You must be level: 10");
                    }
                    break;
                case 3:
                    if(playerLvl < 25){
                        event.setCancelled(true);
                        int slot = event.getPlayer().getInventory().getHeldItemSlot() + 1;
                        if(slot < 9)
                            event.getPlayer().getInventory().setHeldItemSlot(slot);
                        else
                            event.getPlayer().getInventory().setHeldItemSlot(0);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot equip this item! You must be level: 25");
                    }
                    break;
                case 4:
                    if(playerLvl < 40){
                        event.setCancelled(true);
                        int slot = event.getPlayer().getInventory().getHeldItemSlot() + 1;
                        if(slot < 9)
                            event.getPlayer().getInventory().setHeldItemSlot(slot);
                        else
                            event.getPlayer().getInventory().setHeldItemSlot(0);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot equip this item! You must be level: 40");
                    }
                    break;
                case 5:
                    if(playerLvl < 50){
                        event.setCancelled(true);
                        int slot = event.getPlayer().getInventory().getHeldItemSlot() + 1;
                        if(slot < 9)
                            event.getPlayer().getInventory().setHeldItemSlot(slot);
                        else
                            event.getPlayer().getInventory().setHeldItemSlot(0);
                        event.getPlayer().sendMessage(ChatColor.RED + "You cannot equip this item! You must be level: 50");
                    }
                    break;
            }
            return;
        }
    	if(!Mining.isDRPickaxe(event.getPlayer().getItemInHand())) return;
    	ItemStack stackInHand = event.getPlayer().getItemInHand();
        Block block = event.getPlayer().getTargetBlock((HashSet<Byte>) null, 100);	
        if (block == null || block.getType() == Material.AIR) return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
          Player p = event.getPlayer();
          Material type = block.getType();
          int tier = Mining.getBlockTier(type);
          int pickTier = Mining.getPickTier(stackInHand);
          
          switch(pickTier){
          case 1:
        	  break;
          case 2:
        	  break;
          case 3:
        	  if(tier == pickTier)
                  p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
        	  break;
          case 4:
              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
             break;
          case 5:
              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
              break;
          }
        }
    }
    	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void cancelPlayersBlockOpen(PlayerInteractEvent event) {
    	if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
        Block block = event.getClickedBlock();
        if (block == null) return;
        if(event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE)return;
        Material mat = block.getType();
        if (mat == Material.FURNACE || mat == Material.HOPPER || mat == Material.DISPENSER || mat == Material.HOPPER_MINECART || mat == Material.TRAPPED_CHEST)
        	event.setCancelled(true);
    	}
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickAnvil(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.ANVIL) return;
    	event.setCancelled(true);

        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR) {
        	event.getPlayer().sendMessage(ChatColor.YELLOW + "Equip the item to repair and " + ChatColor.UNDERLINE + "RIGHT CLICK" + ChatColor.RESET + ChatColor.YELLOW + " the ANVIL.");
        	event.getPlayer().sendMessage(ChatColor.GRAY + "Or, if you have an item scrap, drag it on top of the item in your inventory.");
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getPlayer().getItemInHand();
        if(!API.isWeapon(item) && !API.isArmor(item) && !Mining.isDRPickaxe(item) && !Fishing.isDRFishingPole(item)){
        	event.setCancelled(true);
        	return;
        }
        
        int cost = RepairAPI.getItemRepairCost(item);
        if(cost < 0){
        	event.setCancelled(true);
        	return;
        }
            if (RepairAPI.canItemBeRepaired(item)) {
                Player player = event.getPlayer();
            	int newCost = RepairAPI.getItemRepairCost(item);
            	
            	Inventory inv = Bukkit.createInventory(null, 9, "Repair your item for " + ChatColor.BOLD + newCost + "g?");	
            	inv.setItem(3, ItemManager.createItemWithData(Material.WOOL, ChatColor.YELLOW + "Accept", new String[] {ChatColor.GRAY + "Repairs your item fully for specified amount."}, DyeColor.LIME.getData()));
            	inv.setItem(5, ItemManager.createItemWithData(Material.WOOL, ChatColor.YELLOW + "Deny", new String[] {ChatColor.GRAY + "Deny the repair of your item."}, DyeColor.RED.getData()));
            	
            	player.openInventory(inv);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
                    int finalCost = RepairAPI.getItemRepairCost(event.getPlayer().getItemInHand());
                	Inventory finalinv = Bukkit.createInventory(null, 9, "Repair your item for " + ChatColor.BOLD + finalCost + "g?");	
                	finalinv.setItem(3, ItemManager.createItemWithData(Material.WOOL, ChatColor.YELLOW + "Accept", new String[] {ChatColor.GRAY + "Repairs your item fully for specified amount."}, DyeColor.LIME.getData()));
                	finalinv.setItem(5, ItemManager.createItemWithData(Material.WOOL, ChatColor.YELLOW + "Deny", new String[] {ChatColor.GRAY + "Deny the repair of your item."}, DyeColor.RED.getData()));
                	
                	player.openInventory(finalinv);
                }, 10l);

            	
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "This item is already repaired all the way!");
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
        LootSpawner loot = LootManager.getSpawner(e.getClickedBlock().getLocation());
        if(loot == null){
        	e.setCancelled(true);
        	return;
        }
                Collection<Entity> list = API.getNearbyMonsters(loot.location, 10);
                if (list.isEmpty()) {
                    Action actionType = e.getAction();
                    switch (actionType) {
                        case RIGHT_CLICK_BLOCK:
                            e.setCancelled(true);
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
                            Block blockLook = e.getPlayer().getTargetBlock((Set<Material>) null, 7);
                            if(blockLook.getType() != Material.CHEST){
                            	return;
                            }else{
                                e.getPlayer().openInventory(loot.inv);
                            }
                            }, 10);
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
                    e.getPlayer().sendMessage(ChatColor.RED + "You can't open this while monsters are around!");
                    e.setCancelled(true);
                }

//        Shop shop = ShopMechanics.getShop(block);
//        if (shop == null){
//        	e.setCancelled(true);
//            return;
//        }
//        Action actionType = e.getAction();
////        switch (actionType) {
////            case RIGHT_CLICK_BLOCK:
////                e.getPlayer().sendMessage(ChatColor.RED + "Shops have been disabled whilst a critical error is resolved.");
////                e.setCancelled(true);
////                break;
////            case LEFT_CLICK_BLOCK:
////                if (shop.ownerUUID.toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
////                    e.setCancelled(true);
////                    e.getPlayer().sendMessage(ChatColor.RED + "Shops have been disabled whilst a critical error is resolved.");
////                    shop.deleteShop();
////                }
////                break;
////            default:
////        }
//
//        switch (actionType) {
//            case RIGHT_CLICK_BLOCK:
//                if (shop.isopen || shop.ownerUUID.toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
//                    e.setCancelled(true);
//                    e.getPlayer().openInventory(shop.getInventory());
//                } else {
//                    e.setCancelled(true);
//                    e.getPlayer().sendMessage(ChatColor.RED + "This shop is closed!");
//                }
//                break;
//            case LEFT_CLICK_BLOCK:
//                if (shop.ownerUUID.toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
//                    e.setCancelled(true);
//                    shop.deleteShop();
//                }
//                break;
//            default:
//        }
    }

    /**
     * Handling setting up shops.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
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
                    if(event.getPlayer().isOp()){
                        Instance.getInstance().tryToOpenRealm(event.getPlayer(), event.getClickedBlock().getLocation());
                	} else {
                		event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "COMING SOON..");
                	}
                }
            } else {
                if (event.getClickedBlock().getType() == Material.PORTAL) {
                    if (Instance.getInstance().getPlayerRealm(event.getPlayer()).isRealmPortalOpen()) {
                        if (Instance.getInstance().getRealmViaLocation(event.getClickedBlock().getLocation()).getRealmOwner().equals(event.getPlayer())) {
                            Instance.getInstance().removeRealm(Instance.getInstance().getRealmViaLocation(event.getClickedBlock().getLocation()), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes snow that snowmen pets
     * create after 3 seconds.
     *
     * @param event
     * @since 1.0
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
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysicsChange(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.PORTAL && event.getChangedType() == Material.AIR) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void shiftRightClickJournal(PlayerInteractEvent e) {
    	// MORE SHOP CODE FOR XFIN TO PULL?
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            ItemStack stack = e.getItem();
            if (stack == null) return;
            if (stack.getType() != Material.WRITTEN_BOOK) return;
            e.setCancelled(true);
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
            if (!nms.hasTag() && !nms.getTag().hasKey("journal")) return;
            Block b1 = e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 1, 0));
            Block b2 = e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(1, 1, 0));
            if (b1.getType() == Material.AIR && b2.getType() == Material.AIR && API.isInSafeRegion(e.getClickedBlock().getLocation())) {
                if (ShopMechanics.ALLSHOPS.containsKey(e.getPlayer().getUniqueId())) {
                    e.getPlayer().sendMessage(ChatColor.RED + "You already have an open shop!");
                    return;
                }
                if (API.isInSafeRegion(b1.getLocation()) && !API.isMaterialNearby(b1, 2, Material.CHEST) && !API.isMaterialNearby(b1, 10, Material.ENDER_CHEST)) {
                	if(!API.getGamePlayer(e.getPlayer()).hasShopOpen()){
                		if(BankMechanics.getInstance().getStorage(e.getPlayer().getUniqueId()).collection_bin != null){
                			e.getPlayer().sendMessage(ChatColor.RED + "You must collect your items from the collection bin!");
                			e.setCancelled(true);
                			return;
                		}
                    Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () ->
                            ShopMechanics.setupShop(e.getClickedBlock(), e.getPlayer().getUniqueId()));
                	}else{
                		e.getPlayer().sendMessage(ChatColor.RED + " You have a shop open already! It may be on another shard.");
                	}
                } else {
                    e.getPlayer().sendMessage(ChatColor.RED + "You can not place a shop here.");
                }
            } else {
                e.getPlayer().sendMessage(ChatColor.RED + "You can not place a shop here.");
            }
        }
    }
}
