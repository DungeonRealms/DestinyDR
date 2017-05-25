package net.dungeonrealms.game.item.items.functional.ecash;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.player.inventory.menus.DPSDummy;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.Purchaseables;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ItemDPSDummy extends FunctionalItem implements ItemClickEvent.ItemClickListener {

    private static BukkitRunnable dummyRunnable;

    public ItemDPSDummy(ItemStack item) {
        super(ItemType.DPS_DUMMY);
        setUndroppable(true);
        setPermUntradeable(true);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.ARMOR_STAND, 1);
    }

    public static Map<Entity, DPSDummy> dpsDummies = new ConcurrentHashMap<>();

    public void startTask() {
        if (dummyRunnable == null) {
            dummyRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (dpsDummies.size() <= 0) {
                        cancel();
                        dummyRunnable = null;
                        return;
                    }

                    dpsDummies.forEach((ent, dummy) -> {
                        if (ent.getTicksLived() >= 20 * 60 * 10) {
                            dummy.destroy();
                        }
                    });
                }
            };
            dummyRunnable.runTaskTimer(DungeonRealms.getInstance(), 20, 20);
        }
    }

    @Override
    public void onClick(ItemClickEvent evt) {

        evt.setCancelled(true);
        Player player = evt.getPlayer();
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (Purchaseables.DPS_DUMMY.getNumberOwned(wrapper) <= 0) {
            player.sendMessage(ChatColor.RED + "You have not unlocked the DPS Dummy!");
            player.sendMessage(ChatColor.GRAY + "You can unlock it at " + ChatColor.UNDERLINE + Constants.STORE_URL);
            evt.setResultItem(null);
            return;
        }

        if (evt.isRightClick()) {
            String cooldownString = GameAPI.getFormattedCooldown(player, MetadataUtils.Metadata.DPS_DUMMY);
            if (cooldownString != null) {
                //ON cd.
                player.sendMessage(ChatColor.RED + "Please wait " + cooldownString + " and try again.");
                return;
            }

            if (!GameAPI.isNonPvPRegion(player.getLocation())) {
                player.sendMessage(ChatColor.RED + "You can only place a DPS Dummy in non chaotic zones!");
                return;
            }

            DPSDummy nearby = dpsDummies.values().stream().filter(d -> d.getLocation().distanceSquared(player.getLocation()) <= 15).findFirst().orElse(null);

            if (nearby != null) {
                //Dont let them..
                player.sendMessage(ChatColor.RED + "You cannot place your DPS Dummy so close to one another!");
                player.sendMessage(ChatColor.GRAY + "Nearest: " + ChatColor.GRAY + nearby.getOwnerName());
                return;
            }

            DPSDummy current = getDPSDummy(player);
            if (current != null) {
                player.sendMessage(ChatColor.RED + "DPS Dummy has been dismissed!");
                current.destroy();
            }

            GameAPI.addCooldown(player, MetadataUtils.Metadata.DPS_DUMMY, 10);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 1F);
            ArmorStand dummy = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
            dummy.setVisible(true);
            String name = ChatColor.GREEN + player.getName() + "'s DPS Dummy";
            dummy.setCustomName(name);
            dummy.setCustomNameVisible(true);
            dummy.setHelmet(Utils.getPlayerHead(player));
            dummy.setArms(true);
            dummy.setInvulnerable(false);
            dummy.setCollidable(true);
            dummy.setGravity(false);

            DPSDummy dpsDummy = new DPSDummy(dummy, dummy.getLocation(), player.getUniqueId(), player.getName());

            dpsDummies.put(dummy, dpsDummy);
            player.sendMessage(ChatColor.GREEN + "DPS Dummy - " + ChatColor.GREEN + ChatColor.BOLD + "SUMMONED");
            startTask();
            MetadataUtils.Metadata.ENTITY_TYPE.set(dummy, EnumEntityType.DPS_DUMMY);
            MetadataUtils.Metadata.CUSTOM_NAME.set(dummy, name);
        } else if (evt.isLeftClick()) {
            DPSDummy alive = getDPSDummy(player);
            if (alive != null) {
                alive.destroy();
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, .7F);
                player.sendMessage(ChatColor.RED + "Your DPS Dummy has been dismissed!");
            }
        }
    }


    public DPSDummy getDPSDummy(Player player) {
        return dpsDummies.values().stream().filter(dummy -> dummy.getOwner().equals(player.getUniqueId())).findFirst().orElse(null);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.GOLD + "DPS Dummy";
    }

    @Override
    protected String[] getLore() {
        return new String[]{
                ChatColor.GRAY + ChatColor.ITALIC.toString() + "A squishy dummy made to take a hit!",
                "",
                ChatColor.GRAY + "Right-Click to place down a DPS Dummy!"};
    }

    @Override
    protected ItemUsage[] getUsage() {
        return INTERACT;
    }
}
