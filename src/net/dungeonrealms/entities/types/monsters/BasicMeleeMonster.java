package net.dungeonrealms.entities.types.monsters;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.base.DRZombie;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMeleeMonster extends DRZombie {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     */
	public EnumMonster monsterType;
	
    public BasicMeleeMonster(World world, EnumMonster type, int tier) {
        super(world, type, tier, EnumEntityType.HOSTILE_MOB, true);
    }

    public BasicMeleeMonster(World world) {
        super(world);
    }

    @Override
    protected Item getLoot() {
        ItemStack item = BankMechanics.gem.clone();
        item.setAmount(this.random.nextInt(5));
        this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 1, 0), item);
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
