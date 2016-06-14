package net.dungeonrealms.game.profession;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.mechanics.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Chase on Oct 28, 2015
 */
public class Fishing implements GenericMechanic {

    public enum EnumFish {
        Bass("Bass", 1), Cod("Cod", 1), Trout("Trout", 2);

        int regenLvl;
        String fishName;

        EnumFish(String fishName, int regenlevel) {
            this.fishName = fishName;
            this.regenLvl = regenlevel;
        }
    }

    public static int T1Exp = 2500;
    public static int T2Exp = 5000;
    public static int T3Exp = 7500;
    public static int T4Exp = 9000;
    public static int T5Exp = 10000;
    private static Random random = new Random();

    public static int getMaxXP(int tier) {
        switch (tier) {
            case 0:
            case 1:
                return T1Exp;
            case 2:
                return T2Exp;
            case 3:
                return T3Exp;
            case 4:
                return T4Exp;
            case 5:
                return T5Exp;
            default:
                return -1;
        }
    }

    public static int getFishEXP(int tier) {
        if (tier == 1) {
            return (int) (2.5D * (250 + random.nextInt((int) (250 * 0.3D))));
        }
        if (tier == 2) {
            return (int) (2.5D * (430 + random.nextInt((int) (430 * 0.3D))));
        }
        if (tier == 3) {
            return (int) (2.5D * (820 + random.nextInt((int) (820 * 0.3D))));
        }
        if (tier == 4) {
            return (int) (2.5D * (1050 + random.nextInt((int) (1050 * 0.3D))));
        }
        if (tier == 5) {
            return (int) (2.5D * (1230 + random.nextInt((int) (1230 * 0.3D))));
        }
        return 1;
    }

