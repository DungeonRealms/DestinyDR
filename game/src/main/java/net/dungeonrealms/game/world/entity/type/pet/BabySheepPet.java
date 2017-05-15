package net.dungeonrealms.game.world.entity.type.pet;

import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.ThreadLocalRandom;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Created by Rar349 and iFamasssxD on 5/2/2017.
 */
public class BabySheepPet extends EntitySheep {
    private static Set<Item> gems = new ConcurrentSet<>();
    private static int taskID, maxGemTicks = 100;
    public BabySheepPet(World world) {
        super(world);
        setAge(-1, false); // Set as baby.
        setColor(EnumColor.values()[ThreadLocalRandom.current().nextInt(EnumColor.values().length)]);
        this.ageLocked = true; // Never become an adult.
        if (taskID == 0) {
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                gems.stream().filter(gem -> gem.getTicksLived() >= maxGemTicks).forEach(gem -> {
                    gem.remove();
                    gems.remove(gem);
                });
            }, 20, 20);
        }
    }


    @Override
    protected void r() {
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(2, new PathfinderGoalRandomLookaround(this));
    }

    @Override // Every Tick.
    public void n() {
        super.n();

        if (ticksLived % 25 == 0)
            poopGem();
    }

    @Override
    protected void M() {
        //Calls the growth goal selector to eat grass and shit
    }

    @Override
    public void B() {
        //Disable wool regrowth mechanic
    }

    private void poopGem() { //Poops out an emerald.
        Location loc = getBukkitEntity().getLocation();
        loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, .2F, 1.4F);
        Item gem = loc.getWorld().dropItem(loc, new ItemStack(Material.EMERALD));
        Metadata.NO_PICKUP.set(gem, true);
        gem.setPickupDelay(Integer.MAX_VALUE);
        gems.add(gem);
    }
}
