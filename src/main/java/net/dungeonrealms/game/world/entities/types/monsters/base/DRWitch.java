package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.DamageAPI;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Kieran Quigley (Proxying) on 09-Jun-16.
 */
public class DRWitch extends EntityWitch implements DRMonster {

    EnumMonster monster;
    int tier;
    net.minecraft.server.v1_8_R3.ItemStack nmsItem;

    public DRWitch(World world) {
        super(world);
    }

    public DRWitch(World world, EnumMonster mon, int tier) {
        super(world);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(18d);
        setArmor(tier);
        tier = tier;
        monster = mon;
        String customName = mon.getPrefix() + " " + mon.name + " " + mon.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
    }

    private void setArmor(int tier) {
        ItemStack[] armor = API.getTierArmor(tier);
        boolean armorMissing = false;
        if (random.nextInt(10) <= 5) {
            ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
            this.setEquipment(1, CraftItemStack.asNMSCopy(armor0));
        } else {
            armorMissing = true;
        }
        if (random.nextInt(10) <= 5 || armorMissing) {
            ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
            this.setEquipment(2, CraftItemStack.asNMSCopy(armor1));
            armorMissing = false;
        } else {
            armorMissing = true;
        }
        if (random.nextInt(10) <= 5 || armorMissing) {
            ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
            this.setEquipment(3, CraftItemStack.asNMSCopy(armor2));
        }

        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
        nmsItem = CraftItemStack.asNMSCopy(weapon);
        //this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(4, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setTier(Item.ItemTier.getByTier(tier)).setType(Item.ItemType.STAFF)
                .setRarity(API.getItemRarity(false)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

    @Override
    protected String z() {
        return "";
    }

    @Override
    protected String bo() {
        return "game.player.hurt";
    }

    @Override
    protected String bp() {
        return "";
    }

    @Override
    public void onMonsterAttack(Player p) {
    }

    @Override
    public void onMonsterDeath(Player killer) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()-> {
            this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monster, this.getBukkitEntity(), killer);
            if (this.random.nextInt(100) < 33) {
                this.getRareDrop();
            }
        });
    }

    @Override
    public EnumMonster getEnum() {
        return null;
    }

    @Override
    public void a(EntityLiving entity, float f) {
        NBTTagCompound tag = nmsItem.getTag();
        DamageAPI.fireStaffProjectileMob((CraftLivingEntity) this.getBukkitEntity(), tag, (CraftLivingEntity) entity.getBukkitEntity());
    }
}
