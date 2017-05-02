package net.dungeonrealms.game.world.entity.type.pet;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BetaZombie extends BabyZombie {

    @Getter
    @Setter
    private long lastBrainEat = 0L;

    @Getter
    @Setter
    private int lastTick = 0;

    public BetaZombie(net.minecraft.server.v1_9_R2.World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world, mobName, ownerUUID, entityType);
    }

    @Override
    protected void r() {
        this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    public void n() {

        super.n();
        if(isPassenger() && this.getVehicle().isAlive()){
            this.setYawPitch(getVehicle().yaw, 90);
        }
        if (lastTick++ % 10 != 0) return;

        if (lastTick % 15 == 0) {
            Player owner = Bukkit.getPlayer(getOwnerUUID());
            if (owner == null) {
                this.getBukkitEntity().remove();
                return;
            }
            if (owner.getWorld() != this.getBukkitEntity().getWorld() || (getBukkitEntity().getLocation().distanceSquared(owner.getLocation()) > 15)) {
                if (this.isPassenger()) {
                    this.getBukkitEntity().eject();
                }
                this.getBukkitEntity().teleport(owner.getLocation());
            }
        }
        if (this.isPassenger()) {
            long time = System.currentTimeMillis() - this.getLastBrainEat();
            if (time >= 750) {
                getBukkitEntity().getWorld().playSound(this.getBukkitEntity().getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1.2F);
                if (time >= 1500) {
                    this.getBukkitEntity().getWorld().playEffect(this.getBukkitEntity().getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
                    setLastBrainEat(System.currentTimeMillis());
                }
            }
            return;
        }
        for (Entity near : this.getBukkitEntity().getNearbyEntities(5, 5, 5)) {
            if (!(near instanceof Player)) continue;
            Player player = (Player) near;
            if(player.hasMetadata("NPC"))continue;
            if (player.getUniqueId().equals(getOwnerUUID())) continue;
            if (player.getPassenger() != null) continue;
            if (this.getBukkitEntity().getVehicle() != null && ThreadLocalRandom.current().nextInt(50) != 5) continue;
            player.setPassenger(this.getBukkitEntity());
        }

    }

}
