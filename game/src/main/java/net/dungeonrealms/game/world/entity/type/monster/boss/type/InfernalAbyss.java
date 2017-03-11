package net.dungeonrealms.game.world.entity.type.monster.boss.type;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.DungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumDungeonBoss;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffWitherSkeleton;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.*;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


/**
 * Created by Chase on Oct 21, 2015
 */
public class InfernalAbyss extends StaffWitherSkeleton implements DungeonBoss {

    public InfernalGhast ghast;

    public InfernalAbyss(World world) {
        super(world);
        this.createEntity(50);
        this.fireProof = true;
        this.persistent = true;
        DungeonManager.getInstance().getFireUnderEntity().add(this);
        ghast = new InfernalGhast(this);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
        EntityStats.setBossRandomStats(this.ghast, 100, 4);
        ghast.setArmor(new ItemGenerator().setTier(Item.ItemTier.getByTier(4)).setRarity(Item.ItemRarity.UNIQUE).getArmorSet(),
                new ItemGenerator().setTier(Item.ItemTier.getByTier(4)).setRarity(Item.ItemRarity.UNIQUE)
                        .setType(Item.ItemType.getRandomWeapon()).generateItem().getItem());
    }

    public String[] getItems() {
        return new String[] {"infernalstaff", "infernalhelmet", "infernalchest", "infernallegging", "infernalboot"};
    }

    @Override
    public EnumDungeonBoss getEnumBoss() {
        return EnumDungeonBoss.InfernalAbyss;
    }

    private boolean hasFiredGhast = false;
    public boolean finalForm = false;

    public void doFinalForm(double hp) {
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        livingEntity.setMetadata("maxHP", new FixedMetadataValue(DungeonRealms.getInstance(), (int) hp));
        HealthHandler.getInstance().setMonsterHPLive(livingEntity, (int) hp);
        livingEntity.setMaximumNoDamageTicks(0);
        livingEntity.setNoDamageTicks(0);
        livingEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
        finalForm = true;
        DamageAPI.setDamageBonus(livingEntity, 50);
        say("You... cannot... kill me IN MY OWN DOMAIN, FOOLISH MORTALS!");
        for (Player pl : livingEntity.getWorld().getPlayers()) {
            pl.sendMessage(ChatColor.GRAY + "The Infernal Abyss has become enraged! " + ChatColor.UNDERLINE + "+50% DMG!");
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_DEATH, 2F, 0.85F);
        }
        
        for (int i = 0; i < 4; i++)
        	this.spawnMinion(EnumMonster.MagmaCube, "Demonic Spawn of Inferno", 3, false);
    }

    @Override
    public void onBossDeath() {
    	for (Player pl : getBukkitEntity().getWorld().getPlayers()) {
            pushAwayPlayer(getBukkitEntity(), pl, 3.5F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_DEATH, 2F, 2F);
        }
    }

    @Override
    public void a(EntityLiving entity, float f) {
        net.minecraft.server.v1_9_R2.ItemStack nmsItem = this.getEquipment(EnumItemSlot.MAINHAND);
        NBTTagCompound tag = nmsItem.getTag();
        Projectile proj = DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
        if(proj != null){
            //Needs to fly a little further so it actually hits the target?
            proj.setVelocity(proj.getVelocity().multiply(1.25));
        }
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
        if (hasFiredGhast) {
            if (!this.ghast.isAlive()) {
                if (en.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    en.removePotionEffect(PotionEffectType.INVISIBILITY);
                }
                if (DamageAPI.isInvulnerable(en)) {
                    DamageAPI.removeInvulnerable(en);
                }
            }
        }

        double halfHP = HealthHandler.getInstance().getMonsterMaxHPLive(en) * 0.5;
        if (HealthHandler.getInstance().getMonsterHPLive(en) <= halfHP && !hasFiredGhast) {
            say("Behold, the powers of the inferno.");
            ghast.setLocation(this.locX, this.locY + 7, this.locZ, 1, 1);
            ghast.init(HealthHandler.getInstance().getMonsterHPLive(en));
            this.getWorld().addEntity(ghast, SpawnReason.CUSTOM);
            ghast.init(HealthHandler.getInstance().getMonsterHPLive(en));
            say("The inferno will devour you!");
            for (Player pl : this.getBukkitEntity().getWorld().getPlayers()) {
                pl.sendMessage(ChatColor.GRAY + "The Infernal Abyss has armored up! " + ChatColor.UNDERLINE + "+50% ARMOR!");
                pl.playSound(pl.getLocation(), Sound.ENTITY_GHAST_WARN, 2F, 0.35F);
                pl.playSound(pl.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
            }
            DamageAPI.setArmorBonus(ghast.getBukkitEntity(), 50);
            hasFiredGhast = true;
            en.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15));
            DamageAPI.setInvulnerable(en);
            DamageAPI.setArmorBonus(en, 50);
        }

        if (hasFiredGhast)
            if (this.ghast.isAlive())
                return;
        
        if (random.nextInt(15) == 1) {
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
