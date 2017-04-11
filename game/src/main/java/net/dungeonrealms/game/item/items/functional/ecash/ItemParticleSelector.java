package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.md_5.bungee.api.ChatColor;

public class ItemParticleSelector extends ItemGeneric {

	@Getter @Setter
	private ParticleEffect effect;
	
	public ItemParticleSelector(ItemStack item) {
		super(item);
	}
	
	public ItemParticleSelector(ParticleEffect effect) {
		super(ItemType.PARTICLE_SELECTOR);
		setEffect(effect);
	}
	
	@Override
	public void loadItem() {
		setEffect(ParticleEffect.valueOf(getTagString("effect")));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagString("effect", getEffect().name());
		getMeta().setDisplayName(ChatColor.YELLOW + getEffect().getDisplayName());
		super.updateItem();
	}

	@Override
	protected ItemStack getStack() {
		return getEffect().getSelectionItem();
	}

}
