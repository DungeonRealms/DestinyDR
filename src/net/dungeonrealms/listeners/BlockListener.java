package net.dungeonrealms.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.minebone.anvilapi.core.AnvilApi;
import com.minebone.anvilapi.nms.anvil.AnvilGUIInterface;
import com.minebone.anvilapi.nms.anvil.AnvilSlot;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.items.repairing.RepairAPI;
import net.dungeonrealms.loot.LootManager;
import net.dungeonrealms.loot.LootSpawner;
import net.dungeonrealms.mastery.RealmManager;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.mongo.EnumOperators;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.SpawningMechanics;
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
        if (block.getType() == Material.CHEST) {
            Shop shop = ShopMechanics.getShop(block);
            if (shop == null) return;
                e.setCancelled(true);
                if (e.getPlayer().isOp()) {
                    shop.deleteShop();
                }
        } else if (block.getType() == Material.ARMOR_STAND) {
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
          
          if(tier == 1)
        	  return;
          
          if (diff >= 3) {
              if (pickTier == 5) {
                  p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 1));
              } else {
                  p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, 0));
              }
              return;
          }
          
          if (pickTier == 4 || pickTier == 5) {
              if (pickTier == 4) {
                  if (diff == 0) { // Diamond ore.
                      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 3));
                  }
                  if (diff == 1) { // Iron ore.
                      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 3));
                  }
              }
              if (pickTier == 5) {
                  if (diff == 0) { // Gold ore.
                      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 3));
                  }
                  if (diff == 1) { // Diamond ore.
                      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 2));
                  }
              }
          } else {
              if (diff == 0) {
                  // Lvl 2 debuff
                  if (pickTier == 2) {
                      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1));
                  } else {
                      p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1));
                  }
              }
              if (diff == 1) {
                  // Lvl 1 debuff
                  p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 1));
              }
              if (diff == 2) {
                  // Lvl 0 debuff
                  p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 40, 0));
              }
          }
        }
    }
    	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickAnvil(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.ANVIL) return;
        if (event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getPlayer().getItemInHand();
        int cost = RepairAPI.getItemRepairCost(item);
        if (RepairAPI.isItemArmorOrWeapon(item) || Mining.isDRPickaxe(item) || Fishing.isDRFishingPole(item)) {
            if (RepairAPI.canItemBeRepaired(item)) {
                Player player = event.getPlayer();
                AnvilGUIInterface gui = AnvilApi.createNewGUI(player, e -> {
                    if (e.getSlot() == AnvilSlot.OUTPUT) {
                        String text = e.getName();
                        if (text.equalsIgnoreCase("yes") || text.equalsIgnoreCase("y") || text.contains("Repair for ")) {
                            boolean tookGems = BankMechanics.getInstance().takeGemsFromInventory(cost, player);
                            if (tookGems) {
                                RepairAPI.setCustomItemDurability(player.getItemInHand(), 1499);
                                player.updateInventory();
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have " + cost + "g");
                            }
                        } else {
                            e.destroy();
                            e.setWillClose(true);
                        }
                    }
                });
                Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{
                if( RepairAPI.getItemRepairCost(player.getItemInHand()) != cost){
                	gui.destroy();
                	player.closeInventory();
                	player.sendMessage(ChatColor.RED + "Don't change your item while repairing.");
                	return;
                }
                 	ItemStack stack = new ItemStack(Material.NAME_TAG, 1);
                 	ItemMeta meta = stack.getItemMeta();
                 	meta.setDisplayName("Repair for " + cost + "g ?");
                 	stack.setItemMeta(meta);
                 	gui.setSlot(AnvilSlot.INPUT_LEFT, stack);
                 	gui.open();
                }, 10l);
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "This item is already repaired all the way!");
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
                if (shop.isopen || shop.getUUID().toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
                    e.setCancelled(true);
                    e.getPlayer().openInventory(shop.getInv());
                } else {
                    e.setCancelled(true);
                    e.getPlayer().sendMessage(ChatColor.RED + "This shop is closed!");
                }
                break;
            case LEFT_CLICK_BLOCK:
                if (shop.getUUID().toString().equalsIgnoreCase(e.getPlayer().getUniqueId().toString())) {
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
                    /*if (ShopMechanics.PLAYER_SHOPS.get(event.getPlayer().getUniqueId()) != null) {
                        event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "You already have an active shop");
                        return;
                    }*/
                    //ShopMechanics.setupShop(event.getClickedBlock(), event.getPlayer().getUniqueId());
                    /*if(event.getPlayer().isOp()){
                        RealmManager.getInstance().tryToOpenRealm(event.getPlayer(), event.getClickedBlock().getLocation());
                	} else {
                		event.getPlayer().sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "COMING SOON..");
                	}*/
                    event.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Coming Soon!");
                }
            } else {
                if (event.getClickedBlock().getType() == Material.PORTAL) {
                    if (RealmManager.getInstance().getPlayerRealm(event.getPlayer()).isRealmPortalOpen()) {
                        if (RealmManager.getInstance().getRealmViaLocation(event.getClickedBlock().getLocation()).getRealmOwner().equals(event.getPlayer().getUniqueId())) {
                            RealmManager.getInstance().removeRealm(RealmManager.getInstance().getRealmViaLocation(event.getClickedBlock().getLocation()), false);
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
                if (RealmManager.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()) != null) {
                    String locationAsString = event.getFrom().getX() + "," + event.getFrom().getY() + "," + event.getFrom().getZ() + "," + event.getFrom().getYaw() + "," + event.getFrom().getPitch();
                    DatabaseAPI.getInstance().update(event.getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.CURRENT_LOCATION, locationAsString, true);
                    event.setTo(RealmManager.getInstance().getRealmLocation(event.getFrom(), event.getPlayer()));
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Sorry, you've tried to enter a null realm. Attempting to remove it!");
                    RealmManager.getInstance().removeRealmViaPortalLocation(event.getFrom());
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
            }
        } else {
            if (!DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId()).equals("")) {
                String[] locationString = String.valueOf(DatabaseAPI.getInstance().getData(EnumData.CURRENT_LOCATION, event.getPlayer().getUniqueId())).split(",");
                event.setTo(new Location(Bukkit.getWorlds().get(0), Double.parseDouble(locationString[0]), Double.parseDouble(locationString[1]), Double.parseDouble(locationString[2]), Float.parseFloat(locationString[3]), Float.parseFloat(locationString[4])));
                RealmManager.getInstance().getPlayersCurrentRealm(event.getPlayer()).getPlayerList().remove(event.getPlayer());
            } else {
                Location realmPortalLocation = RealmManager.getInstance().getPortalLocationFromRealmWorld(event.getPlayer());
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
        if (!RealmManager.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
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
        }
        if (event.getBlockAgainst().getType() == Material.PORTAL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks ontop of Portal blocks!");
        }
        if (!RealmManager.getInstance().getPlayersCurrentRealm(event.getPlayer()).getRealmBuilders().contains(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks in this realm, please ask the owner to add you to the builders list!");
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
//    			ShopMechanics.PENDING.remove(e.getPlayer().getUniqueId());
            Block b1 = e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 1, 0));
            Block b2 = e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(1, 1, 0));
            if (b1.getType() == Material.AIR && b2.getType() == Material.AIR && API.isInSafeRegion(e.getClickedBlock().getLocation())) {
                if (ShopMechanics.PLAYER_SHOPS.containsKey(e.getPlayer().getUniqueId())) {
                    e.getPlayer().sendMessage(ChatColor.RED + "You already have an open shop!");
                    return;
                }
                if (API.isInSafeRegion(b1.getLocation()) && !API.isMaterialNearby(b1, 4, Material.CHEST) && !API.isMaterialNearby(b1, 4, Material.ENDER_CHEST)) {
                	if(!API.getGamePlayer(e.getPlayer()).hasShopOpen()){
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
