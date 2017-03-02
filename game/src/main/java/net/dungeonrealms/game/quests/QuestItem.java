package net.dungeonrealms.game.quests;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.world.item.Item.ItemRarity;
import net.dungeonrealms.game.world.item.Item.ItemTier;
import net.dungeonrealms.game.world.item.Item.ItemType;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonObject;

public class QuestItem implements ISaveable {
	
	private String drItemName;
	
	private Material itemMaterial;
	private int itemAmount;
	private String displayName;
	private boolean isSoulBound;
	private int durability = 100;
	
	private boolean isGeneratedItem;
	private ItemTier tier = ItemTier.TIER_1;
	private ItemRarity rarity = ItemRarity.COMMON;
	private ItemOption generationType = ItemOption.WEAPON;
	
	public QuestItem(ItemStack i){
		this.loadItem(i);
	}
	
	public QuestItem(JsonObject obj){
		this.fromFile(obj);
	}
	
	public void resetItem(){
		this.itemMaterial = Material.BARRIER;
		this.drItemName = null;
		this.itemAmount = 1;
		this.displayName = null;
		this.isSoulBound = false;
		this.durability = 100;
		this.isGeneratedItem = false;
		this.tier = ItemTier.TIER_1;
		this.rarity = ItemRarity.COMMON;
		this.generationType = ItemOption.WEAPON;
		this.isGeneratedItem = false;
	}
	
	/**
	 * Converts this Quest Item into an actual itemStack.
	 */
	public ItemStack createItem(Player player){
		ItemStack created = new ItemStack(Material.DIRT);
		if(this.isGeneratedItem()){
			ItemGenerator generator = new ItemGenerator();
			generator.setTier(this.tier);
			generator.setRarity(this.rarity);
			
			switch(this.generationType){
				case ARMOR:
					generator.setType(ItemType.getRandomArmor());
					created = generator.generateItem().getItem();
					break;
				case WEAPON:
					generator.setType(ItemType.getRandomWeapon());
					created = generator.generateItem().getItem();
					break;
				case ORB:
					created = ItemManager.createOrbofAlteration();
					break;
				case PICKAXE:
					created = ItemManager.createPickaxe(this.tier.getTierId());
					break;
				case FISHING_ROD:
					created = ItemManager.createFishingPole(this.tier.getTierId());
					break;
				case WEAPON_ENCH_SCROLL:
					created = ItemManager.createWeaponEnchant(this.tier.getTierId());
					break;
				case ARMOR_ENCH_SCROLL:
					created = ItemManager.createArmorEnchant(this.tier.getTierId());
					break;
				case PROT_SCROLL:
					created = ItemManager.createProtectScroll(this.tier.getTierId());
					break;
				default:
					System.out.println("Can't generate quest item " + this.generationType.name());
			}
		}else{
			if(this.isDRItem()){
				created = ItemGenerator.getNamedItem(this.drItemName);
			}else{
				if(this.itemMaterial == null || this.itemMaterial == Material.AIR)
					this.itemMaterial = Material.STONE;
				created = new ItemStack(this.itemMaterial, this.itemAmount);
				ItemMeta meta = created.getItemMeta();
				if(this.displayName != null)
					meta.setDisplayName(this.displayName);
				created.setItemMeta(meta);
			}
		}
		
		if(this.isSoulBound)
			created = ItemManager.makeSoulBound(created);
		
		if(this.durability != 100)
			RepairAPI.setCustomItemDurability(created, durability * (1500 / 100) );
		
		return created;
	}
	
	/**
	 * Checks whether an itemstack matches this quest item.
	 */
	public boolean doesItemMatch(ItemStack item){
		if(this.isSoulBound != GameAPI.isItemSoulbound(item))
			return false;
		if(this.isDRItem())
			return this.drItemName.equals(GameAPI.getCustomID(item));
		if(item == null)
			return false;
		if(item.getType() != this.itemMaterial)
			return false;
		if(item.getAmount() < this.itemAmount)
			return false;
		if(this.displayName != null && !item.getItemMeta().getDisplayName().equals(this.displayName))
			return false;
		return true;
	}

