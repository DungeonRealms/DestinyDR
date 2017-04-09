package net.dungeonrealms.game.item.items.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Fishing.EnumFish;
import net.dungeonrealms.game.profession.fishing.FishBuff;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemFish extends FunctionalItem {

	@Getter @Setter
	private boolean cooked;
	
	@Getter @Setter
	private EnumFish fishType;
	
	@Getter @Setter
	private FishBuff fishBuff;
	
	@Getter @Setter
	private FishingTier tier;
	
	public ItemFish(FishingTier tier, EnumFish fishType) {
		super(ItemType.FISH);
		setTier(tier);
		setFishType(fishType);
		
		if (getTier().getBuffChance() >= new Random().nextInt(100))
			setFishBuff(Fishing.getRandomBuff(getTier()));
	}
	
	public ItemFish(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		setCooked(getTagBool("cooked"));
		setTier(FishingTier.values()[getTagInt(TIER) - 1]);
		if(hasTag("buffType"))
			setFishBuff(Fishing.loadBuff(getTag()));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagBool("cooked", isCooked());
		setTagInt(TIER, getTier().getTier());
		if (getFishBuff() != null)
			getFishBuff().save(getTag());
		super.updateItem();
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		evt.setUsed(true);
		getFishBuff().applyBuff(evt.getPlayer());
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		FishBuff buff = getFishBuff();
		String name = (buff != null ? buff.getItemName(getFishType()) : getFishType().getName());
		return getTier().getColor() + (isCooked() ? "Grilled" : "Raw") + " " + name;
	}

	@Override
	protected String[] getLore() {
		List<String> lore = new ArrayList<>();
		if (getFishBuff() != null)
			lore.add(getFishBuff().getLore());
		lore.add(ChatColor.RED + "-" + getTier().getHungerAmount() + "% HUNGER");
		lore.add(isCooked() ? getFishType().getDesciption() : "A freshly caught fish.");
		return lore.toArray(new String[lore.size()]);
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		ItemStack fish = new ItemStack(isCooked() ? Material.COOKED_FISH : Material.RAW_FISH);
		if (getFishBuff() != null)
			fish.setDurability((short)getFishBuff().getBuffType().getFishMeta());
		return fish;
	}
}
