package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.minecraft.server.v1_9_R2.EntitySheep;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 5/2/2017.
 */
public class BabySheepPet extends EntitySheep {

    public BabySheepPet(World world) {
        super(world);
        setAge(-1); // Set as baby.
        this.ageLocked = true; // Never become an adult.
    }


    @Override // Every Tick.
    public void n() {
        super.n();

        if (ticksLived % 15 == 0)
            poopGem();
    }
    
    private void poopGem() { //Poops out an emerald.
    	Location loc = getBukkitEntity().getLocation();
    	loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, .3F, 1.5F);
    	Item gem = loc.getWorld().dropItem(loc, new ItemStack(Material.EMERALD));
    	Metadata.NO_PICKUP.set(gem, true);
    	gem.setPickupDelay(Integer.MAX_VALUE);
    	
    	// Remove the item in a second.
    	Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), gem::remove, 20L);
    }
}
