package net.dungeonrealms.items;

import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.Item;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

/**
 * Created by Kieran on 9/27/2015.
 */
public class DRBow extends ItemBow {

    public static final String[] a = new String[]{"pulling_0", "pulling_1", "pulling_2"};

    public DRBow() {
        this.c("bow");
        this.setMaxDurability(384);
        this.maxStackSize = 1;
    }

    @Override
    public void a(ItemStack itemstack, World world, EntityHuman entityhuman, int i) {
        boolean flag = true;
        if (flag) {
            int j = this.d(itemstack) - i;
            float f = (float) j / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if ((double) f < 0.1D) {
                return;
            }

            if (f > 1.0F) {
                f = 1.0F;
            }

            EntityArrow entityarrow = new EntityArrow(world, entityhuman, f * 2.0F);
            if (f == 1.0F) {
                entityarrow.setCritical(true);
            }

            int k = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, itemstack);
            if (k > 0) {
                entityarrow.b(entityarrow.j() + (double) k * 0.5D + 0.5D);
            }

            int l = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, itemstack);
            if (l > 0) {
                entityarrow.setKnockbackStrength(l);
            }

            if (EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, itemstack) > 0) {
                EntityCombustEvent event = new EntityCombustEvent(entityarrow.getBukkitEntity(), 100);
                entityarrow.world.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    entityarrow.setOnFire(event.getDuration());
                }
            }

            EntityShootBowEvent event1 = CraftEventFactory.callEntityShootBowEvent(entityhuman, itemstack, entityarrow, f);
            if (event1.isCancelled()) {
                event1.getProjectile().remove();
                return;
            }

            if (event1.getProjectile() == entityarrow.getBukkitEntity()) {
                world.addEntity(entityarrow);
            }

            itemstack.damage(0, entityhuman);
            world.makeSound(entityhuman, "random.bow", 1.0F, 1.0F / (g.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
            if (flag) {
                entityarrow.fromPlayer = 2;
            } else {
                entityhuman.inventory.a(Items.ARROW);
            }

            entityhuman.b(StatisticList.USE_ITEM_COUNT[Item.getId(this)]);
            boolean var10000 = world.isClientSide;
        }

    }

    @Override
    public ItemStack b(ItemStack itemstack, World world, EntityHuman entityhuman) {
        return itemstack;
    }

    @Override
    public int d(ItemStack itemstack) {
        return 72000;
    }

    @Override
    public EnumAnimation e(ItemStack itemstack) {
        return EnumAnimation.BOW;
    }

    @Override
    public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
        ((EntityPlayer) entityhuman).playerConnection.sendPacket(new PacketPlayOutSetSlot(0, 9, new net.minecraft.server.v1_8_R3.ItemStack(Items.ARROW)));
        entityhuman.a(itemstack, this.d(itemstack));
        return itemstack;
    }

    @Override
    public int b() {
        return 1;
    }


}
