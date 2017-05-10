package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.FriendHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.RealmMaterialFactory;
import net.dungeonrealms.game.world.realms.RealmTier;
import net.dungeonrealms.game.world.realms.Realms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemPortalRune extends FunctionalItem implements ItemClickListener {

	private Player owner;
	
	public ItemPortalRune(Player player) {
		super(ItemType.PORTAL_RUNE);
		setUndroppable(true);
		setUntradeable(true);
		this.owner = player;
	}
	
	public ItemPortalRune(ItemStack item) {
		super(item);
		setUntradeable(true);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		Realm realm = Realms.getInstance().getRealm(player.getWorld());
		
		if (Cooldown.hasCooldown(player.getUniqueId()))
        	return;
        Cooldown.addCooldown(player.getUniqueId(), 1000);
		
		if (evt.isSneaking()) {
			if (evt.isLeftClick() && evt.hasEntity()) {
				//  Add/Remove Builder
				if (!GameAPI.isPlayer(evt.getClickedEntity()))
					return;
				
				Player target = (Player)evt.getClickedEntity();
				if (realm == null || !realm.isOwner(player)) {
		        	player.sendMessage(ChatColor.RED + "You must be in your realm to add builders.");
		            return;
		        }

		        if (!(FriendHandler.getInstance().areFriends(player, target.getUniqueId()))) {
		            player.sendMessage(ChatColor.RED + "Cannot add a non-buddy to realm build list.");
		            player.sendMessage(ChatColor.GRAY + "Goto your friends list in the character profile to add '" + ChatColor.BOLD
		            		+ target.getName() + ChatColor.GRAY + "' as friend.");
		            return;
		        }

		        if (!realm.getBuilders().contains(target.getUniqueId())) {
		            player.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE + "ADDED " + ChatColor.RESET + ChatColor.GREEN + "" + ChatColor.BOLD + target.getName()
		                    + ChatColor.GREEN + " to your realm builder list.");
		            player.sendMessage(ChatColor.GRAY + target.getName()
		                    + " can now place/destroy blocks in your realm until you logout of your current game session.");
		            target.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "ADDED" + ChatColor.GREEN + " to " + player.getName() + "'s build list.");
		            target.sendMessage(ChatColor.GRAY + "You can now place/destroy blocks in their realm until the end of their game session.");

		            realm.getBuilders().add(target.getUniqueId());

		            if (realm.getProperty("flight")) {
		                target.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
		                target.setAllowFlight(true);
		            }

		        } else {
		            player.sendMessage(ChatColor.RED + "" + ChatColor.UNDERLINE + "REMOVED " + ChatColor.RESET + ChatColor.RED + "" + ChatColor.BOLD + target.getName()
		                    + ChatColor.RED + " from your realm builder list.");
		            player.sendMessage(ChatColor.GRAY + target.getName() + " can no longer place/destroy blocks in your realm.");
		            target.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "REMOVED " + ChatColor.RED + "from " + player.getName() + "'s builder list.");
		            target.sendMessage(ChatColor.GRAY + "You can no longer place/destroy blocks/fly in their realm.");

		            realm.getBuilders().remove(target.getUniqueId());

		            if (realm.getProperty("flight"))
		                target.setAllowFlight(false);

		        }
			} else if (evt.isRightClick()) {
				// Upgrade realm.
				promptRealmUpgrade(player, realm);
			}
			return;
		}
		
		if (evt.isLeftClick()) {
			//Open Material Store.
			if((evt.hasBlock() && evt.getClickedBlock().getType() == Material.PORTAL))
        		return;

            if (!Realms.getInstance().isInRealm(player)) {
                player.sendMessage(ChatColor.RED + "You must be in your realm to open the realm material store.");
                return;
            }

            // OPENS STORE //
            RealmMaterialFactory.getInstance().openMaterialStore(player, 0);
		} else if (evt.isRightClick()) {
			//Open Realm / Move it.
			
			if (realm != null && realm.isOwner(player)) {
                if (evt.hasBlock() && evt.getClickedBlock().getLocation() != null) {
                    Location newLocation = evt.getClickedBlock().getLocation().clone().add(0, 2, 0);

                    if (GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 3, Material.LADDER)
                    		|| GameAPI.isMaterialNearby(newLocation.clone().getBlock(), 5, Material.ENDER_CHEST)
                    		|| newLocation.getBlock().getType() != Material.AIR
                    		|| newLocation.clone().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
                       	player.sendMessage(ChatColor.RED + "You cannot place a realm portal here!");
                        return;
                    }
                    
                    realm.setRealmSpawn(newLocation);
                    return;
                }
            }

            //Open the player's realm.
            if (evt.hasBlock()) {
            	realm = Realms.getInstance().getOrCreateRealm(player, PlayerWrapper.getWrapper(player).getCharacterID());
            	realm.openPortal(player, evt.getClickedBlock().getLocation());
            }
		}
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Realm Portal Rune";
	}

	@Override
	protected String[] getLore() {
		RealmTier tier = Realms.getRealmTier(this.owner.getUniqueId());
		int dim = tier.getDimensions();
		return new String[] {
				"Tier: " + tier.getTier() + "/" + RealmTier.values().length + " [" + dim + "x" + dim + "x" + dim + "]",
				ChatColor.LIGHT_PURPLE + "Right Click: " + ChatColor.GRAY + "Open Portal",
                ChatColor.LIGHT_PURPLE + "Left Click: " + ChatColor.GRAY + "Realm Shop",
                ChatColor.LIGHT_PURPLE + "Sneak-Right Click: " + ChatColor.GRAY + "Upgrade Realm",
                ChatColor.LIGHT_PURPLE + "Sneak-Left Click: " + ChatColor.GRAY + "Add Builder"
		};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.NETHER_STAR);
	}

	
	private void promptRealmUpgrade(Player p, Realm realm) {
		if (DungeonRealms.getInstance().getRebootTime() - System.currentTimeMillis() < 5 * 60 * 1000) {
			p.sendMessage(ChatColor.RED + "This shard is rebooting in less than 5 minutes, so you cannot upgrade this realm on this shard.");
			return;
		}
		
		if (realm == null || !realm.isOwner(p)) {
			p.sendMessage(ChatColor.RED + "You must be inside your realm to modify its size.");
			return;
		}
            
		if (realm.getTier().getTier() >= RealmTier.values().length) {
			p.sendMessage(ChatColor.RED + "You have upgraded your realm to it's final tier");
			return;
		}
		
		int upgradeCost = RealmTier.getByTier(realm.getTier().getTier() + 1).getPrice();
		
		p.sendMessage("");
		p.sendMessage(ChatColor.DARK_GRAY + "           *** " + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Realm Upgrade Confirmation"
				+ ChatColor.DARK_GRAY + " ***");
		p.sendMessage(ChatColor.DARK_GRAY + "FROM Tier " + ChatColor.LIGHT_PURPLE + realm.getTier().getTier() + ChatColor.DARK_GRAY + " TO " + ChatColor.LIGHT_PURPLE
				+ (realm.getTier().getTier() + 1));
		p.sendMessage(ChatColor.DARK_GRAY + "Upgrade Cost: " + ChatColor.LIGHT_PURPLE + "" + upgradeCost + " Gem(s) from your bank");
		p.sendMessage("");
		p.sendMessage(ChatColor.GRAY + "Enter '" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "CONFIRM" + ChatColor.GRAY + "' to confirm realm upgrade.");
		p.sendMessage("");
		p.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "WARNING:" + ChatColor.RED + " Realm upgrades are " + ChatColor.BOLD + ChatColor.RED + "NOT"
				+ ChatColor.RED + " reversible or refundable. Type 'cancel' to void this upgrade request.");
		p.sendMessage("");
		
		PlayerWrapper pw = PlayerWrapper.getWrapper(p);
		
		final Realm r = realm;
		Chat.promptPlayerConfirmation(p, () -> {
                
			if (pw.getGems() < upgradeCost) {
				p.sendMessage(ChatColor.RED + "You do not have enough GEM(s) in your bank to purchase this upgrade. Upgrade cancelled.");
				p.sendMessage(ChatColor.RED + "COST: " + upgradeCost + " gems");
				return;
			}
			
			pw.subtractGems(upgradeCost);
			Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> r.upgradeRealm(p));
			
		}, () -> p.sendMessage(ChatColor.RED + "Realm upgrade cancelled."));
		
		return;
	}
}
