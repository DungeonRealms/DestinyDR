package net.dungeonrealms.game.item.items.functional;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.world.item.Item.ItemTier;

public class ItemProtectionScroll extends ItemEnchantScroll {
	
	public ItemProtectionScroll(ItemStack item) {
		super(item);
	}
	
	public ItemProtectionScroll() {
		this(ItemTier.TIER_1);
	}
	
	public ItemProtectionScroll(int i) {
		this(ItemTier.getByTier(i));
	}
	
	public ItemProtectionScroll(ItemTier tier) {
		super(tier, ItemType.PROTECTION_SCROLL, "");
	}
	
	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {
		ItemStack prot = evt.getSwappedItem();
		if(!ItemGear.isCustomTool(prot))
			return;
		
		evt.setCancelled(true);
		ItemGear gear = (ItemGear)PersistentItem.constructItem(prot);
		
		Player p = evt.getPlayer();
		if (gear.isProtected()) {
			p.sendMessage(ChatColor.RED + "This item is already protected.");
			return;
		}
            
		if (gear.getTier() != getTier()) {
			p.sendMessage(ChatColor.RED + "This protection scroll is not made for this tier!");
			return;
		}
            
		if (gear.getEnchantCount() < 8) {
			p.sendMessage(ChatColor.RED + "This item cannot be protected until +8!");
			return;
		}

		if (gear.getEnchantCount() == 12){
			p.sendMessage(ChatColor.RED + "You cannot protect this item because it is at it's maximum level!");
			return;
		}
            
		evt.setUsed(true);
		gear.setProtected(true);
		evt.setSwappedItem(gear.generateItem());
		p.sendMessage(ChatColor.GREEN + "Your " + prot.getItemMeta().getDisplayName() + ChatColor.GREEN + " is now protected -- you may now enchant your item.");
		
		//  FIREWORK EFFECT  //
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
		Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.GREEN).withFade(Color.GREEN).with(FireworkEffect.Type.STAR).trail(true).build();
		fwm.addEffect(effect);
            fwm.setPower(0);
            fw.setFireworkMeta(fwm);
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.WHITE + "" + ChatColor.BOLD + "White Scroll: " + getTier().getColor() + "Protect " + getTier().getArmorName() + " Equipment";
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.ITALIC + "Apply to any T" + getTier().getTierId() + " item to " + ChatColor.UNDERLINE + "allow" + ChatColor.GRAY + "" + ChatColor.ITALIC + " enchantments past +8."};
	}

	@Override
	protected boolean isApplicable(ItemStack item) {
		return CombatItem.isCombatItem(item);
	}
	
	public static boolean isEnchant(ItemStack item) {
		return isType(item, ItemType.PROTECTION_SCROLL);
	}
}
