package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.mechanic.rifts.RiftPortal;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemRiftFragment extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    @Getter
    private Item.ItemTier fragmentTier;

    public static final int RIFT_COST = 30;


    public ItemRiftFragment(ItemStack item) {
        super(item);
        if (hasTag("fragmentTier"))
            this.fragmentTier = Item.ItemTier.getByTier(getTagInt("fragmentTier"));
        setAntiDupe(false);
    }

    public ItemRiftFragment(Item.ItemTier tier) {
        super(ItemType.RIFT_FRAGMENT);
        this.fragmentTier = tier;
        setAntiDupe(false);
    }
    public ItemRiftFragment(Item.ItemTier tier, int amount) {
        super(ItemType.RIFT_FRAGMENT);
        this.fragmentTier = tier;
        getItem().setAmount(amount);
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
        evt.setCancelled(true);
        evt.getPlayer().sendMessage(ChatColor.RED + "You must have " + RIFT_COST + " Rift Fragments to combine them into a Rift Stone!");
        evt.getPlayer().sendMessage(ChatColor.GRAY + "A Rift Stone is used to summon a Rift, which if defeated can yield rare Shields and other loot.");
    }

    @Override
    protected String getDisplayName() {
        return fragmentTier.getColor() + ChatColor.BOLD.toString() + "Rift Fragment";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "An ancient Fragment fallen",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "from the Rift Walkers.",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "A trail that leads back to its home.",
                "",
                ChatColor.GRAY.toString() + RIFT_COST + " Fragments will combine",
                ChatColor.GRAY + "together once obtained"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.RIGHT_CLICK_BLOCK};
    }
}
