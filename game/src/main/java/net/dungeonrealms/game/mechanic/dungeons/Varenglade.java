package net.dungeonrealms.game.mechanic.dungeons;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.items.core.VanillaItem;
import net.dungeonrealms.game.mechanic.ItemManager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Varenglade Dungeon
 * 
 * Created April 29th, 2017.
 * @author Kneesnap
 */
public class Varenglade extends Dungeon {

	public Varenglade(List<Player> players) {
		super(DungeonType.VARENGLADE, players);
	}
	
	public static ItemStack getKey() {
		ItemStack k = ItemManager.createItem(Material.TRIPWIRE_HOOK, ChatColor.LIGHT_PURPLE + "A mystical key", ChatColor.ITALIC + "One of four mysterious keys.");
		VanillaItem key = new VanillaItem(k);
		key.setDungeon(true);
		return key.generateItem();
	}
	
	public static class VarengladeListener implements Listener {
		
		@EventHandler(priority = EventPriority.HIGHEST)
	    public void monsterDeath(EntityDeathEvent event) {
			World w = event.getEntity().getWorld();
	        if (!DungeonManager.isDungeon(w, DungeonType.VARENGLADE))
	        	return;
	        
	        Dungeon d = DungeonManager.getDungeon(w);
	        if (d.hasSpawned(BossType.BurickPriest) || ThreadLocalRandom.current().nextInt(10) > 7)
	        	return;
	        
	        Player killer = event.getEntity().getKiller();
	        
	        if (killer != null) {
	        	GameAPI.giveOrDropItem(killer, getKey());
	        } else {
	        	w.dropItem(event.getEntity().getLocation().add(0, 1, 0), getKey());
	        }
	    }
	}
}
