package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rar349 on 5/2/2017.
 */
public class RainbowSheep extends EntitySheep {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public RainbowSheep(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;
        this.setAge(0);
        this.ageLocked = true;


        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);

    }

    public RainbowSheep(World world) {
        super(world);
    }

    @Override
    public void n() {
        super.n();

        if (ticksLived % 10 == 0) {
            ((Sheep)this.getBukkitEntity()).setColor(getRandomDyeColor(((Sheep)this.getBukkitEntity()).getColor()));
        }

    }

    public DyeColor getRandomDyeColor(DyeColor toExclude) {
        DyeColor toCheck = DyeColor.values()[ThreadLocalRandom.current().nextInt(DyeColor.values().length)];
        if(toCheck == toExclude) return getRandomDyeColor(toExclude);
        return toCheck;

    }
}
