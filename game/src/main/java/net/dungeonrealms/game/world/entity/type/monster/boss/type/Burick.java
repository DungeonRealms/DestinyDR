package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.enchantments.EnchantmentAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeWitherSkeleton;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chase on Oct 19, 2015
 */
public class Burick extends MeleeWitherSkeleton implements DungeonBoss {

    public Location loc;

    public Burick(World world) {
        super(world);
    }

    public Burick(World world, Location loc) {
        super(world);
        this.loc = loc;
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

    private boolean firstHeal = false;
    private boolean isEnraged = false;
    private CopyOnWriteArrayList<Entity> spawnedMobs = new CopyOnWriteArrayList<>();
    private boolean canAddsRespawn = true;
    private boolean hasMessaged = false;

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
            if (en.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                en.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            if (DamageAPI.isInvulnerable(en)) {
                DamageAPI.removeInvulnerable(en);
            }
        }
        int health = HealthHandler.getInstance().getMonsterMaxHPLive(en);
        int hp = HealthHandler.getInstance().getMonsterHPLive(en);
        float tenPercentHP = (float) (health * .10);
        if (hp <= (float) (health * 0.5)) {
            if (canAddsRespawn) {
                spawnWave();
                try {
                    ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, loc, random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);
                } catch (Exception err) {
                    err.printStackTrace();
                }
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

    private Location toSpawn = new Location(this.getBukkitEntity().getWorld(), -364, 61, -1);

    private void spawnWave() {
        int waveType = random.nextInt(3);
        Location location = new Location(world.getWorld(), toSpawn.getX() + random.nextInt(3), toSpawn.getY(), toSpawn.getZ() + random.nextInt(3));
        switch (waveType) {
            case 0:
                for (int i = 0; i < 4; i++) {
                    Entity entity = SpawningMechanics.getMob(world, 1, EnumMonster.Monk);
                    int level = Utils.getRandomFromTier(3, "high");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(entity, level, 3);
                    SpawningMechanics.rollElement(entity, EnumMonster.Monk);
                    if (entity == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Protector");
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Protector"));
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    ((EntityInsentient) entity).persistent = true;
                    ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    spawnedMobs.add(entity);
                }
                break;
            case 1:
                for (int i = 0; i <= 4; i++) {
                    Entity entity = SpawningMechanics.getMob(world, 3, EnumMonster.Acolyte);
                    int level = Utils.getRandomFromTier(3, "high");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(entity, level, 3);
                    SpawningMechanics.rollElement(entity, EnumMonster.Acolyte);
                    if (entity == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.setCustomName(newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Acolyte");
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(3).toString() + ChatColor.BOLD + "Burick's Acolyte"));
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    ((EntityInsentient) entity).persistent = true;
                    ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    spawnedMobs.add(entity);
                }
                break;
            case 2:
                for (int i = 0; i <= 6; i++) {
                    Entity entity = SpawningMechanics.getMob(world, 2, EnumMonster.Skeleton);
                    int level = Utils.getRandomFromTier(2, "high");
                    String newLevelName = ChatColor.AQUA.toString() + "[Lvl. " + level + "] ";
                    EntityStats.createDungeonMob(entity, level, 2);
                    SpawningMechanics.rollElement(entity, EnumMonster.Skeleton);
                    if (entity == null) {
                        return; //WTF?? UH OH BOYS WE GOT ISSUES
                    }
                    entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                    entity.setCustomName(newLevelName + GameAPI.getTierColor(2).toString() + ChatColor.BOLD + "Burick's Sacrifice");
                    entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), newLevelName + GameAPI.getTierColor(2).toString() + ChatColor.BOLD + "Burick's Sacrifice"));
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    ((EntityInsentient) entity).persistent = true;
                    ((LivingEntity) entity.getBukkitEntity()).setRemoveWhenFarAway(false);
                    world.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    spawnedMobs.add(entity);
                }
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
	public void addKillStat(GamePlayer gp) {
		gp.getPlayerStatistics().setBurickKills(gp.getPlayerStatistics().getBurickKills() + 1);
	}
}
