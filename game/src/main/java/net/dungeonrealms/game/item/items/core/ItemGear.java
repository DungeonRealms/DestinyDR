package net.dungeonrealms.game.item.items.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.GeneratedItemType;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ItemModifier;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierCondition;
import net.dungeonrealms.game.world.item.itemgenerator.engine.ModifierRange;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * ItemGear - Contains shared methods for gear that can have attributes, be orbed, etc.
 * 
 * Created March 28th, 2017.
 * @author Kneesnap
 */
public abstract class ItemGear extends ItemGeneric {

	@Getter @Setter
	private boolean Protected; //Uppercast to avoid being the keyword "protected".
	
	@Getter //The tier of this item.
	private ItemTier tier;
	
	@Getter //The rarity of this item.
	private ItemRarity rarity;
	
	@Getter @Setter
	private int enchantCount;
	
	@Getter
	private int durability;
	
	@Getter
	private AttributeList attributes = new AttributeList();
	
	@Getter
	private GeneratedItemType generatedItemType;
	
	public static final int MAX_DURABILITY = 1500;
	
	//Enchant Success Chances
	private static final int[] SUCCESS_CHANCE = {100, 100, 100, 70, 60, 50, 35, 25, 20, 15, 10, 5};
	private static final int[] DURABILITY_WARNINGS = {30, 10, 5, 2};
	
	public ItemGear(ItemType... types) {
		this(types[new Random().nextInt(types.length)]);
	}
	
	//Used for item generators usually.
	public ItemGear(ItemType type) {
		super(type);
		setType(type);
		setAntiDupe(true);
		setTier(ItemTier.getRandomTier());
		setRarity(ItemRarity.getRandomRarity());
		this.durability = MAX_DURABILITY;
		rollStats(false);
	}
	
	//Used for loading existing items usually.
	public ItemGear(ItemStack item) {
		super(item);
		setAntiDupe(true);
	}
	
	@Override
	protected void loadItem() {
		super.loadItem();
		this.attributes = new AttributeList();
		
		//  LOAD GENERAL DATA  //
		this.generatedItemType = GeneratedItemType.getType(getItem().getType());
		setTier(ItemTier.getByTier(getTagInt(TIER)));
		setProtected(getTagBool("protected"));
		setEnchantCount(getTagInt("enchant"));
		
		if(hasTag("itemRarity"))
			this.setRarity(ItemRarity.valueOf(getTagString("itemRarity")));
		
		//  LOAD DURABILITY  //
		if (hasTag("RepairCost")) {
			this.durability = getTagInt("RepairCost");
		} else {
			double percent = (Math.max(1, (getItem().getType().getMaxDurability() - getItem().getDurability())) / Math.max(1, getItem().getType().getMaxDurability()));
			//We don't just multiply by MAX_DURABILITY because that results in rounding at the wrong decimal place.
			this.durability = (int) (Math.round(percent * 100) * (MAX_DURABILITY / 100));
		}
		
		//  LOAD ATTRIBUTES  //
		if (getGeneratedItemType() != null)
			getAttributes().load(getTag(), getGeneratedItemType().getAttributeBank().getAttributes());
	}
	
	@Override
	public void updateItem() {
		//  SAVE GENERAL DATA  //
		if(getRarity() != null)
			setTagString("itemRarity", getRarity().name());
		
		setTagInt(TIER, getTier().getId());
		setTagInt("enchant", getEnchantCount());
		setTagBool("protected", isProtected());
		setTagInt("RepairCost", getDurability());
		
		// Removes the extra tag on gear, Ie: Diamond Sword - "+7 Attack Damage"
		getTag().set("AttributeModifiers", new NBTTagList());
		updateLore();
		
		getItem().setDurability((short) ((100 - getDurabilityPercent()) * getItem().getType().getMaxDurability()));
		if(getEnchantCount() > 3)
			setGlowing(true);
		super.updateItem();
	}
	
	/**
	 * Change the item type of this item.
	 */
	public ItemGear setType(ItemType type) {
		super.setType(type);
		if (type != null)
			this.generatedItemType = type.getType();
		return this;
	}
	
	@Override
	protected ItemStack getStack() {
		return new ItemStack(getGeneratedItemType().getTier(getTier()));
	}
	
	/**
	 * Called when an item's durability runs out.
	 * Calls functions such as respawn a new pickaxe or recalculate player armor stats.
	 */
	protected abstract void onItemBreak(Player player);
	
	/**
	 * Gets the repair cost for one percent of this item.
	 */
	protected abstract double getBaseRepairCost();
	
	/**
	 * Called to give each item its own attribute upgrades when enchanted.
	 */
	protected abstract void applyEnchantStats();
	
	public ItemGear setRarity(ItemRarity rarity) {
		this.rarity = rarity;
		return this;
	}
	
	public ItemGear setTier(int tier) {
		return setTier(ItemTier.getByTier(tier));
	}
	
