package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Burick extends MeleeWitherSkeleton implements DungeonBoss {

	private boolean firstHeal = false;
    private boolean isEnraged = false;
    private CopyOnWriteArrayList<Entity> spawnedMobs = new CopyOnWriteArrayList<>();
    private boolean canAddsRespawn = true;
    private boolean hasMessaged = false;
	
    public Burick(World world) {
        super(world);
        createEntity(100);
        this.fireProof = true;
        collides = true;
    }

    public String[] getItems() {
        return new String[] {"up_axe", "up_helmet", "up_chest", "up_legs", "up_boots"};
    }

    @Override
    public void onBossDeath() {
        
    }

    public void startEnragedMode(LivingEntity en) {
        isEnraged = true;
        say("Pain. Sufferring. Agony. These are the emotions you will be feeling for the rest of eternity!");
        for (Player pl : en.getWorld().getPlayers()) {
            pl.sendMessage(ChatColor.GOLD + "" + ChatColor.UNDERLINE + "Burick The Fanatic " + ChatColor.GOLD + "has become ENRAGED"
                    + ChatColor.GOLD + " 2X DMG, +80% ARMOR, 2x SPEED, KNOCKBACK IMMUNITY!");
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 0.8F, 0.5F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 1.2F, 0.2F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 0.8F, 1.2F);
        }
        DamageAPI.setDamageBonus(en, 100);
        DamageAPI.setArmorBonus(en, 30);
        this.getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        en.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, true));
    }

    @Override
    public void onBossAttack(EntityDamageByEntityEvent event) {
        if (!spawnedMobs.isEmpty())
            for (Entity entity : spawnedMobs)
                if (!entity.isAlive())
                    spawnedMobs.remove(entity);
        
        LivingEntity en = (LivingEntity) event.getEntity();
        if (spawnedMobs.isEmpty()) {
            if (!canAddsRespawn && !hasMessaged) {
            	this.say("Face me, pathetic creatures!");
                hasMessaged = true;
            }
            if (en.hasPotionEffect(PotionEffectType.INVISIBILITY))
                en.removePotionEffect(PotionEffectType.INVISIBILITY);
            
            if (DamageAPI.isInvulnerable(en))
                DamageAPI.removeInvulnerable(en);
            
        }
        int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
        int hp = HealthHandler.getInstance().getMonsterHPLive(en);
        float tenPercentHP = (float) (health * .10);
        if (hp <= (float) (health * 0.5)) {
            if (canAddsRespawn) {
                spawnWave();
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, getBukkitEntity().getLocation(), random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
                en.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
                DamageAPI.setInvulnerable(en);
                say("To me, my undead brethren! Rip these Andalucians to pieces!");
                for (Player pl : en.getWorld().getPlayers()) {
                    pl.sendMessage(ChatColor.GRAY + "Burick uses the energy of his minions to create a forcefield around himself -- kill the minions!");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_SCREAM, 1F, 0.5F);
                }
                canAddsRespawn = false;
                hasMessaged = false;
            }
        }
        if (hp <= tenPercentHP) {
            if (!firstHeal) {
                HealthHandler.getInstance().healMonsterByAmount(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
                HealthHandler.getInstance().setMonsterHPLive(en, HealthHandler.getInstance().getMonsterMaxHPLive(en));
                canAddsRespawn = true;
                if (!firstHeal) {
                    firstHeal = true;
                    say("Let the powers of Maltai channel into me and give me strength!");
                    for (Player pl : en.getWorld().getPlayers())
                        pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 1F, 0.5F);
                }
            } else if (!isEnraged) {
                startEnragedMode(en);
            }
        }
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return EnumDungeonBoss.Burick;
    }

    private void spawnWave() {
        int waveType = random.nextInt(3);
        switch (waveType) {
            case 0:
                for (int i = 0; i < 4; i++)
                    spawnedMobs.add(spawnMinion(EnumMonster.Monk, "Burick's Protector", 2));
                break;
            case 1:
                for (int i = 0; i <= 4; i++)
                	spawnedMobs.add(spawnMinion(EnumMonster.Acolyte, "Burick's Acolyte", 3));
                break;
            case 2:
                for (int i = 0; i <= 6; i++)
                	spawnedMobs.add(spawnMinion(EnumMonster.Skeleton, "Burick's Sacrifice", 2));
                break;
        }
    }
    
    public int getGemDrop(){
    	return random.nextInt(2500 - 1000) + 1000;
    }
    
    public int getXPDrop(){
    	return 25000;
    }

	@Override
	public void addKillStat(PlayerWrapper gp) {
		gp.getPlayerGameStats().setBossBurickKills(gp.getPlayerGameStats().getBossBurickKills() + 1);
	}
}
