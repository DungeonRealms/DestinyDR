package net.dungeonrealms.game.profession;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemFishingPole;
import net.dungeonrealms.game.item.items.functional.*;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.mechanic.data.PotionTier;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.profession.fishing.*;
import net.dungeonrealms.game.world.item.Item.FishingAttributeType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;

/**
 * Fishing Profession - Contains all the code for the fishing mechanic.
 * 
 * Redone by Kneesnap on April 8th, 2017.
 */
public class Fishing implements GenericMechanic, Listener {

	private Random random = new Random();
	
	public Fishing() {
		Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
	}

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
        ItemFishingPole pole = (ItemFishingPole)PersistentItem.constructItem(held);

        if (e.getState().equals(State.FISHING)) {
            Location loc = Fishing.getInstance().getFishingSpot(e.getPlayer().getLocation());
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
        }

        if (e.getState() == State.CAUGHT_FISH) {
            Random random = new Random();
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
                int fishRoll = new Random().nextInt(100);
                int successRate = pole.getTier().getId() > spotTier ? 100 : 0;

                if (pole.getTier().getId() == spotTier)
                	successRate = 50 + (2 * (20 - Math.abs(pole.getNextTierLevel() - pole.getLevel())));

                successRate += pole.getAttributes().getAttribute(FishingAttributeType.CATCH_SUCCESS).getValue();

                if (successRate <= fishRoll) {
                    pl.sendMessage(ChatColor.RED + "It got away..");
                    if (new Random().nextInt(100) > duraBuff)
                    	pole.damageItem(pl, 1);
                    return;
                }
                
                FishingTier fTier = FishingTier.getTierByLevel(pole.getLevel());
                ItemStack fish = new ItemFish(fTier, EnumFish.getRandomFish(fTier.getTier())).generateItem();
                int fishDrop = 1;
                
                if (new Random().nextInt(100) > duraBuff)
                	pole.damageItem(pl, 2);
                
                pl.sendMessage(ChatColor.GREEN + "... you caught some " + fish.getItemMeta().getDisplayName() + ChatColor.GREEN + "!");
                
                int exp = fTier.getXP();
                pole.addExperience(pl, exp);
                
                GamePlayer gp = GameAPI.getGamePlayer(pl);
                gp.addExperience(exp / 8, false, true);
                gp.getPlayerStatistics().setFishCaught(gp.getPlayerStatistics().getFishCaught() + 1);
                boolean toggleDebug = (boolean) DatabaseAPI.getInstance().getData(PlayerManager.PlayerToggles.DEBUG.getDbField(), pl.getUniqueId());
                
                if (pole.getAttributes().getAttribute(FishingAttributeType.DOUBLE_CATCH).getValue() >= random.nextInt(100) + 1) {
                	fishDrop *= 2;
                	if (toggleDebug)
                		pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE FISH CATCH" + ChatColor.YELLOW + " (2x)");
                }
                
                if (pole.getAttributes().getAttribute(FishingAttributeType.TRIPLE_CATCH).getValue() >= random.nextInt(100) + 1) {
                	fishDrop *= 3;
                	if (toggleDebug)
                		pl.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE FISH CATCH" + ChatColor.YELLOW + " (3x)");
                }
                
                pl.getEquipment().setItemInMainHand(pole.generateItem());
                fish.setAmount(fishDrop);
                GameAPI.giveOrDropItem(pl, fish);
                
                //  Junk Find.
                if (pole.getAttributes().getAttribute(FishingAttributeType.JUNK_FIND).getValue() >= new Random().nextInt(100) + 1) {
                	int junkType = new Random().nextInt(100) + 1; // 0, 1, 2
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
                if (pole.getAttributes().getAttribute(FishingAttributeType.TREASURE_FIND).getValue() >= new Random().nextInt(300) + 1) {
                	// Give em treasure!
                	int treasureType = new Random().nextInt(3); // 0, 1
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
        }
    }
	
	public static FishBuff loadBuff(NBTTagCompound tag) {
		
		FishBuffType fbt = FishBuffType.valueOf(tag.getString("buffType"));
		try {
			Class<? extends FishBuff> buffCls = fbt.getBuffClass();
			FishingTier tier = FishingTier.values()[tag.getInt("itemTier") - 1];
			return buffCls.getConstructor(tag.getClass(), tier.getClass()).newInstance(tag, tier);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to construct " + fbt.name());
		}
		return null;
	}
	
	public static FishBuff getRandomBuff(FishingTier tier) {
		int roll = new Random().nextInt(100);
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
    public enum EnumFish {
		
		//  TIER 1  //
        Shrimp(1, "A raw and pink crustacean"),
        Anchovie(1, "A small blue, oily fish"),
        Crayfish(1, "A lobster-like and brown crustacean"),
        
        //  TIER 2  //
        Carp(2, "A Large, silver-scaled fish"),
        Herring(2, "A colourful and medium-sized fish"),
        Sardine(2, "A small and oily green fish"),
        
        //  TIER 3  //
        Salmon(3, "A beautiful jumping fish"),
        Trout(3, "A non-migrating Salmon"),
        Cod(3, "A cold-water, deep sea fish"),
        
        //  TIER 4  //
        Lobster(4, "A Large, red crustacean"),
        Tuna(4, "A large, sapphire blue fish"),
        Bass(4, "A very large and white fish"),
        
        //  TIER 5  //
        Shark(5, "A terrifying and massive predator"),
        Swordfish(5, "An elongated fish with a long bill"),
        Monkfish(5, "A flat, large, and scary-looking fish");


        @Getter private int tier;
        @Getter private String desciption;
        
        public String getName() {
        	return name();
        }

        public static EnumFish getRandomFish(int tier) {
            List<EnumFish> fishList = new ArrayList<>();
            for (EnumFish fish : values())
            	if (fish.getTier() == tier)
            		fishList.add(fish);
            return fishList.get(new Random().nextInt(fishList.size() - 1));
        }
    }
	
	@AllArgsConstructor
	public enum FishBuffType {
		DAMAGE(FishDamageBuff.class, "+", "% DMG", "", "Power", 0),
		HEALTH(FishHealBuff.class, "+", "% HP", "", "Healing", 0),
		REGEN(FishRegenBuff.class, "+", "% HP", "Healing", "Regeneration", 0),
		SPEED(FishSpeedBuff.class, "SPEED BUFF", "", "", "Agility", 1),
		HUNGER(FishHungerBuff.class, "-", "% HUNGER", "", "Satiety", 0),
		ARMOR(FishArmorBuff.class, "+", "% ARMOR", "", "Defense", 0),
		VISION(FishVisionBuff.class, "NIGHTVISION BUFF", "", "", "", 0),
		BLOCK(FishBlockBuff.class, "+", "% BLOCK", "", "Blocking", 0);
		
		@Getter private Class<? extends FishBuff> buffClass;
		@Getter private String buffPrefix;
		@Getter private String buffSuffix;
		@Getter private String prefix;
		@Getter private String baseSuffix;
		@Getter private int fishMeta;
		
		
		public static FishBuffType getByName(String str) {
			for (FishBuffType t : values())
				if (t.name().equals(str))
					return t;
			return null;
		}
	}

    public HashMap<Location, Integer> FISHING_LOCATIONS = new HashMap<>();
    public HashMap<Location, List<Location>> FISHING_PARTICLES = new HashMap<>();

    public void generateFishingParticleBlockList() {

    }

    public Location getFishingSpot(Location loc) {
        for (Location fish_loc : FISHING_LOCATIONS.keySet()) {
            double dist_sqr = loc.distanceSquared(fish_loc);
            if (dist_sqr <= 100)
                return fish_loc;
        }
        return null;
    }

    public Integer getFishingSpotTier(Location loc) {
        for (Location fish_loc : FISHING_LOCATIONS.keySet()) {
            double dist_sqr = loc.distanceSquared(fish_loc);
            if (dist_sqr <= 100) {
                return FISHING_LOCATIONS.get(fish_loc);
            }
        }
        return -1;
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

    @Getter
    private static Fishing instance = new Fishing();

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
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH,
                                epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.4F, 20), 0L);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    data.getValue().stream().filter(loc -> random.nextInt(chance) == 1).forEach(loc -> {
                        try {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPLASH,
                                    epicenter, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.4F, 20), 0L);
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
