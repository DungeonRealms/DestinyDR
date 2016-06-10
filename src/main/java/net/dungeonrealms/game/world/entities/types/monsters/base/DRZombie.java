package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.items.Item;
import net.dungeonrealms.game.world.items.Item.ItemTier;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * Created by Xwaffle on 8/29/2015.
 */

public abstract class DRZombie extends EntityZombie implements DRMonster {

    protected String name;
    protected String mobHead;
    protected EnumEntityType entityType;
    protected EnumMonster monsterType;
    public int tier;
    @Override
    protected void getRareDrop(){
    }
    
    protected DRZombie(World world, EnumMonster monster, int tier, EnumEntityType entityType, boolean setArmor) {
        this(world);
        this.tier  = tier;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(16d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.getAttributeInstance(GenericAttributes.c).setValue(0.75d);
        this.monsterType = monster;
        this.name = monster.name;
        this.mobHead = monster.mobHead;
        this.entityType = entityType;
        if (setArmor) {
            setArmor(tier);
        }
        setStats();
        String customName = monster.getPrefix().trim() + " " + name.trim() + " " + monster.getSuffix().trim();
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, 1.0D));
    }

    protected DRZombie(World world) {
        super(world);
    }

    protected abstract void setStats();


    protected String getCustomEntityName() {
        return this.name;
    }

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

    private ItemStack getTierWeapon(int tier) {
        Item.ItemType itemType = Item.ItemType.AXE;
        switch (new Random().nextInt(2)) {
            case 0:
                itemType = Item.ItemType.SWORD;
                break;
            case 1:
                itemType = Item.ItemType.POLEARM;
                break;
            case 2:
                itemType = Item.ItemType.AXE;
                break;
        }
        ItemStack item = new ItemGenerator().setType(itemType).setRarity(API.getItemRarity(false))
                .setTier(ItemTier.getByTier(tier)).generateItem().getItem();
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
		if(this.getBukkitEntity().hasMetadata("special")){
				switch(this.getBukkitEntity().getMetadata("special").get(0).asString()){
				case "poison":
	            	switch (this.tier) {
	            	case 1:
	            		p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30, 0));
	            		break;
	            	case 2:
	            		p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
	            		break;
	            	case 3:
	            		p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 0));
	            		break;
	            	case 4:
	            		p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
	            		break;
	            	case 5:
	            		p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
	            		break;
	            	default :
            		p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
	            	}
            	case "ice" :
            	      try {
                          ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SNOWBALL_POOF, p.getLocation(),
                                  new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                      } catch (Exception ex) {
                          ex.printStackTrace();
                      }
                      switch (tier) {
                          case 1:
                              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0));
                              break;
                          case 2:
                              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                              break;
                          case 3:
                              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                              break;
                          case 4:
                        	  p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                              break;
                          case 5:
                              p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 1));
                              break;
                      }
            	case "fire":
            	      try {
                          ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FLAME, p.getLocation(),
                                  new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                      } catch (Exception ex) {
                          ex.printStackTrace();
                      }
                      switch (tier) {
                          case 1:
                              p.setFireTicks(15);
                              break;
                          case 2:
                              p.setFireTicks(25);
                              break;
                          case 3:
                              p.setFireTicks(30);
                              break;
                          case 4:
                              p.setFireTicks(35);
                              break;
                          case 5:
                              p.setFireTicks(40);
                              break;
                      }
				}
			}
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
