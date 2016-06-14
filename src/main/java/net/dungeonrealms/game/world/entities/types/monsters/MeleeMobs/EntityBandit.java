package net.dungeonrealms.game.world.entities.types.monsters.MeleeMobs;

import net.dungeonrealms.API;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.base.DRZombie;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Created by Chase on Sep 21, 2015
 */
public class EntityBandit extends DRZombie {
    public EntityBandit(World world) {
        super(world);
    }

    /**
     * @param world
     * @param tier
     * @param entityType
     */
    public EntityBandit(World world, int tier, EnumEntityType entityType, EnumMonster monster) {
        super(world, EnumMonster.Bandit, tier, entityType);
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        if (monster == EnumMonster.Bandit) {
            this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.BANDIT.getSkull()));
            livingEntity.getEquipment().setHelmet(SkullTextures.BANDIT.getSkull());
        } else {
            this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.BANDIT_2.getSkull()));
            livingEntity.getEquipment().setHelmet(SkullTextures.BANDIT_2.getSkull());
        }
        checkSpecial();
        setWeapon(tier);
    }

    /**
     *
     */
    private void checkSpecial() {
        if (Utils.randInt(1, 10) == 4) {
            int number = Utils.randInt(1, 3);
            if (number == 1) {
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "poison"));
                this.setCustomName(ChatColor.DARK_GREEN.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Poisonous Bandit");
            } else if (number == 2) {
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "fire"));
                this.setCustomName(ChatColor.DARK_RED.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Firey Bandit");

            } else if (number == 3) {
                this.getBukkitEntity().setMetadata("special", new FixedMetadataValue(DungeonRealms.getInstance(), "ice"));
                this.setCustomName(ChatColor.AQUA.toString() + ChatColor.UNDERLINE.toString() + monsterType.getPrefix() + " Freezing Bandit");

            }
        }
    }

    @Override
    public void setWeapon(int tier) {
        ItemStack weapon = getTierWeapon(tier);
        this.setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
        ((LivingEntity) this.getBukkitEntity()).getEquipment().setItemInMainHand(weapon);
    }

    private ItemStack getTierWeapon(int tier) {
        net.dungeonrealms.game.world.items.Item.ItemType itemType = net.dungeonrealms.game.world.items.Item.ItemType.AXE;
        switch (new Random().nextInt(2)) {
            case 0:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.SWORD;
                break;
            case 1:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.POLEARM;
                break;
            case 2:
                itemType = net.dungeonrealms.game.world.items.Item.ItemType.AXE;
                break;
        }
        ItemStack item = new ItemGenerator().setType(itemType).setRarity(API.getItemRarity(false))
                .setTier(Item.ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }


    @Override
    public EnumMonster getEnum() {
        return this.monsterType;
    }


    @Override
    public void setStats() {

    }
}
