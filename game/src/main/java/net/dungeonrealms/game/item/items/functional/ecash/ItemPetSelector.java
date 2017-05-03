package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.md_5.bungee.api.ChatColor;

@Getter @Setter
public class ItemPetSelector extends ItemGeneric {

	private EnumPets pet;
	private String petName;
	
	public ItemPetSelector(ItemStack item) {
		super(item);
		setPet(EnumPets.valueOf(getTagString("pet")));
		setPetName(hasTag("petName") ? getTagString("petName") : getPet().getDisplayName());
	}
	
	public ItemPetSelector(EnumPets pet) {
		super(ItemType.PET_SELECTOR);
		this.pet = pet;
		this.petName = getPet().getDisplayName();
	}
	
	@Override
	public void updateItem() {
		setTagString("pet", getPet().name());
		setTagString("petName", getPetName());
		getMeta().setDisplayName(ChatColor.YELLOW + getPetName());
		super.updateItem();
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.MONSTER_EGG, 1, (short)getPet().getEggShortData());
	}
}
