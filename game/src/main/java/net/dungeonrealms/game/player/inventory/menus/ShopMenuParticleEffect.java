package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.functional.ecash.ItemParticleSelector;
import net.dungeonrealms.game.item.items.functional.ecash.ItemParticleTrail;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.mechanic.PlayerManager;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;

public class ShopMenuParticleEffect extends ShopMenu {

	public ShopMenuParticleEffect(Player player) {
		super(player, "E-Cash Effects", 2);
	}

	@Override
	protected void setItems() {
		for (ParticleEffect effect : ParticleEffect.values()) {
			if (!effect.isEnabled())
				continue;

            addItem(new ItemParticleSelector(effect)).setOnClick((p, s) -> {
            	PlayerWrapper pw = PlayerWrapper.getWrapper(p);
            	Set<ParticleEffect> effects = pw.getParticles();
            	
            	if (effects.contains(effect)) {
            		p.sendMessage(ChatColor.RED + "You already own the " + ChatColor.BOLD + ChatColor.UNDERLINE + effect.getDisplayName() + ChatColor.RED + " effect.");
            		return false;
            	}
            	
            	getPlayer().sendMessage(ChatColor.GREEN + "You have purchased the " + effect.getDisplayName() + " effect.");
            	pw.getParticles().add(effect);
            	pw.setActiveTrail(effect);
            	if (!PlayerManager.hasItem(getPlayer(), ItemType.PARTICLE_TRAIL))
            		getPlayer().getInventory().addItem(new ItemParticleTrail().generateItem());
            	getPlayer().closeInventory();
            	return true;
            }).setPrice(effect.getPrice());
        }
	}
}
