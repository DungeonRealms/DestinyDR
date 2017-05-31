package net.dungeonrealms.game.item.items.functional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.database.PlayerGameStats;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemEXPLamp extends FunctionalItem implements ItemClickEvent.ItemClickListener, ItemInventoryEvent.ItemInventoryListener {

    private ExpType expType;
    private int xpAmount;

    public ItemEXPLamp(ExpType type, int xpAmount) {
        super(ItemType.EXP_LAMP);
        this.expType = type;
        this.xpAmount = xpAmount;
    }

    public ItemEXPLamp(ItemStack item) {
        super(item);
        if (hasTag("expType")) {
            expType = getEnum("expType", ExpType.class);
            xpAmount = getTagInt("xp");
        }
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.EXP_BOTTLE, 1);
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        if (evt.getSwappedItem() != null) {
            //Is this a profession item if needed?
            Player player = evt.getPlayer();

            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
            if (expType == ExpType.PROFESSION && ProfessionItem.isProfessionItem(evt.getSwappedItem())) {
                evt.setUsed(true);
                evt.setCancelled(true);

                ProfessionItem item = (ProfessionItem) ProfessionItem.constructItem(evt.getSwappedItem());
                item.addExperience(evt.getPlayer(), xpAmount);

                evt.setSwappedItem(item.generateItem());

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.1F);
                player.sendMessage(ChatColor.GRAY + "Applied " + ChatColor.GREEN + ChatColor.BOLD + Utils.format(xpAmount) + " EXP" + ChatColor.GRAY + " to your " + evt.getSwappedItem().getItemMeta().getDisplayName() + ChatColor.GRAY + "!");

                wrapper.getPlayerGameStats().addStat(PlayerGameStats.StatColumn.LAMPS_APPLIED);

            } else {
                evt.getPlayer().sendMessage(ChatColor.RED + "You cannot apply a lamp to that item.");
            }
        }
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        if (expType == ExpType.PLAYER) {
            //Claim?
            evt.setUsed(true);
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(evt.getPlayer());
            wrapper.addExperience(xpAmount, false, false, false);
            evt.getPlayer().sendMessage(ChatColor.YELLOW.toString() + Math.round(xpAmount) + ChatColor.BOLD + " EXP " + ChatColor.GRAY + "[" + Math.round(wrapper.getExperience() + xpAmount) + ChatColor.BOLD + "/" + ChatColor.GRAY + Math.round(wrapper.getEXPNeeded(wrapper.getLevel())) + " EXP]");
            evt.getPlayer().playSound(evt.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, .8F);
            evt.setCancelled(true);
        }
    }

    @Override
    public void updateItem() {
        if (expType != null) {
            setTagString("expType", expType.name());
            setTagInt("xp", xpAmount);
        }
        super.updateItem();
    }

    @Override
    protected String getDisplayName() {
        //Wisdom
        return ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + expType.getColor() + expType.getName() + " Wisdom";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GRAY + "A bottle of knowledge",
                ChatColor.GRAY + "containing " + ChatColor.GREEN + ChatColor.UNDERLINE + xpAmount + ChatColor.GREEN + ChatColor.BOLD + " EXP",
                "",
                ChatColor.GRAY + expType.getDescription()};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.INVENTORY_SWAP_PLACE, ItemUsage.LEFT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK};
    }

    @Getter
    @AllArgsConstructor
    public enum ExpType {
        PROFESSION("Profession", "Place on tool to claim xp.", ChatColor.AQUA),
        PLAYER("Player", "Right click to claim exp.", ChatColor.GREEN);

        private String name;
        private String description;
        private ChatColor color;
    }
}
