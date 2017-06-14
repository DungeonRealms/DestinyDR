package net.dungeonrealms.game.mechanic.dungeons;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
		return ItemGenerator.getNamedItem("DOkey");
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

		@EventHandler(priority = EventPriority.HIGHEST)
		public void playerWalk(PlayerMoveEvent event) {
			World w = event.getPlayer().getWorld();
			if (!DungeonManager.isDungeon(w, DungeonType.VARENGLADE))
				return;

			if(event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

			if(event.getTo().getY() >= 80) {
				event.getPlayer().teleport(w.getSpawnLocation());
			}
		}
	}
}
