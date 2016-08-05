package net.dungeonrealms.game.world.entity.type.monster.MeleeMobs;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRZombie;
import net.dungeonrealms.game.world.item.Item;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Kieran Quigley (Proxying) on 14-Jun-16.
 */
public class MeleeZombie extends DRZombie {

	public EnumMonster monsterType;
	
    public MeleeZombie(World world, EnumMonster type, int tier) {
        super(world, type, tier, EnumEntityType.HOSTILE_MOB);
        setWeapon(tier);
    }

    public MeleeZombie(World world) {
        super(world);
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        ((LivingEntity) this.getBukkitEntity()).getEquipment().setItemInMainHand(weapon);
    }

    private ItemStack getTierWeapon(int tier) {
        Item.ItemType itemType;
        switch (new Random().nextInt(3)) {
            case 0:
                itemType = Item.ItemType.SWORD;
                break;
            case 1:
                itemType = Item.ItemType.POLEARM;
                break;
            case 2:
                itemType = Item.ItemType.AXE;
                break;
            default:
                itemType = Item.ItemType.SWORD;
                break;
        }
        ItemStack item = new ItemGenerator().setType(itemType).setRarity(GameAPI.getItemRarity(false))
                .setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiDuplication.getInstance().applyAntiDupe(item);
        return item;
    }

	@Override
	public EnumMonster getEnum() {
		return this.monsterType;
	}

    @Override
    protected void setStats() {
    }

}
