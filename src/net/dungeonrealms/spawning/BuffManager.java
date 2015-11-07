package net.dungeonrealms.spawning;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.EnderCrystal;
import net.dungeonrealms.entities.utils.BuffUtils;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Kieran on 11/5/2015.
 */
public class BuffManager implements GenericMechanic {

    private static BuffManager instance = null;
    public CopyOnWriteArrayList<EnderCrystal> CURRENT_BUFFS = new CopyOnWriteArrayList<>();

    public static BuffManager getInstance() {
        if (instance == null)
            instance = new BuffManager();
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
	public void startInitialization() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::spawnSomeBuffs, 40L, 1800L);
    }

    @Override
    public void stopInvocation() {

    }

    private void spawnSomeBuffs() {
        int MAX_BUFFS = (Bukkit.getOnlinePlayers().size() / 4) + 1;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!API.isPlayer(player)) {
                continue;
            }
            if (API.isInSafeRegion(player.getLocation())) {
                continue;
            }
            if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
                continue;
            }
            if (API.getNearbyPlayers(player.getLocation(), 15).size() > 2) {
                if (new Random().nextInt(21) < 10) {
                    continue;
                }
            }
            if (new Random().nextInt(21) < 4) {
                if (!CURRENT_BUFFS.isEmpty()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        EnderCrystal enderCrystal = CURRENT_BUFFS.get(new Random().nextInt(CURRENT_BUFFS.size()));
                        CURRENT_BUFFS.remove(enderCrystal);
                        enderCrystal.dead = true;
                    }, 0L);
                }
            }
            if (CURRENT_BUFFS.size() >= MAX_BUFFS) {
                continue;
            }
            if (new Random().nextInt(21) < 6) {
                if (CURRENT_BUFFS.size() < MAX_BUFFS) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> CURRENT_BUFFS.add(BuffUtils.spawnBuff(player.getUniqueId())), 0L);
                }
            }
        }
    }
}
