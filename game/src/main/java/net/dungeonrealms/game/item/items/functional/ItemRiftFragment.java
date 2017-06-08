package net.dungeonrealms.game.item.items.functional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.mechanic.rifts.RiftPortal;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ItemRiftFragment extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    private Item.ItemTier fragmentTier;

    private static final int ANIMATION_TIME = 30;
    private static final int RIFT_COST = 30;

    private static Cache<UUID, Item.ItemTier> summoning = CacheBuilder.newBuilder().expireAfterWrite(ANIMATION_TIME, TimeUnit.SECONDS).build();

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
        int amount = evt.getItem().getItem().getAmount();

        evt.setCancelled(true);
        Block block = evt.getClickedBlock();
        Player player = evt.getPlayer();
        if (block != null) {
            if (amount < 30) {
                player.sendMessage(ChatColor.RED + "You must have 30 Rift Fragments to summon this rift!");
                return;
            }

            Item.ItemTier currentlySummoning = summoning.getIfPresent(player.getUniqueId());
            if (currentlySummoning != null) {
                player.sendMessage(ChatColor.RED + "You are already summoning a Tier " + currentlySummoning.getTierId() + " Rift.");
                player.sendMessage(ChatColor.GRAY + "Please wait until it is complete.");
                return;
            }

            if (GameAPI.isInSafeRegion(evt.getPlayer().getLocation())) {
                if (amount > RIFT_COST)
                    evt.getItem().getItem().setAmount(evt.getItem().getItem().getAmount() - RIFT_COST);
                else
                    evt.setResultItem(null);

                player.updateInventory();
                //TODO: Summon breach.
                player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1.1F);
                //TODO: Animation?

                RiftPortal portal = new RiftPortal(player, evt.getClickedBlock());
                if (portal.canPlacePortals()) {

                } else {
                    player.sendMessage(ChatColor.RED + "You cannot place a Rift Portal here!");
                }
                DungeonManager.createDungeon(DungeonType.ELITE_RIFT, Lists.newArrayList(player));
            } else {
                evt.getPlayer().sendMessage(ChatColor.RED + "You must be in a Safe Zone to summon a Rift!");
            }
        }
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
                ChatColor.GRAY + "Click with 30 Fragments",
                ChatColor.GRAY + "to summon a Rift."};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.RIGHT_CLICK_BLOCK};
    }
}
