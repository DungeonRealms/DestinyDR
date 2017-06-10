package net.dungeonrealms.game.item.items.functional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.rifts.RiftPortal;
import net.dungeonrealms.game.world.item.Item;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ItemRiftCrystal extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    private Item.ItemTier tier;
    private static final int ANIMATION_TIME = 30;
    private static Cache<UUID, Item.ItemTier> summoning = CacheBuilder.newBuilder().expireAfterWrite(ANIMATION_TIME, TimeUnit.SECONDS).build();

    public ItemRiftCrystal(Item.ItemTier tier, int amount) {
        super(ItemType.RIFT_CRYSTAL);
        this.tier = tier;
        this.getItem().setAmount(amount);
        setAntiDupe(true);
    }

    public ItemRiftCrystal(ItemStack item) {
        super(item);
        if (hasTag("tier"))
            this.tier = Item.ItemTier.getByTier(getTagInt("tier"));
    }

    @Override
    public void updateItem() {
        if (tier != null)
            setTagInt("tier", tier.getTierId());

        getTag().set("ench", new NBTTagCompound());
        super.updateItem();
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.CHORUS_FRUIT_POPPED);
    }

    @Override
    public void onClick(ItemClickEvent evt) {
        if (evt.hasBlock()) {

            int amount = evt.getItem().getItem().getAmount();
            Block block = evt.getClickedBlock();
            Player player = evt.getPlayer();

            if (GameAPI.isCooldown(player, MetadataUtils.Metadata.RIFT_COOLDOWN)) {
                player.sendMessage(ChatColor.RED + "Please wait 5s before trying to place another Rift Portal.");
                return;
            }

            if (block != null) {
                Item.ItemTier currentlySummoning = summoning.getIfPresent(player.getUniqueId());
                if (currentlySummoning != null) {
                    player.sendMessage(ChatColor.RED + "You are already summoning a Tier " + currentlySummoning.getTierId() + " Rift.");
                    player.sendMessage(ChatColor.GRAY + "Please wait until it is complete.");
                    return;
                }

                RiftPortal existingPortal = RiftPortal.getRiftPortal(player);
                if (existingPortal != null) {
                    player.sendMessage(ChatColor.RED + "You already have a Rift Open!");
                    player.sendMessage(ChatColor.GRAY + "Defeat it or use all attempts to seal the rift.");
                    return;
                }

                GameAPI.addCooldown(player, MetadataUtils.Metadata.RIFT_COOLDOWN, 5);

                if (GameAPI.isInSafeRegion(evt.getPlayer().getLocation()) && GameAPI.isMainWorld(evt.getPlayer().getLocation())) {

                    RiftPortal portal = new RiftPortal(player, evt.getClickedBlock(), tier.getTierId());
                    if (portal.canPlacePortals()) {
                        evt.setUsed(true);
                        summoning.put(player.getUniqueId(), tier);
                        RiftPortal.getRiftPortalMap().put(player.getUniqueId(), portal);
                        portal.createPortals(done -> {
                            summoning.invalidate(player.getUniqueId());
                            Utils.sendCenteredMessage(player, ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Enter the Rift at your own risk!");
                            Utils.sendCenteredMessage(player, ChatColor.GRAY + "You have " + ChatColor.BOLD + "6" + ChatColor.GRAY + " attempts to defeat this rift!");
                            Utils.sendCenteredMessage(player, ChatColor.GRAY + "This rift will seal itself in " + TimeUtil.formatDifference(RiftPortal.MAX_ALIVE_TIME));
                        });
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot place a Rift Portal here!");
                        return;
                    }

                    if (amount > ItemRiftFragment.RIFT_COST)
                        evt.getItem().getItem().setAmount(evt.getItem().getItem().getAmount() - ItemRiftFragment.RIFT_COST);
                    else
                        evt.getItem().setDestroyed(true);
                    player.updateInventory();
                    player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1, 1.1F);
                } else {
                    evt.getPlayer().sendMessage(ChatColor.RED + "You must be in a Safe Zone to summon a Rift!");
                }
            }
        }
    }

    @Override
    protected String getDisplayName() {
        return tier.getColor() + ChatColor.BOLD.toString() + "Rift Stone";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "Hold the power to open a",
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "Rift, allowing Rift Travel",
                "",
                ChatColor.RED + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "Be prepared to",
                ChatColor.RED + "encounter a powerful Rift Lurker",
                "",
                ChatColor.GRAY + "Place on the ground to",
                ChatColor.GRAY + "summon a Tier " + tier.getTierId() + " Rift Portal"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT_RIGHT_CLICK;
    }
}
