package net.dungeonrealms.game.listener.world;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.command.CommandSpawner;
import net.dungeonrealms.game.command.moderation.CommandLootChest;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.loot.LootSpawner;
import net.dungeonrealms.game.world.spawning.BaseMobSpawner;
import net.dungeonrealms.game.world.spawning.EliteMobSpawner;
import net.dungeonrealms.game.world.spawning.MobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//TODO: Modularize this.
public class ModerationListener implements Listener {
	
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
        Player player = event.getPlayer();
        Block bk = event.getBlockPlaced();
        Runnable cancel = () -> Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> bk.setType(Material.AIR));

        if (bk.getType() == Material.MOB_SPAWNER) {
            if (player.hasMetadata("editting")) {
                //Create spawner.
                //Create a spawner..
                promptSpawnerEdit(player, bk, true, false);
            }
        } else if (bk.getType().name().endsWith("_ORE") && player.hasMetadata("oreeedit")) {
                Material mat = bk.getType();
                if (!Mining.isPossibleOre(mat)) {
                    player.sendMessage(ChatColor.RED + "This is not a valid minable ore type.");
                    event.setCancelled(true);
                    return;
                }
                
                Location l = bk.getLocation();
                Mining.addOre(l, mat);
                player.sendMessage(ChatColor.GREEN + mat.name() + " ore registered at " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
                
        } else if (event.getItemInHand() != null && event.getItemInHand().getType() == Material.WATER_LILY) {
            if (player.hasMetadata("fishEdit")) {
                //Create a new fishing spot after prompting?
                player.sendMessage(ChatColor.GREEN + "Please enter the tier of this fishing spot (1-5).");
                
                Chat.listenForNumber(player, 1, 5, tier -> {
                	player.sendMessage(ChatColor.GREEN + "Created T" + tier + " fishing spot.");
                	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> Fishing.addLocation(bk.getLocation(), tier));
                }, cancel);
            }
        } else if (event.getBlockPlaced().getType() == Material.CHEST && player.hasMetadata("lootchestedit")) {

            player.sendMessage(ChatColor.YELLOW + "Please enter the loot table for this chest to spawn.");
            player.sendMessage(ChatColor.YELLOW + "EXAMPLE: 'm1.loot'");
            JSONMessage lootMessage = new JSONMessage(ChatColor.GREEN + "Available Loot Types: ", ChatColor.YELLOW);
            for (String table : LootManager.getLoot().keySet())
                lootMessage.addHoverText(LootManager.getLoot().get(table).getFriendlyList(), ChatColor.YELLOW + table + ", ");
            lootMessage.sendToPlayer(player);
            
            Chat.listenForMessage(player, e -> {
            	String type = e.getMessage();
            	if (type.contains("."))
            		type = type.split("\\.")[0];
            	
            	if (!LootManager.getLoot().containsKey(type)) {
            		player.sendMessage(ChatColor.RED + "Unknown loot table '" + type + "'.");
            		cancel.run();
            		return;
            	}
            	
            	final String finalType = type;
            	player.sendMessage(ChatColor.YELLOW + "Please enter the delay in seconds of how long it takes to respawn this chest.");
            	Chat.listenForNumber(player, 0, 100000, n -> {
            		LootSpawner s = new LootSpawner(bk.getLocation().add(0, 1, 0), n, finalType);
            		LootManager.addSpawner(s);
                    CommandLootChest.createHologram(s);
                    player.sendMessage(ChatColor.RED + "Loot Chest created!");
            	}, cancel);
            	
            }, cancel);
        }
    }

    /**
     * Handles breaking a spawner.
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void spawnerBreakEvent(BlockBreakEvent e) {
        if (!GameAPI.isMainWorld(e.getBlock().getLocation()))
        		return;
        
        Block block = e.getBlock();
        if (block.getType() == Material.MOB_SPAWNER) {
        	new ArrayList<>(SpawningMechanics.getSpawners()).stream().filter(s -> isEqual(s.getLocation(), block.getLocation())).forEach(s -> {
        		SpawningMechanics.remove(s);
                e.getPlayer().sendMessage(ChatColor.RED + "Spawner removed.");
                Bukkit.getLogger().info("Removing spawner at " + s.getLocation().toString());
        	});
        } else if (block.getType().name().endsWith("_ORE") && e.getPlayer().hasMetadata("oreedit")) {
        	if (!Mining.isMineable(block))
        		return;
        	
        	Mining.removeOre(block);
        	e.getPlayer().sendMessage(ChatColor.RED + "Ore removed.");
        } else if (block.getType() == Material.WATER_LILY && e.getPlayer().hasMetadata("fishEdit")) {
        	if (Fishing.getExactTier(block) == -1)
        		return;
        	
        	Fishing.removeLocation(block);
        	e.getPlayer().sendMessage(ChatColor.GREEN + "Fishing location removed.");
        } else if (block.getType() == Material.CHEST && e.getPlayer().hasMetadata("lootchestedit")) {
            LootSpawner spawner = LootManager.getSpawner(block.getLocation());
            if (spawner == null)
            	return;
            
            LootManager.removeSpawner(spawner);
            e.getPlayer().sendMessage(ChatColor.RED + "Loot Chest removed.");
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
                        for (MobSpawner spawner : SpawningMechanics.getSpawners()) {
                            if (spawner.getLocation().equals(block.getLocation())) {
                                foundSpawner = spawner;
                                break;
                            }
                        }

                        if (foundSpawner == null) return;
                        elite.set(foundSpawner instanceof EliteMobSpawner);
                        tier.set(foundSpawner.getTier());
                        levelRange = foundSpawner.getLvlRange();
                        min.set(foundSpawner.getMinimumXZ());
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
                                for (MobSpawner spawner : SpawningMechanics.getSpawners()) {
                                    if (spawner.getLocation().equals(block.getLocation())) {
                                        //Found
                                        mobSpawner = spawner;
                                        break;
                                    }
                                }
                            }

                            if (mobSpawner == null) {
                                if (elite.get())
                                    mobSpawner = new EliteMobSpawner(block.getLocation(), "", monsterType, tier.get(), delay.get(), max.get());
                                else
                                    mobSpawner = new BaseMobSpawner(block.getLocation(), monsterType, "", tier.get(), spawnAmount.get(), levelRange, delay.get(), min.get(), max.get());
                            }

                            boolean noName = false;
                            String name = data == null ? nameChat.getMessage() : data[0];
                            if (name.equalsIgnoreCase("none")) {
                                player.sendMessage(ChatColor.RED + "No name set.");
                                noName = true;
                            } else {
                                player.sendMessage(ChatColor.GREEN + "Name set to " + name);
                            }

                            if (!noName) {
                                mobSpawner.setCustomName(name.replace("_", " "));
                            } else if (checkIfExists) {
                                mobSpawner.setCustomName(null);
                            }

                            mobSpawner.setSpawnAmount(spawnAmount.get());
                            mobSpawner.setTier(tier.get());
                            if (mobSpawner.getInitialRespawnDelay() != delay.get())
                                mobSpawner.setRespawnDelay(delay.get());
                            
                            if (data != null && data.length >= 2) {
                                String weapon = data[1];
                                if (!weapon.equalsIgnoreCase("none")) {

                                	ItemType type = ItemType.getByName(weapon);
                                    if (type != null)
                                        mobSpawner.setWeaponType(type);
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
                                    if (mobSpawner.getElement() != null && checkIfExists)
                                        mobSpawner.setElement(null);
                                } else {
                                    double chance = data.length > 3 ? Double.parseDouble(data[3]) : 100;

                                    ElementalAttribute damage = ElementalAttribute.getByName(elemental);
                                    if (damage != null) {
                                        mobSpawner.setElement(damage);
                                        mobSpawner.setElementChance(chance);
                                    } else {
                                        player.sendMessage(ChatColor.RED + "Invalid element type given.");
                                    }
                                }

                            }

                            
                            if (!checkIfExists)
                            	SpawningMechanics.getSpawners().add(mobSpawner);

                            if (checkIfExists && monsterType != null)
                            	mobSpawner.setMonsterType(monsterType);
                            
                            SpawningMechanics.saveConfig();
                            
                            mobSpawner.kill();
                            //Init mobspawner.
                            mobSpawner.init();
                            mobSpawner.createEditInformation();
                            CommandSpawner.shownMobSpawners.put(mobSpawner.getLocation(), mobSpawner);
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


    private boolean isEqual(Location location, Location loc2) {
        return location.getBlockX() == loc2.getBlockX() && loc2.getBlockY() == loc2.getBlockY() && loc2.getBlockZ() == location.getBlockZ() && loc2.getWorld().getName().equals(location.getWorld().getName());
    }

}
