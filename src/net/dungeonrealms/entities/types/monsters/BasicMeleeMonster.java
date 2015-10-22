package net.dungeonrealms.entities.types.monsters;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.entities.types.MeleeEntityZombie;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Chase on Oct 2, 2015
 */
public class BasicMeleeMonster extends MeleeEntityZombie {

    /**
     * @param world
     * @param mobName
     * @param mobHead
     * @param tier
     */
	public EnumMonster monsterType;
	
    public BasicMeleeMonster(World world, EnumMonster type, int tier) {
        super(world, type, tier, EnumEntityType.HOSTILE_MOB, true);
        int level = Utils.getRandomFromTier(tier);
        MetadataUtils.registerEntityMetadata(this, entityType, tier, level);
        EntityStats.setMonsterRandomStats(this, level, tier);
        this.getBukkitEntity().setCustomName(ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "
				+ ChatColor.RESET + type.getPrefix() + " " + type.name + " " + type.getSuffix());
    }

    public BasicMeleeMonster(World world) {
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
    protected void getRareDrop() {

    }

    @Override
    protected void setStats() {

    }

}
