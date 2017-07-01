package net.dungeonrealms.game.world.entity.type.pet;

import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.EntityCreeper;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * CreeperPet - A creeper pet.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class CreeperPet extends EntityCreeper {

    private long lastFireWork = 0L;
    private int numShot = 0;

    public CreeperPet(World world) {
        super(world);
        setPowered(true);
    }

    @Override
    protected void r() {
    	//Prevents registering default AI goals.
    }

    public boolean isOnCooldown() {
        return System.currentTimeMillis() - lastFireWork <= (numShot <= 2 ? Utils.randInt(250) + 250 : Utils.randInt(1000) + 5000);
    }

    @Override
    public void n() {
        super.n();
        if(isOnCooldown()) return;
        Firework fw = (Firework) getBukkitEntity().getWorld().spawnEntity(getBukkitEntity().getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLUE, Color.RED).withFade(Color.WHITE).with(FireworkEffect.Type.STAR).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(Utils.randInt(2) + 1);
        fw.setFireworkMeta(fwm);
        lastFireWork = System.currentTimeMillis();
        numShot++;
        if(numShot > 3) numShot = 0;
    }
}
