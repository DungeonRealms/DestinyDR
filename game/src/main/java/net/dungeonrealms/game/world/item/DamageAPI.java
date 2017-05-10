package net.dungeonrealms.game.world.item;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerToggles.Toggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.guild.database.GuildDatabase;
import net.dungeonrealms.game.handler.EnergyHandler;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemWeapon;
import net.dungeonrealms.game.item.items.core.ItemWeaponBow;
import net.dungeonrealms.game.item.items.core.ItemWeaponPolearm;
import net.dungeonrealms.game.item.items.core.ItemWeaponStaff;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.listener.combat.AttackResult.CombatEntity;
import net.dungeonrealms.game.listener.combat.DamageResultType;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.player.duel.DuelingMechanics;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.PowerMove;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.minecraft.server.v1_9_R2.EntityArrow;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Kieran on 9/21/2015.
 */
public class DamageAPI {

    private static HashMap<Player, HashMap<Hologram, Integer>> DAMAGE_HOLOGRAMS = new HashMap<Player, HashMap<Hologram, Integer>>();

    public static void calculateWeaponDamage(AttackResult res, boolean removeDurability) {
        CombatEntity attacker = res.getAttacker();
        CombatEntity defender = res.getDefender();
        
        ItemStack item = attacker.getEntity().getEquipment().getItemInMainHand();
        if (!ItemWeapon.isWeapon(item))
        	return;
        
        int weaponTier = 0;
        
        //  BASE DAMAGE  //
        double damage = attacker.getAttributes().getAttribute(WeaponAttributeType.DAMAGE).getValueInRange();
        int critHit = 0;
        
        if (!res.hasProjectile()) {
        	//  MELEE WEAPON  //
        	ItemWeapon weapon = (ItemWeapon)PersistentItem.constructItem(item);
        	weaponTier = weapon.getTier().getId();
        
        	if (attacker.isPlayer()) {
        	
        		//  DAMAGE WEAPON  //
        		if (removeDurability) {
        			int durabilityLoss = 1;
        			
        			//  EXTRA DAMAGE FOR TIER GAPS  //
        			int mobTier = EntityAPI.getTier(defender.getEntity());
        			
        			int tierDif = weaponTier - mobTier;
        			if (tierDif > 1)
        				durabilityLoss = 2 * (tierDif - 1);
        			
        			weapon.damageItem(attacker.getPlayer(), durabilityLoss);
        		}
        		
        		PlayerWrapper.getWrapper(attacker.getPlayer()).updateWeapon();
        		
                //  STAT BONUS  //
                ItemType type = weapon.getItemType();
                
                
                if (type == ItemType.AXE) {
                	critHit += 3;
                } else if (type == ItemType.SWORD) {
                	damage += (damage / 100) * (attacker.getAttributes().getAttribute(ArmorAttributeType.VITALITY).getValue() * 0.23);
                } else if (type == ItemType.POLEARM) {
                	damage += (damage / 100) * (attacker.getAttributes().getAttribute(ArmorAttributeType.STRENGTH).getValue() * 0.23);
                }
        	}
        } else {
        	
        	//  STAT BONUS  //
            switch (res.getProjectile().getType()) {
                case ARROW:
                case TIPPED_ARROW:
                    damage += (damage / 100) * (attacker.getAttributes().getAttribute(ArmorAttributeType.DEXTERITY).getValue() * 0.15);
                    break;
                case SNOWBALL:
                case SMALL_FIREBALL:
                case ENDER_PEARL:
                case FIREBALL:
                case WITHER_SKULL:
                    damage += (damage / 100) * (attacker.getAttributes().getAttribute(ArmorAttributeType.INTELLECT).getValue() * 0.2);
                    break;
                default:
                    break;
            }
        	
        }
        
        //  CRIT  //
        critHit += attacker.getAttributes().getAttribute(WeaponAttributeType.CRITICAL_HIT).getValue();
        boolean isHitCrit = false;
        
        
        //  VS MONSTERS AND PLAYERS  //
        WeaponAttributeType vsEntity = defender.isPlayer() ? WeaponAttributeType.VS_PLAYER : WeaponAttributeType.VS_MONSTERS;
        damage += ((double)attacker.getAttributes().getAttribute(vsEntity).getValue() / 100) * damage;
        
        //  EXECUTE ATTACK HOOK  //
        if (defender.isPlayer() && EnumEntityType.HOSTILE_MOB.isType(attacker.getEntity()) && EntityAPI.isMonster(attacker.getEntity()))
        	EntityAPI.getMonster(attacker.getEntity()).onMonsterAttack(defender.getPlayer());
        
        //  DPS  //
        damage += damage * ((double)attacker.getAttributes().getAttribute(ArmorAttributeType.DAMAGE).getValueInRange() / 100);

        //  KNOCKBACK  //
        if (attacker.isPlayer() && getChance(attacker.getAttributes(), WeaponAttributeType.KNOCKBACK))
        	knockbackEntity(attacker.getPlayer(), defender.getEntity(), 1.5);

        //  BLIND  //
        if (getChance(attacker.getAttributes(), WeaponAttributeType.BLIND))
        	applyBlind(defender.getEntity(), weaponTier);
            
        //  SLOW  //
        if (getChance(attacker.getAttributes(), WeaponAttributeType.SLOW))
        	applySlow(defender.getEntity());

        //  ELEMENTAL DAMAGE  //
        if (attacker.isPlayer()) {
        	if (defender.isPlayer()) {
        		defender.getEntity().setMetadata("lastPlayerToDamageExpire", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + 3000));
        		defender.getEntity().setMetadata("lastPlayerToDamage", new FixedMetadataValue(DungeonRealms.getInstance(), attacker.getEntity().getName()));
        	}
                
        	for (ElementalAttribute ea : ElementalAttribute.values()) {
        		if (attacker.getAttributes().hasAttribute(ea.getAttack())) {
        			applyDebuff(defender.getEntity(), ea, weaponTier);
        			damage += attacker.getAttributes().getAttribute(ea.getAttack()).getValue();
        		}
        	}
                
        } else if (EntityAPI.isElemental(attacker.getEntity())) {
        	applyDebuff(defender.getEntity(), EntityAPI.getElement(attacker.getEntity()), weaponTier);
        }

