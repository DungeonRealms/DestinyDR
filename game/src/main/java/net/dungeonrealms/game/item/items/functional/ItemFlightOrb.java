package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.RealmProperty;
import net.dungeonrealms.game.world.realms.Realms;

public class ItemFlightOrb extends FunctionalItem {

	public ItemFlightOrb(ItemStack item) {
		super(item);
	}
	
	public ItemFlightOrb() {
		this(true);
	}
	
	public ItemFlightOrb(boolean applyAntiDupe) {
		super(ItemType.ORB_OF_FLIGHT);
		setAntiDupe(applyAntiDupe);
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.AQUA + "Orb of Flight";
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.GRAY + "Enables " + ChatColor.UNDERLINE + "FLYING" + ChatColor.GRAY + " in realm for the owner ",
				ChatColor.GRAY + "and all builders for 30 minute(s).",
				ChatColor.RED + "" + ChatColor.BOLD + "REQ:" + ChatColor.RED + " Active Orb of Peace"};
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(ItemClickEvent evt) {
		Player p = evt.getPlayer();
		
		if (Cooldown.hasCooldown(p.getUniqueId()))
			return;
        Cooldown.addCooldown(p.getUniqueId(), 1000);
        
		Realm realm = Realms.getInstance().getRealm(p.getWorld());
		
		if (realm == null) {
			p.sendMessage(ChatColor.RED + "You must be in your realm to use this.");
			return;
		}
		
		if (!realm.isOwner(p) && !Rank.isTrialGM(p)) {
			p.sendMessage(ChatColor.RED + "You may only use an " + ChatColor.UNDERLINE + "Orb of Flight" + ChatColor.RED + " in your OWN realm.");
			return;
		}
		
		if(realm.isChaotic()) {
			p.sendMessage(ChatColor.RED + "You cannot use this in a " + ChatColor.UNDERLINE + "chaotic" + ChatColor.RED + " realm.");
			return;
		}
		
		evt.setUsed(true);
		p.sendMessage("");
        p.sendMessage(ChatColor.AQUA + "Your realm will now be a " + ChatColor.BOLD + "FLY ENABLED ZONE" + ChatColor.AQUA
                + " for 30 minute(s), or until logout.");
        p.sendMessage(ChatColor.GRAY + "Only YOU and anyone you add to your build list will be able to fly in your realm.");
        
        for (Player pl : realm.getWorld().getPlayers()) {
            if (!realm.canBuild(pl))
                continue;
            pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "FLYING ENABLED");
            pl.setAllowFlight(true);
        }

        RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getRealmProperties().get("flight");
        property.setExpiry(System.currentTimeMillis() + 1800000L);

        property.setValue(true);
        property.setAcknowledgeExpiration(true);
        
        realm.updateWGFlags();
        realm.updateHologram();
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.FIREWORK_CHARGE);
	}
	
	public static boolean isFlightOrb(ItemStack item) {
		return isType(item, ItemType.ORB_OF_FLIGHT);
	}
}
