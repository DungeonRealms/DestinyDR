package net.dungeonrealms.game.profession;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.Buff;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.items.core.AuraType;
import net.dungeonrealms.game.item.items.core.ItemWoodAxe;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.data.WoodcutTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.*;
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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Woodcutting
 * @author Kihz
 */
public class Woodcutting implements GenericMechanic, Listener {

    private static HashMap<Location, WoodcutTier> woodLocations = new HashMap<>();

    //public static Set<Location> treasureChests = new HashSet<>();
   // private static Map<Item, Consumer<ArmorStand>> treasureItems = new ConcurrentHashMap<>();
    private static final int[] GEM_FIND_MIN = new int[]{01, 20, 40, 70, 90};
    private static final int[] GEM_FIND_MAX = new int[]{20, 40, 60, 90, 110};

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleWoodcutFatigue(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        Player p = event.getPlayer();
        ItemStack stackInHand = p.getEquipment().getItemInMainHand();
        Block block = event.getClickedBlock();

        if (!ItemWoodAxe.isWoodAxe(stackInHand))
            return;

        ItemWoodAxe axe = new ItemWoodAxe(stackInHand);
        WoodcutTier woodTier = WoodcutTier.getTierByLog(block.getType(), block.getData());

        if (woodTier == null) return;

//        p.removePotionEffect(PotionEffectType.SLOW_DIGGING);

        if (axe.getTier() == net.dungeonrealms.game.world.item.Item.ItemTier.TIER_1)
            return;

        if (axe.getTier() == net.dungeonrealms.game.world.item.Item.ItemTier.TIER_2) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 80, 0));
        } else if (axe.getTier().getId() == woodTier.getTier()) {

           // p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 80, 0));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void breakLog(BlockBreakEvent e) {
        Block block = e.getBlock();
        Random rand = ThreadLocalRandom.current();
        ItemStack item = e.getPlayer().getEquipment().getItemInMainHand();

        // Verify main world.
        if (!GameAPI.isMainWorld(block.getLocation()))
            return;

        // Verify this is an axe.
        if (!ItemWoodAxe.isWoodAxe(item))
            return;

        //Verify we're breaking logs.
        WoodcutTier woodTier = WoodcutTier.getTierByLog(block.getType(), block.getData());
        if (woodTier == null) {
            if (isLogMineable(block.getLocation()) && block.getType() == Material.STONE)
                e.setCancelled(true);
            return;
        }

        e.setCancelled(true);

        if (!isLogMineable(block.getLocation())) return;
        ItemWoodAxe axe = new ItemWoodAxe(item);
        Player p = e.getPlayer();

        //  WRONG TIER  //
        if (axe.getTier().getId() < woodTier.getTier()) {
            p.sendMessage(ChatColor.RED + "Your axe is not strong enough to mine this log!");
            return;
        }

        //  ADD PLAYER XP  //
        int xpGain = woodTier.getXP();

        double mult = ItemLootAura.getDropMultiplier(p.getLocation(), AuraType.PROFESSION);

        int xpToAdd = 0;
        if (mult > 0)
            xpToAdd = (int) (xpGain * mult * .01);

        PlayerWrapper pw = PlayerWrapper.getWrapper(p);
        pw.addExperience(xpGain / 12, false, true, true);

        Material type = e.getBlock().getType();
        int logsToAdd = 0;
        p.playSound(block.getLocation(), Sound.BLOCK_WOOD_BREAK, 1F, 0.75F);
        e.getBlock().setType(Material.AIR);

        //  REPLACE LOG  //
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> e.getBlock().setType(woodTier.getLog()), woodTier.getLogRespawnTime() * 15);
        double chance = rand.nextInt(100);

        //  DAMAGE ITEM  //
        if (rand.nextInt(100) > axe.getAttributes().getAttribute(net.dungeonrealms.game.world.item.Item.WoodAxeAttributeType.DURABILITY).getValue() + Trinket.getTrinketValue(p, Trinket.MINE_DURABILITY))
            axe.damageItem(p, logsToAdd > 0 ? 2 : 1);

        //   SUCCESS   //
        if (chance < axe.getSuccessChance() || axe.getTier().getId() > woodTier.getTier()) {
         logsToAdd = 1;
        }

        //  FAILED  //
        if (logsToAdd == 0) {
            axe.updateItem(p, false);
            p.getEquipment().setItemInMainHand(axe.generateItem());
            p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString() + "You fail to gather any wood.");
            return;
        }

        axe.addExperience(p, xpGain);

        if (xpToAdd > 0) {
            axe.addExperience(p, xpToAdd, false);
            pw.sendDebug(ChatColor.YELLOW.toString() + ChatColor.BOLD + "    " + ChatColor.GOLD
                    .toString() + ChatColor.BOLD + "PROF. AURA >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                    + "+" + ChatColor.YELLOW + Math.round(xpToAdd) + ChatColor.BOLD + " EXP " +
                    ChatColor.GRAY + "[" + axe.getXP() + ChatColor.BOLD + "/" + ChatColor.GRAY + axe.getNeededXP() + " EXP]");
        }

        if (xpGain > 0) {
            Buff active = DonationEffects.getInstance().getWeekendBuff();
            if (active != null && active.getType() == EnumBuff.PROFESSION) {
                double toGive = xpGain * (active.getBonusAmount() * 0.01);
                if (toGive > 0) {
                    axe.addExperience(p, (int) toGive, false);
                    pw.sendDebug(ChatColor.YELLOW.toString() + ChatColor.BOLD + "    " + ChatColor.GOLD
                            .toString() + ChatColor.BOLD + (int) active.getBonusAmount() + "% XP WEEKEND >> " + ChatColor.YELLOW.toString() + ChatColor.BOLD
                            + "+" + ChatColor.YELLOW + Math.round(toGive) + ChatColor.BOLD + " EXP " +
                            ChatColor.GRAY + "[" + axe.getXP() + ChatColor.BOLD + "/" + ChatColor.GRAY + axe.getNeededXP() + " EXP]");
                }
            }
        }

        boolean hasAxe = axe.updateItem(p, false);
        if (!hasAxe) {
            p.sendMessage(ChatColor.RED + "It seems your Axe has disappeared?");
            return;
        }
        p.updateInventory();

        //  DOUBLE LOG  //
        if (axe.getAttributes().getAttribute(net.dungeonrealms.game.world.item.Item.WoodAxeAttributeType.DOUBLE_LOG).getValue() >= rand.nextInt(100) + 1) {
            logsToAdd *= 2;
            pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE LOG DROP" + ChatColor.YELLOW + " (2x)");
        }

        //  TRIPLE LOG  //
        if (axe.getAttributes().getAttribute(net.dungeonrealms.game.world.item.Item.WoodAxeAttributeType.TRIPLE_LOG).getValue() >= rand.nextInt(100) + 1) {
            logsToAdd *= 3;
            pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE LOG DROP" + ChatColor.YELLOW + " (3x)");
        }

        //  GIVE LOG  //
        ItemStack log = woodTier.createLogItem();
        log.setAmount(logsToAdd);
        GameAPI.giveOrDropItem(p, log);


        if (axe.getAttributes().getAttribute(net.dungeonrealms.game.world.item.Item.WoodAxeAttributeType.GEM_FIND).getValue() >= rand.nextInt(100) + 1) {
            int tier = woodTier.getTier() - 1;
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

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadLogLocations();
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), this::placeLog);

    }

    private void loadLogLocations() {
        int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig().getStringList("logspawns");
        for (String line : CONFIG) {
            if (line.contains("=")) {
                try {
                    String[] cords = line.split("=")[0].split(",");
                    Location loc;
                    if(cords.length >= 4) {
                        loc = new Location(Bukkit.getWorld(cords[0]), Double.parseDouble(cords[1]),
                                Double.parseDouble(cords[2]), Double.parseDouble(cords[3]));
                    } else {
                        loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
                                Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));
                    }

                    int tier = Integer.parseInt(line.split("=")[1]);
                    woodLocations.put(loc, WoodcutTier.values()[tier - 1]);

                    count++;
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
        Utils.log.info("[Woodcutting] Loaded " + count + " log spawns.");
    }

    /**
     * Add a log spawn location.
     */
    public static void addLogs(Location loc, Material m, short logType) {
        assert isPossibleLog(m, logType);
        woodLocations.put(loc, WoodcutTier.getTierByLog(m, logType));
        updateConfig();
    }

    /**
     * Remove a log for the spawn db.
     */
    public static void removeLog(Block log) {
        woodLocations.remove(log.getLocation());
        updateConfig();
    }

    /**
     * Save the config. Called whenever a change is made.
     */
    private static void updateConfig() {
        List<String> saveLog = new ArrayList<>();
        for (Location l : woodLocations.keySet())
            saveLog.add(l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "=" + woodLocations.get(l).getTier());
        DungeonRealms.getInstance().getConfig().set("logspawns", saveLog);
        DungeonRealms.getInstance().saveConfig();
    }

    /**
     * Places every log in the map. (Called on startup.)
     */
    private void placeLog() {
        woodLocations.keySet().forEach(logLoc -> logLoc.getWorld().getBlockAt(logLoc).setType(woodLocations.get(logLoc).getLog()));
    }

    /**
     * Is this material a correct type of log?
     */
    public static boolean isPossibleLog(Material mat, short logType) {
        return WoodcutTier.getTierByLog(mat, logType) != null;
    }

    /**
     * Is this block a registered log mining spot?
     */
    public static boolean isLogMineable(Block block) {
        return isLogMineable(block.getLocation());
    }

    /**
     * Is this location a registered log mining spot?
     *
     * @param loc
     * @return
     */
    public static boolean isLogMineable(Location loc) {
        return woodLocations.keySet().stream().anyMatch(l -> l.equals(loc));
    }

    @Override
    public void stopInvocation() {

    }
}
