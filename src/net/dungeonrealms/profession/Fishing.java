package net.dungeonrealms.profession;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.ParticleAPI.ParticleEffect;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by Chase on Oct 28, 2015
 */
public class Fishing implements GenericMechanic {

	public enum EnumFish {
		Bass("Bass", 1), Cod("Cod", 1), Trout("Trout", 2);

		// TODO All this shit

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

	public static int getMaxXP(int tier) {
		switch (tier) {
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

	/**
	 * Check if itemstack is a DR fishing pole.
	 * 
	 * @param stack
	 * @return boolean
	 * @since 1.0
	 */
	public static boolean isDRFishingPole(ItemStack stack) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		return nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("rod");
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
			size = new Random().nextInt(4) + new Random().nextInt(2);
			break;
		case 2:
			size = new Random().nextInt(8) + new Random().nextInt(4);
			break;
		case 3:
			if (new Random().nextBoolean())
				size = new Random().nextInt(12) + new Random().nextInt(6) + new Random().nextInt(3);
			else
				size = new Random().nextInt(12) + new Random().nextInt(6) - new Random().nextInt(3);
			break;
		case 4:
			if (new Random().nextBoolean())
				size = new Random().nextInt(16) + new Random().nextInt(8) + new Random().nextInt(6);
			else
				size = new Random().nextInt(16) + new Random().nextInt(8) - new Random().nextInt(6);
			break;
		case 5:
			if (new Random().nextBoolean())
				size = new Random().nextInt(20) + new Random().nextInt(10) + new Random().nextInt(9);
			else
				size = new Random().nextInt(20) + new Random().nextInt(10) - new Random().nextInt(9);
			break;
		}
		if (size <= 0)
			size = new Random().nextInt(5);
		return size;
	}

	/**
	 * 
	 * gets a random fish name
	 * 
	 * @param tier
	 * @return String
	 * @since 1.0
	 * 
	 */
	public static String getFish(int tier) {
		// TODO MAKE MORE FISH AND CHANCES FOR POLES
		String[] list = new String[] { "Herring", "Salmon", "Eel", "Whiting", "Turbot", "Plaice", "Cod", "Trout",
		        "Pike", "Skate", "Oysters", "Crab", "Cockles", "Mussels" };
		return list[new Random().nextInt(list.length - 1)];
	}

	/**
	 * Return new Fish caught by stack(fishing pole)
	 * 
	 * @param stack
	 * @return ItemStac
	 * @since 1.0
	 */
	public static ItemStack getFishItem(ItemStack stack) {
		ItemStack fish = new ItemStack(Material.RAW_FISH);
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		int tier = nms.getTag().getInt("itemTier");
		ItemMeta meta = fish.getItemMeta();
		Utils.log.info(tier + "tier");
		int size = Fishing.getSize(tier);
		String type = Fishing.getFish(tier);
		meta.setDisplayName(size + "in. " + type);
		fish.setItemMeta(meta);
		nms = CraftItemStack.asNMSCopy(fish);
		nms.getTag().setInt("size", size);
		nms.getTag().setString("type", type);
		return CraftItemStack.asBukkitCopy(nms);
	}

	/**
	 * Add Expereicen to the specified stack(fishing pole)
	 * 
	 * @param stack
	 */
	public static void gainExp(ItemStack stack, Player p) {
		net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		int xp = nms.getTag().getInt("XP");
		int maxXP = nms.getTag().getInt("maxXP");
		xp += 25;
		nms.getTag().setInt("XP", xp);

		double percentDone = 100.0 * xp / maxXP;
		double percentDoneDisplay = (percentDone / 100) * 50.0D;
		int display = (int) percentDoneDisplay;
		if (display <= 0) {
			display = 1;
		}
		if (display > 50) {
			display = 50;
		}
		String expBar = "||||||||||||||||||||" + "||||||||||||||||||||" + "||||||||||";
		String newexpBar = ChatColor.GREEN.toString() + expBar.substring(0, display) + ChatColor.RED.toString()
		        + expBar.substring(display, expBar.length());
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(Collections.singletonList(newexpBar));
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
                        if (loc.getBlock().getType() == Material.WATER || loc.getBlock().getType() == Material.STATIONARY_WATER) {
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
				// Within 10 blocks.
				closest_loc = fish_loc;
			}
		}

		if (closest_loc == null) {
			return null; // No spot within 50 blocks.
		}

		return closest_loc;
	}

	public void loadFishingLocations() {
		int count = 0;
		try {
			File file = new File(DungeonRealms.getInstance().getDataFolder() + "//fishing_spawns.dat");
			if (!(file.exists())) {
				file.createNewFile();
			}
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("=")) {
					String[] cords = line.split("=")[0].split(",");
					Location loc = new Location(Bukkit.getWorlds().get(0), Double.parseDouble(cords[0]),
					        Double.parseDouble(cords[1]), Double.parseDouble(cords[2]));

					int tier = Integer.parseInt(line.split("=")[1]);
					FISHING_LOCATIONS.put(loc, tier);
					count++;
				}
			}
			reader.close();
		} catch (Exception exc) {
			exc.printStackTrace();
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
             Random r = new Random();
             if (FISHING_PARTICLES.size() <= 0) {
                 return; // Do nothing.
             }
             try {
                 for (Entry<Location, List<Location>> data : FISHING_PARTICLES.entrySet()) {
                     Location epicenter = data.getKey();
                     try {
                         ParticleAPI.sendParticleToLocation(ParticleEffect.SPLASH, epicenter, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0.4F, 20);
                     } catch (Exception e1) {
                         e1.printStackTrace();
                     }
                     data.getValue().stream().filter(loc -> r.nextInt(chance) == 1).forEach(loc -> {
                         try {
                             ParticleAPI.sendParticleToLocation(ParticleEffect.SPLASH, loc, r.nextFloat(), r.nextFloat(), r.nextFloat(), 0.4F, 20);
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
