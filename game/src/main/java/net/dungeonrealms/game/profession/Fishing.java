package net.dungeonrealms.game.profession;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.command.moderation.CommandFishing;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.ReflectionAPI;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.profession.fishing.*;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveCatchFish;
import net.dungeonrealms.game.world.item.Item.FishingAttributeType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.minecraft.server.v1_9_R2.EntityFishingHook;
import net.minecraft.server.v1_9_R2.MathHelper;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fishing Profession - Contains all the code for the fishing mechanic.
 * <p>
 * Redone by Kneesnap on April 8th, 2017.
 */
public class Fishing implements GenericMechanic, Listener {

    private static HashMap<Location, Integer> FISHING_LOCATIONS = new HashMap<>();
    private static HashMap<Location, List<Location>> FISHING_PARTICLES = new HashMap<>();
    private static int splashCounter = 10;

    @Override
    public void startInitialization() {
        loadFishingLocations();
        FISHING_LOCATIONS.keySet().forEach(Fishing::generateParticles);
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());

        DungeonRealms.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
            int chance = splashCounter * splashCounter;
            if (splashCounter == 1)
                splashCounter = 21;

            splashCounter--;
            if (FISHING_PARTICLES.isEmpty())
                return;

            try {
                Random random = ThreadLocalRandom.current();
                for (Entry<Location, List<Location>> data : FISHING_PARTICLES.entrySet()) {
                    Location epicenter = data.getKey();
                    ParticleAPI.spawnParticle(Particle.WATER_SPLASH, epicenter, 20, 1.4F);
                    data.getValue().stream().filter(loc -> random.nextInt(chance) == 1).forEach(
                            loc -> ParticleAPI.spawnParticle(Particle.WATER_SPLASH, loc, 20, 1.4F));
                }
            } catch (ConcurrentModificationException cme) {
                cme.printStackTrace();
                Utils.log.info("[Professions] [ASYNC] Something went wrong checking a fishing spot and adding particles!");
            }
        }, 200L, 15L);
    }

    @Override
    public void stopInvocation() {

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    public static void addLocation(Location l, int tier) {
        FISHING_LOCATIONS.put(l, tier);
        updateConfig();
        CommandFishing.createHologram(l, tier);
        generateParticles(l);
    }

    public static void removeLocation(Block block) {
        FISHING_LOCATIONS.remove(block.getLocation());
        CommandFishing.removeHologram(block.getLocation());
        updateConfig();
    }

    private static void updateConfig() {
        List<String> locations = new ArrayList<>();
        for (Location l : FISHING_LOCATIONS.keySet())
            locations.add(l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "=" + FISHING_LOCATIONS.get(l));
        DungeonRealms.getInstance().getConfig().set("fishingspawns", locations);
        DungeonRealms.getInstance().saveConfig();
    }

    public static FishBuff loadBuff(NBTTagCompound tag) {
        FishBuffType fbt = FishBuffType.valueOf(tag.getString("buffType"));
        try {
            Class<? extends FishBuff> buffCls = fbt.getBuffClass();
            FishingTier tier = FishingTier.values()[tag.getInt("itemTier") - 1];
            try {
                return buffCls.getConstructor(tag.getClass(), tier.getClass()).newInstance(tag, tier);
            } catch (NoSuchMethodException ex) {
                return buffCls.getConstructor(tag.getClass()).newInstance(tag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to construct " + fbt.name());
        }
        return null;
    }

    public static FishBuff getRandomBuff(FishingTier tier) {
        int roll = ThreadLocalRandom.current().nextInt(100);
        int check = 0;
        for (FishBuffType bType : FishBuffType.values()) {
            try {
                Class<? extends FishBuff> buffCls = bType.getBuffClass();
                FishBuff buff = buffCls.getConstructor(tier.getClass()).newInstance(tier);
                if (roll >= check && roll < check + buff.getChance())
                    return buff;
                check += buff.getChance();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed to construct " + bType.name());
            }
        }
        return null;
    }

    @AllArgsConstructor
    @Getter
    public enum EnumFish {

        //  TIER 1  //
        Shrimp(1, "A pink scaled crustacean."),
        Anchovie(1, "A small blue, oily fish."),
        Crayfish(1, "A lobster-like and brown crustacean."),

        //  TIER 2  //
        Carp(2, "A Large, silver-scaled fish."),
        Herring(2, "A colourful and medium-sized fish."),
        Sardine(2, "A small and oily green fish."),

        //  TIER 3  //
        Salmon(3, "A beautiful jumping fish."),
        Trout(3, "A non-migrating Salmon."),
        Cod(3, "A cold-water, deep sea fish."),

        //  TIER 4  //
        Lobster(4, "A large, red crustacean."),
        Tuna(4, "A large, sapphire blue fish."),
        Bass(4, "A very large and white fish."),

        //  TIER 5  //
        Shark(5, "A terrifying and massive predator."),
        Swordfish(5, "An elongated fish with a long bill."),
        Monkfish(5, "A flat, large, and scary looking fish.");


        private int tier;
        private String desciption;

        public String getName() {
            return name();
        }

        public static EnumFish getRandomFish(int tier) {
            List<EnumFish> fishList = new ArrayList<>();
            for (EnumFish fish : values())
                if (fish.getTier() == tier)
                    fishList.add(fish);
            return fishList.get(ThreadLocalRandom.current().nextInt(fishList.size() - 1));
        }
    }

    @AllArgsConstructor
    @Getter
    public enum FishBuffType {
        DAMAGE(FishDamageBuff.class, "+", "% DMG", "", "Power", 0),
        HEALTH(FishHealBuff.class, "+", "% HP", "", "Healing", 0),
        REGEN(FishRegenBuff.class, "+", "% HP", "Healing", "Regeneration", 0),
        SPEED(FishSpeedBuff.class, "SPEED BUFF", "", "", "Agility", 1),
        HUNGER(FishHungerBuff.class, "-", "% HUNGER", "", "Satiety", 0),
        ARMOR(FishArmorBuff.class, "+", "% ARMOR", "", "Defense", 0),
        VISION(FishVisionBuff.class, "NIGHTVISION ", " BUFF", "", "Vision", 0),
        BLOCK(FishBlockBuff.class, "+", "% BLOCK", "", "Blocking", 0);

        private Class<? extends FishBuff> buffClass;
        private String buffPrefix;
        private String buffSuffix;
        private String prefix;
        private String baseSuffix;
        private int fishMeta;


        public static FishBuffType getByName(String str) {
            for (FishBuffType t : values())
                if (t.name().equals(str))
                    return t;
            return null;
        }
    }

    private static void generateParticles(Location l) {
        List<Location> lfishingParticles = new ArrayList<>();

        int radius = 10;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = l.clone().add(x, y, z);

                    // Add to the list of particles to show if it's in the water.
                    if (loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.STATIONARY_WATER)
                        if (loc.add(0, 1, 0).getBlock().getType() == Material.AIR && !lfishingParticles.contains(loc))
                            lfishingParticles.add(loc);
                }
            }
        }

        Collections.reverse(lfishingParticles);
        FISHING_PARTICLES.put(l, lfishingParticles);
    }

    public static Location getFishingSpot(Location loc) {
        for (Location fishLoc : FISHING_LOCATIONS.keySet())
            if (loc.distanceSquared(fishLoc) <= 100)
                return fishLoc;
        return null;
    }

    public static Integer getFishingSpotTier(Location loc) {
        for (Location fishLoc : FISHING_LOCATIONS.keySet())
            if (loc.distanceSquared(fishLoc) <= 100)
                return FISHING_LOCATIONS.get(fishLoc);
        return -1;
    }

    public static int getExactTier(Block bk) {
        return FISHING_LOCATIONS.containsKey(bk.getLocation()) ? FISHING_LOCATIONS.get(bk.getLocation()) : -1;
    }

    private void loadFishingLocations() {
        int count = 0;
        ArrayList<String> CONFIG = (ArrayList<String>) DungeonRealms.getInstance().getConfig()
                .getStringList("fishingspawns");
        for (String line : CONFIG) {
            if (line.contains("=")) {
                String[] cords = line.split("=")[0].split(",");
                Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
                        Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));

                int tier = Integer.parseInt(line.split("=")[1]);
                FISHING_LOCATIONS.put(loc, tier);
                count++;
            }
        }
        Utils.log.info("[Professions] " + count + " FISHING SPOT locations have been LOADED.");
    }

    private Map<UUID, FishTracker> fishCaughtLog = new HashMap<>();

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e) {
        final Player pl = e.getPlayer();
        if (!GameAPI.isMainWorld(pl.getWorld())) {
            e.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
            e.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
            e.setCancelled(true);
            return;
        }

        e.setExpToDrop(0);

        ItemStack held = pl.getEquipment().getItemInMainHand();
        if (!ItemFishingPole.isFishingPole(held)) {
            e.setCancelled(true);
            return;
        }
        ItemFishingPole pole = (ItemFishingPole) PersistentItem.constructItem(held);

        if (e.getState().equals(State.FISHING)) {
            Location loc = getFishingSpot(e.getPlayer().getLocation());
            if (loc == null) {
                e.getPlayer().sendMessage(ChatColor.RED + "There are " + ChatColor.UNDERLINE + "no" + ChatColor.RED + " populated fishing spots near this location.");
                e.getPlayer().sendMessage(ChatColor.GRAY + "Look for particles above water blocks to signify active fishing spots.");
                e.setCancelled(true);
                return;
            }


            int areaTier = getFishingSpotTier(loc);
            if (areaTier > pole.getTier().getId()) {
                e.getPlayer().sendMessage(ChatColor.RED + "This area is a Tier " + areaTier + " fishing zone.");
                e.getPlayer().sendMessage(ChatColor.RED + "Your current pole is too weak to catch any fish here.");
                e.setCancelled(true);
                return;
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                EntityFishingHook fishHook = (EntityFishingHook) ((CraftEntity) e.getHook()).getHandle();
                ReflectionAPI.setField("ay", fishHook, MathHelper.a(ThreadLocalRandom.current(), 0.0F, 360.0F));
                ReflectionAPI.setField("ax", fishHook, MathHelper.nextInt(ThreadLocalRandom.current(), 20, 80));
            }, 10);
        }

        if (e.getState() == State.CAUGHT_FISH) {
            EntityFishingHook fishHook = (EntityFishingHook) ((CraftEntity) e.getHook()).getHandle();
            int ticks = (int) ReflectionAPI.getObjectFromField("av", fishHook);

            int maxFishTicks = e.getHook().hasMetadata("maxTicks") ? e.getHook().getMetadata("maxTicks").get(0).asInt() : -1;

            if (maxFishTicks == -1) {
                maxFishTicks = ticks;
                Bukkit.getLogger().info("No ticks able to be set, max was: " + maxFishTicks);
            }
            FishTracker tracker = fishCaughtLog.computeIfAbsent(pl.getUniqueId(), m -> new FishTracker());
            int caughtSpeed = maxFishTicks - ticks;
            tracker.trackFishCatch(pl, caughtSpeed);

            Random random = ThreadLocalRandom.current();
            final Location fishLoc = getFishingSpot(pl.getLocation());
            final int spotTier = getFishingSpotTier(pl.getLocation());
            if (e.getCaught() != null)
                e.getCaught().remove();

            if (fishLoc == null || spotTier == -1) {
                pl.sendMessage(ChatColor.RED + "You must be near a Fishing Location to catch fish!");
                return;
            }

            int duraBuff = pole.getAttributes().getAttribute(FishingAttributeType.DURABILITY).getValue();

            pl.sendMessage(ChatColor.GRAY + "You examine your catch... ");
            Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
                int fishRoll = ThreadLocalRandom.current().nextInt(100);
                int successRate = pole.getTier().getId() > spotTier ? 100 : 0;

                if (pole.getTier().getId() == spotTier)
                    successRate = 50 + 2 * (20 - Math.abs(pole.getNextTierLevel() - pole.getLevel()));

                successRate += pole.getAttributes().getAttribute(FishingAttributeType.CATCH_SUCCESS).getValue();

                if (TutorialIsland.onTutorialIsland(pl.getLocation())) {
                    if (Quests.getInstance().hasCurrentQuestObjective(pl, "Tutorial Island", ObjectiveCatchFish.class)) {
                        successRate = 100;
                        Bukkit.getLogger().info("Catching fish for sure for " + pl.getName() + " due to Tutorial Island");
                    }
                }

                if (successRate <= fishRoll) {
                    pl.sendMessage(ChatColor.RED + "It got away..");
                    if (ThreadLocalRandom.current().nextInt(100) > duraBuff)
                        pole.damageItem(pl, 1);
                    return;
                }

                FishingTier fTier = FishingTier.getTierByLevel(pole.getLevel());
                ItemStack fish = new ItemFish(fTier, EnumFish.getRandomFish(fTier.getTier())).generateItem();
                int fishDrop = 1;

                if (ThreadLocalRandom.current().nextInt(100) > duraBuff)
                    pole.damageItem(pl, 2);

                pl.sendMessage(ChatColor.GREEN + "... you caught some " + fish.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");

                int exp = fTier.getXP();
                pole.addExperience(pl, exp);


                PlayerWrapper pw = PlayerWrapper.getWrapper(pl);
                pw.addExperience(exp / 8, false, true);
                pw.getPlayerGameStats().addStat(StatColumn.FISH_CAUGHT);

                if (pole.getAttributes().getAttribute(FishingAttributeType.DOUBLE_CATCH).getValue() >= random.nextInt(100) + 1) {
                    fishDrop *= 2;
                    pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE FISH CATCH" + ChatColor.YELLOW + " (2x)");
                }

                if (pole.getAttributes().getAttribute(FishingAttributeType.TRIPLE_CATCH).getValue() >= random.nextInt(100) + 1) {
                    fishDrop *= 3;
                    pw.sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE FISH CATCH" + ChatColor.YELLOW + " (3x)");
                }

                pl.getEquipment().setItemInMainHand(pole.generateItem());
                fish.setAmount(fishDrop);
                GameAPI.giveOrDropItem(pl, fish);

                Quests.getInstance().triggerObjective(pl, ObjectiveCatchFish.class);

                //  Junk Find.
                if (pole.getAttributes().getAttribute(FishingAttributeType.JUNK_FIND).getValue() >= ThreadLocalRandom.current().nextInt(100) + 1) {
                    int junkType = ThreadLocalRandom.current().nextInt(100) + 1; // 0, 1, 2
                    ItemStack junk = null;

                    if (junkType < 70) {
                        junk = new PotionItem(PotionTier.getById(spotTier)).generateItem();
                        junk.setAmount(Math.max(1, 6 - spotTier) + random.nextInt(3));
                    } else if (junkType < 95) {
                        junk = new ItemScrap(ScrapTier.getScrapTier(spotTier)).generateItem();
                        junk.setAmount(Math.max(2, 25 - (spotTier * 5)) + random.nextInt(7));
                    } else {
                        int tierRoll = random.nextInt(100);
                        int junkTier = tierRoll >= 95 ? 5 : (tierRoll <= 70 ? 3 : spotTier);
                        junkTier = Math.max(junkTier, spotTier);
                        junk = ItemManager.createRandomCombatItem().setRarity(ItemRarity.COMMON)
                                .setTier(ItemTier.getByTier(junkTier)).generateItem();
                    }

                    if (junk != null) {
                        int itemCount = junk.getAmount();
                        if (junk.getType() == Material.POTION) {
                            int amount = junk.getAmount();
                            junk.setAmount(1);
                            while (amount > 0) {
                                amount--;
                                GameAPI.giveOrDropItem(pl, junk);
                            }
                        } else {
                            GameAPI.giveOrDropItem(pl, junk);
                        }

                        pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME JUNK! -- " + itemCount + "x "
                                + junk.getItemMeta().getDisplayName());
                    }
                }

                // Treasure Find.
                if (pole.getAttributes().getAttribute(FishingAttributeType.TREASURE_FIND).getValue() >= ThreadLocalRandom.current().nextInt(300) + 1) {
                    // Give em treasure!
                    int treasureType = ThreadLocalRandom.current().nextInt(3); // 0, 1
                    ItemStack treasure = null;
                    if (treasureType == 0) {
                        treasure = new ItemOrb().generateItem();
                    } else if (treasureType == 1) {
                        int tierRoll = random.nextInt(100);
                        int treasureTier = tierRoll >= 95 ? 5 : (tierRoll <= 70 ? 3 : spotTier);
                        treasureTier = Math.max(treasureTier, spotTier);
                        ItemRarity rarity = random.nextInt(100) <= 75 ? ItemRarity.UNCOMMON : ItemRarity.RARE;
                        treasure = ItemManager.createRandomCombatItem().setTier(ItemTier.getByTier(treasureTier))
                                .setRarity(rarity).generateItem();
                    } else if (treasureType == 2) {
                        treasure = new ItemFlightOrb().generateItem();
                    }

                    if (treasure != null) {
                        GameAPI.giveOrDropItem(pl, treasure);
                        pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "  YOU FOUND SOME TREASURE! -- a(n) "
                                + treasure.getItemMeta().getDisplayName());
                    }
                }
            }, 10);
        } else if (e.getState() == State.BITE) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                EntityFishingHook fishHook = (EntityFishingHook) ((CraftEntity) e.getHook()).getHandle();
                //The bobber is down, get the timer?
                int ticks = (int) ReflectionAPI.getObjectFromField("av", fishHook);
                Bukkit.getLogger().info("Max Fish ticks: " + ticks);
                fishHook.getBukkitEntity().setMetadata("maxTicks", new FixedMetadataValue(DungeonRealms.getInstance(), ticks));
            });
        }
    }

    public static Map<Location, Integer> getLocations() {
        return FISHING_LOCATIONS;
    }
}