        //  CRIT CHANCE  //
        if (new Random().nextInt(100) < critHit) {
        	defender.getEntity().getWorld().spawnParticle(Particle.CRIT, defender.getEntity().getLocation(), 10,
        			new Random().nextDouble(), new Random().nextDouble(), new Random().nextDouble(), 0.5F);
        	isHitCrit = true;
        }

        //  LIFESTEAL  //
        if (attacker.isPlayer() && attacker.getAttributes().hasAttribute(WeaponAttributeType.LIFE_STEAL)) {
        	double lifeToHeal = ((double)attacker.getAttributes().getAttribute(WeaponAttributeType.LIFE_STEAL).getValue() / 100) * damage;
        	HealthHandler.heal(attacker.getPlayer(), (int)lifeToHeal + 1);
        }
        
        //  STRENGTH BUFF  //
        damage = applyIncreaseDamagePotion(attacker.getEntity(), damage);

        //  ADD DAMAGE BONUS  //
        if (attacker.getEntity().hasMetadata("damageBonus"))
        	damage += (damage * (attacker.getEntity().getMetadata("damageBonus").get(0).asDouble() / 100.));
        
        //  ADD ELITE BONUS  //
        damage = addSpecialDamage(attacker.getEntity(), damage);
        
        //  LEVEL DAMAGE  //
        if (defender.isPlayer() && !attacker.isPlayer()) {
        	// add 5% damage per level difference
        	int attackerLevel = EntityAPI.getLevel(attacker.getEntity());
        	int defenderLevel = PlayerWrapper.getWrapper(defender.getPlayer()).getLevel();
        	if (attackerLevel > defenderLevel)
        		damage = addLevelDamage(attackerLevel, defenderLevel, damage);
        }

