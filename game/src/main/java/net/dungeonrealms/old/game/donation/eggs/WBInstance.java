package net.dungeonrealms.old.game.donation.eggs;

import lombok.Data;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.WorldBoss;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/23/2016
 */

@Data
public class WBInstance {

    private UUID host;
    private WorldBoss boss;
    private Location location;

    public WBInstance(UUID host, Location location, WorldBoss boss) {
        this.host = host;
        this.boss = boss;
        this.location = location;
    }

    public void spawnBoss() {

    }
}
