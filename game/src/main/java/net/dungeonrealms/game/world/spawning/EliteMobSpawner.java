package net.dungeonrealms.game.world.spawning;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;

import org.bukkit.Location;

/**
 * Handles spawning of elites.
 * 
 * Redone on April 20th, 2017.
 * @author Kneesnap
 */
public class EliteMobSpawner extends MobSpawner {

	@Setter @Getter
    private EnumNamedElite eliteType;

    public EliteMobSpawner(Location loc, EnumNamedElite elite, EnumMonster monster, int tier, String mobPower, int respawnDelay, int mininmumXZ, int maximumXZ) {
        super(loc, monster, "", tier, 1, mobPower, respawnDelay, mininmumXZ, maximumXZ);
        setEliteType(elite);
    }

    @Override
    public void init() {
    	setTimer(this::spawnIn, 20);
    }
    
    @Override
    public int getSpawnAmount() {
    	return 1;
    }

    @Override
    protected boolean canSpawnMobs() {
    	boolean nearby = GameAPI.arePlayersNearby(getLocation(), 24);
    	if (nearby)
    		setCounter(0);
        return super.canSpawnMobs() && nearby;
    }
}
