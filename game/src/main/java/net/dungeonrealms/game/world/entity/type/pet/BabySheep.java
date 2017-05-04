package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Rar349 on 5/2/2017.
 */
public class BabySheep extends EntitySheep {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;
    private static ItemStack gemStack;


    private static ConcurrentHashMap<Item, Long> gemCache = new ConcurrentHashMap<>();
    private static boolean startedTask = false;

    public BabySheep(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
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

        if (gemStack == null) {
            gemStack = new ItemStack(Material.EMERALD, 1);
        }

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);

        if(!startedTask) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                for (Map.Entry<Item, Long> entry : gemCache.entrySet()) {
                    Item key = entry.getKey();
                    Long value = entry.getValue();
                    if(key == null || value == null) continue;
                    if(value < System.currentTimeMillis()) {
                        gemCache.remove(key);
                        key.remove();
                    }
                }
            },20,20);
            startedTask = true;
        }
    }

    public BabySheep(World world) {
        super(world);
    }

    @Override
    public void n() {
        super.n();

        if (ticksLived % 15 == 0) {
            getBukkitEntity().getWorld().playSound(getBukkitEntity().getLocation(), Sound.ENTITY_CHICKEN_EGG, .3F, 1.5F);
            Item gemEntity = this.getBukkitEntity().getWorld().dropItem(this.getBukkitEntity().getLocation(), gemStack.clone());
            gemEntity.setMetadata("no_pickup", new FixedMetadataValue(DungeonRealms.getInstance(), "69"));
            gemEntity.setPickupDelay(Integer.MAX_VALUE);
            gemCache.put(gemEntity, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3));
        }

    }
}
