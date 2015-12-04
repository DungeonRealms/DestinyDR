package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.miscellaneous.RandomHelper;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.Monster;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemType;
import net.dungeonrealms.game.world.items.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Chase on Sep 19, 2015
 */
public abstract class DRSkeleton extends EntitySkeleton implements Monster{
    private String name;
    private String mobHead;
    protected EnumEntityType entityType;
    protected EnumMonster monsterType;
    
    /**
     * @param world
     */
    protected DRSkeleton(World world, EnumMonster monster, int tier, EnumEntityType entityType) {
        super(world);
//        this.goalSelector.a(1, new PathfinderGoalFloat(this));
//        this.goalSelector.a(4, new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F));
//        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
//        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
//        this.targetSelector.a(5, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, true));
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(16d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        monsterType = monster;
        this.name = monster.name;
        this.mobHead = monster.mobHead;
        this.entityType = entityType;
        setArmor(tier);
        this.getBukkitEntity().setCustomNameVisible(true);
        String customName = monster.getPrefix() + " " + name + " " + monster.getSuffix() + " ";
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        setStats();
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
        
        ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
        ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
        ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);

        this.setEquipment(0, CraftItemStack.asNMSCopy(weapon));
        this.setEquipment(1, CraftItemStack.asNMSCopy(armor0));
        this.setEquipment(2, CraftItemStack.asNMSCopy(armor1));
        this.setEquipment(3, CraftItemStack.asNMSCopy(armor2));
        this.setEquipment(4, this.getHead());
    }

    protected String getCustomEntityName() {
        return this.name;
    }

    protected net.minecraft.server.v1_8_R3.ItemStack getHead() {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(mobHead);
        head.setItemMeta(meta);
        return CraftItemStack.asNMSCopy(head);
    }

    private ItemStack getTierWeapon(int tier) {
    	ItemStack item = new ItemGenerator().next(net.dungeonrealms.game.world.items.Item.ItemType.values()[RandomHelper.getRandomNumberBetween(0, ItemType.values().length - 1)], net.dungeonrealms.game.world.items.Item.ItemTier.getByTier(tier), API.getItemModifier());
        AntiCheat.getInstance().applyAntiDupe(item);
        item.addEnchantment(Enchantment.KNOCKBACK, 1);
        return item;
    }

    
    @Override
	public void onMonsterAttack(Player p) {
    	
    	
    }
    
	@Override
	public abstract EnumMonster getEnum();

	@Override
	public void onMonsterDeath() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), ()->{
		this.checkItemDrop(this.getBukkitEntity().getMetadata("tier").get(0).asInt(), monsterType, this.getBukkitEntity());
		if(this.random.nextInt(100) < 33)
			this.getRareDrop();
		});
	}
}
