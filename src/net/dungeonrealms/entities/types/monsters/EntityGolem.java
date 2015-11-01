package net.dungeonrealms.entities.types.monsters;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.base.DRZombie;
import net.dungeonrealms.items.ItemGenerator;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Sep 28, 2015
 */
public class EntityGolem extends DRZombie {

    public EntityGolem(World world, int tier, EnumEntityType entityType) {
        super(world, EnumMonster.Golem, tier, entityType, true);
        this.setEquipment(0, CraftItemStack.asNMSCopy(new ItemGenerator().next()));
    }

    /**
     * @param world
     */
    public EntityGolem(World world) {
        super(world);
    }

    @Override
    protected Item getLoot() {
        ItemStack item = BankMechanics.gem.clone();
        item.setAmount(this.random.nextInt(5));
        this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation(), item);
        return null;
    }
    
	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

    @Override
    protected void getRareDrop() {

    }

    @Override
    protected void setStats() {

    }

}
