package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.teleportation.TeleportAPI;
import net.dungeonrealms.game.world.teleportation.Teleportation;

public class ItemHearthstone extends FunctionalItem implements ItemInventoryListener {

	private Player player;
	
	public ItemHearthstone(ItemStack item) {
		this((Player)null);
		setUndroppable(true);
	}
	
	public ItemHearthstone(Player player) {
		super(ItemType.HEARTHSTONE);
		this.player = player;
	}
	
	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "Hearthstone";
	}

	@Override
	protected String[] getLore() {
		return new String[]{
                ChatColor.DARK_GRAY + "Home location",
                "",
                "Use: Returns you to " + ChatColor.YELLOW + (player != null ? TeleportAPI.getLocationFromDatabase(player.getUniqueId()) : "Error Village"),
                "",
                ChatColor.YELLOW + "Speak to an Innkeeper to change location."};
	}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		this.player = evt.getPlayer();
		evt.setCancelled(true);
		evt.closeInventory();
		if (!CombatLog.isInCombat(evt.getPlayer())) {
			
            if (TeleportAPI.isPlayerCurrentlyTeleporting(evt.getPlayer().getUniqueId())) {
            	evt.getPlayer().sendMessage("You cannot restart a teleport during a cast!");
                return;
            }
            
            if (TeleportAPI.canUseHearthstone(player))
                Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.HEARTHSTONE, null);
        
		} else {
			evt.getPlayer().sendMessage(ChatColor.RED + "You are in combat! Please wait (" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
		}
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INVENTORY_PICKUP;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.QUARTZ);
	}
}
