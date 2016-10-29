package net.dungeonrealms.old.game.world.entity.type.pet;

import net.dungeonrealms.old.game.mastery.MetadataUtils;
import net.dungeonrealms.old.game.world.entity.EnumEntityType;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_9_R2.util.CraftMagicNumbers;
import org.bukkit.event.block.EntityBlockFormEvent;

import java.util.UUID;

/**
 * Created by Kieran on 9/25/2015.
 */
public class Snowman extends EntitySnowman {

    private String mobName;
    private UUID ownerUUID;
    private EnumEntityType entityType;

    public Snowman(World world, String mobName, UUID ownerUUID, EnumEntityType entityType) {
        super(world);
        this.mobName = mobName;
        this.ownerUUID = ownerUUID;
        this.entityType = entityType;
        this.getBukkitEntity().setCustomNameVisible(true);
        this.getBukkitEntity().setCustomName(mobName);
        this.canPickUpLoot = false;
        this.persistent = true;

        MetadataUtils.registerEntityMetadata(this, this.entityType, 0, 0);
    }

    public Snowman(World world) {
        super(world);
    }

    @Override
    public void m() {
        super.m();
        if(!this.world.isClientSide) {
            int i;
            int j;
            int k;
            for(int l = 0; l < 4; ++l) {
                i = MathHelper.floor(this.locX + (double)((float)(l % 2 * 2 - 1) * 0.25F));
                j = MathHelper.floor(this.locY);
                k = MathHelper.floor(this.locZ + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
                BlockPosition blockposition = new BlockPosition(i, j, k);
                if(this.world.getType(blockposition).getMaterial() == Material.AIR && this.world.getBiome(new BlockPosition(i, 0, k)).a(blockposition) < 0.8F && Blocks.SNOW_LAYER.canPlace(this.world, blockposition)) {
                    BlockState blockState = this.world.getWorld().getBlockAt(i, j, k).getState();
                    blockState.setType(CraftMagicNumbers.getMaterial(Blocks.SNOW_LAYER));
                    EntityBlockFormEvent event = new EntityBlockFormEvent(this.getBukkitEntity(), blockState.getBlock(), blockState);
                    this.world.getServer().getPluginManager().callEvent(event);
                    if(!event.isCancelled()) {
                        blockState.update(true);
                    }
                }
            }
        }

    }
}
