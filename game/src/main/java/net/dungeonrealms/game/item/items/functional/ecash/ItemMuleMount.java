package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mechanic.data.MuleTier;

public class ItemMuleMount extends FunctionalItem implements ItemClickListener {
	
	private MuleTier tier;
	
	public ItemMuleMount(MuleTier tier) {
		super(ItemType.MULE);
		this.tier = tier;
	}
	
	public ItemMuleMount(Player player) {
		super(ItemType.MULE);
		setUntradeable(true);
		
		PlayerWrapper pw = PlayerWrapper.getWrapper(player);
		
		if (pw.getMuleLevel() == 0)
			pw.setMuleLevel(1);
		
		this.tier = MuleTier.getByTier(pw.getMuleLevel());
	}
	
	public ItemMuleMount(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		ItemMount.attemptSummonMount(evt.getPlayer());
	}

	@Override
	protected String getDisplayName() {
		return tier.getName();
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "Storage Size: " + tier.getSize() + " Items",
				ChatColor.RED + "An old worn mule."
		};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.LEASH);
	}
	
	public static boolean isMule(ItemStack item) {
		return isType(item, ItemType.MULE);
	}
}