    /**
     * Check if itemstack is a DR fishing pole.
     *
     * @param stack
     * @return boolean
     * @since 1.0
     */
    public static boolean isDRFishingPole(ItemStack stack) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        return nms.hasTag() && nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("rod");
    }

    /**
     * return chance of capture out of 100
     *
     * @param tier
     * @return integer
     * @since 1.0
     */
    public static int getChance(int tier) {
        switch (tier) {
            case 1:
                return 25;
            case 2:
                return 30;
            case 3:
                return 45;
            case 4:
                return 65;
            case 5:
                return 85;
        }
        return 0;
    }

    /**
     * return size of fish based on tier
     *
     * @return integer
     * @since 1.0
     */
    public static int getSize(int tier) {
        int size = 0;
        switch (tier) {
            case 1:
                size = random.nextInt(4) + random.nextInt(2);
                break;
            case 2:
                size = random.nextInt(8) + random.nextInt(4);
                break;
            case 3:
                if (random.nextBoolean())
                    size = random.nextInt(12) + random.nextInt(6) + random.nextInt(3);
                else
                    size = random.nextInt(12) + random.nextInt(6) - random.nextInt(3);
                break;
            case 4:
                if (random.nextBoolean())
                    size = random.nextInt(16) + random.nextInt(8) + random.nextInt(6);
                else
                    size = random.nextInt(16) + random.nextInt(8) - random.nextInt(6);
                break;
            case 5:
                if (random.nextBoolean())
                    size = random.nextInt(20) + random.nextInt(10) + random.nextInt(9);
                else
                    size = random.nextInt(20) + random.nextInt(10) - random.nextInt(9);
                break;
        }
        if (size <= 0)
            size = random.nextInt(5) + 1;
        return size;
    }

    /**
     * gets a random fish name
     *
     * @param tier
     * @return String
     * @since 1.0
     */
    public static String getFish(int tier) {
        // TODO MAKE MORE FISH AND CHANCES FOR POLES
        String[] list = new String[]{"Herring", "Salmon", "Eel", "Whiting", "Turbot", "Plaice", "Cod", "Trout",
                "Pike", "Skate", "Oysters", "Crab", "Cockles", "Mussels"};
        return list[new Random().nextInt(list.length - 1)];
    }

    public static HashMap<UUID, String> fishBuffs = new HashMap<>();

    /**
     * Return new Fish caught by stack(fishing pole)
     *
     * @param stack
     * @return ItemStac
     * @since 1.0
     */
    public static ItemStack getFishItem(ItemStack stack) {
        ItemStack fish = new ItemStack(Material.RAW_FISH);
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        int rodTier = nms.getTag().getInt("itemTier");
        ItemMeta meta = fish.getItemMeta();
        int tier = getFishTier(rodTier);
        int size = Fishing.getSize(tier);
        String type = Fishing.getFish(tier);
        meta.setDisplayName(ChatColor.GRAY.toString() + size + "in. " + API.getTierColor(tier) + type);
        String buff = "none";
        if (RandomHelper.getRandomNumberBetween(1, 100) <= 30) {
            String[] list = new String[]{"HP Regen", "Energy Regen"};
            buff = list[RandomHelper.getRandomNumberBetween(0, list.length - 1)];
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "+ " + ChatColor.RED + buff);
            meta.setLore(lore);
        }
        fish.setItemMeta(meta);
        nms = CraftItemStack.asNMSCopy(fish);
        nms.getTag().setInt("size", size);
        nms.getTag().setString("fishType", type);
        nms.getTag().setString("type", "fishBuff");
        if (!buff.equalsIgnoreCase("none"))
            nms.getTag().setString("buff", buff);
        return CraftItemStack.asBukkitCopy(nms);
    }

    /**
     * @param rodTier
     * @return
     */
    private static int getFishTier(int rodTier) {
        int chance = new Random().nextInt(100);
        switch (rodTier) {
            case 1:
                if (chance <= 85) {
                    return 1;
                } else if (chance > 85 && chance <= 90) {
                    return 2;
                } else if (chance > 90 && chance <= 95) {
                    return 3;
                } else if (chance > 95 && chance <= 99) {
                    return 4;
                } else {
                    return 5;
                }
            case 2:
                if (chance <= 80) {
                    return 1;
                } else if (chance > 80 && chance <= 85) {
                    return 2;
                } else if (chance > 85 && chance <= 95) {
                    return 3;
                } else if (chance > 95 && chance <= 99) {
                    return 4;
                } else {
                    return 5;
                }
            case 3:
                if (chance <= 70) {
                    return 1;
                } else if (chance > 70 && chance <= 80) {
                    return 2;
                } else if (chance > 80 && chance <= 90) {
                    return 3;
                } else if (chance > 90 && chance <= 95) {
                    return 4;
                } else {
                    return 5;
                }
            case 4:
                if (chance <= 60) {
                    return 1;
                } else if (chance > 60 && chance <= 70) {
                    return 2;
                } else if (chance > 70 && chance <= 80) {
                    return 3;
                } else if (chance > 80 && chance <= 95) {
                    return 4;
                } else {
                    return 5;
                }
            case 5:
                if (chance <= 50) {
                    return 1;
                } else if (chance > 50 && chance <= 65) {
                    return 2;
                } else if (chance > 65 && chance <= 75) {
                    return 3;
                } else if (chance > 75 && chance <= 85) {
                    return 4;
                } else {
                    return 5;
                }
        }
        return 100;
    }

    /**
     * Add Experience to the specified stack(fishing pole)
     *
     * @param stack
     */
    public static void gainExp(ItemStack stack, Player p, int exp) {
        net.minecraft.server.v1_9_R2.ItemStack nms = CraftItemStack.asNMSCopy(stack);
        int tier = Fishing.getRodTier(stack);
        int xp = nms.getTag().getInt("XP");
        int maxXP = nms.getTag().getInt("maxXP");
        xp += exp;
        nms.getTag().setInt("XP", xp);
        ItemMeta meta = stack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
        double percentDone = 100.0 * xp / maxXP;
        double percentDoneDisplay = (percentDone / 100) * 50.0D;
        int display = (int) percentDoneDisplay;
        if (display <= 0) {
            display = 1;
        }
        if (display > 50) {
            display = 50;
        }
        expBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
                + expBar.substring(display, expBar.length());
        lore.add(ChatColor.GRAY.toString() + "Tier: " + ChatColor.WHITE.toString() + tier);
        lore.add(ChatColor.WHITE.toString() + xp + ChatColor.GRAY + "/" + ChatColor.GRAY + maxXP);
        lore.add(ChatColor.GRAY.toString() + "EXP: " + expBar);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        p.setItemInHand(stack);
    }

    /**
     * Get the tier of said Rod.
     *
     * @param rodStack
     * @return Integer
     * @since 1.0
     */
    public static int getRodTier(ItemStack rodStack) {
        return CraftItemStack.asNMSCopy(rodStack).getTag().getInt("itemTier");
    }

    private HashMap<Location, Integer> FISHING_LOCATIONS = new HashMap<>();
    public HashMap<Location, List<Location>> FISHING_PARTICLES = new HashMap<>();

    public void generateFishingParticleBlockList() {
        int count = 0;

        for (Entry<Location, Integer> data : FISHING_LOCATIONS.entrySet()) {
            Location epicenter = data.getKey();
            List<Location> lfishingParticles = new ArrayList<>();
            int radius = 10;
            for (int x = -(radius); x <= radius; x++) {
                for (int y = -(radius); y <= radius; y++) {
                    for (int z = -(radius); z <= radius; z++) {
                        Location loc = epicenter.getBlock().getRelative(x, y, z).getLocation();
                        if (loc.getBlock().getType() == Material.WATER
                                || loc.getBlock().getType() == Material.STATIONARY_WATER) {
                            if (loc.add(0, 1, 0).getBlock().getType() == Material.AIR) {
                                if (!(lfishingParticles.contains(loc))) {
                                    lfishingParticles.add(loc);
                                    count++;
                                }
                            }
                        }
                    }
                }
            }

            FISHING_PARTICLES.put(epicenter, lfishingParticles);
        }

        Utils.log.info("[Professions] Loaded a total of " + count + " possible FISHING PARTICLE locations.");
    }

    public Location getFishingSpot(Location loc) {
        Location closest_loc = null;
        for (Location fish_loc : FISHING_LOCATIONS.keySet()) {
            double dist_sqr = loc.distanceSquared(fish_loc);
            if (dist_sqr <= 100) {
                closest_loc = fish_loc;
            }
        }

        if (closest_loc == null) {
            return null;
        }

        return closest_loc;
    }

    public void loadFishingLocations() {
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

    private static Fishing instance;

    public static Fishing getInstance() {
        if (instance == null)
            instance = new Fishing();
        return instance;

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    public static int splashCounter = 10;

    @Override
    public void startInitialization() {
        loadFishingLocations();
        generateFishingParticleBlockList();
        DungeonRealms.getInstance().getServer().getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
            int chance = splashCounter * splashCounter;
            if (splashCounter == 1) {
                splashCounter = 21;
            }
            splashCounter--;
            if (FISHING_PARTICLES.size() <= 0) {
                return;
            }
            try {
                for (Entry<Location, List<Location>> data : FISHING_PARTICLES.entrySet()) {
                    Location epicenter = data.getKey();
                    try {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleEffect.SPLASH,
                                epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.4F, 20), 0L);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    data.getValue().stream().filter(loc -> random.nextInt(chance) == 1).forEach(loc -> {
                        try {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleEffect.SPLASH,
                                    epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.4F, 20), 0L);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    });
                }
            } catch (ConcurrentModificationException cme) {
                Utils.log.info("[Professions] [ASYNC] Something went wrong checking a fishing spot and adding particles!");
            }
        }, 200L, 15L);
    }

    @Override
    public void stopInvocation() {

    }

}
