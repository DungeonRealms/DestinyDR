package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.event.ItemInventoryEvent;
import net.dungeonrealms.game.item.event.ItemInventoryEvent.ItemInventoryListener;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ProfessionItem;
import net.dungeonrealms.game.mechanic.data.ScrapTier;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemScrap extends FunctionalItem implements ItemInventoryListener {

    @Getter
    @Setter
    private ScrapTier tier;

    public ItemScrap(ScrapTier tier) {
        super(ItemType.SCRAP);
        setAntiDupe(false);
        setTier(tier);
    }

    public ItemScrap(ItemStack item) {
        super(item);
        setAntiDupe(false);
        setTier(ScrapTier.getScrapTier(getTagInt(TIER)));
    }

    @Override
    public void updateItem() {
        setTagInt(TIER, tier.getTier());
        super.updateItem();
    }

    @Override
    public void onInventoryClick(ItemInventoryEvent evt) {
        if (!ItemGear.isCustomTool(evt.getSwappedItem()))
            return;
        ItemGear gear = (ItemGear) PersistentItem.constructItem(evt.getSwappedItem());
        Player player = evt.getPlayer();

        if (!gear.canRepair())
            return;

        evt.setCancelled(true);
        if (gear instanceof ProfessionItem && ((ProfessionItem) gear).getLevel() >= 100) {
            player.sendMessage(ChatColor.RED + "This item is much too warn to be repaired.");
            return;
        }

        gear.scrapRepair();
        evt.setUsed(true);

        int particleId = gear.getRepairParticle(getTier());

        for (int i = 0; i < 6; i++) {
            player.getWorld().playEffect(player.getLocation().add(i, 1.3, i), Effect.TILE_BREAK, particleId, 12);
            player.getWorld().playEffect(player.getLocation().add(i, 1.15, i), Effect.TILE_BREAK, particleId, 12);
            player.getWorld().playEffect(player.getLocation().add(i, 1, i), Effect.TILE_BREAK, particleId, 12);
        }

        PlayerWrapper.getWrapper(player).sendDebug(ChatColor.GREEN
                + "You used an Item Scrap to repair 3% durability to " + (int) Math.ceil(gear.getDurabilityPercent()) + "%");

        evt.setSwappedItem(gear.generateItem());
    }

    @Override
    protected String getDisplayName() {
        return getTier().getName() + " Scrap";
    }

    @Override
    protected String[] getLore() {
        return new String[]{"Repairs 3% durability on " + getTier().getName() + ChatColor.GRAY + " equipment."};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return new ItemUsage[]{ItemUsage.INVENTORY_SWAP_PLACE};
    }

    @Override
    protected ItemStack getStack() {
        ItemStack i = getTier().getRawStack();
        i.setAmount(1);
        return i;
    }

    public static boolean isScrap(ItemStack item) {
        return isType(item, ItemType.SCRAP);
    }
}
