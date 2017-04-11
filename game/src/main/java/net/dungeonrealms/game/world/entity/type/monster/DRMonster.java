package net.dungeonrealms.game.world.entity.type.monster;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemWeaponMelee;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.DropRate;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.EnumItemSlot;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

/**
 * Created by Chase on Oct 21, 2015
 */
public interface DRMonster {

    default void onMonsterAttack(Player p) {
    	
    }

    default void onMonsterDeath(Player killer) {
    	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> checkItemDrop(killer));
    }
    
    default void setupMonster(int tier) {
    	setTier(tier);
    	setGear();
    	setSkullTexture();
    	
    	//  SET CUSTOM NAME  //
    	String customName = getEnum().getPrefix() + " " + getEnum().name + " " + getEnum().getSuffix() + " ";
        getNMS().setCustomName(customName);
        getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), customName));
        getBukkitEntity().setCustomNameVisible(true);
        
        //  SET NMS DATA  //
        setupNMS();
    }
    
    default void setupNMS() {
    	getNMS().getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        getNMS().getAttributeInstance(GenericAttributes.c).setValue(1.00d);
        getNMS().noDamageTicks = 0;
        getNMS().maxNoDamageTicks = 0;
    }
    
    default void setGear() {
    	int tier = getTier();
    	ItemStack[] armor = GameAPI.getTierArmor(tier);
		LivingEntity livingEntity = (LivingEntity) this.getBukkitEntity();
		Random random = new Random();
		boolean forcePlace = tier >= 3;
		
		int chance = 6 + tier;
		//  SET BOOTS  //
		if (forcePlace || random.nextInt(10) <= chance) {
			livingEntity.getEquipment().setBoots(armor[0]);
			getNMS().setEquipment(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(armor[0]));
		} else {
			forcePlace = true;
		}
		
		//  SET LEGGINGS  //
		if (forcePlace || random.nextInt(10) <= chance) {
			livingEntity.getEquipment().setLeggings(armor[1]);
			getNMS().setEquipment(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(armor[1]));
			forcePlace = tier >= 3;
		} else {
			forcePlace = true;
		}
		
		//  SET CHESTPLATE  //
		if (forcePlace || random.nextInt(10) <= chance) {
			livingEntity.getEquipment().setChestplate(armor[2]);
			getNMS().setEquipment(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(armor[2]));
		}
		
		//  SET WEAPON  //
		ItemStack weapon = getWeapon();
		getNMS().setEquipment(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(weapon));
		livingEntity.getEquipment().setItemInMainHand(weapon);
		
    }

    EnumMonster getEnum();
    
    EntityLiving getNMS();

    AttributeList getAttributes();
    
    CraftEntity getBukkitEntity();
    
    default ItemStack getWeapon() {
    	return makeItem(new ItemWeaponMelee());
    }
    
    default ItemStack makeItem(ItemGear gear) {
    	return gear.setTier(ItemTier.getByTier(getTier())).generateItem();
    }
    
    default void setSkullTexture() {
    	if(getEnum() != null && getEnum().getSkullItem() != null) {
    		ItemStack helmet = getEnum().getSkullItem();
    		((LivingEntity)getBukkitEntity()).getEquipment().setHelmet(helmet);
    		getNMS().setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
    	}
    }
    
    default void setTier(int tier) {
    	getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), tier));
    }
    
    default int getTier(){
    	return getBukkitEntity().getMetadata("tier").get(0).asInt();
    }

    default void checkItemDrop(Player killer) {
    	Entity ent = getBukkitEntity();
    	
    	//No normal drops in dungeons.
        if (ent.getWorld().getName().contains("DUNGEON"))
            return;
        
        //Boss will handle this.
        if (ent.hasMetadata("boss"))
            return;
        
        //combat log npcs have special drop mechanics
        if (ent.hasMetadata("combatlog"))
            return;
        
        int tier = getTier();
        Random random = new Random();
        GamePlayer gp = GameAPI.getGamePlayer(killer);
        boolean toggleDebug = (Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_DEBUG, killer.getUniqueId());
        
        ModifierRange gemFinder = gp.getAttributes().getAttribute(ArmorAttributeType.GEM_FIND);
        double gemFind = (gemFinder.getValHigh() / 100) + 1;
        int killerItemFind = gp.getAttributes().getAttribute(ArmorAttributeType.ITEM_FIND).getValHigh();
        
        Location loc = ent.getLocation();
        World world = ((CraftWorld) loc.getWorld()).getHandle();

        int gemRoll = random.nextInt(100);
        DropRate dr = DropRate.getRate(tier);
        int gemChance = dr.getMobGemChance();
        int chance = ent.hasMetadata("elite") ? dr.getEliteDropChance() : dr.getNormalDropChance();
        
        // If it's a named elite, bring the drop chances down.
        if (ent.hasMetadata("namedElite"))
            chance /= 3;

        if (DonationEffects.getInstance().hasBuff(EnumBuff.LOOT))
        	chance += chance * (DonationEffects.getInstance().getBuff(EnumBuff.LOOT).getBonusAmount() / 100f);

        if (gemRoll < (gemChance * gemFind)) {
            if (gemRoll >= gemChance)
                if (toggleDebug)
                    killer.sendMessage(ChatColor.GREEN + "Your " + gemFinder.getValHigh() + "% Gem Find has resulted in a drop.");

            double gemsDropped = Utils.randInt(dr.getGemDropMin(), dr.getGemDropMax());
            gemsDropped *= gemFind;
            if (ent.hasMetadata("elite"))
            	gemsDropped *= 1.5;
            
            while (gemsDropped > 0) {
            	int drop = Math.min((int)gemsDropped, 64);
            	gemsDropped -= drop;
            	ItemGem gem = new ItemGem(drop);
            	ItemManager.whitelistItemDrop(killer, world.getWorld().dropItem(loc.add(0, 1, 0), gem.generateItem()));
            }
        }

        int dropRoll = random.nextInt(1000);
        
        List<ItemStack> toDrop = new ArrayList<>();
        for (ItemStack stack : ((LivingEntity) ent).getEquipment().getArmorContents())
            if (stack != null && stack.getType() != Material.AIR && stack.getType() != Material.SKULL && stack.getType() != Material.SKULL_ITEM)
            	toDrop.add(stack);
        
        if (!ent.hasMetadata("elite"))
        	toDrop.add(new ItemArmor().setTier(ItemTier.getByTier(getTier())).setType(GeneratedItemType.HELMET).generateItem());
        
        //Random drop choice, as opposed dropping in the same order (boots>legs>chest>head)
        Collections.shuffle(toDrop);
        if (dropRoll < chance + (chance * killerItemFind / 100)) {
            if (dropRoll >= chance)
                if (toggleDebug)
                    killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
            
            ItemStack drop = toDrop.get(random.nextInt(toDrop.size()));
            if (new Random().nextInt(2) == 0) { // 50% chance for weapon, 50% for armor
                ItemStack weapon = ((LivingEntity) ent).getEquipment().getItemInMainHand();
                if (weapon != null && weapon.getType() != Material.AIR)
                    drop = weapon;
            }
            
            //  DROP ITEM  //
            if (drop != null && drop.getType() != Material.AIR) {
            	ItemGear gear = (ItemGear)PersistentItem.constructItem(drop);
            	gear.damageItem(null, Utils.randInt(0, ItemGear.MAX_DURABILITY - (int)(ItemGear.MAX_DURABILITY / 7.5)));
                ItemManager.whitelistItemDrop(killer, world.getWorld().dropItem(loc.add(0, 1, 0), gear.generateItem()));
            }
        }
        
        int scrollDrop = random.nextInt(100);
        int scrollDropChance = dr.getTeleportBookChance();
        
        if (scrollDropChance >= scrollDrop) {
            TeleportLocation[][] locations = new TeleportLocation[][] {
            		{TeleportLocation.CYRENNICA},
            		{TeleportLocation.CYRENNICA, TeleportLocation.HARRISON_FIELD},
            		{TeleportLocation.CYRENNICA, TeleportLocation.HARRISON_FIELD, TeleportLocation.DARK_OAK, TeleportLocation.TROLLSBANE, TeleportLocation.TRIPOLI},
            		{TeleportLocation.CYRENNICA, TeleportLocation.DARK_OAK, TeleportLocation.TROLLSBANE, TeleportLocation.GLOOMY_HOLLOWS, TeleportLocation.CRESTGUARD},
            		{TeleportLocation.DEADPEAKS, TeleportLocation.GLOOMY_HOLLOWS},
            		{TeleportLocation.DEADPEAKS, TeleportLocation.GLOOMY_HOLLOWS}
            };
            
            ItemStack teleport = new ItemTeleportBook(locations[tier][random.nextInt(locations[tier].length)]).generateItem();
            
            if (teleport != null)
                ItemManager.whitelistItemDrop(killer, ent.getWorld().dropItem(ent.getLocation().add(0, 1, 0), teleport));
        }
        
        /*if (weapon.getType() == Material.BOW) {
            int arrowRoll = random.nextInt(99);
            if (arrowRoll <= (25 + (25 * killerItemFind / 100))) {
                if (arrowRoll > 25) {
                    if (toggleDebug) {
                        killer.sendMessage(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
                    }
                }
                ItemStack item = new ItemStack(Material.ARROW);
                int amount = (tier * 2);
                item.setAmount(amount);
                world.getWorld().dropItem(loc.add(0, 1, 0), item);
            }
        }*/ // arrows are no longer needed (uncomment if we ever add them back)
    }
    
    default ItemStack getHeld() {
    	return ((LivingEntity)this.getBukkitEntity()).getEquipment().getItemInMainHand();
    }
    
    default void calculateAttributes() {
    	GameAPI.calculateAllAttributes((LivingEntity) getBukkitEntity(), getAttributes());
    }

}
