package net.dungeonrealms.game.world.entity.type.pet;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_9_R2.util.CraftMagicNumbers;
import org.bukkit.event.block.EntityBlockFormEvent;

/**
 * SnowmanPets
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class SnowmanPet extends EntitySnowman {

    public SnowmanPet(World world) {
        super(world);
        getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(.45F);
    }

    @Override //Places snow behind it. TODO: Why do we override this? What's wrong with the default method? This looks like it's just a modified version of n().
    public void m() {
        super.m();
        if(!this.world.isClientSide) {
            for(int l = 0; l < 4; ++l) {
                int i = MathHelper.floor(this.locX + (double)((float)(l % 2 * 2 - 1) * 0.25F));
                int j = MathHelper.floor(this.locY);
                int k = MathHelper.floor(this.locZ + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
                BlockPosition blockposition = new BlockPosition(i, j, k);
                if(this.world.getType(blockposition).getMaterial() == Material.AIR && this.world.getBiome(new BlockPosition(i, 0, k)).a(blockposition) < 0.8F && Blocks.SNOW_LAYER.canPlace(this.world, blockposition)) {
                    BlockState blockState = this.world.getWorld().getBlockAt(i, j, k).getState();
                    blockState.setType(CraftMagicNumbers.getMaterial(Blocks.SNOW_LAYER));
                    EntityBlockFormEvent event = new EntityBlockFormEvent(this.getBukkitEntity(), blockState.getBlock(), blockState);
                    this.world.getServer().getPluginManager().callEvent(event);
                    if(!event.isCancelled())
                        blockState.update(true);
                }
            }
        }

    }
}
