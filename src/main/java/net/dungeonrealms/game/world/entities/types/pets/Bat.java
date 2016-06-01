package net.dungeonrealms.game.world.entities.types.pets;

import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.minecraft.server.v1_8_R3.EntityBat;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 28-May-16.
 */
public class Bat extends EntityBat {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;
    private Player target;
    private int repetitions;

    public Bat(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;
        this.target = Bukkit.getPlayer(ownerUUID);

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Bat(World world) {
        super(world);
    }

    @Override
    public void E() {
        if (getBukkitEntity().getLocation().distanceSquared(target.getLocation()) < 3) {
            return;
        }
        repetitions++;
        if (repetitions % 5 == 0) {
            if (repetitions == 20) {
                repetitions = 0;
            }
            return;
        }
        if (target == null) {
            die();
            return;
        }
        if (!target.isOnline()) {
            die();
            return;
        }
        setAsleep(false);

        double dX = target.getLocation().getX() - locX;
        double dY = target.getLocation().getY() + 1 - locY;
        double dZ = target.getLocation().getZ() - locZ;

        Vector posVec = new Vector(dX + 1, dY + 1, dZ + 1);
        posVec.normalize();

        motX = posVec.getX() * 0.3;
        motY = posVec.getY() * 0.3;
        motZ = posVec.getZ() * 0.3;

        Vector dirVec = new Vector(dX, dY, dZ);
        dirVec.multiply(1 / dirVec.length());

        yaw = target.getLocation().getYaw();
        pitch = target.getLocation().getPitch();
    }
}
