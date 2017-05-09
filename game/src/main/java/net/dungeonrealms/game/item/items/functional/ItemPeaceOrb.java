package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.util.Cooldown;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.world.realms.Realm;
import net.dungeonrealms.game.world.realms.RealmProperty;
import net.dungeonrealms.game.world.realms.Realms;

public class ItemPeaceOrb extends FunctionalItem implements ItemClickListener {

	public ItemPeaceOrb(ItemStack item) {
		super(item);
	}
	
	public ItemPeaceOrb() {
		this(true);
	}
	
	public ItemPeaceOrb(boolean antiDupe) {
		super(ItemType.ORB_OF_PEACE);
		setAntiDupe(antiDupe);
	}
	
	@Override
	protected String getDisplayName() {
		return ChatColor.LIGHT_PURPLE + "Orb of Peace";
	}

	@Override
	protected String[] getLore() {
		return new String[]{
				"Set realm to " + ChatColor.UNDERLINE + "SAFE ZONE" + ChatColor.GRAY + " for 1 hour(s)."};
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
			p.sendMessage(ChatColor.RED + "You may only use an " + ChatColor.UNDERLINE + "Orb of Peace" + ChatColor.RED + " in your OWN realm.");
			return;
		}
		
		if(!realm.isChaotic()) {
			p.sendMessage(ChatColor.RED + "This realm is already peaceful.");
			return;
		}
		
		PlayerWrapper pw = PlayerWrapper.getWrapper(p);

        if (pw.getAlignment() == KarmaHandler.EnumPlayerAlignments.CHAOTIC) {
            p.sendMessage(ChatColor.RED + "You " + ChatColor.UNDERLINE + "cannot" + ChatColor.RED + " use an orb of peace while chaotic.");
            return;
        }
        
        evt.setUsed(true);
        p.sendMessage("");
        p.sendMessage(ChatColor.GREEN + "Your realm will now be a " + ChatColor.BOLD + "SAFE ZONE" + ChatColor.GREEN + " for 1 hour(s), or until logout.");
        p.sendMessage(ChatColor.GRAY + "All damage in your realm will be disabled for this time period.");
        p.getWorld().playEffect(p.getLocation(), Effect.ENDER_SIGNAL, 10);

        RealmProperty<Boolean> property = (RealmProperty<Boolean>) realm.getRealmProperties().get("peaceful");
        property.setExpiry(System.currentTimeMillis() + 3600000L);

        property.setValue(true);
        property.setAcknowledgeExpiration(true);
        
        realm.updateWGFlags();
        realm.updateHologram();
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.ENDER_PEARL);
	}

	public static boolean isPeaceOrb(ItemStack item) {
		return isType(item, ItemType.ORB_OF_PEACE);
	}
}
