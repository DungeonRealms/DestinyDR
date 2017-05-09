package net.dungeonrealms.game.world.spawning;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.EnumEntityType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * BuffMechanics - Handles Ender Crystal Buffs.
 * 
 * Redone on April 20th, 2017.
 * @author Kneesnap
 */
public class BuffMechanics implements GenericMechanic, Listener {
	
	@Getter //List of all spawned buffs.
    private static List<Entity> buffs = new CopyOnWriteArrayList<>();
	
	private static final PotionEffectType[] BUFFS = new PotionEffectType[] {PotionEffectType.INCREASE_DAMAGE, PotionEffectType.DAMAGE_RESISTANCE,
		PotionEffectType.SPEED, PotionEffectType.NIGHT_VISION, PotionEffectType.JUMP, PotionEffectType.FIRE_RESISTANCE,
		PotionEffectType.WATER_BREATHING, PotionEffectType.HEAL};
	
	private static int BUFF_DURATION = 90;
	private static int BUFF_RADIUS = 8;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::spawnSomeBuffs, 40L, 1800L);
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @Override
    public void stopInvocation() {

    }
    
    /**
     * Handles a player activating an entity crystal buff.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBuffExplode(EntityExplodeEvent event) {
        if (DungeonManager.isDungeon(event.getEntity()))
        	return;
        
        event.blockList().clear();
        event.setYield(0.0F);
        event.setCancelled(true);
        
        if (!EnumEntityType.BUFF.isType(event.getEntity()))
        	return;
    	
        Entity buff = event.getEntity();
    	List<Player> toBuff = GameAPI.getNearbyPlayers(buff.getLocation(), BUFF_RADIUS);
        PotionEffectType effectType = BUFFS[new Random().nextInt(BUFFS.length)];

        buff.getWorld().playSound(buff.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 5F, 1.5F);
        removeBuff(buff);
        
        String message = ChatColor.BLUE + "" + ChatColor.BOLD + "           ";
        
        if (effectType != PotionEffectType.HEAL) {
        	message += Utils.capitalize(effectType.getName().replaceAll("_", " ")) + " Buff [" + BUFF_DURATION + "s]";
        	// Give the receivers the buff.
        	for (Player player : toBuff)
        		player.addPotionEffect(new PotionEffect(effectType, BUFF_DURATION * 20, Utils.randInt(0, 2)));
        } else {
        	double heal = Utils.randInt(0, 99) < 20 ? 100 : 50;
        	message += "Instant Health (" + heal + "%)";
        	for (Player player : toBuff)
        		HealthHandler.heal(player, (int) ((heal / 100D) * HealthHandler.getHPPercent(player)));
        }
        
        // Message receivers.
        for (Player p : toBuff)
        	p.sendMessage(message);
    }

    private void spawnSomeBuffs() {
        int maxBuffs = (Bukkit.getOnlinePlayers().size() / 4) + 1;
        for (Player player : Bukkit.getOnlinePlayers()) {
        	// Don't spawn buffs in safe-zones.
            if (!GameAPI.isPlayer(player) || GameAPI.isInSafeRegion(player.getLocation()))
            	continue;
            // Skip if they're vanished.
            if (player.getGameMode().equals(GameMode.SPECTATOR) || GameAPI._hiddenPlayers.contains(player))
            	continue;
            // Only spawn them if you're in the main world and not doing profession stuff.
            if (!GameAPI.isMainWorld(player.getLocation()) || ProfessionItem.isProfessionItem(player.getEquipment().getItemInMainHand()))
            	continue;
            // Don't spawn buffs to close to players or buffs.
            if (getNearbyBuffs(player, 15).size() >= 1 || GameAPI.getNearbyPlayers(player.getLocation(), 10).size() > 2)
            	continue;
            
            // TODO: Verify this is correct. This looks like it'll just make buffs not spawn.
            if (new Random().nextInt(21) >= 5 || getBuffs().isEmpty())
            	continue;
            
            Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
            	int bound = Utils.randInt(0, getBuffs().size());
                if (bound > 0)
                	removeBuff(getBuffs().get(bound));
            });
            
            if (getBuffs().size() < maxBuffs && Utils.randInt(0, 20) < 6)
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> spawnBuff(player));
        }
    }
    
    public static void spawnBuff(Player player) {
    	Location loc = player.getLocation();
    	World world = loc.getWorld();
        Block b = world.getHighestBlockAt(loc.getBlockX(), loc.getBlockZ());
        if (b.getLocation().distanceSquared(loc) < 25)
        	loc = b.getLocation();
        
        // Create Entity.
        Entity enderCrystal = world.spawnEntity(loc, EntityType.ENDER_CRYSTAL);
        enderCrystal.teleport(loc);
        player.playSound(loc, Sound.ENTITY_ENDERDRAGON_FLAP, 1f, 63f);
        
        // Set Meta.
        MetadataUtils.registerEntityMetadata(enderCrystal, EnumEntityType.BUFF);
        
        getBuffs().add(enderCrystal);
    }

    private static Set<Entity> getNearbyBuffs(Player player, int radius) {
        return player.getNearbyEntities(radius, radius, radius).stream()
        		.filter(EnumEntityType.BUFF::isType).collect(Collectors.toSet());
    }
    
    public static void removeBuff(Entity buff) {
    	buff.remove();
    	getBuffs().remove(buff);
    }
}
