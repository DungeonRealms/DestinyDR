package net.dungeonrealms.game.listener.world;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.command.CommandSpawner;
import net.dungeonrealms.game.command.moderation.CommandFishing;
import net.dungeonrealms.game.command.moderation.CommandLootChest;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.entity.ElementalDamage;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.loot.LootSpawner;
import net.dungeonrealms.game.world.loot.types.LootType;
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

        if (event.getBlockPlaced().getType() == Material.MOB_SPAWNER) {
            if (player.hasMetadata("editting")) {
                //Create spawner.
                //Create a spawner..

                Block block = event.getBlock();
                promptSpawnerEdit(player, block, true, false);
            }
        } else if (event.getBlockPlaced().getType().name().endsWith("_ORE")) {
            if (player.hasMetadata("oreedit")) {
                //orespawns in config
                //Create an ore.
                Material mat = event.getBlockPlaced().getType();
                if (Mining.getBlockTier(mat) <= 0) {
                    player.sendMessage(ChatColor.RED + "This is not a valid minable ore type.");
                    event.setCancelled(true);
                    return;
                }
                Location l = event.getBlockPlaced().getLocation();
                Mining.getInstance().addOreLocation(event.getBlockPlaced().getLocation(), mat);
                player.sendMessage(ChatColor.GREEN + mat.name() + " ore registered at " + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
            }
        } else if (event.getItemInHand() != null && event.getItemInHand().getType() == Material.WATER_LILY) {
            if (player.hasMetadata("fishEdit")) {
                //Create a new fishing spot after prompting?
                player.sendMessage(ChatColor.GREEN + "Please enter the tier of this fishing spot (1-5).");
                Chat.listenForMessage(player, (chatEvent) -> {

                    if (StringUtils.isNumeric(chatEvent.getMessage())) {
                        int tier = Integer.parseInt(chatEvent.getMessage());
                        if (tier > 5 || tier < 1) {
                            player.sendMessage(ChatColor.RED + "Only 1 - 5 are valid fishing tiers...");
                            return;
                        }

                        player.sendMessage(ChatColor.GREEN + "Fishing spot created!");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Fishing.getInstance().addFishingLocation(event.getBlockPlaced().getLocation(), tier));
                        return;
                    }

                    player.sendMessage(ChatColor.RED + "Invalid data given.");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlockPlaced().setType(Material.AIR));
                }, (pl) -> {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlockPlaced().setType(Material.AIR));

                });
            }
        } else if (event.getBlockPlaced().getType() == Material.CHEST && player.hasMetadata("lootchestedit")) {
            //Create loot chest after prompt?

            player.sendMessage(ChatColor.YELLOW + "Please enter the loot type for this chest to spawn and the delay in seconds for it to respawn.");
            player.sendMessage(ChatColor.YELLOW + "EXAMPLE: m1.loot 900 - Is m1.loot file with a 900 second respawn time.");
            JSONMessage lootMessage = new JSONMessage(ChatColor.GREEN + "Available Loot Types: ", ChatColor.YELLOW);
            for (LootType loot : LootType.values()) {
                lootMessage.addHoverText(loot.getLootList(), ChatColor.YELLOW + loot.fileName + ", ");
            }

            lootMessage.sendToPlayer(player);
            Chat.listenForMessage(player, (e) -> {
                String msg = e.getMessage();
                if (msg.contains(" ")) {
                    String[] args = msg.split(" ");
                    if (args.length == 2 && args[0].contains(".")) {
                        //Loot type
                        LootType type = LootType.getLootType(args[0]);
                        if (type == null) {
                            player.sendMessage(ChatColor.RED + "Invalid Loot Type!");
                        } else {
                            if (StringUtils.isNumeric(args[1])) {
                                int seconds = Integer.parseInt(args[1]);
                                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                                    LootSpawner spawner = LootManager.createLootChest(event.getBlockPlaced().getLocation(), seconds, type);
                                    CommandLootChest.createHologram(spawner);
                                    player.sendMessage(ChatColor.RED + "Loot Chest created!");
                                });
                                return;
                            }
                        }
                    }
                }
                player.sendMessage(ChatColor.RED + "Invalid Loot Chest parameters.");
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlockPlaced().setType(Material.AIR));
            }, (pl) -> {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> event.getBlockPlaced().setType(Material.AIR));
            });
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
        } else if (block.getType().name().endsWith("_ORE") && e.getPlayer().hasMetadata("oreedit")) {
            //Check if its Ore?
            Mining mining = Mining.getInstance();
            if (mining.getORE_LOCATIONS().containsKey(block.getLocation())) {
                //Its a real ore..
                mining.removeOreLocation(block.getLocation());
                e.getPlayer().sendMessage(ChatColor.RED + block.getType().name() + " unregistered.");
            }
        } else if (block.getType() == Material.WATER_LILY && e.getPlayer().hasMetadata("fishEdit")) {
            Fishing fishing = Fishing.getInstance();
            Integer tier = fishing.FISHING_LOCATIONS.get(block.getLocation());
            if (tier != null) {
                CommandFishing.removeHologram(block.getLocation());
                fishing.removeFishingLocation(block.getLocation());
                e.getPlayer().sendMessage(ChatColor.RED + "Unregistering Tier " + tier + " fishing spot.");
            }
        } else if (block.getType() == Material.CHEST) {
            //Breaking loot chest?
            LootSpawner spawner = LootManager.getSpawner(block.getLocation());
            if (spawner != null) {
                //Its a spawner chest..
                if (e.getPlayer().hasMetadata("lootchestedit")) {
                    //Destroy chest..
                    CommandLootChest.removeHologram(block.getLocation());
                    LootManager.removeLootSpawner(block.getLocation());
                    e.getPlayer().sendMessage(ChatColor.RED + "Loot Chest removed with loot type: " + spawner.getLootType().fileName + "!");
                }
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
                            if (mobSpawner.getInitialRespawnDelay() != delay.get()) {
                                mobSpawner.setInitialRespawnDelay(delay.get());
                                mobSpawner.setRespawnDelay(delay.get());
                            }
                            if (data != null && data.length >= 2) {
                                String weapon = data[1];
                                if (!weapon.equalsIgnoreCase("none")) {

                                    net.dungeonrealms.game.world.item.Item.ItemType type = net.dungeonrealms.game.world.item.Item.ItemType.getByName(weapon);
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


    private boolean isEqual(Location location, Location loc2) {
        return location.getBlockX() == loc2.getBlockX() && loc2.getBlockY() == loc2.getBlockY() && loc2.getBlockZ() == location.getBlockZ() && loc2.getWorld().getName().equals(location.getWorld().getName());
    }

}
