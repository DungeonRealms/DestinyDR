package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemConsumeEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.data.EnumBuff;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.md_5.bungee.api.ChatColor;






import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemBuff extends FunctionalItem {
	
	@Getter @Setter //The type of buff.
	private EnumBuff buffType;
	
	@Getter @Setter // The duration of this buff, in ??? units.
	private int duration;
	
	@Getter @Setter //The % increase of this buff.
	private int buffPower;
	
	public ItemBuff(EnumBuff buff, int duration, int buffPower) {
		super(ItemType.BUFF);
		setDuration(duration);
		setBuffType(buff);
		setBuffPower(buffPower);
		setPermUntradeable(true);
	}
	
	public ItemBuff(ItemStack stack) {
		super(stack);
	}
	
	@Override
	public void loadItem() {
		setBuffType(EnumBuff.valueOf(getTagString("buff")));
		setDuration(getTagInt("duration"));
		setBuffPower(getTagInt("buffPower"));
		super.loadItem();
	}
	
	@Override
	public void updateItem() {
		setTagString("buff", getBuffType().name());
		setTagInt("duration", getDuration());
		setTagInt("buffPower", getBuffPower());
		super.updateItem();
	}
	
	@Override
	public void onClick(ItemClickEvent evt) {
		evt.setUsed(true);
		
		//  SEND MESSAGE  //
		Player player = evt.getPlayer();
		player.sendMessage("");
        Utils.sendCenteredMessage(player, ChatColor.DARK_GRAY + "***" + ChatColor.GREEN.toString() +
                ChatColor.BOLD + getDisplayName().toUpperCase() + " CONFIRMATION" + ChatColor.DARK_GRAY + "***");
        player.sendMessage(ChatColor.GOLD + "Are you sure you want to use this item? It will apply a " + getBuffPower() +
                "% buff to all " + getBuffType().getMiniDescription() + " across all servers for " + getFormattedTime()
                + ". This cannot be undone once it has begun.");
        
        if (DonationEffects.getInstance().hasBuff(getBuffType()))
        	player.sendMessage(ChatColor.RED + "NOTICE: There is an ongoing " + getBuffType().getItemName() + " buff, so your buff " +
        			"will be activated afterwards. Cancel if you do not wish to queue yours.");
		
        player.sendMessage(ChatColor.GRAY + "Type '" + ChatColor.GREEN + "Y" + ChatColor.GRAY + "' to confirm, or any other message to cancel.");
        player.sendMessage("");
		Chat.promptPlayerConfirmation(player, () -> {
			GameAPI.sendNetworkMessage("buff", getBuffType().name(), getDuration() + "", getBuffPower() + "",
					GameChat.getFormattedName(player), DungeonRealms.getShard().getShardID());
		}, () -> {
			GameAPI.giveOrDropItem(player, getItem());
			player.sendMessage(ChatColor.RED + getDisplayName() + " - CANCELLED");
		});
	}

	@Override
	public void onConsume(ItemConsumeEvent evt) {}

	@Override
	public void onInventoryClick(ItemInventoryEvent evt) {}

	@Override
	protected String getDisplayName() {
		return getBuffType().getItemName();
	}

	@Override
	protected String[] getLore() {
		return new String[] { ChatColor.GOLD + "Duration: " + ChatColor.GRAY + getFormattedTime(),
				ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1",
				ChatColor.GRAY + "" + ChatColor.ITALIC + getBuffType().getDescription(),
				ChatColor.GRAY + "" + ChatColor.ITALIC + "by " + getBuffPower() + "% across " + ChatColor.UNDERLINE + "ALL SHARDS."
		};
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(getBuffType().getIcon());
	}
	
	private String getFormattedTime() {
		return DurationFormatUtils.formatDurationWords(getDuration() * 1000, true, true);
	}
}
