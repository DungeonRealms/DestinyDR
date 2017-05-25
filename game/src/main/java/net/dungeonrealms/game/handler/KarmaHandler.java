package net.dungeonrealms.game.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kieran on 10/7/2015.
 */
public class KarmaHandler implements GenericMechanic, Listener {
	
    public static List<Location> CHAOTIC_RESPAWNS = new ArrayList<>();
    
    @AllArgsConstructor @Getter
    public enum WorldZoneType {
    	SAFE("SAFE ZONE (DMG-OFF)", EnumPlayerAlignments.LAWFUL),
    	WILD("WILDERNESS (MOBS-ON, PVP-OFF)", EnumPlayerAlignments.NEUTRAL),
    	CHAOTIC("CHAOTIC ZONE (PVP-ON)", EnumPlayerAlignments.CHAOTIC);
    	
    	private String entryMessage;
    	private EnumPlayerAlignments alignment;
    }

    @AllArgsConstructor @Getter
    public enum EnumPlayerAlignments {
        LAWFUL(ChatColor.GREEN, "-30% Durability Keep Arm/Wep on Death", 0, 0, "While lawful, you will not lose any equipped armor on death, instead, all armor will lose 30% of its durability when you die."),
        NEUTRAL(ChatColor.YELLOW, "25%/50% Lose Arm/Wep on Death", 120, 120, "While neutral, you have a 50% chance of dropping your weapon, and a 25% chance of dropping each piece of equipped armor on death."),
        CHAOTIC(ChatColor.RED, "Inventory LOST on Death", 1200, 1800, "While chaotic, you cannot enter any major cities or safe zones. If you are killed while chaotic, you will lose everything in your inventory.");

        private ChatColor alignmentColor;
        private String description;
        private int timer;
        private int maxTimer;
        private String longDescription;
        
        public ChatColor getColor() {
        	return getAlignmentColor();
        }
        
        public ChatColor getNameColor() {
        	return this == LAWFUL ? ChatColor.GRAY : getColor();
        }
        
        public String getName() {
        	return name().toLowerCase();
        }
        
        public static EnumPlayerAlignments getByName(String rawName) {
            for (EnumPlayerAlignments playerAlignments : values())
                if (playerAlignments.getName().equalsIgnoreCase(rawName))
                    return playerAlignments;
            return null;
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -382, 68, 867));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -350, 67, 883));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -330, 65, 898));
        CHAOTIC_RESPAWNS.add(new Location(Bukkit.getWorlds().get(0), -419, 61, 830));

        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::updateAllPlayerAlignments, 100L, 20L);
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    @Override
    public void stopInvocation() {

    }
    
    /**
     * Called when a player attacks another player, and presumably goes neutral.
     */
    public static void update(Player player) {
    	PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
    	if (wrapper.getAlignment() != EnumPlayerAlignments.CHAOTIC)
    		wrapper.setAlignment(EnumPlayerAlignments.NEUTRAL);
    }

    /**
     * Updates all player alignments
     * from Chaotic->Neutral or Neutral->Lawful
     * if they are not in combat and in the
     * main world
     *
     * @since 1.0
     */
    private void updateAllPlayerAlignments() {
        PlayerWrapper.getPlayerWrappers().values().stream().filter(wrap -> wrap.getPlayer() != null && wrap.getPlayer().isOnline() && wrap.getAlignmentTime() != -1 && GameAPI.isMainWorld(wrap.getPlayer().getWorld()))
                .forEach(wrap -> {
                	EnumPlayerAlignments alignment = wrap.getAlignment();
                	if (alignment.getTimer() <= 0)
                		return;
                    int timeLeft = wrap.getAlignmentTime();
                    timeLeft--;
                    if (timeLeft <= 0) {
                    	wrap.setAlignment(EnumPlayerAlignments.values()[alignment.ordinal() - 1]);
                    } else {
                        wrap.setAlignmentTime(timeLeft);
                    }
                });
    }

    /**
     * Handles when the player "dies" in combat
     * Checks to see if their killer should change alignment
     * and changes it if they should.
     *
     * @param player
     * @param killer
     * @since 1.0
     */
    public static void handlePlayerPsuedoDeath(Player player, Entity killer) {
        LivingEntity leKiller = null;
        switch (killer.getType()) {
            case ARROW:
            case TIPPED_ARROW:
            case SNOWBALL:
            case SMALL_FIREBALL:
            case ENDER_PEARL:
            case FIREBALL:
            case WITHER_SKULL:
            case DRAGON_FIREBALL:
                Projectile projectile = (Projectile) killer;
                if (!(projectile.getShooter() instanceof LivingEntity)) break;
                leKiller = (LivingEntity) projectile.getShooter();
                break;
            case PLAYER:
                leKiller = (LivingEntity) killer;
                break;
            default:
                break;
        }
        Player killerPlayer;
        if (!GameAPI.isPlayer(leKiller))
        	return;
        
        killerPlayer = (Player) leKiller;
        PlayerWrapper deathWrapper = PlayerWrapper.getPlayerWrapper(player);
        PlayerWrapper killerWrapper = PlayerWrapper.getPlayerWrapper(killerPlayer);
        if (deathWrapper != null)
        	deathWrapper.getPlayerGameStats().addStat(StatColumn.DEATHS);
        
        EnumPlayerAlignments alignmentPlayer = deathWrapper.getAlignment();
        
        killerWrapper.getPlayerGameStats().addStat(StatColumn.PLAYER_KILLS);
        killerWrapper.getPlayerGameStats().addStat(alignmentPlayer == EnumPlayerAlignments.LAWFUL ? StatColumn.LAWFUL_KILLS : StatColumn.UNLAWFUL_KILLS);
        
        EnumPlayerAlignments a = killerWrapper.getAlignment();
        killerWrapper.setAlignment(a == EnumPlayerAlignments.LAWFUL ? EnumPlayerAlignments.CHAOTIC : a);
    }
    
    /**
     * Alerts a player as to when they've changed world regions.
     */
    @EventHandler
    public void onRegionChance(PlayerEnterRegionEvent evt) {
    	WorldZoneType type = evt.getNewZone();
    	if (type == evt.getOldZone())
    		return;
    	
    	Utils.sendCenteredMessage(evt.getPlayer(), type.getAlignment().getColor() + "" + ChatColor.BOLD + "*** " + type.getEntryMessage() + " ***");
    	evt.getPlayer().playSound(evt.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.25F, 0.30F);
    	HealthHandler.updateBossBar(evt.getPlayer()); //Forces the color of the bossbar to change.
    }
}
