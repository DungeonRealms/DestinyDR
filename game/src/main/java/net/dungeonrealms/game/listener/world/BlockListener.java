package net.dungeonrealms.game.listener.world;

import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.command.CommandSpawner;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.miscellaneous.Repair;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveUseAnvil;
import net.dungeonrealms.game.world.entity.ElementalDamage;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.loot.LootSpawner;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.spawning.BaseMobSpawner;
import net.dungeonrealms.game.world.spawning.EliteMobSpawner;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.md_5.bungee.api.ChatColor;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nick on 9/18/2015.
 */
public class BlockListener implements Listener {

    private Map<Location, Repair> repairMap = new HashMap<>();

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
        if (event.getBlock().getWorld() == Bukkit.getWorlds().get(0) || event.getBlock().getWorld().getName().contains("DUNGEON")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickupWhileRepairing(PlayerPickupItemEvent event) {
        for (Repair repair : repairMap.values()) {
            if (repair.getRepairing().equals(event.getPlayer().getName())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Disables the placement of Realm Chest.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void placeRealmChest(BlockPlaceEvent event) {
        if (event.getItemInHand() == null || event.getItemInHand().getType() != Material.CHEST)
        	return;

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItemInHand());
        if (nms.hasTag() && nms.getTag().hasKey("type"))
            event.setCancelled(true);

        Realm realm = Realms.getInstance().getRealm(event.getBlock().getWorld());
        if (realm == null || !realm.isOpen() || !realm.canBuild(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place Realm Chests here.");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.MOB_SPAWNER) {
                if (CommandSpawner.shownMobSpawners.containsKey(block.getLocation()) && Rank.isGM(event.getPlayer())) {
                    //Exists
                    //EDIT
                    event.setCancelled(true);
                    promptSpawnerEdit(event.getPlayer(), block, false, true);
                    event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlaceSpawner(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.MOB_SPAWNER) {
            Player player = (Player) event.getPlayer();
            if (player.hasMetadata("editting")) {
                //Create spawner.
                //Create a spawner..

                Block block = event.getBlock();
                promptSpawnerEdit(player, block, true, false);
            }
        }
    }

    public static void promptSpawnerEdit(Player player, Block block, boolean deleteBlock, boolean checkIfExists) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "SPAWNER SETUP STEP 1 / 2");
        player.sendMessage(ChatColor.GRAY + "Please enter the enum monster type and the monster elite enum name (if applicable), the TIER of the mob, the level range (high / low), spawn delay in seconds, amount to spawn, range to spawn around");
        player.sendMessage(ChatColor.GREEN + "EX: daemon false 4 high 120 1 1-4");
        if (checkIfExists)
            player.sendMessage(ChatColor.GREEN + "If you want to skip this step and go to step 2 instead type '2'");
        player.sendMessage("");

        Chat.listenForMessage(player, (chat) -> {
            chat.setCancelled(true);
            if (chat.getMessage().equals("cancel")) {
                player.sendMessage(ChatColor.RED + "Cancelled");
                return;
            }
            String[] args = chat.getMessage().split(" ");
            if ((chat.getMessage().equals("2") && checkIfExists) || args.length == 7) {

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    EnumMonster monsterType = EnumMonster.getMonsterByString(args[0]);

                    boolean forceStage2 = chat.getMessage().equals("2") && checkIfExists;

                    AtomicBoolean elite = new AtomicBoolean(false);
                    AtomicInteger tier = new AtomicInteger(0), min = new AtomicInteger(0), max = new AtomicInteger(0),
                            delay = new AtomicInteger(0), spawnAmount = new AtomicInteger(0);
                    String levelRange;
                    MobSpawner foundSpawner = null;
                    if (!forceStage2) {
                        if (monsterType == null) {
                            player.sendMessage(ChatColor.RED + "Invalid monster type given.");
                            return;
                        }
                        elite.set(Boolean.parseBoolean(args[1]));
                        tier.set(Integer.parseInt(args[2]));
                        levelRange = args[3];
                        delay.set(StringUtils.isNumeric(args[4]) ? Integer.parseInt(args[4]) : -1);
                        spawnAmount.set(StringUtils.isNumeric(args[5]) ? Integer.parseInt(args[5]) : -1);
                        if (delay.get() == -1 || spawnAmount.get() == -1) {
                            if (delay.get() == -1)
                                player.sendMessage(ChatColor.RED + "Invalid spawn delay given");
                            else
                                player.sendMessage(ChatColor.RED + "Invalid spawn amount given");

                            if (deleteBlock)
                                block.setType(Material.AIR);
                            return;
                        }

                        String range = args[6];

                        min.set(Integer.parseInt(range.split("-")[0]));
                        max.set(Integer.parseInt(range.split("-")[1]));
                    } else {
                        for (MobSpawner spawner : SpawningMechanics.getAllSpawners()) {
                            if (spawner.getLoc().getBlockX() == block.getLocation().getBlockX() &&
                                    spawner.getLoc().getBlockY() == block.getLocation().getBlockY() &&
                                    spawner.getLoc().getBlockZ() == block.getLocation().getBlockZ()) {
                                //Found
                                foundSpawner = spawner;
                                break;
                            }
                        }

                        if (foundSpawner == null) return;
                        elite.set(foundSpawner instanceof EliteMobSpawner);
                        tier.set(foundSpawner.getTier());
                        levelRange = foundSpawner.getLvlRange();
                        min.set(foundSpawner.getMininmumXZ());
                        max.set(foundSpawner.getMaximumXZ());
                        spawnAmount.set(foundSpawner.getSpawnAmount());
                        delay.set(foundSpawner.getRespawnDelay());
                    }
                    player.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "Spawner Setup STEP 2 / 2");
                    player.sendMessage(ChatColor.GREEN + "Please enter the custom mob name if applicable or 'none' for no name, (Explicit Weapon type or none), If you want to specify element damage: fire/ice/poison/pure <chance for elemental damage>");
                    player.sendMessage(ChatColor.GRAY + "EX: none none fire 80 or none none 0 or &aThe_Devestator sword");
                    MobSpawner temp = foundSpawner;
                    Chat.listenForMessage(player, (nameChat) -> {

                        if (nameChat.getMessage().equals("cancel")) {
                            player.sendMessage(ChatColor.RED + "Cancelled");
                            return;
                        }
                        String[] data = nameChat.getMessage().contains(" ") ? nameChat.getMessage().split(" ") : null;

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            MobSpawner mobSpawner = temp;
                            if (checkIfExists && mobSpawner == null) {
                                for (MobSpawner spawner : SpawningMechanics.getAllSpawners()) {
                                    if (spawner.getLoc().getBlockX() == block.getLocation().getBlockX() &&
                                            spawner.getLoc().getBlockY() == block.getLocation().getBlockY() &&
                                            spawner.getLoc().getBlockZ() == block.getLocation().getBlockZ()) {
                                        //Found
                                        mobSpawner = spawner;
                                        break;
                                    }
                                }
                            }

                            if (mobSpawner == null) {
                                if (elite.get())
                                    mobSpawner = new EliteMobSpawner(block.getLocation(), monsterType.getIdName(), tier.get(), SpawningMechanics.getELITESPAWNERS().size(), levelRange, delay.get(), min.get(), max.get());
                                else
                                    mobSpawner = new BaseMobSpawner(block.getLocation(), monsterType.getIdName(), tier.get(), spawnAmount.get(), SpawningMechanics.getALLSPAWNERS().size(), levelRange, delay.get(), min.get(), max.get());
                            }

                            boolean noName = false;
                            String name = data == null ? nameChat.getMessage() : data[0];
                            if (name.equalsIgnoreCase("none")) {
                                player.sendMessage(ChatColor.RED + "No name set.");
                                noName = true;
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Name set to " + name);
                            }

                            if (elite.get()) {
                                EnumNamedElite eliteEnum = EnumNamedElite.getFromName(name.toLowerCase().replace("_", " "));
                                if (eliteEnum != null && eliteEnum != EnumNamedElite.NONE)
                                    player.sendMessage(ChatColor.GREEN + "Found Elite named " + eliteEnum.getConfigName());

                            }

                            if (!noName) {
                                mobSpawner.setCustomName(name.replace("_", " "));
                                mobSpawner.setHasCustomName(true);
                            } else if (checkIfExists) {
                                mobSpawner.setCustomName(null);
                                mobSpawner.setHasCustomName(false);
                            }

                            mobSpawner.setSpawnAmount(spawnAmount.get());
                            mobSpawner.setTier(tier.get());
                            if(mobSpawner.getInitialRespawnDelay() != delay.get()){
                                mobSpawner.setInitialRespawnDelay(delay.get());
                                mobSpawner.setRespawnDelay(delay.get());
                            }
                            if (data != null && data.length >= 2) {
                                String weapon = data[1];
                                if (!weapon.equalsIgnoreCase("none")) {

                                    GeneratedItemType type = GeneratedItemType.getByName(weapon);
                                    if (type != null)
                                        mobSpawner.setWeaponType(weapon);
                                    else
                                        player.sendMessage(ChatColor.RED + "Weapon not found for " + weapon);
                                } else if (mobSpawner.getWeaponType() != null && checkIfExists) {
                                    //Clear this
                                    mobSpawner.setWeaponType(null);
                                }
                            }

                            if (data != null && data.length > 2) {
                                String elemental = data[2].toLowerCase();

                                if (elemental.equals("none")) {
                                    if (mobSpawner.getElementalDamage() != null && checkIfExists) {
                                        //Set current to null.
                                        mobSpawner.setElementalDamage(null);
                                    }
                                } else {
                                    double chance = data.length > 3 ? Double.parseDouble(data[3]) : 100;

                                    ElementalDamage damage = ElementalDamage.getFromName(elemental);
                                    if (damage != null) {
                                        mobSpawner.setElementalDamage(damage.getName().toLowerCase());
                                        mobSpawner.setElementChance(chance);
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Invalid element type given.");
                                    }
                                }

                            }

                            if (!checkIfExists) {
                                if (elite.get())
                                    SpawningMechanics.getELITESPAWNERS().add((EliteMobSpawner) mobSpawner);
                                else
                                    SpawningMechanics.getALLSPAWNERS().add((BaseMobSpawner) mobSpawner);
                            }

                            //Get serialized string?
                            String serialized = mobSpawner.getSerializedString();

                            if (checkIfExists) {
                                if (monsterType != null)
                                    mobSpawner.setSpawnType(monsterType.getIdName());
                                for (int i = 0; i < SpawningMechanics.SPAWNER_CONFIG.size(); i++) {
                                    String line = SpawningMechanics.SPAWNER_CONFIG.get(i);
                                    if (MobSpawner.doesLineMatchLocation(mobSpawner.getLoc(), line)) {
                                        //Remove that whole line..
                                        String newString = mobSpawner.getSerializedString();
                                        SpawningMechanics.SPAWNER_CONFIG.set(i, newString);
                                        DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                                        DungeonRealms.getInstance().saveConfig();
                                        Bukkit.getLogger().info("Updating spawner line from '" + line + "' to '" + newString + "'");
                                        break;
                                    }
                                }
                            } else {
                                SpawningMechanics.SPAWNER_CONFIG.add(serialized);
                                DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                                DungeonRealms.getInstance().saveConfig();
                            }

                            mobSpawner.kill();
                            //Init mobspawner.
                            mobSpawner.init();
                            mobSpawner.createEditInformation();
                            CommandSpawner.shownMobSpawners.put(mobSpawner.getLoc(), mobSpawner);
                            if (checkIfExists)
                                player.sendMessage(ChatColor.RED + "Spawner created!");
                            else
                                player.sendMessage(ChatColor.RED + "Spawner Editted.");
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1F);

                        });
                    }, (cancelled) -> {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            player.sendMessage(ChatColor.RED + "Spawner creation failed");
                            if (deleteBlock)
                                block.setType(Material.AIR);
                        });
                    });
                });
            } else {

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    player.sendMessage(ChatColor.GRAY + "Please enter the enum monster type and the monster elite enum name (if applicable), the TIER of the mob, the level range (high / low), spawn delay in seconds, amount to spawn, range to spawn around");
                    if (deleteBlock)
                        block.setType(Material.AIR);
                });
                return;
            }

            //coords=type*(name):tier;amount<high/low (lvl range)>@SpawnTime#rangeMin-rangMax$
            //x,y,z=type*(Name):4;1-@400#1-1$

        }, (cancel) -> {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {

                player.sendMessage(ChatColor.RED + "Spawner creation failed");
                if (deleteBlock)
                    block.setType(Material.AIR);
            });
        });
    }

    /**
     * Handles breaking a spawner.
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void spawnerBreakEvent(BlockBreakEvent e) {
        if (!e.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;
        Block block = e.getBlock();
        if (block == null) return;
        if (block.getType() == Material.MOB_SPAWNER) {
            Lists.newArrayList(SpawningMechanics.getALLSPAWNERS()).stream().filter(spawner -> isEqual(spawner.getLoc(), block.getLocation())).forEach((spawner) -> {
                SpawningMechanics.remove(spawner);
                spawner.setRemoved(true);
                spawner.remove();
                e.getPlayer().sendMessage(ChatColor.RED + "Spawner removed.");
                Bukkit.getLogger().info("Removing spawner at " + spawner.getLoc().toString());
            });

            Lists.newArrayList(SpawningMechanics.getELITESPAWNERS()).stream().filter(spawner -> isEqual(spawner.getLoc(), block.getLocation())).forEach((spawner) -> {
                SpawningMechanics.remove(spawner);
                spawner.setRemoved(true);
                spawner.remove();
                e.getPlayer().sendMessage(ChatColor.RED + "Elite Spawner removed.");
                Bukkit.getLogger().info("Removing elite spawner at " + spawner.getLoc().toString());

            });
        }
    }

    private boolean isEqual(Location location, Location loc2) {
        return location.getBlockX() == loc2.getBlockX() && loc2.getBlockY() == loc2.getBlockY() && loc2.getBlockZ() == location.getBlockZ() && loc2.getWorld().getName().equals(location.getWorld().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakBlock(BlockBreakEvent e) {
        if (e.getPlayer().isOp() || e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (e.getBlock().getWorld() == Bukkit.getWorlds().get(0) || e.getBlock().getWorld().getName().contains("DUNGEON")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getLocation().getWorld().getName().contains("DUNGEON")) {
            event.setCancelled(true);
            event.getLocation().getWorld().playEffect(event.getLocation(), Effect.PARTICLE_SMOKE, 10);
            List<Block> list = event.blockList();
            for (Block b : list) {
                b.setType(Material.AIR);
            }
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void cancelPlayersBlockOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            Material mat = block.getType();

            if (mat == Material.DISPENSER && Realms.getInstance().getRealm(block.getWorld()) == null)
                event.setCancelled(true);

            if (mat == Material.HOPPER || mat == Material.FURNACE || mat == Material.HOPPER_MINECART || mat == Material.TRAPPED_CHEST || mat == Material.BREWING_STAND || mat == Material.ENCHANTMENT_TABLE)
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void itemMove(InventoryMoveItemEvent event) {
        if (event.getDestination().getName().equalsIgnoreCase("container.furnace")) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRightClickAnvil(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.ANVIL) return;
        event.setCancelled(true);
        if (!GameAPI.isMainWorld(block.getLocation())) return;
        

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
                Chat.listenForMessage(pl, null, null);
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
        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : StringUtils.capitaliseAllWords(item.getType().name().toLowerCase().replace("_", ""));
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
        if (GameAPI.isUUID(block.getWorld().getName())) return;
        LootSpawner loot = LootManager.getSpawner(e.getClickedBlock().getLocation());
        if (loot == null) {
            e.setCancelled(true);
            return;
        }
        Collection<Entity> list = GameAPI.getNearbyMonsters(loot.location, 10);
        if (list.isEmpty()) {
            Action actionType = e.getAction();
            switch (actionType) {
                case RIGHT_CLICK_BLOCK:
                    e.setCancelled(true);
                    e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
                    e.getPlayer().openInventory(loot.inv);
                    LootManager.getOpenChests().put(e.getPlayer().getName(), loot.inv);
                    Achievements.getInstance().giveAchievement(e.getPlayer().getUniqueId(), Achievements.EnumAchievements.OPEN_LOOT_CHEST);
                    break;
                case LEFT_CLICK_BLOCK:
                    e.setCancelled(true);
                    for (ItemStack stack : loot.inv.getContents()) {
                        if (stack == null) {
                            continue;
                        }
                        loot.inv.remove(stack);
                        if (stack.getType() != Material.AIR) {
                            e.getPlayer().getWorld().dropItemNaturally(loot.location, stack);
                        }
                    }
                    loot.update(e.getPlayer());
                    break;
            }
        } else {
            e.getPlayer().sendMessage(ChatColor.RED + "It is " + org.bukkit.ChatColor.BOLD + "NOT" + org.bukkit.ChatColor.RESET + org.bukkit.ChatColor.RED + " safe to open that right now");
            e.getPlayer().sendMessage(ChatColor.GRAY + "Eliminate the monsters in the area first.");
            e.setCancelled(true);
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
    public void playerCloseLootChest(InventoryCloseEvent event) {
        if (LootManager.getOpenChests().containsKey(event.getPlayer().getName())) {
            Inventory inventory = LootManager.getOpenChests().get(event.getPlayer().getName());
            if (inventory.equals(event.getInventory())) {
                LootManager.LOOT_SPAWNERS.forEach(lootSpawner1 -> {
                    if (lootSpawner1.inv.equals(inventory)) {
                        lootSpawner1.update((Player) event.getPlayer());
                        LootManager.getOpenChests().remove(event.getPlayer().getName());
                    }
                });
            }
        }
    }
}
