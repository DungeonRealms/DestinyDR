package net.dungeonrealms.game.item.items.functional.ecash;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.world.entity.type.mounts.mule.MuleTier;

public class ItemMuleMount extends FunctionalItem {

	@Getter @Setter
	private MuleTier tier;
	
	public ItemMuleMount(MuleTier tier) {
		super(ItemType.MULE);
		setTier(tier);
	}
	
	public ItemMuleMount(Player player) {
		super(ItemType.MULE);
		setUntradeable(true);
		
		Object muleTier = DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, player.getUniqueId());
        if (muleTier == null) {
            player.sendMessage(ChatColor.RED + "Creating mule data...");
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, 1, true);
            muleTier = 1;
        }
		setTier(MuleTier.getByTier((int) muleTier));
	}
	
	public ItemMuleMount(ItemStack item) {
		super(item);
		setTier(MuleTier.ADVENTURER); //Failsafe. Shouldn't ever happen.
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		ItemMount.attemptSummonMount(evt.getPlayer(), true);
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}
	
	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return getTier().getName();
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.RED + "Storage Size: " + getTier().getSize() + " Items",
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