	@Override
	public void fromFile(JsonObject obj) {
		
		if(obj.has("soulbound"))
			this.isSoulBound = obj.get("soulbound").getAsBoolean();
		
		if(obj.has("durability"))
			this.durability = obj.get("durability").getAsInt();
		
		if(obj.has("generatedItem") && obj.get("generatedItem").getAsBoolean()){
			this.setItemRarity(ItemRarity.valueOf(obj.get("itemRarity").getAsString()));
			this.setItemTier(ItemTier.valueOf(obj.get("itemTier").getAsString()));
			this.setGenerationType(ItemOption.valueOf(obj.get("generationType").getAsString()));
			return;
		}
		
		if(obj.has("drItemName")){
			this.drItemName = obj.get("drItemName").getAsString();
			return;
		}
		
		if(obj.has("displayName"))
			this.displayName = obj.get("displayName").getAsString();
		if(obj.has("amount"))
			this.itemAmount = obj.get("amount").getAsInt();
		if(obj.has("type"))
			this.itemMaterial = Material.valueOf(obj.get("type").getAsString());
	}

	@Override
	public JsonObject toJSON() {
		JsonObject obj = new JsonObject();
		obj.addProperty("soulbound", this.isSoulBound);
		obj.addProperty("durability", this.durability);
		
		if(this.isGeneratedItem()){
			obj.addProperty("generatedItem", true);
			obj.addProperty("itemTier", this.tier.name());
			obj.addProperty("itemRarity", this.rarity.name());
			obj.addProperty("generationType", this.generationType.name());
			return obj;
		}
		
		if(this.isDRItem()){
			obj.addProperty("drItemName", this.drItemName);
			return obj;
		}
		obj.addProperty("displayName", this.displayName);
		obj.addProperty("amount", this.itemAmount);
		obj.addProperty("type", this.itemMaterial.name());
		return obj;
	}

	@Override
	public String getFileName() {
		return null;
	}

	public int getAmount() {
		return this.itemAmount;
	}
	
	public void setAmount(int a){
		this.itemAmount = a;
	}
	
	public void setDurability(int d){
		this.durability = d;
	}
	
	public int getDurability(){
		return this.durability;
	}
	
	public Material getType(){
		return this.itemMaterial;
	}

	public void setType(Material mat) {
		this.itemMaterial = mat;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public void setDisplayName(String name){
		this.displayName = name;
	}
	
	/**
	 * Take an ItemStack and load it into this quest item.
	 * @param Item to load from
	 */
	public void loadItem(ItemStack i) {
		String drItemName = GameAPI.getCustomID(i);
		if(drItemName != null){
			this.drItemName = drItemName;
			return;
		}
		this.itemMaterial = i.getType();
		this.itemAmount = i.getAmount();
		this.durability = i.getDurability();
		this.displayName = i.getItemMeta().getDisplayName();
		if(this.displayName == null)
			this.displayName = this.itemMaterial.name();
		this.isSoulBound = GameAPI.isItemSoulbound(i);
	}
	
	public boolean isSoulbound(){
		return this.isSoulBound;
	}
	
	public void setSoulbound(boolean questItem){
		this.isSoulBound = questItem;
	}

	public boolean isDRItem() {
		return this.drItemName != null;
	}
	
	public String getDRItemName(){
		return this.drItemName;
	}
	
	public void setDRItemName(String dr){
		this.drItemName = dr;
	}
	
	/**
	 * Returns the ItemTier (Only if it's a custom generated item.)
	 * @return
	 */
	public ItemTier getTier(){
		return this.tier;
	}
	
	/**
	 * Returns the ItemRarity (Only if it's a custom generated item.)
	 */
	public ItemRarity getRarity(){
		return this.rarity;
	}
	
	public boolean isGeneratedItem(){
		return this.isGeneratedItem;
	}
	
	public void setItemRarity(ItemRarity r){
		this.rarity = r;
	}
	
	public void setItemTier(ItemTier t){
		this.tier = t;
	}
	
	public void setCustomGenerated(){
		this.isGeneratedItem = true;
	}
	
	public void setGenerationType(ItemOption type){
		this.generationType = type;
		this.setCustomGenerated();
	}
	
	public enum ItemOption {
		ARMOR,
		WEAPON,
		ORB,
		PICKAXE,
		FISHING_ROD,
		PROT_SCROLL,
		WEAPON_ENCH_SCROLL,
		ARMOR_ENCH_SCROLL;
	}
}
