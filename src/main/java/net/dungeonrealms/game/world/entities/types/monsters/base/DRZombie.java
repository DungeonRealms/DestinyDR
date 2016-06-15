package net.dungeonrealms.game.world.entities.types.monsters.base;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ParticleAPI;
import net.dungeonrealms.game.miscellaneous.SkullTextures;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.DRMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.minecraft.server.v1_9_R2.EntityZombie;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
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
    
    protected DRZombie(World world, EnumMonster monster, int tier, EnumEntityType entityType) {
        this(world);
        this.tier  = tier;
        this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(16d);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.29D);
        this.monsterType = monster;
        this.name = monster.name;
        this.mobHead = monster.mobHead;
        this.entityType = entityType;
        setArmor(tier);
        setStats();
        String customName = monster.getPrefix().trim() + " " + name.trim() + " " + monster.getSuffix().trim();
        this.setCustomName(customName);
        this.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        switch (monsterType) {
            case Troll:
            case Troll1:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.TROLL.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.TROLL.getSkull());
                break;
            case Goblin:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.GOBLIN.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.GOBLIN.getSkull());
                break;
            case Naga:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.NAGA.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.NAGA.getSkull());
                break;
            case Lizardman:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.LIZARD.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.LIZARD.getSkull());
                break;
            case Zombie:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.ZOMBIE.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.ZOMBIE.getSkull());
                break;
            case Monk:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.MONK.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.MONK.getSkull());
                break;
            case Tripoli:
            case Tripoli1:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.TRIPOLI_SOLDIER.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.TRIPOLI_SOLDIER.getSkull());
                break;
            case Undead:
                if (random.nextBoolean()) {
                    this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.ZOMBIE.getSkull()));
                    livingEntity.getEquipment().setHelmet(SkullTextures.ZOMBIE.getSkull());
                } else {
                    this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.SKELETON.getSkull()));
                    livingEntity.getEquipment().setHelmet(SkullTextures.SKELETON.getSkull());
                }
                break;
            case Mage:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.MAGE.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.MAGE.getSkull());
                break;
            case Daemon2:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.DEVIL.getSkull());
                break;
            case FireImp:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.DEVIL.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.DEVIL.getSkull());
                break;
            case MayelPirate:
            case Pirate:
                this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.PIRATE.getSkull()));
                livingEntity.getEquipment().setHelmet(SkullTextures.PIRATE.getSkull());
                break;
            case Bandit:
            case Bandit1:
                if (random.nextBoolean()) {
                    this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.BANDIT.getSkull()));
                    livingEntity.getEquipment().setHelmet(SkullTextures.BANDIT.getSkull());
                } else {
                    this.setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(SkullTextures.BANDIT_2.getSkull()));
                    livingEntity.getEquipment().setHelmet(SkullTextures.BANDIT_2.getSkull());
                }
                break;
            default:
                break;
        }
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
        LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
        boolean armorMissing = false;
        if (random.nextInt(10) <= 5) {
            ItemStack armor0 = AntiCheat.getInstance().applyAntiDupe(armor[0]);
            livingEntity.getEquipment().setBoots(armor0);
            this.setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor0));
        } else {
            armorMissing = true;
        }
        if (random.nextInt(10) <= 5 || armorMissing) {
            ItemStack armor1 = AntiCheat.getInstance().applyAntiDupe(armor[1]);
            livingEntity.getEquipment().setLeggings(armor1);
            this.setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor1));
            armorMissing = false;
        } else {
            armorMissing = true;
        }
        if (random.nextInt(10) <= 5 || armorMissing) {
            ItemStack armor2 = AntiCheat.getInstance().applyAntiDupe(armor[2]);
            livingEntity.getEquipment().setChestplate(armor2);
            this.setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor2));
        }
    }

    public void setWeapon(int tier) {

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
		});
	}
}