        //  CRITICAL HIT  //
        if (isHitCrit) {
        	if (attacker.isPlayer()) {
        		attacker.getWrapper().sendDebug(ChatColor.YELLOW + "" + ChatColor.BOLD + "                        *CRIT*");
        		defender.getEntity().getWorld().playSound(attacker.getEntity().getLocation(), Sound.BLOCK_WOOD_BUTTON_CLICK_ON, 1.5F, 0.5F);
        	}
        	damage *= 2;
        }
        
        //  DAMAGE CAP  //
        if (!attacker.isPlayer() && damage >= weaponTier * 600)
        	damage = weaponTier * 600;

        res.setDamage(damage);
        return;
    }
    
    private static boolean getChance(AttributeList al, AttributeType at) {
    	return al.hasAttribute(at) && new Random().nextInt(100) < al.getAttribute(at).getValue();
    }
    
    private static void applyDebuff(LivingEntity defender, ElementalAttribute ea, int tier) {
    	if (ea == ElementalAttribute.PURE)
    		return;
    	
    	Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
    		defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
    		
    		if (ea == ElementalAttribute.FIRE) {
    			ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.FLAME, defender.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
                
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SPELL, defender.getLocation(),
                        new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1f, 10);
                final int[] FIRE_TICKS = new int[] {15, 25, 30, 35, 40};
                defender.setFireTicks(FIRE_TICKS[tier - 1]);
    		} else {
    			defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 1F, 1F);
                defender.getWorld().playEffect(defender.getLocation().add(0, 1.3, 0), Effect.POTION_BREAK, 8228);
                
                final int[] POTION_TICKS = new int[] {30, 40, 50, 40, 50};
                defender.addPotionEffect(new PotionEffect(ea.getAttackPotion(), POTION_TICKS[tier - 1], tier / 4));
    		}
    	}, 1);
    }

    public static void applySlow(LivingEntity defender) {
        Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
        	int tickLength = EntityAPI.getTier(defender) >= 4 ? 100 : 40;
            defender.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, tickLength, 1));
        });
    }

    public static void applyBlind(LivingEntity defender, int weaponTier) {
        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
        	int tickDelay = Math.min((weaponTier + 2) * 10, 60);
        	defender.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, tickDelay, 1));
        }, 1);
    }
    
    //This makes mobs stronger when they hit you IF they are higher than you. Not if you are higher than them...
    private static double addLevelDamage(int attackerLevel, int defenderLevel, double damage) {
        int difference = attackerLevel - defenderLevel;
        if (difference > 10)
        	difference = 10;
        return damage * (1 + (difference * 0.09));
    }

    public static void handlePolearmAOE(EntityDamageByEntityEvent event, double damage, Player damager) {
        ItemStack held = damager.getEquipment().getItemInMainHand();
        
        if (!ItemWeaponPolearm.isPolearm(held))
        	return;
        
        boolean damagerIsMob = !(damager instanceof Player);
        int hitCount = 0;
        
        for (Entity entity : event.getEntity().getNearbyEntities(2.5, 3, 2.5)) {
        	//  ARE WE AN ALLOWED ENTITY  //
        	if (!(entity instanceof LivingEntity) || (damagerIsMob && !GameAPI.isPlayer(entity)))
        		continue;
        	//  NO DAMAGE IN SAFE ZONES  //
        	if (GameAPI.isInSafeRegion(event.getEntity().getLocation()) || GameAPI.isInSafeRegion(damager.getLocation()))
        		continue;
        	//  DONT DAMAGE OURSELVES  //
        	if (entity.equals(damager))
        		continue;
        	
        	AttackResult res = new AttackResult(damager, (LivingEntity)event.getEntity());
        	res.setDamage(damage);
        	applyArmorReduction(res, true);
        	
        	if (entity != event.getEntity() && !res.getDefender().isPlayer()) {
        		//  DAMAGING HOSTILE MOB  //
        		if (!EnumEntityType.HOSTILE_MOB.isType(entity))
        			continue;
        		
        		HealthHandler.damageMonster(res);
        	} else if (res.getDefender().isPlayer()) {
        		if (!GameAPI.isNonPvPRegion(entity.getLocation())) {
        			if (GameAPI._hiddenPlayers.contains((Player) entity))
        				continue;
        			if (!DuelingMechanics.isDuelPartner(damager.getUniqueId(), entity.getUniqueId())) {
        				PlayerWrapper attacker = PlayerWrapper.getPlayerWrapper(damager);
        				if (!attacker.getToggles().getState(Toggles.PVP)) {
        					attacker.sendDebug(ChatColor.YELLOW + "You have toggle PvP disabled. You currently cannot attack players.");
        					continue;
        				}
        				//  IGNORE PARTIES  //
        				if (Affair.areInSameParty(damager, (Player) entity))
        					continue;
        				
        				//  IGNORE GUILDS  //
        				if (GuildDatabase.getAPI().areInSameGuild(damager, res.getDefender().getPlayer()))
        					continue;
        			}
        			HealthHandler.damagePlayer(res);
        		}
        	}
        	
        	hitCount++;
        }
        
        if (hitCount > 0) {
        	if (hitCount > 2)
        		hitCount--;
        	
        	EnergyHandler.removeEnergyFromPlayerAndUpdate(damager.getUniqueId(),
        			(EnergyHandler.getSwingCost(held) * hitCount) / 4F);
        }
    }

    /**
     * Adds extra damage for dungeon, elite, and boss mobs.
     *
     * @param attacker
     * @param damage
     * @return
     */
    public static double addSpecialDamage(LivingEntity attacker, double damage) {
        if (PowerMove.doingPowerMove(attacker.getUniqueId()))
            return damage;
        
        //  ELITE DMG BOOST  //
        if (EntityAPI.isElite(attacker)) {
            int tier = EntityAPI.getTier(attacker);
            return damage * (tier <= 2 ? 2.5f : (3 + (tier - 3) * 2));
        } else if (EntityAPI.isBoss(attacker)) {
            return damage * 3;
        }
        return damage;
    }

    public static double applyIncreaseDamagePotion(LivingEntity attacker, double damage) {
        if (attacker.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            int potionTier = 0;
            for (PotionEffect potionEffect : attacker.getActivePotionEffects()) {
                if (potionEffect.getType() == PotionEffectType.INCREASE_DAMAGE) {
                    potionTier = potionEffect.getAmplifier();
                    break;
                }
            }
            
            if(potionTier > 2)
            	potionTier = 2;
            
            damage *= (1.1 + (0.2 * potionTier));
        }
        return damage;
    }

    public static void applyArmorReduction(AttackResult res, boolean takeDura) {
        
    	CombatEntity attacker = res.getAttacker();
    	CombatEntity defender = res.getDefender();
    	double originalDamage = res.getDamage();
    	double damage = res.getDamage();
    	
    	double totalArmor = 0;
    	double totalArmorReduction = 0;
    	
    	//  DAMAGE ARMOR  //
    	if (defender.isPlayer())
    		if (takeDura)
    			for (ItemStack armor : defender.getPlayer().getEquipment().getArmorContents())
    				new ItemArmor(armor).damageItem(defender.getPlayer(), 1);
    	
    	int accuracy = res.hasProjectile() ? 0 : attacker.getAttributes().getAttribute(WeaponAttributeType.ACCURACY).getValue();
    	
    	//  BLOCK AND DODGE  //
    	Random rand = new Random();
    	
    	int dodgeChance = defender.getAttributes().getAttribute(ArmorAttributeType.DODGE).getValue();
    	int blockChance = defender.getAttributes().getAttribute(ArmorAttributeType.BLOCK).getValue();
    	final int dodgeRoll = rand.nextInt(100);
    	final int blockRoll = rand.nextInt(100);
    	
    	if (dodgeRoll < dodgeChance - accuracy) {
    		if (dodgeRoll >= dodgeChance) {
    			attacker.getWrapper().sendDebug(ChatColor.GREEN + "Your " + accuracy + "% accuracy has prevented " +
    					defender.getEntity().getCustomName() + ChatColor.GREEN + " from dodging.");
    		}
    		removeElementalEffects(defender.getEntity());
    		ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.CLOUD, defender.getEntity().getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
    		res.setResult(DamageResultType.DODGE);
    		return;
    	} else if (blockRoll < blockChance - accuracy) {
    		if (blockRoll >= blockChance) {
    			attacker.getWrapper().sendDebug(ChatColor.GREEN + "Your " + accuracy + "% accuracy has prevented " +
    					defender.getEntity().getCustomName() + ChatColor.GREEN + " from blocking.");
    		}
    		removeElementalEffects(defender.getEntity());
    		ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.REDSTONE, defender.getEntity().getLocation(), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 0.5F, 10);
    		res.setResult(DamageResultType.BLOCK);
    		return;
    	}
    	
    	//  REFLECT  //
    	int reflectChance = defender.getAttributes().getAttribute(ArmorAttributeType.REFLECTION).getValue();
    	if (rand.nextInt(100) < Math.min(75, reflectChance)) {
    		res.setResult(DamageResultType.REFLECT);
    		return;
    	}
    	
    	//  BASE ARMOR  //
    	totalArmor = Math.min(75, defender.getAttributes().getAttribute(ArmorAttributeType.ARMOR).getValueInRange());
    	
    	//  ARMOR PENETRATION  //
    	ModifierRange range = attacker.getAttributes().getAttribute(WeaponAttributeType.ARMOR_PENETRATION);
    	if (!res.hasProjectile() && range.getValue() > 0) {
    		totalArmor -= range.getValue();
    		if (totalArmor < 0)
    			totalArmor = 0;
    	}
    	
    	//  THORNS  //
    	ModifierRange mr = defender.getAttributes().getAttribute(ArmorAttributeType.THORNS);
    	if (mr.getValue() != 0 && !res.hasProjectile()) { // Only applies to Melee
    		int damageFromThorns = Math.max(1, (int) Math.round(damage * (mr.getValue() / 100f)));
    		res.setDamage(damageFromThorns);
    		attacker.getEntity().getWorld().playEffect(attacker.getEntity().getLocation(), Effect.STEP_SOUND, 18);
    		HealthHandler.damageEntity(res);
    		return;
    	}
    	
    	//  ELEMENTAL DAMAGE  //
    	int elementalDamage = 0;
    	int armorResistance = 0;
    	
    	if (attacker.isPlayer()) {
    		for(ElementalAttribute ea : ElementalAttribute.values()) {
    			//  ADD DAMAGE  //
    			int eDamage = attacker.getAttributes().getAttribute(ea.getAttack()).getValue();
    			if (res.hasProjectile())
    				eDamage = res.getProjectile().getMetadata(ea.getAttack().getNBTName()).get(0).asInt();
    			elementalDamage += eDamage;
    			
    			//  ADD RESISTANCE  //
    			if (ea.getResist() != null)
    				armorResistance += defender.getAttributes().getAttribute(ea.getResist()).getValue();
    		}
    	} else if (EntityAPI.isElemental(attacker.getEntity())) {
			ElementalAttribute ea = EntityAPI.getElement(attacker.getEntity());
			
			if (ea == ElementalAttribute.PURE) {
				totalArmor = 0;
			} else {
				totalArmor *= 0.2;
				totalArmor += Math.min(75, defender.getAttributes().getAttribute(ea.getResist()).getValue());
			}
		}
    	
    	//  APPLY ELEMENTAL  //
    	damage -= elementalDamage;
    	damage *= (100 - totalArmor) / 100D;
    	
    	// elemental damage ignores 80% but add on resistance
    	if (elementalDamage != 0)
    		damage += (0.8 * elementalDamage) * ((double) (100 - armorResistance)) / 100d;
    	
    	//  ARMOR BONUS  //
    	if (defender.getEntity().hasMetadata("armorBonus"))
    		totalArmor += (defender.getEntity().getMetadata("armorBonus").get(0).asFloat() / 100f) * totalArmor;
    	
    	totalArmorReduction = originalDamage - damage;
    	
    	//  POTION BUFF	  //
    	if (defender.getEntity().hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
    		int potionTier = 1;
    		for (PotionEffect pe : defender.getEntity().getActivePotionEffects()) {
    			if (pe.getType() == PotionEffectType.DAMAGE_RESISTANCE) {
    				potionTier = pe.getAmplifier();
    				break;
    			}
    		}
    		final double[] LEVEL_REDUCTION = new double[] {1, 1.05, 1.1, 1.2};
    		if(potionTier < LEVEL_REDUCTION.length)
    			totalArmorReduction *= LEVEL_REDUCTION[potionTier];
    	}
    	
    	res.setDamage(damage);
    	res.setTotalArmor(totalArmor);
    	res.setTotalArmorReduction(totalArmorReduction);
    }
    
    public static void fireStaffProjectile(Player player, ItemWeapon staff, boolean subtractDurability) {
    	if(subtractDurability)
    		staff.damageItem(player, 1);
    	PlayerWrapper pw = PlayerWrapper.getWrapper(player);
    	pw.calculateAllAttributes();
    	EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(staff.getItem()));
    	fireStaffProjectile(player, pw.getAttributes(), staff);
    }
    
    public static Projectile fireStaffProjectile(LivingEntity attacker, ItemWeaponStaff staff) {
    	return fireStaffProjectile(attacker, staff.getAttributes(), staff);
    }

    //TODO: Modularize.
    public static Projectile fireStaffProjectile(LivingEntity attacker, AttributeList attributes, ItemWeapon staff) {
    	System.out.println("Fire staff projectile method!");
        double accuracy = attributes.getAttribute(WeaponAttributeType.PRECISION).getValue();
		Bukkit.getLogger().info("Yaw: " + attacker.getLocation().getYaw() + " Pitch: " + attacker.getLocation().getPitch());

		System.out.println("Attackers direction: " + attacker.getLocation().getDirection());
        Projectile projectile = null;
        switch (staff.getTier()) {
            case TIER_1:
                projectile = attacker.launchProjectile(Snowball.class);
                projectile.setVelocity(projectile.getVelocity().multiply(1.15));
                System.out.println("Snowballs Yaw: " + projectile.getLocation().getYaw() + " Pitch: " + projectile.getLocation().getPitch());
                System.out.println("Snowballs direction: " + projectile.getLocation().getDirection());
                break;
            case TIER_2:
                projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) attacker.getWorld()).getHandle(), (CraftLivingEntity)attacker, null, SmallFireball.class, accuracy);
                projectile.setVelocity(projectile.getVelocity().multiply(1.5));
                ((SmallFireball) projectile).setYield(0);
                ((SmallFireball) projectile).setIsIncendiary(false);
                break;
            case TIER_3:
                projectile = attacker.launchProjectile(EnderPearl.class);
                projectile.setVelocity(projectile.getVelocity().multiply(1.75));
                break;
            case TIER_4:
                projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) attacker.getWorld()).getHandle(), (CraftLivingEntity) attacker, null, WitherSkull.class, accuracy);
                projectile.setVelocity(projectile.getVelocity().multiply(2.25));
                break;
            case TIER_5:
                projectile = EntityMechanics.spawnFireballProjectile(((CraftWorld) attacker.getWorld()).getHandle(), (CraftLivingEntity) attacker, null, LargeFireball.class, accuracy);
                projectile.setVelocity(projectile.getVelocity().multiply(2.5));
                ((LargeFireball) projectile).setYield(0);
                ((LargeFireball) projectile).setIsIncendiary(false);
                break;
        }
        System.out.println("Staff projectile debug 1");
        if (projectile == null) return null;
		System.out.println("Staff projectile debug 2");
        projectile.setBounce(false);
        projectile.setShooter(attacker);
        MetadataUtils.registerProjectileMetadata(attributes, staff.getTier().getId(), projectile);
        return projectile;
    }
    
    public static void fireBowProjectile(Player player, ItemWeaponBow bow, boolean takeDura) {
    	if (takeDura)
        	bow.damageItem(player, 1);
    	PlayerWrapper.getWrapper(player).calculateAllAttributes();
    	EnergyHandler.removeEnergyFromPlayerAndUpdate(player.getUniqueId(), EnergyHandler.getWeaponSwingEnergyCost(bow.getItem()), !takeDura);
    	fireBowProjectile(player, bow);
    }

    public static void fireBowProjectile(LivingEntity ent, ItemWeaponBow bow) {
        
        Projectile projectile = null;
        
        for (AttributeType type : bow.getAttributes().keySet()) {
        	ElementalAttribute ea = ElementalAttribute.getByAttribute(type);
        	if (ea != null) {
        		if (projectile == null)
        			projectile = ent.launchProjectile(TippedArrow.class);
        		((TippedArrow)projectile).addCustomEffect(new PotionEffect(ea.getDefensePotion(), 0, 0), true);
        	}
        }
        
        if(projectile == null)
            projectile = ent.launchProjectile(Arrow.class);
        
        projectile.setBounce(false);
        projectile.setVelocity(projectile.getVelocity().multiply(1.15));
        projectile.setShooter(ent);
        ((CraftArrow) projectile).getHandle().fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        MetadataUtils.registerProjectileMetadata(bow.getAttributes(), bow.getTier().getId(), projectile);
    }

    public static void removeElementalEffects(LivingEntity ent) {
    	for(ElementalAttribute ea : ElementalAttribute.values())
    		if (ea.getAttackPotion() != null && ent.hasPotionEffect(ea.getAttackPotion()))
    			ent.removePotionEffect(ea.getAttackPotion());
    	
    	//  FIRE  //
        if (ent.getFireTicks() > 0)
            ent.setFireTicks(0);
    }

    public static void knockbackEntity(Player p, Entity ent, double speed) {
        if (ent instanceof Horse) {
            return;
        }
        // Get velocity unit vector:
        org.bukkit.util.Vector unitVector = ent.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
        unitVector.setY(0.35);
        if (speed > 1) unitVector.setY(0.2);
        if (p.getVelocity().getY() > 0) unitVector.setY(0);
        // Set speed and push entity:

        if (ent instanceof Player) {
            EntityMechanics.setVelocity((Player) ent, unitVector.multiply(speed));
            return;
        }
        ent.setVelocity(unitVector.multiply(speed));
    }

    @SuppressWarnings("deprecation")
	public static void newKnockbackEntity(Player p, Entity ent, double speed) {
        if (ent instanceof Horse) {
            return;
        }
        // Get velocity unit vector:
        org.bukkit.util.Vector unitVector = ent.getLocation().toVector().subtract(p.getLocation().toVector()).normalize();
        unitVector.setY(p.isOnGround() ? 0.35 : 0.2);
        if (speed > 1) unitVector.setY(0.2);
        if (p.getVelocity().getY() > 0) unitVector.setY(0);
        // Set speed and push entity:
        if (ent instanceof Player) {
            EntityMechanics.setVelocity((Player) ent, unitVector.multiply(speed));
            return;
        }
        ent.setVelocity(unitVector.multiply(speed));
    }

    public static boolean isStaffProjectile(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.SNOWBALL || type == EntityType.SMALL_FIREBALL || type == EntityType.ENDER_PEARL ||
                type == EntityType.FIREBALL || type == EntityType.WITHER_SKULL;
    }

    public static boolean isBowProjectile(Entity entity) {
        EntityType type = entity.getType();
        return type == EntityType.ARROW || type == EntityType.TIPPED_ARROW;
    }

    /**
     * Sets a blanket damage bonus for a specified entity. When the entity
     * attacks, the damage bonus will be added on to the final calculated
     * damage.
     *
     * @param ent
     * @param bonusPercent - the bonus amount as a percentage. (e.g. 50.0 will
     *                     increase damage by 50%).
     */
    public static void setDamageBonus(Entity ent, float bonusPercent) {
        ent.setMetadata("damageBonus", new FixedMetadataValue(DungeonRealms.getInstance(), bonusPercent));
    }

    public static float getDamageBonus(Entity ent) {
        return ent.hasMetadata("damageBonus") ? ent.getMetadata("damageBonus").get(0).asFloat() : 0;
    }

    public static void removeDamageBonus(Entity ent) {
        if (ent.hasMetadata("damageBonus")) ent.removeMetadata("damageBonus", DungeonRealms.getInstance());
    }

    /**
     * Sets a blanket armor ignore bonus for a specified entity. When the entity
     * attacks, the armor reduction of the attacked entity will be reduced by this
     * bonus.
     *
     * @param ent
     * @param bonusPercent - the bonus amount as a percentage. (e.g. 50.0 will
     *                     reduce armor reduction by 50%).
     */
    public static void setArmorBonus(Entity ent, float bonusPercent) {
        ent.setMetadata("armorBonus", new FixedMetadataValue(DungeonRealms.getInstance(), bonusPercent));
    }

    public static float getArmorBonus(Entity ent) {
        return ent.hasMetadata("armorBonus") ? ent.getMetadata("armorBonus").get(0).asFloat() : 0;
    }

    public static void removeArmorBonus(Entity ent) {
        if (ent.hasMetadata("armorBonus")) ent.removeMetadata("armorBonus", DungeonRealms.getInstance());
    }

    public static void setInvulnerable(Entity ent) {
    	Metadata.INVULNERABLE.set(ent, true);
    }

    public static boolean isInvulnerable(Entity ent) {
        return Metadata.INVULNERABLE.get(ent).asBoolean();
    }

    public static void removeInvulnerable(Entity ent) {
    	Metadata.INVULNERABLE.remove(ent);
    }

    public static void createDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.RED + "-" + (int) hp + " ❤");
    }

    /**
     * Create a hologram that floats up and deletes itself.
     */
    public static void createDamageHologram(Player createFor, Location createAround, String display) {
        if (createFor != null && !PlayerWrapper.getPlayerWrapper(createFor).getToggles().getState(Toggles.DAMAGE_INDICATORS))
            return;
        double xDif = (Utils.randInt(0, 20) - 10) / 10D;
        double yDif = Utils.randInt(0, 15) / 10D;
        double zDif = (Utils.randInt(0, 20) - 10) / 10D;
        Hologram hologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), createAround.add(xDif, yDif, zDif));
        hologram.appendTextLine(display);
        hologram.getVisibilityManager().setVisibleByDefault(true);

        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> 
            hologram.teleport(hologram.getLocation().add(0.0, 0.1D, 0.0)), 0, 1l);
        
        if (!DAMAGE_HOLOGRAMS.containsKey(createFor))
            DAMAGE_HOLOGRAMS.put(createFor, new HashMap<Hologram, Integer>());

        HashMap<Hologram, Integer> holograms = DAMAGE_HOLOGRAMS.get(createFor);
        holograms.put(hologram, taskId);
        if (holograms.keySet().size() > 4)
            removeDamageHologram(createFor, holograms.keySet().toArray(new Hologram[1])[0]);

        Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(),
                () -> removeDamageHologram(createFor, hologram), 20L);
    }

    private static void removeDamageHologram(Player player, Hologram hologram) {
        if (hologram.isDeleted())
            return;
        Bukkit.getScheduler().cancelTask(DAMAGE_HOLOGRAMS.get(player).get(hologram));
        DAMAGE_HOLOGRAMS.get(player).remove(hologram);
        hologram.delete();
    }
}