package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Sep 19, 2015
 */
public abstract class DRSkeleton extends EntitySkeleton implements DRMonster {
    private String name;
    private String mobHead;
    protected EnumEntityType entityType;
    protected EnumMonster monsterType;
    
    /**
     * @param world
     */
    protected DRSkeleton(World world, EnumMonster monster, int tier, EnumEntityType entityType) {
        super(world);
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(18d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        monsterType = monster;
        this.name = monster.name;
        this.mobHead = monster.mobHead;
        this.entityType = entityType;
        setArmor(tier);
        if (this.getEquipment(0) != null && this.getEquipment(0).hasTag()) {
            if (this.getEquipment(0).getTag().hasKey("itemType") && this.getEquipment(0).getTag().getInt("itemType") == 3) {
                this.goalSelector.a(1, new PathfinderGoalFloat(this));
                this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
                this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
                this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
                this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
            }
        }
        this.getBukkitEntity().setCustomNameVisible(true);
        String customName = monster.getPrefix() + " " + name + " " + monster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        setStats();
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
    }

    @Override
    protected void getRareDrop(){
    	this.world.getWorld().dropItemNaturally(this.getBukkitEntity().getLocation().add(0, 1, 0), new ItemStack(Material.ARROW, RandomHelper.getRandomNumberBetween(2, 5)));
    }
    
    protected DRSkeleton(World world) {
        super(world);
    }

    @Override
    public abstract void a(EntityLiving entityliving, float f);

    protected abstract void setStats();

    public void setArmor(int tier) {
        ItemStack[] armor = API.getTierArmor(tier);
        // weapon, boots, legs, chest, helmet/head
        ItemStack weapon = getTierWeapon(tier);
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
        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
    }

    protected String getCustomEntityName() {
        return this.name;
    }

    private ItemStack getTierWeapon(int tier) {
        ItemStack item = new ItemGenerator().setType(ItemType.getRandomWeapon()).setRarity(API.getItemRarity(false))
                .setTier(ItemTier.getByTier(tier)).generateItem().getItem();
        AntiCheat.getInstance().applyAntiDupe(item);
        return item;
    }

    
    @Override
	public void onMonsterAttack(Player p) {
    	
    	
    }
    
	@Override
	public abstract EnumMonster getEnum();

	@Override
	public void onMonsterDeath(Player killer) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity(), killer);
		if(this.random.nextInt(100) < 33)
			this.getRareDrop();
		});
	}
}
