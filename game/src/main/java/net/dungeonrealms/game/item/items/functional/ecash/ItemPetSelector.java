package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.md_5.bungee.api.ChatColor;

public class ItemPetSelector extends ItemGeneric {

	@Getter @Setter
	private EnumPets pet;
	
	public ItemPetSelector(ItemStack item) {
		super(item);
	}
	
	public ItemPetSelector(EnumPets pet) {
		super(ItemType.PET_SELECTOR);
	}
	
	@Override
	public void loadItem() {
		setPet(EnumPets.valueOf(getTagString("pet")));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagString("pet", getPet().name());
		getMeta().setDisplayName(ChatColor.YELLOW + getPet().getDisplayName());
		super.updateItem();
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.MONSTER_EGG, 1, (short)getPet().getEggShortData());
	}
}
