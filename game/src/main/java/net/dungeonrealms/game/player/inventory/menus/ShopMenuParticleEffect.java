package net.dungeonrealms.game.player.inventory.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.functional.ecash.ItemParticleSelector;
import net.dungeonrealms.game.item.items.functional.ecash.ItemParticleTrail;
import net.dungeonrealms.game.mechanic.ParticleAPI.ParticleEffect;
import net.dungeonrealms.game.mechanic.PlayerManager;
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
            
            addItem(new ItemParticleSelector(effect)).setOnClick((p, s) -> {
            	List<String> playerEffects = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PARTICLES, getPlayer().getUniqueId());
                
            	if (!playerEffects.isEmpty() && playerEffects.contains(effect.name())) {
            		getPlayer().sendMessage(ChatColor.RED + "You already own the " + ChatColor.BOLD + ChatColor.UNDERLINE + effect.getDisplayName() + ChatColor.RED + " effect.");
            		return false;
                }
            	
            	DatabaseAPI.getInstance().update(getPlayer().getUniqueId(), EnumOperators.$PUSH, EnumData.PARTICLES, effect.name(), true);
            	DatabaseAPI.getInstance().update(getPlayer().getUniqueId(), EnumOperators.$SET, EnumData.ACTIVE_TRAIL, effect.name(), true);
            	getPlayer().sendMessage(ChatColor.GREEN + "You have purchased the " + effect.getDisplayName() + " effect.");

            	if (!PlayerManager.hasItem(getPlayer(), ItemType.PARTICLE_TRAIL))
            		getPlayer().getInventory().addItem(new ItemParticleTrail().generateItem());
            	getPlayer().closeInventory();
            	return true;
            }).setPrice(price);
        }
	}
}
