package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemRiftFragment extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    private Item.ItemTier fragmentTier;

    public ItemRiftFragment(ItemStack item) {
        super(item);
        if (hasTag("fragmentTier"))
            this.fragmentTier = Item.ItemTier.getByTier(getTagInt("fragmentTier"));
    }

    public ItemRiftFragment(Item.ItemTier tier) {
        super(ItemType.RIFT_FRAGMENT);
        this.fragmentTier = tier;
    }

    @Override
    public void updateItem() {
        if (this.fragmentTier != null)
            setTagInt("fragmentTier", this.fragmentTier.getId());
        super.updateItem();
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.PRISMARINE_SHARD, 1);
    }

    @Override
    public void onClick(ItemClickEvent evt) {

    }

    @Override
    protected String getDisplayName() {
        return fragmentTier.getColor() + ChatColor.BOLD.toString() + "Rift Fragment";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "A lost Fragment fallen from",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "the Rift Walkers, a trail",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "that leads back to home.",
                "",
                ChatColor.GRAY + "Combine with 30 Fragments",
                ChatColor.GRAY + "to summon a Rift."};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.RIGHT_CLICK_BLOCK};
    }
}
