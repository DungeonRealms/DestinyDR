package net.dungeonrealms.mechanics;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.items.ItemGenerator;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.miscellaneous.RandomHelper;
import net.dungeonrealms.spawning.LootSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Chase on Oct 9, 2015
 */
public class LootManager implements GenericMechanic{

    public static List<LootSpawner> LOOT_SPAWNERS = new ArrayList<>();
    public static List<String> SPAWNER_CONFIG = new ArrayList<>();
    public static List<ItemStack> tier1Loot = new ArrayList<>();
    public static List<ItemStack> tier2Loot = new ArrayList<>();
    public static List<ItemStack> tier3Loot = new ArrayList<>();
    public static List<ItemStack> tier4Loot = new ArrayList<>();
    public static List<ItemStack> tier5Loot = new ArrayList<>();

    /**
     * Manually load of all items to an ArrayList.
     */
    private static void loadLootItems() {
        ItemStack moneyT1 = BankMechanics.gem.clone();
        ItemStack moneyT2 = BankMechanics.gem.clone();
        ItemStack moneyT3 = BankMechanics.gem.clone();
        ItemStack moneyT4 = BankMechanics.gem.clone();
        ItemStack moneyT5 = BankMechanics.gem.clone();

        ItemStack weapon = null;

        // Tier 1 Loot
        for (int i = 0; i < 2; i++) {
            weapon = new ItemGenerator().next(ItemTier.TIER_1);
            tier1Loot.add(weapon);
        }
        moneyT1.setAmount(10);
        tier1Loot.add(moneyT1);
        for(int i = 0;i < 5; i++){
        	ItemStack t1Potion = ItemManager.createHealthPotion(1, false, new Random().nextBoolean());
        	tier1Loot.add(t1Potion);
        }
        // Tier 2 Loot
        for (int i = 0; i < 2; i++) {
            weapon = new ItemGenerator().next(ItemTier.TIER_2);
            tier2Loot.add(weapon);
        }
        moneyT2.setAmount(20);
        tier2Loot.add(moneyT2);
        for(int i = 0;i < 5; i++){
        	ItemStack t2Potion = ItemManager.createHealthPotion(2, false, new Random().nextBoolean());
        	tier2Loot.add(t2Potion);
        }
        // Tier 3 Loot
        for (int i = 0; i < 2; i++) {
            weapon = new ItemGenerator().next(ItemTier.TIER_3);
            tier3Loot.add(weapon);
        }
        moneyT3.setAmount(30);
        tier3Loot.add(moneyT3);
        for(int i = 0;i < 5; i++){
        	ItemStack t3Potion = ItemManager.createHealthPotion(3, false, new Random().nextBoolean());
        	tier3Loot.add(t3Potion);
        }
        // Tier 4 Loot
        for (int i = 0; i < 2; i++) {
            weapon = new ItemGenerator().next(ItemTier.TIER_4);
            tier4Loot.add(weapon);
        }
        moneyT4.setAmount(40);
        tier4Loot.add(moneyT4);
        for(int i = 0;i < 5; i++){
        	ItemStack t4Potion = ItemManager.createHealthPotion(4, false, new Random().nextBoolean());
        	tier4Loot.add(t4Potion);
        }
        // Tier 5 Loot
        for (int i = 0; i < 2; i++) {
            weapon = new ItemGenerator().next(ItemTier.TIER_5);
            tier5Loot.add(weapon);
        }
        moneyT5.setAmount(50);
        tier5Loot.add(moneyT5);
        for(int i = 0;i < 5; i++){
        	ItemStack t5Potion = ItemManager.createHealthPotion(5, false, new Random().nextBoolean());
        	tier5Loot.add(t5Potion);
        }
    }

    /**
     * returns an array of Items that are dedicated to the tier.
     *
     * @param tier
     * @return
     */
    public static ItemStack[] getLoot(int tier) {
        int num = RandomHelper.getRandomNumberBetween(1, 2);
        ItemStack[] items = new ItemStack[num];
        switch (tier) {
            case 1:
                for (int i = 0; i < num; i++)
                    items[i] = tier1Loot.get(new Random().nextInt(tier1Loot.size()));
                break;
            case 2:
                for (int i = 0; i < num; i++)
                    items[i] = tier2Loot.get(new Random().nextInt(tier2Loot.size()));
                break;
            case 3:
                for (int i = 0; i < num; i++)
                    items[i] = tier3Loot.get(new Random().nextInt(tier3Loot.size()));
                break;
            case 4:
                for (int i = 0; i < num; i++)
                    items[i] = tier4Loot.get(new Random().nextInt(tier4Loot.size()));
                break;
            case 5:
                for (int i = 0; i < num; i++)
                    items[i] = tier5Loot.get(new Random().nextInt(tier5Loot.size()));
                break;
        }
        return items;
    }

    /**
     * Initialization of loot spawners from config.
     */
    public static void loadLootSpawners() {
        loadLootItems();
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
