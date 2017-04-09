package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.dungeonrealms.game.world.teleportation.Teleportation;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemTeleportBook extends FunctionalItem {

	@Getter @Setter
	private TeleportLocation teleportLocation;
	
	public ItemTeleportBook() {
		this(TeleportLocation.getRandomBookTP());
	}
	
	public ItemTeleportBook(TeleportLocation tl) {
		super(ItemType.TELEPORT_BOOK);
	}
	
	public ItemTeleportBook(ItemStack item) {
		super(item);
	}
	
	@Override
	public void loadItem() {
		setTeleportLocation(TeleportLocation.valueOf(getTagString("location")));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagString("location", getTeleportLocation().name());
		super.updateItem();
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		GamePlayer gp = GameAPI.getGamePlayer(player);
		if (gp.isJailed()) {
            player.sendMessage(ChatColor.RED + "You have been jailed.");
            return;
        }
		
		if (CombatLog.isInCombat(player)) {
			player.sendMessage(ChatColor.RED + "You are in combat! " + "(" + ChatColor.UNDERLINE + CombatLog.COMBAT.get(player) + "s" + ChatColor.RED + ")");
			return;
		}
		
		if (!GameAPI.isMainWorld(player.getWorld())) {
            player.sendMessage(ChatColor.RED + "You can only use teleport books in the main world.");
            return;
        }
		evt.setUsed(true);
		
		if (!getTeleportLocation().canBeABook()) {
			player.sendMessage(ChatColor.RED + "This teleport book is invalid, so it has vanished into the wind.");
			GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Removed 1x " + getTeleportLocation().getDisplayName() + " teleport books from " + player.getName() + ".");
			return;
		}
        
		if (!getTeleportLocation().canTeleportTo(player)) {
			player.sendMessage(ChatColor.RED + "You cannot warp to Safezones whilst chaotic.");
			return;
		}
		
		Teleportation.getInstance().teleportPlayer(player.getUniqueId(), Teleportation.EnumTeleportType.TELEPORT_BOOK, getTeleportLocation());
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return ChatColor.WHITE + "" + ChatColor.BOLD + "Teleport: " + ChatColor.WHITE + teleportLocation.getDisplayName();
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				"(Right-Click) Teleport to " + teleportLocation.getDisplayName()
        		+ ( getTeleportLocation().isChaotic() ? ChatColor.RED + " WARNING: CHAOTIC ZONE" : "")};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.BOOK);
	}
	
	public static boolean isTeleportBook(ItemStack item) {
		return isType(item, ItemType.TELEPORT_BOOK);
	}

}