	public ItemGear setTier(ItemTier tier) {
		this.tier = tier;
		return this;
	}
	
	private void updateLore() {
		//  SAVE ATTRIBUTES TO NBT  //
		NBTTagCompound nbtAttributes = new NBTTagCompound();
		for(AttributeType t : this.getAttributes().keySet()) {
			ModifierRange range = getAttributes().getAttribute(t);
			range.save(nbtAttributes, t.getNBTName());
			addLore(t.getPrefix() + range.toString() + t.getSuffix());
		}
		getTag().set("itemAttributes", nbtAttributes);
		
		//Show Custom lore.
		
		if(getRarity() != null)
			addLore(getRarity().getName());
		
		if (isProtected())
			addLore(ChatColor.GOLD + "Protected");
		
		//  UPDATE DISPLAY NAME  //
		getMeta().setDisplayName(generateItemName());
	}
	
	private String generateItemName() {
		String name = getTier().getColor().toString();
		
		List<AttributeType> sorted = new ArrayList<>(this.attributes.keySet());
		sorted.sort((a1, a2) -> a2.getDisplayPriority() - a1.getDisplayPriority());
		
		GeneratedItemType itemType = this.getGeneratedItemType();
		String rawItemName = itemType.getTierName(getTier());
		
		//  ADD PREFIXES  //
		for (AttributeType type : sorted)
			if (!type.getDisplayPrefix().equals(""))
				name += type.getDisplayPrefix() + " ";
		
		//  ADD SUFFIXES  //
		for (AttributeType type : sorted) {
			boolean contains = name.contains(rawItemName);
			String suffix = type.getDisplaySuffix(contains);
			if(!suffix.equals(""))
				name += (contains ? "" : rawItemName + " of") + " " + suffix;
		}
		
		if(!name.contains(rawItemName))
			name += rawItemName;
		
		if (getEnchantCount() > 0)
			name = ChatColor.RED + "[+" + getEnchantCount() + "] " + name;
		return name;
	}
	
