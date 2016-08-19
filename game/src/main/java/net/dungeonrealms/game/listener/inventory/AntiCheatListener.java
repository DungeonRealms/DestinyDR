package net.dungeonrealms.game.listener.inventory;

import org.bukkit.event.Listener;

/**
 * Created by Alan Lu (dartaran) on 06-Jul-16.
 */
public class AntiCheatListener implements Listener {

    //THIS IS DISABLED WHILE I FIGURE OUT HOW TO BEST REMOVE ALL THE GLITCHED "method" ITEMS. PLEASE DON'T ENABLE AGAIN.
    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        AntiCheat.getInstance().checkForDupedItems((Player) event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        AntiCheat.getInstance().checkForDupedItems((Player) event.getPlayer());
    }*/

    /*@EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Utils.log.info(System.currentTimeMillis() + " Pick up item");
        ItemStack i = event.getItem().getItemStack();
        if (!AntiCheat.getInstance().isRegistered(i)) return;

        if (AntiCheat.getInstance().checkIfDupedDatabase(i)) {
            event.setCancelled(true);
            AntiCheat.getInstance().dupedItemFound(event.getPlayer(), i);
            return;
        }

        // add it to the stack if necessary
        boolean newStack = true;
        NBTItem nbtItem = new NBTItem(i);

//        if (i.getMaxStackSize() == 1) return;
        for (ItemStack is : event.getPlayer().getInventory()) {
            if (!AntiCheat.getInstance().isRegistered(is)) continue;
            if (is.getAmount() + 1 > is.getMaxStackSize()) continue;
            if (is.getType().equals(i.getType())) {
                nbtItem.setString("u", AntiCheat.getInstance().getUniqueEpochIdentifier(is));
                newStack = false;
                break;
            }
        }

        if (newStack) AntiCheat.getInstance().getUids().add(nbtItem.getString("u"));
        Utils.log.info(System.currentTimeMillis() + "");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        Utils.log.info(System.currentTimeMillis() + " Dropping item");
        if (AntiCheat.getInstance().isRegistered(event.getItemDrop().getItemStack())) {
            // check if we need to remove this UID.
            if (event.getItemDrop().getItemStack().getMaxStackSize() <= 1 || !AntiCheat.getInstance()
                    .checkIfUIDPresentPlayer(event.getItemDrop().getItemStack(), event.getPlayer())) {
                AntiCheat.getInstance().getUids().remove(AntiCheat.getInstance().getUniqueEpochIdentifier(event
                        .getItemDrop().getItemStack()));
            }
            // generate new UID
            event.getItemDrop().setItemStack(AntiCheat.getInstance().applyNewUID(event.getItemDrop().getItemStack()));
        }
        Utils.log.info(System.currentTimeMillis() + "");
    }
/*
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSplitItemStack(InventoryClickEvent event) {
        Utils.log.info(System.currentTimeMillis() + " Splitting stack");
        if (event.getCurrentItem().getMaxStackSize() <= 1) return;
        if (event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME ||
                event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PLACE_ONE ||
                event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_SOME) {
            AntiCheat.getInstance().applyNewUID(event.getCurrentItem());
        }
        Utils.log.info(System.currentTimeMillis() + "");
    }*/

    /*@EventHandler(priority = EventPriority.MONITOR)
    public void onCombineItemStack(InventoryClickEvent event) {
        Utils.log.info(System.currentTimeMillis() + " Combining stack");
        if (event.getClick() == ClickType.SHIFT_LEFT || event.getAction() == InventoryAction.COLLECT_TO_CURSOR) { // check for available stacks
            return;
        }
        if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
        if (!AntiCheat.getInstance().isRegistered(event.getCursor())) return;
        if (!AntiCheat.getInstance().isRegistered(event.getCurrentItem())) return;
        if (event.getCursor().getType() == event.getCurrentItem().getType()) {
            if (event.getCursor().getAmount() + event.getCurrentItem().getAmount() > event.getCurrentItem().getMaxStackSize()) return;
            if (event.getAction() == InventoryAction.PLACE_SOME || event.getAction() == InventoryAction.PLACE_ONE ||
                    event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.DROP_ALL_CURSOR) {
                AntiCheat.getInstance().mergeUniqueIdentifiers(event.getCurrentItem(), event.getCursor());
            }
        }
        Utils.log.info(System.currentTimeMillis() + "");
    }*/

}
