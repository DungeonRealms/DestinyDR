package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.quests.Quests;
import net.dungeonrealms.game.quests.objectives.ObjectiveOpenJournal;
import net.dungeonrealms.game.world.shops.Shop;
import net.dungeonrealms.game.world.shops.ShopMechanics;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemPlayerJournal extends FunctionalItem {
	
	public ItemPlayerJournal() {
		super(ItemType.PLAYER_JOURNAL);
		setUndroppable(true);
	}
	
	public ItemPlayerJournal(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		if (evt.isSneaking() && evt.isRightClick() && evt.hasBlock()) {
			// Open Shop.
			if (DungeonRealms.getInstance().isEventShard) {
				player.sendMessage(ChatColor.RED + "You cannot create a shop on this shard.");
				return;
			}
			
			if (ShopMechanics.ALLSHOPS.containsKey(player.getName())) {
                Shop shop = ShopMechanics.getShop(player.getName());
                player.sendMessage(ChatColor.YELLOW + "You already have an open shop on " + ChatColor.UNDERLINE + "this" + ChatColor.YELLOW + " server.");
                player.sendMessage(ChatColor.GRAY + "Shop Location: " + (int) shop.block1.getLocation().getX() + ", " + (int) shop.block1.getLocation().getY() + ", " + (int) shop.block1.getLocation().getZ());
                return;
            }
			
			GamePlayer gp = GameAPI.getGamePlayer(player);
			Block b1 = evt.getClickedBlock().getLocation().clone().add(0, 1, 0).getBlock();
			Block b2 = evt.getClickedBlock().getLocation().clone().add(1, 1, 0).getBlock();
            
			if (b1.getType() != Material.AIR || b2.getType() != Material.AIR)
				return;
			
			boolean foundNearbyBlocks = GameAPI.isMaterialNearby(b1, 2, Material.CHEST)
					|| GameAPI.isMaterialNearby(b1, 10, Material.ENDER_CHEST) || GameAPI.isMaterialNearby(b1, 3, Material.PORTAL);
			
			if (!GameAPI.isInSafeRegion(b1.getLocation()) || foundNearbyBlocks || !GameAPI.isMainWorld(b1.getWorld())) {
				player.sendMessage(ChatColor.RED + "You cannot place a shop here.");
				return;
			}
			
			if (gp != null && !gp.hasShopOpen()) {
                Storage storage = BankMechanics.getStorage(player.getUniqueId());
                if(storage == null){
                    player.sendMessage(ChatColor.RED + "Please wait for your storage bin to load...");
                    return;
                }
                
                ShopMechanics.setupShop(evt.getClickedBlock(), player.getUniqueId());
            } else {
                player.sendMessage(ChatColor.RED + "You have a shop open already! It may be on another shard.");
            }
		}
		
		//Open a real character journal.
		//Not saving a full one in a player's inventory will save CPU power and storage space.
		GameAPI.openBook(evt.getPlayer(), ItemManager.createCharacterJournal(evt.getPlayer()));
		Quests.getInstance().triggerObjective(evt.getPlayer(), ObjectiveOpenJournal.class);
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + "" + ChatColor.BOLD + "Character Journal";
	}

	@Override
	protected String[] getLore() {
		return null;
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.WRITTEN_BOOK);
	}
}
