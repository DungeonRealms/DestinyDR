package net.dungeonrealms.game.world.entity.type.monster.boss;

import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.dungeons.BossType;
import net.dungeonrealms.game.mechanic.dungeons.DungeonBoss;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss.InfernalGhast;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.ranged.staff.StaffWitherSkeleton;
import net.dungeonrealms.game.world.item.DamageAPI;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 * InfernalAbyss Boss
 * <p>
 * Redone on April 28th, 2017.
 *
 * @author Kneesnap
 */
public class InfernalAbyss extends StaffWitherSkeleton implements DungeonBoss {

    public InfernalGhast ghast;
    public boolean finalForm;

    public InfernalAbyss(World world) {
        super(world);
        this.fireProof = true;

        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(40);
    }

    @Override
    public BossType getBossType() {
        return BossType.InfernalAbyss;
    }

    public void doFinalForm(int hp) {
        HealthHandler.initHP(getBukkit(), hp);

        getBukkit().setMaximumNoDamageTicks(0);
        getBukkit().setNoDamageTicks(0);

        finalForm = true;

        DamageAPI.setDamageBonus(getBukkit(), 50);
        say("You... cannot... kill me IN MY OWN DOMAIN, FOOLISH MORTALS!");
        getDungeon().announce(ChatColor.GRAY + "The Infernal Abyss has become enraged! " + ChatColor.UNDERLINE + "+50% DMG!");
        playSound(Sound.ENTITY_ENDERDRAGON_GROWL, 2F, 0.85F);
        playSound(Sound.ENTITY_GHAST_DEATH, 2F, 0.85F);

        for (int i = 0; i < 4; i++)
            spawnMinion(EnumMonster.MagmaCube, "Demonic Spawn of Inferno", 3, false);
    }

    @Override
    public void onBossDeath(Player player) {
        getDungeon().getPlayers().forEach(p -> pushAwayPlayer(p, 3.5F));
        playSound(Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F);
        playSound(Sound.ENTITY_ENDERDRAGON_DEATH, 2F, 2F);
    }

    @Override
    public void a(EntityLiving entity, float f) {
        if (!ItemWeaponStaff.isStaff(getHeld()))
            return;
        ItemWeaponStaff staff = new ItemWeaponStaff(getHeld());

        LivingEntity target = getGoalTarget() != null ? (LivingEntity) getGoalTarget().getBukkitEntity() : null;
        Projectile proj = DamageAPI.fireStaffProjectile((LivingEntity) this.getBukkitEntity(), staff.getAttributes(), target, staff);
        if (proj != null)
            proj.setVelocity(proj.getVelocity().multiply(1.25));
    }

    private void pushAwayPlayer(Player p, double speed) {
        org.bukkit.util.Vector unitVector = p.getLocation().toVector().subtract(getBukkit().getLocation().toVector()).normalize();
        Material m = p.getLocation().subtract(0, 1, 0).getBlock().getType();

        if (p.getLocation().getY() - 1 <= getBukkit().getLocation().getY() || m == Material.AIR)
            EntityMechanics.setVelocity(p, unitVector.multiply(speed));
    }

    @Override
    public void onBossAttacked(Player attacker) {
        //if player.
        if (attacker.getLocation().distanceSquared(getBukkit().getLocation()) <= 16) {
            pushAwayPlayer(attacker, 2F);
            attacker.setFireTicks(80);
            attacker.playSound(getBukkit().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1F, 1F);
        }

        boolean spawnedGhast = getDungeon().hasSpawned(BossType.InfernalGhast);

        if (spawnedGhast && !this.ghast.isAlive())
            setVulnerable(true);

        if (getPercentHP() <= 0.5D && !spawnedGhast) {
            // Summons the ghast.
            say("Behond, the powers of the inferno!");
            ghast = (InfernalGhast) getDungeon().spawnBoss(BossType.InfernalGhast, getBukkit().getLocation().clone().add(0, 7, 0));
            ghast.init();
            say("The inferno will devour you!");
            //playSound(Sound.ENTITY_GHAST_WARN, 2F, 0.35F);
            setVulnerable(false);
        }

        if (spawnedGhast && !ghast.isAlive())
            return;

        if (random.nextInt(15) == 0) {
            Location loc = getBukkit().getLocation();
            ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SMOKE_LARGE, loc.add(0, 0.5, 0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 1F, 100);

            // Spawn minions
            int minionType = random.nextInt(2);
            EnumMonster monsterType = finalForm ? EnumMonster.Silverfish : EnumMonster.MagmaCube;
            String name = finalForm ? "Abyssal Demon" : "Demonic Spawn of Inferno";
            if (minionType == 1)
                name = (finalForm ? "Greater" : "Demonic") + " " + name;
            spawnMinion(monsterType, name, 3 + minionType, false);
        }
    }
}