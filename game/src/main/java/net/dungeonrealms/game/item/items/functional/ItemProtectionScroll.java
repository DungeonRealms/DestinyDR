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
            
		if (gear.getEnchantCount() >= 8) {
			p.sendMessage(ChatColor.RED + "This item can no longer be protected!");
			return;
		}
            
		evt.setUsed(true);
		gear.protectItem();
		evt.setSwappedItem(gear.generateItem());
		p.sendMessage(ChatColor.GREEN + "Your " + prot.getItemMeta().getDisplayName() + ChatColor.GREEN + " is now protected -- even if an enchant scroll fails, it will " + ChatColor.UNDERLINE + "NOT" + ChatColor.GREEN + " be destroyed up to +8 status.");
		
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
				ChatColor.ITALIC + "Apply to any T" + getTier().getTierId() + " item to " + ChatColor.UNDERLINE + "prevent" + ChatColor.GRAY + "" + ChatColor.ITALIC + " it",
				ChatColor.ITALIC + "from being destroyed if the next",
				ChatColor.ITALIC + "enchantment scroll (up to +8) fails."};
	}

	@Override
	protected boolean isApplicable(ItemStack item) {
		return CombatItem.isCombatItem(item);
	}
	
	public static boolean isEnchant(ItemStack item) {
		return isType(item, ItemType.PROTECTION_SCROLL);
	}
}
