package net.dungeonrealms.game.player.inventory.menus;


import org.bukkit.entity.Player;

import net.dungeonrealms.game.item.items.functional.ecash.ItemParticleSelector;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class ShopMenuParticleEffect extends ShopMenu {

	public ShopMenuParticleEffect(Player player) {
		super(player, "E-Cash Effects", 2);
	}

	@Override
	protected void setItems() {
		for (ParticleEffect effect : ParticleEffect.values()) {
            if (effect == ParticleEffect.BUBBLE || effect == ParticleEffect.SPELL || effect == ParticleEffect.VALENTINES
            		|| effect == ParticleEffect.LARGE_SMOKE || effect == ParticleEffect.LAVA) 
                continue;

            // Holiday Effects | Only to be sold in the BC shop.
            if (effect == ParticleEffect.VALENTINES)
                continue;

            int price = 650;
            if (effect == ParticleEffect.RED_DUST || effect == ParticleEffect.NOTE
            		|| effect == ParticleEffect.FLAME || effect == ParticleEffect.PORTAL
                    || effect == ParticleEffect.CLOUD || effect == ParticleEffect.SMALL_SMOKE)
                price = 1250;
            
            addItem(new ItemParticleSelector(effect)).setPrice(price);
        }
	}
}
