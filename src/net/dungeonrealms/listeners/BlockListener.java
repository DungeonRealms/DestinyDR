package net.dungeonrealms.listeners;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.handlers.FriendHandler;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.loot.LootManager;
import net.dungeonrealms.loot.LootSpawner;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.dungeonrealms.world.realms.Instance;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashSet;
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
            SpawningMechanics.getSpawners().stream().filter(spawner -> spawner.loc == block.getLocation()).forEach(SpawningMechanics::remove);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakBlock(BlockBreakEvent e) {
    	if(e.getPlayer().isOp() || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
    	if(e.getBlock().getWorld() == Bukkit.getWorlds().get(0))
    		e.setCancelled(true);
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
                    p.sendMessage(ChatColor.RED + "Your pick not strong enough to mine this ore!");
                    e.setCancelled(true);
                    return;
                }
                int experienceGain = Mining.getOreEXP(stackInHand, type);
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
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> e.getBlock().setType(type), (Mining.getOreRespawnTime(type) * 20L));
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
    	if(!Mining.isDRPickaxe(event.getPlayer().getItemInHand())) return;
    	ItemStack stackInHand = event.getPlayer().getItemInHand();
        Block block = event.getPlayer().getTargetBlock((HashSet<Byte>) null, 100);	
        if (block == null || block.getType() == Material.AIR) return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
          Player p = event.getPlayer();
          Material type = block.getType();
          int tier = Mining.getBlockTier(type);
          int pickTier = Mining.getPickTier(stackInHand);
          
          int diff = pickTier - tier;
          
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
        	  if(tier == 3 || tier == 4)
              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1));
        	  if(tier == 2 || tier == 1)
                  p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
             break;
          case 5:
        	  if(tier == 4 || tier == 5){
              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1));
        	  }else
              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
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
        if (mat == Material.FURNACE || mat == Material.HOPPER || mat == Material.DISPENSER || mat == Material.HOPPER_MINECART)
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
            event.setCancelled(true);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
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
            	
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "This item is already repaired all the way!");
            }
        }, 20l);
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
        if(loot != null){
                Collection<Entity> list = API.getNearbyMonsters(loot.location, 5);
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
                    e.getPlayer().sendMessage(ChatColor.RED + "You can't open this while monsters are around!");
                    e.setCancelled(true);
                }
        }

        Shop shop = ShopMechanics.getShop(block);
        if (shop == null){
        	e.setCancelled(true);
            return;
        }
        Action actionType = e.getAction();
        switch (actionType) {
            case RIGHT_CLICK_BLOCK:
                if (shop.isopen || shop.ownerUUID.toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
                    e.setCancelled(true);
                    e.getPlayer().openInventory(shop.getInventory());
                } else {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "This shop is closed!");
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (shop.ownerUUID.toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
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

    /**
     * Handles a player entering a portal,
     * teleports them to wherever they should
     * be, or cancels it if they're in combat
     * etc.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEnterPortal(PlayerPortalEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) {
            if (EntityAPI.hasPetOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_8_R3.Entity pet = Entities.PLAYER_PETS.get(event.getPlayer().getUniqueId());
                pet.dead = true;
                EntityAPI.removePlayerPetList(event.getPlayer().getUniqueId());
            }
            if (EntityAPI.hasMountOut(event.getPlayer().getUniqueId())) {
                net.minecraft.server.v1_8_R3.Entity mount = Entities.PLAYER_MOUNTS.get(event.getPlayer().getUniqueId());
                mount.dead = true;
                EntityAPI.removePlayerMountList(event.getPlayer().getUniqueId());
            }
            if (!CombatLog.isInCombat(event.getPlayer())) {
                if (Instance.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()) != null) {
                    String locationAsString = event.getFrom().getX() + "," + event.getFrom().getY() + "," + event.getFrom().getZ() + "," + event.getFrom().getYaw() + "," + event.getFrom().getPitch();
                    DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);
                    event.setTo(Instance.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()));
                    Instance.getInstance().addPlayerToRealmList(event.getPlayer(), Instance.getInstance().getRealmViaLocation(event.getFrom()));
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Sorry, you've tried to enter a null realm. Attempting to remove it!");
                    Instance.getInstance().removeRealmViaPortalLocation(event.getFrom());
                    event.getFrom().getBlock().setType(Material.AIR);
                    if (event.getFrom().subtract(0, 1, 0).getBlock().getType() == Material.PORTAL) {
                        event.getFrom().getBlock().setType(Material.AIR);
                    }
                    if (event.getFrom().add(0, 2, 0).getBlock().getType() == Material.PORTAL) {
                        event.getFrom().getBlock().setType(Material.AIR);
                    }
                }
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "You cannot enter a realm while in combat!");
            }
        } else {
            if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId()).equals("")) {
                String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId())).split(",");
                event.setTo(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                Instance.getInstance().removePlayerFromRealmList(event.getPlayer(), Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()));
            } else {
                Location realmPortalLocation = Instance.getInstance().getPortalLocationFromRealmWorld(event.getPlayer());
                event.setTo(realmPortalLocation.clone().add(0, 2, 0));
            }
            event.getPlayer().setFlying(false);
        }
    }

    /**
     * Handles a player breaking a block
     * within a realm.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBreakBlockInRealm(BlockBreakEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getBlock().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break Portal blocks!");
        }
        if (!Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
            event.setCancelled(true);
            event.setExpToDrop(0);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks in this realm, please ask the owner to add you to the builders list!");
        }
    }

    /**
     * Handles a player placing a block
     * within a realm.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPlaceBlockInRealm(BlockPlaceEvent event) {
        if (event.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        if (event.getPlayer().getWorld().getName().contains("DUNGEON")) return;
        if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getBlockPlaced().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place Portal blocks!");
            return;
        }
        if (event.getBlockAgainst().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks on-top of Portal blocks!");
            return;
        }
        if (!FriendHandler.getInstance().areFriends(event.getPlayer(), Instance.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmOwner().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to their friends list!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void shiftRightClickJournal(PlayerInteractEvent e) {
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
                if (API.isInSafeRegion(b1.getLocation()) && !API.isMaterialNearby(b1, 4, Material.CHEST) && !API.isMaterialNearby(b1, 10, Material.ENDER_CHEST)) {
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
