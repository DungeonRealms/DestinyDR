package net.dungeonrealms.old.game.world.spawning;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.old.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.old.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.old.game.profession.Fishing;
import net.dungeonrealms.old.game.profession.Mining;
import net.dungeonrealms.old.game.world.entity.type.EnderCrystal;
import net.dungeonrealms.old.game.world.entity.util.BuffUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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
            if (!GameAPI.isPlayer(player)) continue;
            if (GameAPI.isInSafeRegion(player.getLocation())) continue;
            if (player.getGameMode().equals(GameMode.SPECTATOR)) continue;
            if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) continue;
            if (GameAPI._hiddenPlayers.contains(player)) continue;
            if (player.getEquipment().getItemInMainHand().getType() != Material.AIR && player.getEquipment().getItemInMainHand() != null)
                if (Mining.isDRPickaxe(player.getEquipment().getItemInMainHand()) || Fishing.isDRFishingPole(player.getEquipment().getItemInMainHand()))
                    continue;
            if (getNearbyBuffs(player, 15).size() >= 1) continue;
            if (GameAPI.getNearbyPlayers(player.getLocation(), 10).size() > 2) continue;
            if (new Random().nextInt(21) < 4) {
                if (!CURRENT_BUFFS.isEmpty()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        try {
                            EnderCrystal enderCrystal = CURRENT_BUFFS.get(Math.max(0, new Random().nextInt(CURRENT_BUFFS.size())));
                            CURRENT_BUFFS.remove(enderCrystal);
                            enderCrystal.dead = true;
                        } catch (NullPointerException ignored) {
                        }
                    }, 0L);
                }
            }
            if (CURRENT_BUFFS.size() >= MAX_BUFFS) continue;
            if (new Random().nextInt(21) < 6) if (CURRENT_BUFFS.size() < MAX_BUFFS)
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> CURRENT_BUFFS.add(BuffUtils.spawnBuff(player.getUniqueId())), 0L);
        }
    }

    private static Set<Entity> getNearbyBuffs(Player player, int radius) {
        return player.getNearbyEntities(radius, radius, radius)
                .stream().filter(entity -> entity.hasMetadata("type")).filter(entity -> entity.getMetadata("type").get(0).asString().equalsIgnoreCase("buff")).collect(Collectors.toSet());
    }
}
