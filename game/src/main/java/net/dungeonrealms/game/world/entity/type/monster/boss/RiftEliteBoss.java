package net.dungeonrealms.game.world.entity.type.monster.boss;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.base.DRGiant;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Mayel - The Bandit Trove's boss.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class RiftEliteBoss extends DRGiant implements DungeonBoss {

	// Are mobs not allowed to spawn due to a cooldown?
	private boolean cooldown = false;
	@Getter
    @Setter
	private int tier;

    public RiftEliteBoss(World world) {
        super(world);
        this.fireProof = true;
    }


    @Override
    public void onBossAttacked(Player player) {
    	if (!canSpawnMobs())
    		return;
    	
    	// Spawn minions.
    	ParticleAPI.spawnParticle(Particle.SPELL, getBukkitEntity().getLocation(), 100, 1F);
        for (int i = 0; i < 4; i++)
            spawnMinion(EnumMonster.MayelPirate, "Mayel's Crew", 1);
        say("Come to my call, brothers!");
    }
    
    private boolean canSpawnMobs() {
    	boolean temp = cooldown;
    	
    	if (!cooldown) {
    		cooldown = true;
    		Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> cooldown = false, 100L);
    	}
    	
        return getPercentHP() <= 0.8D && !temp;
    }

    @Override
    public BossType getBossType() {
        return BossType.RiftEliteBoss;
    }

    @Override
    public int getTier() {
        return getDungeon().getType().getTier();
    }

    @Override
    public EnumMonster getEnum() {
        return EnumMonster.RiftElite;
    }


    @Override
    public void collide(Entity e) {}
}