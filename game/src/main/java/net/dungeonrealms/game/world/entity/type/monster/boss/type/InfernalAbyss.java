package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


/**
 * InfernalAbyss Boss
 * 
 * Redone on April 28th, 2017.
 * @author Kneesnap
 */
public class InfernalAbyss extends StaffWitherSkeleton implements DungeonBoss {

    public InfernalGhast ghast;
    public boolean finalForm = false;

    public InfernalAbyss(World world) {
        super(world);
        createEntity(50);
        this.fireProof = true;
        
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
    }

    public String[] getItems() {
        return new String[] {"infernalstaff", "infernalhelmet", "infernalchest", "infernallegging", "infernalboot"};
    }

    @Override
    public BossType getBossType() {
        return BossType.InfernalAbyss;
    }

    public void doFinalForm(double hp) {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        HealthHandler.setMaxHP(getBukkit(), (int) hp);
        HealthHandler.setMonsterHP(livingEntity, (int) hp);
        
        livingEntity.setMaximumNoDamageTicks(0);
        livingEntity.setNoDamageTicks(0);
        livingEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
        
        finalForm = true;
        
        DamageAPI.setDamageBonus(livingEntity, 50);
        say("You... cannot... kill me IN MY OWN DOMAIN, FOOLISH MORTALS!");
        getDungeon().announce(ChatColor.GRAY + "The Infernal Abyss has become enraged! " + ChatColor.UNDERLINE + "+50% DMG!");
        for (Player pl : getDungeon().getAllPlayers()) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_DEATH, 2F, 0.85F);
        }
        
        for (int i = 0; i < 4; i++)
        	this.spawnMinion(EnumMonster.MagmaCube, "Demonic Spawn of Inferno", 3, false);
    }

    @Override
    public void onBossDeath(Player player) {
    	for (Player pl : getDungeon().getAllPlayers()) {
            pushAwayPlayer(getBukkitEntity(), pl, 3.5F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 2F, 2F);
        }
    }

    @Override
    public void a(EntityLiving entity, float f) {
    	if (!ItemWeaponStaff.isStaff(getHeld()))
    		return;
    	ItemWeaponStaff staff = new ItemWeaponStaff(getHeld());
    	
        Projectile proj = DamageAPI.fireStaffProjectile((LivingEntity) this.getBukkitEntity(), staff.getAttributes(), staff);
        if(proj != null)
            proj.setVelocity(proj.getVelocity().multiply(1.25));
    }

    private void pushAwayPlayer(Entity entity, Player p, double speed) {
        org.bukkit.util.Vector unitVector = p.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
        double e_y = entity.getLocation().getY();
        double p_y = p.getLocation().getY();

        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();

        if ((p_y - 1) <= e_y || m == Material.AIR) {
            EntityMechanics.setVelocity(p, unitVector.multiply(speed));
        }
    }

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {
        LivingEntity en = (LivingEntity) event.getEntity();
        if (event.getDamager() instanceof Player) {
            Player p_attacker = (Player) event.getDamager();
            if (p_attacker.getLocation().distanceSquared(en.getLocation()) <= 16) {
                pushAwayPlayer(en, p_attacker, 2F);
                p_attacker.setFireTicks(80);
                p_attacker.playSound(p_attacker.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1F, 1F);
            }
        }
        
        boolean spawnedGhast = getDungeon().hasSpawned(BossType.InfernalGhast);
        
        if (spawnedGhast && !this.ghast.isAlive()) {
        	if (en.hasPotionEffect(PotionEffectType.INVISIBILITY))
        		en.removePotionEffect(PotionEffectType.INVISIBILITY);
        	
        	if (DamageAPI.isInvulnerable(en))
        		DamageAPI.removeInvulnerable(en);
        }

        double halfHP = HealthHandler.getMonsterMaxHP(en) * 0.5;
        if (HealthHandler.getMonsterHP(en) <= halfHP && !spawnedGhast) {
            say("Behold, the powers of the inferno.");
            
            ghast = (InfernalGhast) getDungeon().spawnBoss(BossType.InfernalGhast);
            ghast.init(HealthHandler.getMaxHP(getBukkit()));
            
            say("The inferno will devour you!");
            getDungeon().announce(ChatColor.GRAY + "The Infernal Abyss has armored up! " + ChatColor.UNDERLINE + "+50% ARMOR!");
            for (Player pl : this.getBukkitEntity().getWorld().getPlayers())
            	pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_WARN, 2F, 0.35F);
            
            en.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
            DamageAPI.setInvulnerable(en);
            DamageAPI.setArmorBonus(en, 50);
        }

        if (spawnedGhast && this.ghast.isAlive())
        	return;
        
        if (random.nextInt(15) == 0) {
            Location hit_loc = this.getBukkitEntity().getLocation();
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LARGE_SMOKE, hit_loc.add(0, 0.5, 0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
            
            //  SPAWN MINIONS  //
            int minionType = random.nextInt(2);
            EnumMonster monsterType = finalForm ? EnumMonster.Silverfish : EnumMonster.MagmaCube;
            String name = finalForm ? "Abyssal Demon" : "Demonic Spawn of Inferno";
            if(minionType == 1)
            	name = (finalForm ? "Greater" : "Demonic") + " " + name;
            spawnMinion(monsterType, name, 3 + minionType, false);
        }
    }
    
    public int getGemDrop(){
    	return random.nextInt(2000) + 10000;
    } 

	@Override
	public int getXPDrop() {
		return 50000;
	}

	@Override
	public void addKillStat(GamePlayer gp) {
		gp.getPlayerStatistics().setInfernalAbyssKills(gp.getPlayerStatistics().getInfernalAbyssKills() + 1);
	}
}
