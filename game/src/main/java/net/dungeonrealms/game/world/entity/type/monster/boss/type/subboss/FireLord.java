package net.dungeonrealms.game.world.entity.type.monster.boss.type.subboss;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.world.entity.type.monster.DRMonster;
import net.dungeonrealms.game.world.entity.type.monster.base.DRBlaze;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.inventory.ItemStack;

public class FireLord extends DRBlaze implements DRMonster {

    public FireLord(World world) {
        super(world);
        setTier(4);
        setGear();
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(45);
    }

    @Override
    public void setGear() {
        super.setGear();

        ItemArmor am = new ItemArmor();
        am.setTier(4).setRarity(Item.ItemRarity.RARE);
        getBukkit().getEquipment().setArmorContents(am.generateArmorSet());
    }

    @Override
    public ItemStack getWeapon() {
        return makeItem(new ItemWeapon(ItemType.SWORD));
    }

}
