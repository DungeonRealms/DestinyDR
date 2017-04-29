package net.dungeonrealms.game.mechanic.dungeons;

import java.util.Random;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;

/**
 * The Infernal Abyss Dungeon
 * 
 * TODO: Infernal should ride the ghast.
 * 
 * Created April 28th, 2017.
 * @author Kneesnap
 */
@Getter
public class InfernalAbyss extends Dungeon {
	
	public InfernalAbyss() {
		super(DungeonType.THE_INFERNAL_ABYSS);
	}
	
	@Override
	protected void setupWorld(String worldName) {
		super.setupWorld(worldName);
		
		CraftWorld world = (CraftWorld)getWorld();
    	world.setEnvironment(Environment.NETHER);
    	world.getHandle().worldProvider.a(world.getHandle()); //<- Prevents an NMS crash, by initing the world.
	}
	
	@Override
	public void updateMob(Entity e) {
		if (e.getLocation().getY() < 90)
			returnToSpawner(e); //Return any mobs that fall out of the world.
		
		super.updateMob(e);
	}
	
	public class InfernalListener implements Listener {
		
		public InfernalListener() {
			
			// Spawn minions and leaves a trail of fire behind Infernal.
			Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> {
				for (Dungeon o : DungeonManager.getDungeons(DungeonType.THE_INFERNAL_ABYSS)) {
					InfernalAbyss dungeon = (InfernalAbyss) o;
					
					Entity e = dungeon.getBoss().getBukkit();
					if (e.isDead() || !e.isOnGround())
						return;
					
					Location l = e.getLocation();
					if (l.getBlock().getType() == Material.AIR)
						l.getBlock().setType(Material.FIRE);
						
					if (new Random().nextInt(20) == 0)
						EntityAPI.spawnCustomMonster(l.clone().add(0, 2, 0), EnumMonster.MagmaCube, "low", 3, null);
				}
			}, 0L, 5L);
			
			// Handle wither effects.
			Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> DungeonManager.getDungeons(DungeonType.THE_INFERNAL_ABYSS).forEach(d -> {
				for (Player p : d.getPlayers()) {
					int left = -1;
					for (PotionEffect pe : p.getActivePotionEffects())
						if (pe.getType() == PotionEffectType.WITHER)
							left = (pe.getDuration() / 20) - 1;
					
					if (left == -1)
						continue;
					
					if (left == 30) {
						p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + ">> " + ChatColor.RED + "You have " + ChatColor.UNDERLINE +
								left + "s" + ChatColor.RED + " left until the inferno consumes you.");
					} else if (left <= 1) {
						p.removePotionEffect(PotionEffectType.WITHER);
						HealthHandler.setPlayerHP(p, 1);
						p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "You have been drained of nearly all your life by the power of the inferno.");
						p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2, 1.3F);
					}
				}
			}), 200L, 20L);
		}
	}
}
