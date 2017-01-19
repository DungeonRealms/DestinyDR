package net.dungeonrealms.game.listener.world;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.miscellaneous.Repair;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.loot.LootSpawner;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.obj.RealmToken;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

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

    /**
     * Disables the placement of Realm Chest.
     *
     * @param event
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void placeRealmChest(BlockPlaceEvent event) {
        if (event.getItemInHand() == null) return;
        if (event.getItemInHand().getType() != Material.CHEST)
            return;

        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(event.getItemInHand());
        if (nms.hasTag() && nms.getTag().hasKey("type"))
            event.setCancelled(true);

        RealmToken realm = Realms.getInstance().getToken(event.getPlayer().getWorld());
        if (realm == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place Realm Chests here.");
            event.setCancelled(true);
            return;
        }
        if (!realm.getBuilders().contains(event.getPlayer().getUniqueId()) && !realm.getOwner().equals(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.RED + "You can't place Realm Chests here.");
            event.setCancelled(true);
        }

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
        if (block.getType() == Material.ARMOR_STAND) {
            SpawningMechanics.getALLSPAWNERS().stream().filter(spawner -> spawner.getLoc() == block.getLocation()).forEach(SpawningMechanics::remove);
        }
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

    /**
     * Handles `ing ore
     *
     * @param e
     * @since 1.0
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void breakOre(BlockBreakEvent e) {
        Block block = e.getBlock();
        Random rand = new Random();
        if (!e.getPlayer().getWorld().equals(Bukkit.getWorlds().get(0))) return;

        if (block == null) return;
        if (e.getPlayer().getEquipment().getItemInMainHand() == null || e.getPlayer().getEquipment().getItemInMainHand().getType() == Material.AIR)
            return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
            e.setCancelled(true);
            ItemStack stackInHand = e.getPlayer().getEquipment().getItemInMainHand();
            if (Mining.isDRPickaxe(stackInHand)) {
                Player p = e.getPlayer();
                Material type = block.getType();
                int tier = Mining.getBlockTier(type);
                int pickTier = Mining.getPickTier(stackInHand);
                if (pickTier < tier) {
                    p.sendMessage(ChatColor.RED + "Your pick is not strong enough to mine this ore!");
                    return;
                }

                int experienceGain = Mining.getOreEXP(stackInHand, type);
                GamePlayer gamePlayer = GameAPI.getGamePlayer(e.getPlayer());
                if (gamePlayer == null) return;
                gamePlayer.addExperience((experienceGain / 12), false, true);
                int duraBuff = Mining.getDurabilityBuff(stackInHand);
                int breakChance = Mining.getBreakChance(stackInHand);
                breakChance += Mining.getSuccessChance(stackInHand);
                int willBreak = rand.nextInt(100);
                int oreToAdd = 0;

                p.playSound(p.getLocation(), Sound.BLOCK_STONE_BREAK, 1F, 0.75F);
                e.getBlock().setType(Material.STONE);

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> e.getBlock().setType(type), (Mining.getOreRespawnTime(type) * 15));

                if (willBreak < breakChance || pickTier > tier) {
                    Mining.addExperience(stackInHand, experienceGain, p);
                    oreToAdd++;
                    gamePlayer.getPlayerStatistics().setOreMined(gamePlayer.getPlayerStatistics().getOreMined() + 1);
                    if (rand.nextInt(100) > duraBuff) {
                        RepairAPI.subtractCustomDurability(p, p.getEquipment().getItemInMainHand(), 2);
                    }
                } else {
                    if (rand.nextInt(100) > duraBuff) {
                        RepairAPI.subtractCustomDurability(p, p.getEquipment().getItemInMainHand(), 1);
                    }
                    p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "You fail to gather any ore.");
                    return;
                }


                int doubleDrop = rand.nextInt(100) + 1;

                if (Mining.getDoubleDropChance(stackInHand) >= doubleDrop) {
                    oreToAdd++;
                    gamePlayer.getPlayerStatistics().setOreMined(gamePlayer.getPlayerStatistics().getOreMined() + 1);
                    if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId()))
                        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE ORE DROP" + ChatColor.YELLOW + " (2x)");
                }

                int tripleDrop = rand.nextInt(100) + 1;
                if (Mining.getTripleDropChance(stackInHand) >= tripleDrop) {
                    oreToAdd = oreToAdd + 2;
                    gamePlayer.getPlayerStatistics().setOreMined(gamePlayer.getPlayerStatistics().getOreMined() + 2);
                    if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId()))
                        p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE ORE DROP" + ChatColor.YELLOW + " (3x)");
                }

                ItemStack ore = Mining.getBlock(type);
                ore.setAmount(oreToAdd);

                if (p.getInventory().firstEmpty() == -1) {
                    p.getWorld().dropItem(p.getLocation(), ore);
                    p.sendMessage(ChatColor.GRAY + "Your inventory was " + ChatColor.UNDERLINE + "full" + ChatColor.GRAY + ", so the ore has been dropped at your feet.");
                } else {
                    p.getInventory().addItem(ore);
                }


                int dropGems = rand.nextInt(100) + 1;
                if (Mining.getGemFindChance(stackInHand) >= dropGems) {
                    int amount = 0;
                    switch (tier) {
                        case 1:
                            amount = rand.nextInt(20) + 1;
                            break;
                        case 2:
                            amount = rand.nextInt(40 - 20) + 20;
                            break;
                        case 3:
                            amount = rand.nextInt(60 - 40) + 40;
                            break;
                        case 4:
                            amount = rand.nextInt(90 - 70) + 70;
                            break;
                        case 5:
                            amount = rand.nextInt(110 - 90) + 90;
                            break;
                    }
                    amount = (int) (amount * 0.80D);

                    if (amount > 0) {
                        p.getWorld().dropItemNaturally(p.getLocation(), BankMechanics.createGems(amount));
                        if ((boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId()))
                            p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          FOUND " + amount + " GEM(s)" + ChatColor.YELLOW + "");
                    }
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void cookFish(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = e.getClickedBlock();
        if (block == null) return;
        if (block.getType() == Material.TORCH || block.getType() == Material.BURNING_FURNACE || block.getType() == Material.FURNACE || block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.FIRE) {
            e.setCancelled(true);
            if (e.getPlayer().getEquipment().getItemInMainHand() == null || e.getPlayer().getEquipment().getItemInMainHand().getType() == Material.AIR)
                return;
            if (e.getPlayer().getEquipment().getItemInMainHand().getType() == Material.RAW_FISH) {
                e.setCancelled(true);
                if (block.getState() instanceof Furnace) {
                    final Furnace furnace = (Furnace) block.getState();
                    furnace.setBurnTime((short) 20);
                }
                e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1);
                ItemStack stack = e.getPlayer().getEquipment().getItemInMainHand();

                ItemStack cookedFish = stack;
                ItemMeta cookedFishMeta = cookedFish.getItemMeta();
                String newFishName = cookedFishMeta.getDisplayName().split(" ", 2)[1];
                cookedFishMeta.setDisplayName(newFishName);
                cookedFish.setItemMeta(cookedFishMeta);
                cookedFish.setType(Material.COOKED_FISH);

                e.getPlayer().updateInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleMiningFatigue(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        if (event.getClickedBlock() == null || event.getClickedBlock().getType() == null || event.getClickedBlock().getType() == Material.AIR)
            return;

        Player p = event.getPlayer();

        if (p.getEquipment().getItemInMainHand() == null || event.getPlayer().getEquipment().getItemInMainHand().getType() == Material.AIR)
            return;
        if (!Mining.isDRPickaxe(p.getEquipment().getItemInMainHand())) return;

        ItemStack stackInHand = p.getEquipment().getItemInMainHand();
        Block block = event.getClickedBlock();
        if (block == null || block.getType() == Material.AIR) return;
        if (block.getType() == Material.COAL_ORE || block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE || block.getType() == Material.DIAMOND_ORE || block.getType() == Material.EMERALD_ORE) {
            Material type = block.getType();
            int tier = Mining.getBlockTier(type);
            int pickTier = Mining.getPickTier(stackInHand);

            p.removePotionEffect(PotionEffectType.SLOW_DIGGING);

            switch (pickTier) {
                case 1:
                    break;
                case 2:
                    if (tier == pickTier && block.getType() == Material.EMERALD_ORE)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0));
                    break;
                case 3:
                    if (tier == pickTier)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0));
                    break;
                case 4:
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0));
                    break;
                case 5:
                    if (tier != 4)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0));
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void cancelPlayersBlockOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block == null) return;
            if (event.getPlayer().isOp() || event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
            Material mat = block.getType();

            if (mat == Material.HOPPER && mat == Material.DISPENSER
                    && ((block.getWorld().equals(Bukkit.getWorlds().get(0)) || block.getWorld().getName().contains("DUNGEON")) && Realms.getInstance().getToken(block.getWorld()) == null))
                event.setCancelled(true);

            if (mat == Material.FURNACE || mat == Material.HOPPER_MINECART || mat == Material.TRAPPED_CHEST || mat == Material.BREWING_STAND || mat == Material.ENCHANTMENT_TABLE)
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

        Player player = event.getPlayer();
        if (player.getEquipment().getItemInMainHand() == null || player.getEquipment().getItemInMainHand().getType() == Material.AIR) {
            player.sendMessage(ChatColor.YELLOW + "Equip the item to repair and " + ChatColor.UNDERLINE + "RIGHT CLICK" + ChatColor.RESET + ChatColor.YELLOW + " the ANVIL.");
            player.sendMessage(ChatColor.GRAY + "Or, if you have an item scrap, drag it on top of the item in your inventory.");
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getPlayer().getEquipment().getItemInMainHand();
        if (!GameAPI.isWeapon(item) && !GameAPI.isArmor(item) && !Mining.isDRPickaxe(item) && !Fishing.isDRFishingPole(item)) {
            event.setCancelled(true);
            return;
        }


        if (Mining.isDRPickaxe(item) || Fishing.isDRFishingPole(item)) {
            int lvl = Mining.getLvl(item);
            if (lvl >= 100) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can not repair this level 100 item.");
                return;
            }
        }

        if (RepairAPI.canItemBeRepaired(item)) {

            if (item.getDurability() == (short) 0) {
                //Fully repaired already, carry on pls.
                player.sendMessage(ChatColor.YELLOW + "This item is " + ChatColor.UNDERLINE + "NOT" + ChatColor.YELLOW + " damaged.");
                return;
            }

            if (repairMap.containsKey(block.getLocation())) {
                //Cant repair?
                Repair currentRepair = repairMap.get(block.getLocation());
                if (player.getName().equals(currentRepair.getRepairing())) {
                    player.sendMessage(ChatColor.RED + "Please finish repairing your item.");
                    return;
                }
                for (Entity nearby : player.getNearbyEntities(20, 20, 20)) {
                    if (nearby instanceof Player) {
                        Player pl = (Player) nearby;
                        if (pl.getName().equals(currentRepair.getRepairing())) {
                            player.sendMessage(ChatColor.RED + "This anvil is currently in use by " + currentRepair.getRepairing() + "!");
                            return;
                        }
                    }
                }


                //Return the item?
                Player pl = Bukkit.getPlayer(currentRepair.getRepairing());
                if (pl != null) {
                    //They are too far away.
                    currentRepair.getRepairItem().remove();
                    pl.sendMessage(ChatColor.RED + "You were > 10 blocks from the anvil.");
                    returnItem(pl, currentRepair.getItem());
                }
                //Player isnt nearby anymore?
                repairMap.remove(block.getLocation());
            }

            int newCost = RepairAPI.getItemRepairCost(item);
            if (BankMechanics.getInstance().getTotalGemsInInventory(player) < newCost) {
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

//            block.setMetadata("repairing", new FixedMetadataValue(DungeonRealms.getInstance(), player.getName()));


            Repair repair = new Repair(item, itemEntity, player.getName());
            repairMap.put(block.getLocation(), repair);

            String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : StringUtils.capitaliseAllWords(item.getType().name().toLowerCase().replace("_", ""));
            player.setCanPickupItems(false);
            player.sendMessage(ChatColor.YELLOW + "It will cost " + ChatColor.GREEN + ChatColor.BOLD.toString() + newCost + "G" + ChatColor.YELLOW + " to repair '" + name + ChatColor.YELLOW + "'");
            player.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + ChatColor.BOLD.toString() + "Y" + ChatColor.GRAY + " to confirm this repair. Or type " + ChatColor.RED + ChatColor.BOLD.toString() + "N" + ChatColor.GRAY + " to cancel.");
            Chat.listenForMessage(player, chat -> {
                // Anvil is in use by someone else, we can't return them this item again.
                if (!repairMap.containsKey(block.getLocation()) || !repairMap.get(block.getLocation()).getRepairing().equalsIgnoreCase(player.getName())) {
                    itemEntity.remove();
                    player.setCanPickupItems(true);
                    return;
                }

                if (chat.getMessage().equalsIgnoreCase("yes") || chat.getMessage().equalsIgnoreCase("y")) {
                    //Not enough? cya.
                    if (BankMechanics.getInstance().getTotalGemsInInventory(player) < newCost) {
                        player.sendMessage(ChatColor.RED + "You do not have enough gems to repair this item.");
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "COST: " + ChatColor.RED + newCost + ChatColor.BOLD.toString() + " GEMS(s)");
                        return;
                    }
                    //Reset durability.
                    RepairAPI.setCustomItemDurability(item, 1500);
                    player.updateInventory();
                    itemEntity.remove();
                    middle.getWorld().playEffect(middle, Effect.STEP_SOUND, Material.IRON_BLOCK);
                    middle.getWorld().playSound(middle, Sound.BLOCK_ANVIL_USE, 3, 1.4F);

                    player.sendMessage(ChatColor.RED + "-" + newCost + ChatColor.BOLD.toString() + "G");
                    player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "ITEM REPAIRED");
                    BankMechanics.getInstance().takeGemsFromInventory(newCost, player);
                    if (player.getEquipment().getItemInMainHand() == null) {
                        player.getEquipment().setItemInMainHand(item);
                        player.setCanPickupItems(true);
                    } else {
                        if (player.getInventory().firstEmpty() != -1)
                            player.getInventory().setItem(player.getInventory().firstEmpty(), item);
                        else {
                            player.sendMessage(ChatColor.RED + "No inventory space.");
                            itemEntity.remove();
                            returnItem(player, item);
                            repairMap.remove(block.getLocation());
                            player.sendMessage(ChatColor.RED + "Item Repair - " + ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED");
                            player.setCanPickupItems(true);
                        }
                    }
                    player.updateInventory();
                    repairMap.remove(block.getLocation());
                } else {
                    //Cancel
                    itemEntity.remove();
                    returnItem(player, item);
                    repairMap.remove(block.getLocation());
                    player.sendMessage(ChatColor.RED + "Item Repair - " + ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED");
                    player.setCanPickupItems(true);
                }
            }, p -> {
                // Anvil is in use by the current user, return those items.
                if (repairMap.containsKey(block.getLocation()) && repairMap.get(block.getLocation()).getRepairing().equalsIgnoreCase(player.getName()))
                    returnItem(player, item);

                itemEntity.remove();
                repairMap.remove(block.getLocation());
                p.sendMessage(ChatColor.RED + "Item Repair - " + ChatColor.RED + ChatColor.BOLD.toString() + "CANCELLED");
                player.setCanPickupItems(true);
            });
        } else {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This item is already repaired all the way!");
        }
    }

    private void returnItem(Player player, ItemStack item) {
        if (player.getEquipment().getItemInMainHand() == null) {
            player.getEquipment().setItemInMainHand(item);
        } else {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
        player.updateInventory();
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
    public void shiftRightClickJournal(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking()) {
            ItemStack stack = e.getItem();
            if (stack == null) return;
            if (stack.getType() != Material.WRITTEN_BOOK) return;
            e.setCancelled(true);
            net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
            if (!nms.hasTag() && !nms.getTag().hasKey("journal")) return;
            Block b1 = e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 1, 0));
            Block b2 = e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(1, 1, 0));
            Player player = e.getPlayer();
            if (b1.getType() == Material.AIR && b2.getType() == Material.AIR && GameAPI.isInSafeRegion(e.getClickedBlock().getLocation()) && player.getLocation().getWorld().equals(Bukkit.getWorlds().get(0))) {

                if (ShopMechanics.ALLSHOPS.containsKey(player.getName())) {
                    Shop shop = ShopMechanics.getShop(player.getName());
                    player.sendMessage(ChatColor.YELLOW + "You already have an open shop on " + ChatColor.UNDERLINE + "this" + ChatColor.YELLOW + " server.");
                    player.sendMessage(ChatColor.GRAY + "Shop Location: " + (int) shop.block1.getLocation().getX() + ", " + (int) shop.block1.getLocation().getY() + ", " + (int) shop.block1.getLocation().getZ());
                    return;
                }
                GamePlayer gp = GameAPI.getGamePlayer(player);
                if (!TutorialIsland.onTutorialIsland(player.getLocation())) {
                    if (GameAPI.isInSafeRegion(b1.getLocation()) && !GameAPI.isMaterialNearby(b1, 2, Material.CHEST) && !GameAPI.isMaterialNearby(b1, 10, Material.ENDER_CHEST) && !GameAPI.isMaterialNearby(b1, 3, Material.PORTAL)) {
                        if (gp != null && !gp.hasShopOpen()) {
                            if (BankMechanics.getInstance().getStorage(player.getUniqueId()).collection_bin != null) {
                                player.sendMessage(ChatColor.RED + "You have item(s) waiting in your collection bin.");
                                player.sendMessage(ChatColor.GRAY + "Access your bank chest to claim them.");
                                e.setCancelled(true);
                                return;
                            }

                            ShopMechanics.setupShop(e.getClickedBlock(), player.getUniqueId());
                        } else {
                            player.sendMessage(ChatColor.RED + "You have a shop open already! It may be on another shard.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot place a shop here.");
                    }
                } else {
                	player.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED
                            + " open a shop until you have left the tutorial.");
                }
            }
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
