package net.dungeonrealms.game.profession;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveMineOre;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mining - Core listeners for the mining profession.
 * <p>
 * Redone by Kneesnap on April 7th, 2017.
 */
public class Mining implements GenericMechanic, Listener {

    private static HashMap<Location, MiningTier> oreLocations = new HashMap<>();

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

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadOreLocations();
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::placeOre);
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