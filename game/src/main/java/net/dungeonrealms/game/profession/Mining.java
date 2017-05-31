package net.dungeonrealms.game.profession;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.overrides.CosmeticOverrides;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.functional.ItemEXPLamp;
import net.dungeonrealms.game.item.items.functional.ItemEnchantPickaxe;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemOrb;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.quests.Quest;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveMineOre;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

/**
 * Mining - Core listeners for the mining profession.
 * <p>
 * Redone by Kneesnap on April 7th, 2017.
 */
public class Mining implements GenericMechanic, Listener {

    private static HashMap<Location, MiningTier> oreLocations = new HashMap<>();

    public static Set<Location> treasureChests = new HashSet<>();
    private static Map<Item, Consumer<ArmorStand>> treasureItems = new ConcurrentHashMap<>();
    private static final int[] GEM_FIND_MIN = new int[]{01, 20, 40, 70, 90};
    private static final int[] GEM_FIND_MAX = new int[]{20, 40, 60, 90, 110};

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleMiningFatigue(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player p = event.getPlayer();
        ItemStack stackInHand = p.getEquipment().getItemInMainHand();
        Block block = event.getClickedBlock();

        if (!ItemPickaxe.isPickaxe(stackInHand))
            return;

        ItemPickaxe pickaxe = new ItemPickaxe(stackInHand);
        MiningTier oreTier = MiningTier.getTierFromOre(block.getType());

        if (oreTier == null) return;

        p.removePotionEffect(PotionEffectType.SLOW_DIGGING);

        if (pickaxe.getTier() == ItemTier.TIER_1)
            return;

        if (pickaxe.getTier() == ItemTier.TIER_2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0));
        } else if (pickaxe.getTier().getId() == oreTier.getTier()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void breakOre(BlockBreakEvent e) {
        Block block = e.getBlock();
        Random rand = ThreadLocalRandom.current();
        ItemStack item = e.getPlayer().getEquipment().getItemInMainHand();

        // Verify main world.
        if (!GameAPI.isMainWorld(block.getLocation()))
            return;

        // Verify this is a pickaxe.
        if (!ItemPickaxe.isPickaxe(item))
            return;

        //Verify we're breaking ore.
        MiningTier oreTier = MiningTier.getTierFromOre(block.getType());
        if (oreTier == null) {
            if (isMineable(block.getLocation()) && block.getType() == Material.STONE)
                e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        ItemPickaxe pickaxe = new ItemPickaxe(item);
        Player p = e.getPlayer();

        //  WRONG TIER  //
        if (pickaxe.getTier().getId() < oreTier.getTier()) {
            p.sendMessage(ChatColor.RED + "Your pick is not strong enough to mine this ore!");
            return;
        }

        //  ADD PLAYER XP  //
        int xpGain = oreTier.getXP();
        PlayerWrapper pw = PlayerWrapper.getWrapper(p);
        pw.addExperience(xpGain / 12, false, true);

        Material type = e.getBlock().getType();
        int oreToAdd = 0;
        p.playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1F, 0.75F);
        e.getBlock().setType(Material.STONE);

        //  REPLACE ORE  //
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> e.getBlock().setType(oreTier.getOre()), oreTier.getOreRespawnTime() * 15);
        double chance = rand.nextInt(100);
        if (TutorialIsland.onTutorialIsland(p.getLocation()) && Quests.getInstance().hasCurrentQuestObjective(p, "Tutorial Island", ObjectiveMineOre.class)) {
            chance = 1;
        }
        //  SUCCESS  //
        if (chance < pickaxe.getSuccessChance() || pickaxe.getTier().getId() > oreTier.getTier()) {
            oreToAdd = 1;
            pw.getPlayerGameStats().addStat(StatColumn.ORE_MINED);
        }

        //  DAMAGE ITEM  //
        if (rand.nextInt(100) > pickaxe.getAttributes().getAttribute(PickaxeAttributeType.DURABILITY).getValue())
            pickaxe.damageItem(p, oreToAdd > 0 ? 2 : 1);

        //  FAILED  //
        if (oreToAdd == 0) {
            p.getEquipment().setItemInMainHand(pickaxe.generateItem());
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "You fail to gather any ore.");
            return;
        }

        Location l = block.getLocation();
        if (rand.nextInt(100) < pickaxe.getAttributes().getAttribute(PickaxeAttributeType.TREASURE_FIND).getValue()) {
            //Spawn treasure?
            block.setType(Material.CHEST);
            Byte direction = direction(p);
            System.out.println("The block direction: " + direction);
            block.setData(direction);
            treasureChests.add(e.getBlock().getLocation());
            l.getWorld().playSound(l, Sound.ENTITY_WITHER_BREAK_BLOCK, 1, 1.8F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                ((CraftWorld) l.getWorld()).getHandle().playBlockAction(new BlockPosition(l.getX(), l.getY(), l.getZ()), net.minecraft.server.v1_9_R2.Block.getById(e.getBlock().getTypeId()), 1, 1);
                l.getWorld().playSound(l, Sound.BLOCK_CHEST_OPEN, 1, 1.1F);
                Quest.spawnFirework(l.clone().add(0.5, 0.7, 0.5), FireworkEffect.builder().withColor(Color.GREEN).build());
            }, 10);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                //Give loot.
                ItemStack loot = createTreasureFindItem(oreTier);

                ArmorStand rise = (ArmorStand) block.getWorld().spawnEntity(block.getLocation().clone().add(0.5, -1.3, 0.5), EntityType.ARMOR_STAND);
                rise.setVisible(false);
                rise.setGravity(false);
                rise.setInvulnerable(true);
                rise.setCollidable(false);
                rise.setCustomName(Utils.getItemName(loot));
                rise.setCustomNameVisible(false);
                Item itemEnt = ItemManager.whitelistItemDrop(p, block.getWorld().dropItem(rise.getLocation(), new ItemStack(loot.getType(), loot.getAmount(), loot.getDurability())));
                MetadataUtils.Metadata.NO_PICKUP.set(itemEnt, true);
                itemEnt.setPickupDelay(Integer.MAX_VALUE);
                rise.setPassenger(itemEnt);
                //Called after its done floating or after 6 seconds.
                treasureItems.put(itemEnt, stand -> {
                    stand.remove();
                    giveLoot(p, loot);
                    for(int k = 0; k < 500; k++) {
                        giveLoot(p,createTreasureFindItem(oreTier));
                    }
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_TOUCH, 1, 1.2F);
                });
            }, 20);

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                //5 seconds.
                if (block.getType() == Material.CHEST) {
                    block.getWorld().playEffect(block.getLocation().add(0.5, .5, .5), Effect.STEP_SOUND, Material.CHEST);
                    block.setType(Material.STONE);
                }
            }, 20 * 5);
        }

        pickaxe.addExperience(p, xpGain);
        ItemStack hand = pickaxe.generateItem();
        p.getEquipment().setItemInMainHand(hand);
        Bukkit.getLogger().info("Dura: " + hand.getDurability());
        p.updateInventory();

        //  DOUBLE ORE  //
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.DOUBLE_ORE).getValue() >= rand.nextInt(100) + 1) {
            oreToAdd *= 2;
            pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE ORE DROP" + ChatColor.YELLOW + " (2x)");
        }

        //  TRIPLE ORE  //
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.TRIPLE_ORE).getValue() >= rand.nextInt(100) + 1) {
            oreToAdd *= 3;
            pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE ORE DROP" + ChatColor.YELLOW + " (3x)");
        }

        //  GIVE ORE  //
        ItemStack ore = oreTier.createOreItem();
        ore.setAmount(oreToAdd);
        GameAPI.giveOrDropItem(p, ore);


        if (oreToAdd > 0)
            Quests.getInstance().triggerObjective(p, ObjectiveMineOre.class);

        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.GEM_FIND).getValue() >= rand.nextInt(100) + 1) {
            int tier = oreTier.getTier() - 1;
            int amount = (int) (Utils.randInt(GEM_FIND_MIN[tier], GEM_FIND_MAX[tier]) * 0.8);

            //  DROP GEMS  //
            if (amount > 0) {
                Item is = p.getWorld().dropItemNaturally(block.getLocation().add(.5, 1.2, .5), new ItemGem(amount).generateItem());
                is.setVelocity(new Vector(0, -.3D, 0));
                pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          FOUND " + amount + " GEM(s)");
            }
        }
    }

    public Byte direction(Player player) {
        double rotation = (player.getLocation().getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (rotation >= 45 && rotation <= 135) return 3;
        else if (rotation >= 135 && rotation <= 225) return 4;
        else if (rotation <= 45 || rotation >= 315) return 5;
        else return 1;
    }

    public void giveLoot(Player player, ItemStack toGive) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        String name = Utils.getItemName(toGive);
        if (toGive.getType().equals(Material.GOLD_ORE)) {
            wrapper.getPurchaseablesUnlocked().put(Purchaseables.GOLD_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.GOLD_ORE_HAT);
        } else if (toGive.getType().equals(Material.DIAMOND_ORE)) {
            wrapper.getPurchaseablesUnlocked().put(Purchaseables.DIAMOND_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.DIAMOND_ORE_HAT);
        } else if (toGive.getType().equals(Material.IRON_ORE)) {
            wrapper.getPurchaseablesUnlocked().put(Purchaseables.IRON_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.IRON_ORE_HAT);
        } else if (toGive.getType().equals(Material.EMERALD_ORE)) {
            wrapper.getPurchaseablesUnlocked().put(Purchaseables.EMERALD_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.EMERALD_ORE_HAT);
        } else if (toGive.getType().equals(Material.COAL_ORE)) {
            wrapper.getPurchaseablesUnlocked().put(Purchaseables.COAL_ORE_HAT, 1);
            wrapper.setActiveHatOverride(CosmeticOverrides.COAL_ORE_HAT);
        } else {
            GameAPI.giveOrDropItem(player, toGive);
        }

        player.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "** " + ChatColor.GREEN + "You discovered " + (toGive.getAmount() > 1 ? ChatColor.GREEN.toString() + ChatColor.BOLD + toGive.getAmount() + "x " + ChatColor.GREEN : "a(n) ") + name + ChatColor.GREEN + " inside a Treasure Chest! " + ChatColor.GREEN + ChatColor.BOLD + "**");
        if (toGive.getType().name().endsWith("_ORE"))
            player.sendMessage(ChatColor.GRAY + "Use " + ChatColor.UNDERLINE + "/hats" + ChatColor.GRAY + " to view all available hats!");
    }

    public ItemStack createTreasureFindItem(MiningTier tier) {
        Random r = ThreadLocalRandom.current();
        //FOOD, ORE, POTIONS, ENCHANT SCROLLS, ORB OF FLIGHT, ORB OF ALT .001%, PROF_EXP LAMP?
        if (r.nextInt(500) == 5) {
            //Hat
            ItemStack oreHelm = new ItemStack(tier.getOre());

            ItemMeta im = oreHelm.getItemMeta();
            im.setDisplayName(tier.getColor() + Utils.getItemName(oreHelm) + " Hat");
            oreHelm.setItemMeta(im);
            return oreHelm;
        } else if (r.nextInt(250) == 5) {
            return new ItemOrb().generateItem();
        } else if (r.nextInt(100) == 5) {
            return new ItemEXPLamp(ItemEXPLamp.ExpType.PROFESSION, Utils.randInt(tier.getMinXPBottle(), tier.getMaxXPBottle())).generateItem();
        } else if (r.nextInt(750) == 5) {
            return new ItemEnchantPickaxe().generateItem();
        } else {
            List<Material> junks = Lists.newArrayList(Material.COOKED_BEEF, Material.CARROT_ITEM, Material.BAKED_POTATO, Material.APPLE, Material.BREAD, Material.PUMPKIN_PIE);
            return new ItemStack(junks.get(r.nextInt(junks.size())), r.nextInt(6) + 3);
        }

//        return new ItemEXPLamp(ItemEXPLamp.ExpType.PROFESSION, 100 + r.nextInt(1000)).generateItem();
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadOreLocations();
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::placeOre);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            treasureItems.forEach((is, callback) -> {

                ArmorStand vehicle = is.getVehicle() != null ? (ArmorStand) is.getVehicle() : null;

                int ticksLived = is.getTicksLived();
                if (ticksLived >= 20 * 4) {
                    is.remove();
                    callback.accept(vehicle);
                    treasureItems.remove(is);
                } else if (vehicle != null) {
                    if (ticksLived < 12) {
                        EntityArmorStand stand = (EntityArmorStand) ((CraftLivingEntity) vehicle).getHandle();
                        //riseeeee up.
                        stand.locY = stand.locY + .1D;
                    } else if (ticksLived > 12 && !vehicle.isCustomNameVisible()) {
                        vehicle.setCustomNameVisible(true);
                    }
                }
            });
        }, 2, 1);
    }

    private void loadOreLocations() {
        int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("orespawns");
        for (String line : CONFIG) {
            if (line.contains("=")) {
                try {
                    String[] cords = line.split("=")[0].split(",");
                    Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
                            Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));

                    int tier = Integer.parseInt(line.split("=")[1]);
                    oreLocations.put(loc, MiningTier.values()[tier - 1]);

                    count++;
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
        Utils.log.info("[Mining] Loaded " + count + " ore spawns.");
    }

    /**
     * Add an ore spawn location.
     */
    public static void addOre(Location loc, Material m) {
        assert isPossibleOre(m);
        oreLocations.put(loc, MiningTier.getTierFromOre(m));
        updateConfig();
    }

    /**
     * Remove an ore for the spawn db.
     */
    public static void removeOre(Block ore) {
        oreLocations.remove(ore.getLocation());
        updateConfig();
    }

    /**
     * Save the config. Called whenever a change is made.
     */
    private static void updateConfig() {
        List<String> save = new ArrayList<>();
        for (Location l : oreLocations.keySet())
            save.add(l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "=" + oreLocations.get(l));
        DungeonRealms.getInstance().getConfig().set("orespawns", save);
        DungeonRealms.getInstance().saveConfig();
    }

    /**
     * Places every ore in the map. (Called on startup.)
     */
    private void placeOre() {
        oreLocations.keySet().forEach(loc -> loc.getWorld().getBlockAt(loc).setType(oreLocations.get(loc).getOre()));
    }

    /**
     * Is this material a correct type of ore?
     */
    public static boolean isPossibleOre(Material mat) {
        return MiningTier.getTierFromOre(mat) != null;
    }

    /**
     * Is this block a registered ore mining spot?
     */
    public static boolean isMineable(Block block) {
        return isMineable(block.getLocation());
    }

    /**
     * Is this location a registered ore mining spot?
     *
     * @param loc
     * @return
     */
    public static boolean isMineable(Location loc) {
        return oreLocations.keySet().stream().anyMatch(l -> l.equals(loc));
    }

    @Override
    public void stopInvocation() {

    }
}