package net.dungeonrealms.game.profession;


import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.MiningTier;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.*;
import org.bukkit.block.Block;
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

import java.util.*;

/**
 * Mining - Core listeners for the mining profession.
 * 
 * Redone by Kneesnap on April 7th, 2017.
 */
public class Mining implements GenericMechanic, Listener {

	private static HashMap<Location, Material> ORE_LOCATIONS = new HashMap<>();
	
	private static final int[] GEM_FIND_MIN = new int[] {01, 20, 40, 70,  90};
	private static final int[] GEM_FIND_MAX = new int[] {20, 40, 60, 90, 110};
	
	public Mining() {
		Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
	}
	
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
        Random rand = new Random();
        ItemStack item = e.getPlayer().getEquipment().getItemInMainHand();
        
        // Verify main world.
        if (!GameAPI.isMainWorld(block.getLocation()))
        	return;
        
        // Verify this is a pickaxe.
        if (!ItemPickaxe.isPickaxe(item))
        	return;
        
        //Verify we're breaking ore.
        MiningTier oreTier = MiningTier.getTierFromOre(block.getType());
        if (oreTier == null)
        	return;
        
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
        GamePlayer gp = GameAPI.getGamePlayer(p);
        gp.addExperience(xpGain / 12, false, true);
        
        int oreToAdd = 0;
        boolean toggleDebug = (boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, p.getUniqueId());
        p.playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1F, 0.75F);
        e.getBlock().setType(Material.STONE);
        
        //  REPLACE ORE  //
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> e.getBlock().setType(oreTier.getOre()), oreTier.getOreRespawnTime() * 15);
        
        //  SUCCESS  //
        if (rand.nextInt(100) < pickaxe.getSuccessChance() || pickaxe.getTier().getId() > oreTier.getTier()) {
        	oreToAdd = 1;
        	gp.getPlayerStatistics().setOreMined(gp.getPlayerStatistics().getOreMined() + 1);
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
        p.getEquipment().setItemInMainHand(pickaxe.generateItem());
        
        //  DOUBLE ORE  //
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.DOUBLE_ORE).getValue() >= rand.nextInt(100) + 1) {
        	oreToAdd *= 2;
        	if (toggleDebug)
        		p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          DOUBLE ORE DROP" + ChatColor.YELLOW + " (2x)");
        }
        
        //  TRIPLE ORE  //
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.TRIPLE_ORE).getValue() >= rand.nextInt(100) + 1) {
        	oreToAdd *= 3 ;
        	if (toggleDebug)
        		p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          TRIPLE ORE DROP" + ChatColor.YELLOW + " (3x)");
        }
        
        //  GIVE ORE  //
        ItemStack ore = oreTier.createOreItem();
        ore.setAmount(oreToAdd);
        GameAPI.giveOrDropItem(p, ore);
        
        if (pickaxe.getAttributes().getAttribute(PickaxeAttributeType.GEM_FIND).getValue() >= rand.nextInt(100) + 1) {
        	int tier = oreTier.getTier() - 1;
        	int amount = (int) (Utils.randInt(GEM_FIND_MIN[tier], GEM_FIND_MAX[tier]) * 0.8);
        	
        	//  DROP GEMS  //
        	if (amount > 0) {
        		p.getWorld().dropItemNaturally(block.getLocation(), new ItemGem(amount).generateItem());
        		if (toggleDebug)
        			p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "          FOUND " + amount + " GEM(s)");
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

                    String material_data = line.split("=")[1];
                    Material m = Material.getMaterial(material_data);

                    ORE_LOCATIONS.put(loc, m);

                    count++;
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
        Utils.log.info("[Profession] " + count + " ORE SPAWN locations have been LOADED.");
    }
    
    private void placeOre() {
        ORE_LOCATIONS.keySet().forEach(loc -> loc.getWorld().getBlockAt(loc).setType(ORE_LOCATIONS.get(loc)));
    }
    
    public static boolean isMineable(Block block){
    	return isMineable(block.getLocation());
    }
    
    public static boolean isMineable(Location loc){
    	for(Location check : ORE_LOCATIONS.keySet())
    		if(check.distance(loc) == 0D)
    			return true;
		return false;
    }

    @Override
    public void stopInvocation() {

    }
}
