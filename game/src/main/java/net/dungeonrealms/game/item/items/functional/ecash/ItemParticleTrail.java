package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;

public class ItemParticleTrail extends FunctionalItem implements ItemClickListener {

	public ItemParticleTrail() {
		super(ItemType.PARTICLE_TRAIL);
		setUntradeable(true);
	}
	
	public ItemParticleTrail(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		PlayerWrapper pw = PlayerWrapper.getWrapper(player);
		
		if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
			pw.disableTrail();
            player.sendMessage(ChatColor.GREEN + "Your have disabled your trail.");
            return;
        }
		
        if (pw.getActiveTrail() == null) {
        	player.sendMessage(ChatColor.RED + "You don't have an active trail, please enter the trails section in your profile to set one.");
            player.closeInventory();
            return;
        }
        
        pw.setActiveTrail(pw.getActiveTrail());
        player.sendMessage(ChatColor.GREEN + "Your active trail has been activated.");
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Effect";
	}

	@Override
	protected String[] getLore() {
		return new String[] { ChatColor.DARK_GRAY + "Equips your active Effect." };
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.EYE_OF_ENDER);
	}
}
