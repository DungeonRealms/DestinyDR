package net.dungeonrealms.game.world.entity.type.monster.boss;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.RangedWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Mayel - The Bandit Trove's boss.
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class Mayel extends RangedWitherSkeleton implements DungeonBoss {

	// Are mobs not allowed to spawn due to a cooldown?
	private boolean cooldown = false;
	
    public Mayel(World world) {
        super(world);
        this.fireProof = true;
    }
    
    public String[] getItems(){
    	return new String[] {"mayelbow", "mayelhelmet", "mayelchest", "mayelpants", "mayelboot"};
    }

    /**
     * Called when entity fires a projectile.
     */
    @Override
    public void a(EntityLiving entityliving, float f) {
        DamageAPI.fireBowProjectile((LivingEntity) getBukkitEntity(), new ItemWeaponBow(getHeld()));
    }
    
    @Override
    public void onBossAttacked(Player player) {
    	if (!canSpawnMobs())
    		return;
    	
    	// Spawn minions.
    	ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, getBukkitEntity().getLocation(), random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
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
        return BossType.Mayel;
    }
}