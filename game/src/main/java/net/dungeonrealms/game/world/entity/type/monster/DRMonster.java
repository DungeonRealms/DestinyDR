package net.dungeonrealms.game.world.entity.type.monster;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.core.ItemArmor;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemWeaponMelee;
import net.dungeonrealms.game.item.items.functional.ItemGem;
import net.dungeonrealms.game.item.items.functional.ItemTeleportBook;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.data.DropRate;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
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
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public interface DRMonster {
	
	public static final TeleportLocation[][] TELEPORT_DROPS = new TeleportLocation[][] {
				{TeleportLocation.CYRENNICA, TeleportLocation.HARRISON_FIELD},
				{TeleportLocation.CYRENNICA, TeleportLocation.HARRISON_FIELD, TeleportLocation.DARK_OAK, TeleportLocation.TROLLSBANE, TeleportLocation.TRIPOLI},
				{TeleportLocation.CYRENNICA, TeleportLocation.DARK_OAK, TeleportLocation.TROLLSBANE, TeleportLocation.GLOOMY_HOLLOWS, TeleportLocation.CRESTGUARD},
				{TeleportLocation.DEADPEAKS, TeleportLocation.GLOOMY_HOLLOWS},
				{TeleportLocation.DEADPEAKS, TeleportLocation.GLOOMY_HOLLOWS}};

    default void onMonsterAttack(Player p) {
    	
    }

    default void onMonsterDeath(Player killer) {
    	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> checkItemDrop(killer));
    }
    
    default void setupMonster(int tier) {
    	setTier(tier);
    	setGear();
    	setSkullTexture();
        
        //  SET NMS DATA  //
        setupNMS();
    }
    
    default void setupNMS() {
//    	getNMS().getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(20d);
        getNMS().getAttributeInstance(GenericAttributes.c).setValue(.75D);
        getNMS().noDamageTicks = 0;
        getNMS().maxNoDamageTicks = 0;
    }
    
    default void setGear() {
    	ItemStack[] armor = GameAPI.getTierArmor(getTier());
		Random random = new Random();
		boolean forcePlace = false;
		EntityEquipment e = getBukkit().getEquipment();
		
		int chance = 6 + getTier();
		
		ItemStack[] entityArmor = e.getArmorContents();
		for (int i = 0; i <= 2; i++) { //Chestplate, boots, leggings. No helmet.
			if (forcePlace || getTier() >= 3 || random.nextInt(10) <= chance) {
				entityArmor[i] = armor[i];
				
				if (i == 1) //Reset force place for low tiers at leggings.
					forcePlace = false;
			} else {
				forcePlace = true;
			}
		}
		e.setArmorContents(entityArmor);
		e.setItemInMainHand(getWeapon());
    }

    default void setMonster(EnumMonster m) {}
    
    EnumMonster getEnum();
    
    default EntityLiving getNMS() {
    	return (EntityLiving) this;
    }
    
    default AttributeList getAttributes() {
    	return EntityAPI.getEntityAttributes().get(this);
    }
    
    default LivingEntity getBukkit() {
    	return (LivingEntity) ((net.minecraft.server.v1_9_R2.Entity)this).getBukkitEntity();
    }
    
    default ItemStack getWeapon() {
    	return makeItem(new ItemWeaponMelee());
    }
    
    default ItemStack makeItem(ItemGear gear) {
    	return gear.setTier(ItemTier.getByTier(getTier())).generateItem();
    }
    
    default void setSkullTexture() {
    	if(getEnum() != null && getEnum().getSkullItem() != null) {
    		ItemStack helmet = getEnum().getSkullItem();
    		getBukkit().getEquipment().setHelmet(helmet);
    		getNMS().setEquipment(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(helmet));
    	}
    }
    
    default void setTier(int tier) {
    	Metadata.TIER.set(getBukkit(), tier);
    }
    
    default int getTier(){
    	return Metadata.TIER.get(getBukkit()).asInt();
    }

    default void checkItemDrop(Player killer) {
    	LivingEntity ent = getBukkit();
    	PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(killer);
    	
    	//No normal drops in dungeons.
        if (DungeonManager.isDungeon(ent))
            return;
        
        //Boss will handle this.
        if (Metadata.BOSS.has(ent))
            return;
        
        //combat log npcs have special drop mechanics
        if (EnumEntityType.COMBATLOG_NPC.isType(ent))
            return;
        
        int tier = getTier();
        Random random = new Random();
        PlayerWrapper pw = PlayerWrapper.getWrapper(killer);
        
        ModifierRange gemFinder = pw.getAttributes().getAttribute(ArmorAttributeType.GEM_FIND);
        double gemFind = (gemFinder.getValHigh() / 100) + 1;
        int killerItemFind = pw.getAttributes().getAttribute(ArmorAttributeType.ITEM_FIND).getValHigh();
        
        Location loc = ent.getLocation();
        World world = ((CraftWorld) loc.getWorld()).getHandle();

        int gemRoll = random.nextInt(100);
        DropRate dr = DropRate.getRate(tier);
        int gemChance = dr.getMobGemChance();
        boolean elite = Metadata.ELITE.has(ent);
        int chance = elite ? dr.getEliteDropChance() : dr.getNormalDropChance();
        
        // If it's a named elite, bring the drop chances down.
        if (Metadata.NAMED_ELITE.has(ent))
            chance /= 3;

        if (DonationEffects.getInstance().hasBuff(EnumBuff.LOOT))
        	chance += chance * (DonationEffects.getInstance().getBuff(EnumBuff.LOOT).getBonusAmount() / 100f);

        chance *= 2; //Double drop rates this wipe.
        
        if (gemRoll < (gemChance * gemFind)) {
            if (gemRoll >= gemChance)
                wrapper.sendDebug(ChatColor.GREEN + "Your " + gemFinder.getValHigh() + "% Gem Find has resulted in a drop.");
            
            double gemsDropped = Utils.randInt(dr.getGemDropMin(), dr.getGemDropMax());
            gemsDropped *= gemFind;
            if (elite)
            	gemsDropped *= 1.5;
            
            while (gemsDropped > 0) {
            	int drop = Math.min((int)gemsDropped, 64);
            	gemsDropped -= drop;
            	ItemGem gem = new ItemGem(drop);
            	ItemManager.whitelistItemDrop(killer, world.getWorld().dropItem(loc.add(0, 1, 0), gem.generateItem()));
            }
        }

        //Disabled as of 4/19/17
        //4%, 3%, 2%, 1%, .6%
//        if(random.nextInt(1000) <= (tier == 5 ? 40 : tier == 4 ? 30 : tier == 3 ? 20 : tier == 2 ? 10 : tier == 1 ? 4 : 6)){
//            ItemStack item = EasterListener.createEasterEgg();
//            ParticleAPI.sendParticleToEntityLocation(ParticleAPI.ParticleEffect.HAPPY_VILLAGER, ent, .5F, .5F, .5F, .1F, 20);
//            ItemManager.whitelistItemDrop(killer, world.getWorld().dropItem(loc.add(0, 1, 0), item));
//            world.getWorld().playSound(ent.getLocation(), Sound.ENTITY_CHICKEN_EGG, 3, 1.1F);
//            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> GameAPI.sendIngameDevMessage(ChatColor.LIGHT_PURPLE + "Dropping easter egg for " + killer.getName() + " on {SERVER}."));
//        }

        int dropRoll = random.nextInt(1000);
        
        List<ItemStack> toDrop = new ArrayList<>();
        for (ItemStack stack : ((LivingEntity) ent).getEquipment().getArmorContents())
            if (stack != null && stack.getType() != Material.AIR && stack.getType() != Material.SKULL && stack.getType() != Material.SKULL_ITEM)
            	toDrop.add(stack);
        
        if (!elite)
        	toDrop.add(new ItemArmor(ItemType.HELMET).setTier(ItemTier.getByTier(getTier())).generateItem());
        
        //Random drop choice, as opposed dropping in the same order (boots>legs>chest>head)
        Collections.shuffle(toDrop);
        if (dropRoll < chance + (chance * killerItemFind / 100)) {
            if (dropRoll >= chance)
            	wrapper.sendDebug(ChatColor.GREEN + "Your " + killerItemFind + "% Item Find has resulted in a drop.");
            
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
                ItemManager.whitelistItemDrop(killer, loc, gear.generateItem());
            }
        }
        
        // Drop teleport book.
        if (dr.getTeleportBookChance() >= random.nextInt(100))
            ItemManager.whitelistItemDrop(killer, ent.getLocation(), new ItemTeleportBook(
            		TELEPORT_DROPS[tier][random.nextInt(TELEPORT_DROPS[tier].length)]).generateItem());
    }
    
    default ItemStack getHeld() {
    	return getBukkit().getEquipment().getItemInMainHand();
    }
    
    default void calculateAttributes() {
    	EntityAPI.calculateAttributes(this);
    }
    
    default int getHP() {
    	return HealthHandler.getHP(getBukkit());
    }
    
    default void setHP(int hp) {
    	HealthHandler.setHP(getBukkit(), hp);
    }
    
    default int getMaxHP() {
    	return HealthHandler.getMaxHP(getBukkit());
    }
    
    default void setMaxHP(int max) {
    	HealthHandler.setMaxHP(getBukkit(), max);
    }
    
    default double getPercentHP() {
    	return HealthHandler.getHPPercent(getBukkit());
    }

}
