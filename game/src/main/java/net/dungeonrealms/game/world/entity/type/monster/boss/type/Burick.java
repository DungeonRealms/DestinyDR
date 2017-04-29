package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Oct 19, 2015
 * Heavily simplified by Kneesnap.
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
                if (entity.isDead())
                    spawnedMobs.remove(entity);
        
        LivingEntity en = (LivingEntity) event.getEntity();
        if (spawnedMobs.isEmpty()) {
            if (!canAddsRespawn && !hasMessaged) {
            	say("Face me, pathetic creatures!");
                hasMessaged = true;
            }
            if (en.hasPotionEffect(PotionEffectType.INVISIBILITY))
                en.removePotionEffect(PotionEffectType.INVISIBILITY);
            
            if (DamageAPI.isInvulnerable(en))
                DamageAPI.removeInvulnerable(en);
            
        }
        int health = HealthHandler.getMonsterMaxHP(en);
        int hp = HealthHandler.getMonsterHP(en);
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
                HealthHandler.healMonster(en, HealthHandler.getMonsterMaxHP(en));
                HealthHandler.setMonsterHP(en, HealthHandler.getMonsterMaxHP(en));
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
    public BossType getBossType() {
        return BossType.Burick;
    }

    private void spawnWave() {
    	MinionType type = MinionType.getRandom();
    	for (int i = 0; i < type.getGroupSize(); i++)
    		spawnedMobs.add(spawnMinion(type.getMonster(), type.getName(), type.getTier()));
    }
    
    public int getGemDrop(){
    	return random.nextInt(2500 - 1000) + 1000;
    }
    
    public int getXPDrop(){
    	return 25000;
    }

	@Override
	public void addKillStat(GamePlayer gp) {
		gp.getPlayerStatistics().setBurickKills(gp.getPlayerStatistics().getBurickKills() + 1);
	}
	
	@AllArgsConstructor
	private enum MinionType {
		PROTECTOR(EnumMonster.Monk, 2, 4),
		ACOLYTE(EnumMonster.Acolyte, 3, 4),
		SACRIFICE(EnumMonster.Skeleton, 2, 6);
		
		@Getter private EnumMonster monster;
		@Getter private int tier;
		@Getter private int groupSize;
		
		public String getName() {
			return "Burick's " + Utils.capitalize(name());
		}
		
		public static MinionType getRandom() {
			return values()[new Random().nextInt(values().length)];
		}
	}
}