	/**
	 * Enchants this item.
	 */
	public void enchantItem(Player p) {
		boolean success = new Random().nextInt(100) <= SUCCESS_CHANCE[enchantCount];
        GamePlayer gp = GameAPI.getGamePlayer(p);
        
        if (!success) {
        	gp.getPlayerStatistics().setFailedEnchants(gp.getPlayerStatistics().getFailedEnchants() + 1);
        	
        	if (enchantCount <= 8 && isProtected()) {
        		removeProtection();
        		p.sendMessage(ChatColor.RED + "Your enchantment scroll " + ChatColor.UNDERLINE + "FAILED" + ChatColor.RED + " but since you had white scroll protection, your item did not vanish.");
        		return;
        	}
        	
        	p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2F, 1.25F);
        	ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.LAVA, p.getLocation().add(0, 2.5, 0), new Random().nextFloat(), new Random().nextFloat(), new Random().nextFloat(), 1F, 75);
        	p.sendMessage(ChatColor.RED + "While dealing with magical enchants, your item VANISHED.");
        	setDestroyed(true);
        }
        
        applyEnchantStats();
        
        this.enchantCount++;
        removeProtection();
        
        //Play Effect
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.25F);
        Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.YELLOW).withFade(Color.YELLOW).with(FireworkEffect.Type.BURST).trail(true).build();
        fwm.addEffect(effect);
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        gp.getPlayerStatistics().setSuccessfulEnchants(gp.getPlayerStatistics().getSuccessfulEnchants() + 1);
	}
	
	/**
	 * Repairs this item (DR Custom Durability, not vanilla.)
	 */
	public void repair() {
		this.durability = MAX_DURABILITY;
	}
	
	
	/**
	 * Subtracts durability from this item, and alerts the player if it reaches a certain level.
	 * Supplied player is who should receive the damage warning, if any.
	 */
	public void damageItem(Player player, int durability) {
		this.durability -= durability;
		
		if (this.durability <= 1) {
			setDestroyed(true);
			if (player == null)
				return;
			// Item has broken!
			player.getInventory().remove(getItem());
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
			onItemBreak(player);
			player.updateInventory();
			return;
		}
		
		if (player == null)
			return;
		
		// Update the item in the player's inventory.
		int slot = player.getInventory().first(getItem());
		if (slot > -1)
			player.getInventory().setItem(slot, generateItem());
		player.updateInventory();
		
		// Durability warnings.
		for (int i : DURABILITY_WARNINGS) {
			int max = MAX_DURABILITY / i;
			int min = (MAX_DURABILITY - 100) / i;
			if (getDurability() >= min && getDurability() <= max) {
				player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
	            player.sendMessage(ChatColor.RED + " **" + ChatColor.BOLD + i + "% DURABILITY " + ChatColor.RED + "left on " + getItem().getItemMeta().getDisplayName() + ChatColor.RED + "**");
			}
		}
	}
	
	/**
	 * Gets the gem cost of this item to repair via anvil.
	 */
	public int getRepairCost() {
		double totalCost = (100 - getDurabilityPercent()) * getBaseRepairCost(); //Percentage Broken * Cost of 1 percentage
		totalCost *= getGeneratedItemType().getAttributeBank().getRepairMultiplier(getTier()); //Multiplier for this tier.
		totalCost *= getGeneratedItemType().getAttributeBank().getGlobalRepairMultiplier(); //Multiplier for this item type.
		return totalCost > 10 ? (int) Math.round(totalCost) : 10; //Don't allow prices under 10 gems.
	}
	
	/**
	 * Gets the durability value as a percent
	 */
	public double getDurabilityPercent() {
        return Math.round((getDurability() / MAX_DURABILITY) * 100);
	}
	
	/**
	 * Can this item be repaired right now?
	 */
	public boolean canRepair() {
		return getDurability() < MAX_DURABILITY;
	}
	
	/**
	 * Sets this item as protected.
	 */
	public void protectItem() {
		if (isProtected())
			return;
		setTagBool("protected", true);
	}
	
	/**
	 * Unprotects this item.
	 */
	public void removeProtection() {
		if (!isProtected())
			return;
		removeTag("protected");
	}
	
	/**
	 * Rolls the stats for this item.
	 */
	public void rollStats(boolean isReroll) {
		
		//Simulate random order.
		Collections.shuffle(ItemGenerator.modifierObjects);
		ItemMeta meta = getItem().getItemMeta();
		
		Map<ModifierCondition, ItemModifier> conditionMap = new HashMap<>();
		Random rand = new Random();
		
		//  ROLL STATS  //
		for (ItemModifier im : ItemGenerator.modifierObjects) {
			//Is this applicable to the current item material?
			// In the future if we add t6 the generatedItemType system will need to be changed.
			if (im.canApply(getItemType())) {
				ModifierCondition mc = im.tryModifier(meta, getTier(), getRarity());
				
				if (mc != null)
					attemptAddModifier(conditionMap, mc, im, rand, isReroll);
			}
		}
		
		List<ModifierCondition> sortedStats = new ArrayList<>(conditionMap.keySet());
		
		for (ItemModifier modifier : conditionMap.values())
			for (ModifierCondition mc : conditionMap.keySet())
				if(!mc.checkCantContain(modifier.getClass()))
					sortedStats.remove(mc);
		
		//Sort stats by priority
		Collections.sort(sortedStats, (mc1, mc2) -> conditionMap.get(mc1).compareTo(conditionMap.get(mc2)));
		
		Map<AttributeType, ModifierRange> keptAttributes = new HashMap<>();
		
		if (isReroll)
			for(AttributeType attribute : this.attributes.keySet())
				if (attribute.isIncludeOnReroll())
					keptAttributes.put(attribute, this.attributes.get(attribute));
		
		this.attributes.clear();
		
		for (ModifierCondition mc : sortedStats) {
			ItemModifier im = conditionMap.get(mc);
			im.rollAttribute();
			
			//No duplicate attributes.
			if(this.attributes.containsKey(im.getCurrentAttribute()))
				continue;
			
			//  GENERATE NEW STAT VALUE  //
			ModifierRange range = mc.getRange();
			range.generateRandom();
			//Keep the old one if it's not supposed to get rerolled.
			if (keptAttributes.containsKey(im.getCurrentAttribute()))
				range = keptAttributes.get(im.getCurrentAttribute());
			
			//  SAVE NEW STAT  //
			this.attributes.put(im.getCurrentAttribute(), range.clone());
		}
	}
	
	private void attemptAddModifier(Map<ModifierCondition, ItemModifier> conditions, ModifierCondition mc, ItemModifier im, Random rand, boolean reRoll) {
		int belowChance = (mc.getChance() < 0) ? im.getCurrentAttribute().getChance() : mc.getChance();
        
		//Randomly add bonus.
        if (rand.nextInt(100) < belowChance || (im.getCurrentAttribute().isIncludeOnReroll() && reRoll))
        	conditions.put(mc, im);
        
		if (mc.getBonus() != null)
			attemptAddModifier(conditions, mc.getBonus(), im, rand, reRoll);
	}
	
	public static boolean isCustomTool(ItemStack item) {
		return CombatItem.isCombatItem(item) || ProfessionItem.isProfessionItem(item);
	}

	/**
	 * Handles adding durability back when a scrap is used.
	 */
	public void scrapRepair() {
		double newDura = (double)MAX_DURABILITY * (1D / 3D);
		this.durability = Math.min(this.durability + (int)newDura, MAX_DURABILITY);
	}
	
	/**
	 * Returns the repair particle id for being repaired by scrap.
	 */
	public int getRepairParticle(ScrapTier tier) {
		return tier.getParticleId();
	}
}
