package net.dungeonrealms.game.item.items.functional.ecash;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class ItemLightningRod extends FunctionalItem implements ItemClickListener {

    public ItemLightningRod() {
        super(ItemType.LIGHTNING_ROD);
        setSoulbound(true);
    }

    public ItemLightningRod(ItemStack item) {
        super(item);
        setSoulbound(true);
    }


    @Override
    public void onClick(ItemClickEvent evt) {
        Player player = evt.getPlayer();

        if (evt.isRightClick()) {
            Location lightningLoc = null;
            if (evt.hasEntity()) {
                lightningLoc = evt.getClickedEntity().getLocation();
            } else if (evt.hasBlock()) {
                lightningLoc = evt.getClickedBlock().getLocation();
            } else {
                Block block = player.getTargetBlock((Set<Material>) null, 25);
                if (block != null) lightningLoc = block.getLocation();
            }

            if (lightningLoc != null) lightningLoc.getWorld().strikeLightningEffect(lightningLoc);
        }
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.AQUA.toString() + ChatColor.BOLD + "Lightning Rod";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GREEN + ChatColor.BOLD.toString() + "Right Click: " + ChatColor.GRAY + "Strike lightning"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return arr(ItemUsage.RIGHT_CLICK_AIR, ItemUsage.RIGHT_CLICK_BLOCK, ItemUsage.RIGHT_CLICK_ENTITY);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.BLAZE_ROD);
    }
}
