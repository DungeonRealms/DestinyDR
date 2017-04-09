package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mechanic.ParticleAPI;

public class ItemParticleTrail extends FunctionalItem {

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
		
		if (DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.containsKey(player)) {
            DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.remove(player);
            player.sendMessage(ChatColor.GREEN + "Your have disabled your trail.");
            return;
        }
        
		String trailType = (String) DatabaseAPI.getInstance().getData(EnumData.ACTIVE_TRAIL, player.getUniqueId());
        if (trailType == null || trailType.equals("")) {
            player.sendMessage(ChatColor.RED + "You don't have an active trail, please enter the trails section in your profile to set one.");
            player.closeInventory();
            return;
        }
        
        DonationEffects.getInstance().PLAYER_PARTICLE_EFFECTS.put(player, ParticleAPI.ParticleEffect.getByName(trailType));
        player.sendMessage(ChatColor.GREEN + "Your active trail has been activated.");
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

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
