package net.dungeonrealms.game.world.entities;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.world.entities.powermoves.PowerStrike;
import net.dungeonrealms.game.world.entities.powermoves.WhirlWind;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by chase on 6/30/2016.
 */
public abstract class PowerMove {
    public static CopyOnWriteArrayList<UUID> chargingMonsters = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<UUID> chargedMonsters = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<PowerMove> powermoves = new CopyOnWriteArrayList<>();

    public static void registerPowerMoves() {
        powermoves.add(new WhirlWind());
        powermoves.add(new PowerStrike());

        World world = Bukkit.getWorlds().get(0);
        Random rand = new Random();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {

            world.getLivingEntities().stream().filter(ent -> chargedMonsters.contains(ent.getUniqueId())).forEach(ent -> {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.WITCH_MAGIC, ent.getLocation().add(0, 1, 0), new Random().nextFloat(),
                        rand.nextFloat(), rand.nextFloat(), 0.5F, 35);



            });
        }, 0, 20);

    }

    public static void doPowerMove(String identifier, LivingEntity entity, Player p) {
        for (PowerMove move : powermoves)
            if (move.id.equalsIgnoreCase(identifier))
                move.schedulePowerMove(entity, p);
    }


    public String id;

    public PowerMove(String identifier) {
        this.id = identifier;
    }


    public abstract void schedulePowerMove(LivingEntity entity, Player attack);


}
