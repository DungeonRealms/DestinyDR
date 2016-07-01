package net.dungeonrealms.game.world.entities;

import net.dungeonrealms.game.world.entities.powermoves.PowerStrike;
import net.dungeonrealms.game.world.entities.powermoves.WhirlWind;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by chase on 6/30/2016.
 */
public abstract class PowerMove {
    public static CopyOnWriteArrayList<UUID> chargingMonsters = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<PowerMove> powermoves = new CopyOnWriteArrayList<>();

    public static void registerPowerMoves() {
        powermoves.add(new WhirlWind());
        powermoves.add(new PowerStrike());

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
