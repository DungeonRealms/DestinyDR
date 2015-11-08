package net.dungeonrealms.mechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.items.armor.ArmorGenerator;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.miscellaneous.Glyph;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.dungeonrealms.spawning.LootSpawner;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootManager implements GenericMechanic{

    public static List<LootSpawner> LOOT_SPAWNERS = new ArrayList<>();
    public static List<String> SPAWNER_CONFIG = new ArrayList<>();
//    public static List<ItemStack> tier1Loot = new ArrayList<>();
//    public static List<ItemStack> tier2Loot = new ArrayList<>();
//    public static List<ItemStack> tier3Loot = new ArrayList<>();
//    public static List<ItemStack> tier4Loot = new ArrayList<>();
//    public static List<ItemStack> tier5Loot = new ArrayList<>();

    /**
     * Weapons/Armor 1%
     * Glyphs .2%
     * 
     * 
     * 
     * Health Potion %75
     * Food 75%
     * TP Books 10%
     * 
     */
//    
//    /**
//     * Manually load of all items to an ArrayList.
//     */
//    private static void loadLootItems() {
//        ItemStack weapon = null;
//
//        // Tier 1 Loot
//        for (int i = 0; i < 2; i++) {
//            weapon = new ItemGenerator().next(ItemTier.TIER_1);
//            tier1Loot.add(weapon);
//        }
//        for(int i = 0;i < 5; i++){
//        	ItemStack t1Potion = ItemManager.createHealthPotion(1, false, new Random().nextBoolean());
//        	tier1Loot.add(t1Potion);
//        }
//        // Tier 2 Loot
//        for (int i = 0; i < 2; i++) {
//            weapon = new ItemGenerator().next(ItemTier.TIER_2);
//            tier2Loot.add(weapon);
//        }
//        for(int i = 0;i < 5; i++){
//        	ItemStack t2Potion = ItemManager.createHealthPotion(2, false, new Random().nextBoolean());
//        	tier2Loot.add(t2Potion);
//        }
//        // Tier 3 Loot
//        for (int i = 0; i < 2; i++) {
//            weapon = new ItemGenerator().next(ItemTier.TIER_3);
//            tier3Loot.add(weapon);
//        }
//        for(int i = 0;i < 5; i++){
//        	ItemStack t3Potion = ItemManager.createHealthPotion(3, false, new Random().nextBoolean());
//        	tier3Loot.add(t3Potion);
//        }
//        // Tier 4 Loot
//        for (int i = 0; i < 2; i++) {
//            weapon = new ItemGenerator().next(ItemTier.TIER_4);
//            tier4Loot.add(weapon);
//        }
//        for(int i = 0;i < 5; i++){
//        	ItemStack t4Potion = ItemManager.createHealthPotion(4, false, new Random().nextBoolean());
//        	tier4Loot.add(t4Potion);
//        }
//        // Tier 5 Loot
//        for (int i = 0; i < 2; i++) {
//            weapon = new ItemGenerator().next(ItemTier.TIER_5);
//            tier5Loot.add(weapon);
//        }
//        for(int i = 0;i < 5; i++){
//        	ItemStack t5Potion = ItemManager.createHealthPotion(5, false, new Random().nextBoolean());
//        	tier5Loot.add(t5Potion);
//        }
//    }

    /**
     * returns an array of Items that are dedicated to the tier.
     *
     * @param tier
     * @return
     */
    public static ArrayList<ItemStack> getLoot(int tier) {
        ArrayList<ItemStack> loot = new ArrayList<>();
        
        switch(tier){
        case 1:
        	ItemStack t1Potion = ItemManager.createHealthPotion(1, false, new Random().nextBoolean());
        		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
        			loot.add(t1Potion);
        		}
        		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
        			loot.add(new ItemStack(Material.APPLE));
        		}
        		if(RandomHelper.getRandomNumberBetween(1, 100) <= 10){
        			loot.add(ItemManager.createRandomTeleportBook("Teleport Book"));
        		}
        		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
        			loot.add(new ItemGenerator().next(ItemTier.TIER_1));
        		}
        		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
        			loot.add(new ArmorGenerator().nextTier(1)[RandomHelper.getRandomNumberBetween(0, 2)]);
        		}
        		
        		if(RandomHelper.getRandomNumberBetween(1, 1000) <= 2){
        			loot.add(Glyph.getInstance().nextWeaponGlyph("Lucky Glyph", 1));
        		}
        	break;
        case 2:
        	ItemStack t2Potion = ItemManager.createHealthPotion(2, false, new Random().nextBoolean());
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(t2Potion);
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(new ItemStack(Material.BREAD));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 10){
    			loot.add(ItemManager.createRandomTeleportBook("Teleport Book"));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ItemGenerator().next(ItemTier.TIER_2));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ArmorGenerator().nextTier(2)[RandomHelper.getRandomNumberBetween(0, 2)]);
    		}
    		
    		if(RandomHelper.getRandomNumberBetween(1, 1000) <= 2){
    			loot.add(Glyph.getInstance().nextWeaponGlyph("Lucky Glyph", 2));
    		}
    	break;
        case 3:
        	ItemStack t3Potion = ItemManager.createHealthPotion(3, false, new Random().nextBoolean());
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(t3Potion);
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(new ItemStack(Material.COOKED_CHICKEN));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 10){
    			loot.add(ItemManager.createRandomTeleportBook("Teleport Book"));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ItemGenerator().next(ItemTier.TIER_3));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ArmorGenerator().nextTier(3)[RandomHelper.getRandomNumberBetween(0, 2)]);
    		}
    		
    		if(RandomHelper.getRandomNumberBetween(1, 1000) <= 2){
    			loot.add(Glyph.getInstance().nextWeaponGlyph("Lucky Glyph", 3));
    		}
    	break;
        case 4:
        	ItemStack t4Potion = ItemManager.createHealthPotion(4, false, new Random().nextBoolean());
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(t4Potion);
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(new ItemStack(Material.COOKED_BEEF));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 10){
    			loot.add(ItemManager.createRandomTeleportBook("Teleport Book"));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ItemGenerator().next(ItemTier.TIER_4));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ArmorGenerator().nextTier(4)[RandomHelper.getRandomNumberBetween(0, 2)]);
    		}
    		
    		if(RandomHelper.getRandomNumberBetween(1, 1000) <= 2){
    			loot.add(Glyph.getInstance().nextWeaponGlyph("Lucky Glyph", 4));
    		}
    	break;
        case 5:
        	ItemStack t5Potion = ItemManager.createHealthPotion(5, false, new Random().nextBoolean());
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(t5Potion);
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 75){
    			loot.add(new ItemStack(Material.RABBIT_STEW));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 10){
    			loot.add(ItemManager.createRandomTeleportBook("Teleport Book"));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ItemGenerator().next(ItemTier.TIER_5));
    		}
    		if(RandomHelper.getRandomNumberBetween(1, 100) <= 5){
    			loot.add(new ArmorGenerator().nextTier(5)[RandomHelper.getRandomNumberBetween(0, 2)]);
    		}
    		
    		if(RandomHelper.getRandomNumberBetween(1, 1000) <= 2){
    			loot.add(Glyph.getInstance().nextWeaponGlyph("Lucky Glyph", 5));
    		}
    	break;
        }
        
        if(loot.isEmpty()){
        	loot.add(ItemManager.createHealthPotion(2, false, new Random().nextBoolean()));
        }
        return loot;
    }

    /**
     * Initialization of loot spawners from config.
     */
    public static void loadLootSpawners() {
//        loadLootItems();
        SPAWNER_CONFIG = DungeonRealms.getInstance().getConfig().getStringList("loot");
        for (String line : SPAWNER_CONFIG) {
//            int tier = Integer.parseInt(line.split(":")[1]);
        	int tier = 1;
        	int num = RandomHelper.getRandomNumberBetween(1, 100);
        	if(num <= 10){
        		tier = 5;
        	}else if(num <= 25 && num > 10){
        		tier = 4;
        	}else if(num <=40 && num > 25){
        		tier = 3;
        	}else if(num  <= 65 && num > 40){
        		tier = 2;
        	}else{
        		tier = 1;
        	}
        		
            double x, y, z;
            String[] location = line.split(":")[0].split(",");
            x = Double.parseDouble(location[0]);
            y = Double.parseDouble(location[1]);
            z = Double.parseDouble(location[2]);
            World world = Bukkit.getWorlds().get(0);
            Location loc = new Location(world, x, y, z);
            Block chest = world.getBlockAt(loc);
            chest.setType(Material.CHEST);
            LootSpawner lootSpawner = new LootSpawner(loc, tier, chest);
            LOOT_SPAWNERS.add(lootSpawner);
        }

    }

    public static boolean checkLocationForLootSpawner(Location location) {
        for (LootSpawner lootSpawner : LOOT_SPAWNERS) {
            if (lootSpawner.location.distanceSquared(location) <= 2) {
                return true;
            }
        }
        return false;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.ARCHBISHOPS;
    }

    @Override
    public void startInitialization() {
        loadLootSpawners();
    }

    @Override
    public void stopInvocation() {

    }
}
